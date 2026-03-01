#!/usr/bin/env bash

# ==============================================================================
# Building Wands - Automated Test Environment Builder
# Features: Modrinth + CurseForge APIs, PortableMC, Isolated Instances, Caching
# ==============================================================================

if ! command -v jq &> /dev/null; then
    echo "ERROR: 'jq' is not installed."
    exit 1
fi
if ! command -v curl &> /dev/null; then
    echo "ERROR: 'curl' is not installed."
    exit 1
fi

# ------------------------------------------------------------------------------
# 0. Argument Parsing
# ------------------------------------------------------------------------------
FORCE_UPDATE_DEPS=false
WITH_OPTIONAL=false
for arg in "$@"; do
    if [ "$arg" == "--force-update-deps" ]; then
        FORCE_UPDATE_DEPS=true
        echo ">>> [FLAG] Force updating dependencies and clearing caches..."
    fi
    if [ "$arg" == "--with-optional" ]; then
        WITH_OPTIONAL=true
        echo ">>> [FLAG] Including optional/compat mods (JEI, OPAC, FTB Chunks, etc.)..."
    fi
done

TEST_ENV_DIR="./test-env"
DEPS_CACHE_DIR="$TEST_ENV_DIR/modrinth-cache"
CF_CACHE_DIR="$TEST_ENV_DIR/curseforge-cache"
LOCAL_CONFIG_FILE="$TEST_ENV_DIR/test-env-config.json"
INSTANCES_FILE="instances.json"

mkdir -p "$TEST_ENV_DIR"
mkdir -p "$DEPS_CACHE_DIR"
mkdir -p "$CF_CACHE_DIR"

# ------------------------------------------------------------------------------
# 1. Configuration Validation
# ------------------------------------------------------------------------------
if [ ! -f "$INSTANCES_FILE" ]; then
    echo "ERROR: $INSTANCES_FILE not found in the project root!"
    exit 1
fi

if [ ! -f "$LOCAL_CONFIG_FILE" ]; then
    echo ">>> Local configuration not found."
    read -p "Enter your player name for testing [default: Nico]: " INPUT_NAME
    INPUT_NAME=${INPUT_NAME:-Nico}
    read -p "Enter guest player name for LAN testing [default: Guest]: " INPUT_GUEST
    INPUT_GUEST=${INPUT_GUEST:-Guest}

    echo ">>> Generating $LOCAL_CONFIG_FILE..."
    cat <<EOF > "$LOCAL_CONFIG_FILE"
{
  "player_name": "$INPUT_NAME",
  "guest_name": "$INPUT_GUEST"
}
EOF
fi

PLAYER_NAME=$(jq -r '.player_name // "Nico"' "$LOCAL_CONFIG_FILE")
GUEST_NAME=$(jq -r '.guest_name // "Guest"' "$LOCAL_CONFIG_FILE")

# ------------------------------------------------------------------------------
# 2. Launcher Verification & Downloader (PortableMC)
# ------------------------------------------------------------------------------
download_launcher() {
    if [ -f "$TEST_ENV_DIR/portablemc" ]; then
        CMD_LAUNCHER="$TEST_ENV_DIR/portablemc"
        return
    elif [ -f "$TEST_ENV_DIR/portablemc.exe" ]; then
        CMD_LAUNCHER="$TEST_ENV_DIR/portablemc.exe"
        return
    elif command -v portablemc &> /dev/null; then
        CMD_LAUNCHER="portablemc"
        return
    fi

    echo ">>> 'portablemc' not found. Fetching latest release from GitHub into $TEST_ENV_DIR..."
    local OS_TYPE=$(uname -s | tr '[:upper:]' '[:lower:]')
    local ARCH_TYPE=$(uname -m | tr '[:upper:]' '[:lower:]')
    local OS_REGEX="linux"
    local IS_WINDOWS=false
    
    case "$OS_TYPE" in
        linux*)     OS_REGEX="linux" ;;
        darwin*)    OS_REGEX="darwin|mac" ;;
        msys*|cygwin*|mingw*) OS_REGEX="win"; IS_WINDOWS=true ;;
        *) echo "ERROR: Unsupported OS: $OS_TYPE"; exit 1 ;;
    esac

    local ARCH_REGEX="x86_64|amd64"
    case "$ARCH_TYPE" in
        aarch64|arm64) ARCH_REGEX="aarch64|arm64" ;;
    esac

    local API_URL="https://api.github.com/repos/mindstorm38/portablemc/releases/latest"
    local JSON_ASSETS=$(curl -s "$API_URL" | jq -c '.assets[]')
    
    if [ -z "$JSON_ASSETS" ]; then
        echo "ERROR: Failed to fetch releases from $API_URL"
        exit 1
    fi

    local VALID_ASSETS=$(echo "$JSON_ASSETS" | jq -c 'select(.name | ascii_downcase | test("\\.(sig|sha256|txt)$") | not)')
    local DOWNLOAD_URL=$(echo "$VALID_ASSETS" | jq -r "select(.name | ascii_downcase | test(\"$OS_REGEX\")) | select(.name | ascii_downcase | test(\"$ARCH_REGEX\")) | .browser_download_url" | head -n 1)
    
    if [ -z "$DOWNLOAD_URL" ] || [ "$DOWNLOAD_URL" == "null" ]; then
        DOWNLOAD_URL=$(echo "$VALID_ASSETS" | jq -r "select(.name | ascii_downcase | test(\"$OS_REGEX\")) | .browser_download_url" | head -n 1)
    fi

    if $IS_WINDOWS && [[ -z "$DOWNLOAD_URL" || "$DOWNLOAD_URL" == "null" ]]; then
        DOWNLOAD_URL=$(echo "$VALID_ASSETS" | jq -r 'select(.name | ascii_downcase | test("\\.exe$")) | .browser_download_url' | head -n 1)
    fi

    if [ -z "$DOWNLOAD_URL" ] || [ "$DOWNLOAD_URL" == "null" ]; then
        echo "ERROR: Could not find PortableMC binary for OS: $OS_TYPE Arch: $ARCH_TYPE"
        exit 1
    fi

    local FILE_NAME=$(basename "$DOWNLOAD_URL")
    echo "  -> Downloading $FILE_NAME..."
    curl -s -L -o "$TEST_ENV_DIR/$FILE_NAME" "$DOWNLOAD_URL"
    
    pushd "$TEST_ENV_DIR" >/dev/null
    
    if [[ "$FILE_NAME" == *.zip ]]; then
        unzip -q -o "$FILE_NAME"
        rm -f "$FILE_NAME"
    elif [[ "$FILE_NAME" == *.tar.gz ]]; then
        tar -xzf "$FILE_NAME"
        rm -f "$FILE_NAME"
    fi
    
    if $IS_WINDOWS; then
        local EXE_PATH=$(find . -type f -iname "portablemc.exe" | head -n 1)
        if [ -n "$EXE_PATH" ] && [ "$EXE_PATH" != "./portablemc.exe" ]; then
            mv "$EXE_PATH" ./portablemc.exe
        fi
        rm -rf portablemc-* x86_64-* aarch64-* 2>/dev/null
        CMD_LAUNCHER="$TEST_ENV_DIR/portablemc.exe"
    else
        local BIN_PATH=$(find . -type f -iname "portablemc" | head -n 1)
        if [ -n "$BIN_PATH" ] && [ "$BIN_PATH" != "./portablemc" ]; then
            mv "$BIN_PATH" ./portablemc
        fi
        rm -rf portablemc-* x86_64-* aarch64-* 2>/dev/null
        chmod +x portablemc
        CMD_LAUNCHER="$TEST_ENV_DIR/portablemc"
    fi
    popd >/dev/null
}

# ------------------------------------------------------------------------------
# 3. Modrinth Downloader (Optimized with JSON API Caching)
# ------------------------------------------------------------------------------
download_modrinth_dep() {
    local SLUG=$1; local GAME_VER=$2; local LOADER=$3; local DEST_DIR=$4
    local LOADERS_ENC="%5B%22${LOADER}%22%5D"
    local VERSIONS_ENC="%5B%22${GAME_VER}%22%5D"
    local API_URL="https://api.modrinth.com/v2/project/${SLUG}/version?loaders=${LOADERS_ENC}&game_versions=${VERSIONS_ENC}"
    
    # Define a unique cache file for this specific API request
    local API_CACHE_FILE="${DEPS_CACHE_DIR}/api_${SLUG}_${GAME_VER}_${LOADER}.json"
    local RESPONSE=""

    # 1. Fetch JSON from API or read from local cache
    if [ "$FORCE_UPDATE_DEPS" = true ] || [ ! -f "$API_CACHE_FILE" ]; then
        RESPONSE=$(curl -s -H "User-Agent: BuildingWands-TestEnvBuilder/1.0" "$API_URL")
        # Only cache if the response looks like a valid Modrinth JSON array
        if [[ "$RESPONSE" == *"["* ]]; then
            echo "$RESPONSE" > "$API_CACHE_FILE"
        fi
    else
        RESPONSE=$(cat "$API_CACHE_FILE")
    fi

    local DOWNLOAD_URL=$(echo "$RESPONSE" | jq -r '.[0].files[0].url // empty')
    local FILENAME=$(echo "$RESPONSE" | jq -r '.[0].files[0].filename // empty')

    if [ -n "$DOWNLOAD_URL" ] && [ "$DOWNLOAD_URL" != "null" ]; then
        
        # 2. Skip entirely if the jar is already sitting in the instance's mod folder
        if [ "$FORCE_UPDATE_DEPS" = false ] && [ -f "$DEST_DIR/$FILENAME" ]; then
            echo "  -> Cached $SLUG ($FILENAME) already present in instance."
            return
        fi

        local CACHED_FILE="${DEPS_CACHE_DIR}/${FILENAME}"
        
        # 3. Download the actual Jar file if missing from the global cache
        if [ "$FORCE_UPDATE_DEPS" = true ] || [ ! -f "$CACHED_FILE" ]; then
            echo "  -> Downloading $SLUG ($FILENAME)..."
            curl -s -L -o "$CACHED_FILE" "$DOWNLOAD_URL"
        else
            echo "  -> Found $SLUG in global cache."
        fi
        
        # 4. Copy to the instance folder
        cp "$CACHED_FILE" "$DEST_DIR/"
    else
        echo "  -> ERROR: Could not resolve download for $SLUG. (Check API cache)"
    fi
}

# ------------------------------------------------------------------------------
# 4. CurseForge Downloader (Website API, no key required)
# ------------------------------------------------------------------------------
download_curseforge_dep() {
    local PROJECT_ID=$1; local SLUG=$2; local GAME_VER=$3; local LOADER=$4; local DEST_DIR=$5

    # Map loader name to CurseForge's capitalized format
    local CF_LOADER
    case "$LOADER" in
        fabric)   CF_LOADER="Fabric" ;;
        forge)    CF_LOADER="Forge" ;;
        neoforge) CF_LOADER="NeoForge" ;;
        *)        CF_LOADER="$LOADER" ;;
    esac

    local API_CACHE_FILE="${CF_CACHE_DIR}/api_${PROJECT_ID}_${GAME_VER}_${LOADER}.json"
    local RESPONSE=""

    # 1. Fetch JSON from API or read from local cache
    if [ "$FORCE_UPDATE_DEPS" = true ] || [ ! -f "$API_CACHE_FILE" ]; then
        local API_URL="https://www.curseforge.com/api/v1/mods/${PROJECT_ID}/files?gameVersion=${GAME_VER}&pageSize=50"
        RESPONSE=$(curl -s "$API_URL")
        # Only cache if the response contains valid CurseForge data
        if [[ "$RESPONSE" == *"data"* ]]; then
            echo "$RESPONSE" > "$API_CACHE_FILE"
        fi
    else
        RESPONSE=$(cat "$API_CACHE_FILE")
    fi

    # 2. Client-side filter: exact match on game version AND loader
    #    Server-side gameVersion filter is unreliable (fuzzy matches).
    #    Prefer releaseType 1 (Release); fall back to any type if none found.
    local FILE_INFO=$(echo "$RESPONSE" | jq -r --arg ver "$GAME_VER" --arg loader "$CF_LOADER" '
        [.data[] | select(
            (.gameVersions | index($ver)) and
            (.gameVersions | index($loader))
        )] |
        sort_by(.id) | reverse |
        ((map(select(.releaseType == 1)) | first) // first) |
        {id: .id, fileName: .fileName}
    ')

    local FILE_ID=$(echo "$FILE_INFO" | jq -r '.id // empty')
    local FILENAME=$(echo "$FILE_INFO" | jq -r '.fileName // empty')

    if [ -z "$FILE_ID" ] || [ "$FILE_ID" == "null" ]; then
        echo "  -> ERROR: No CurseForge file found for $SLUG (project $PROJECT_ID) matching $GAME_VER + $CF_LOADER"
        return
    fi

    # 3. Skip if already present in instance mods dir
    if [ "$FORCE_UPDATE_DEPS" = false ] && [ -f "$DEST_DIR/$FILENAME" ]; then
        echo "  -> Cached $SLUG ($FILENAME) already present in instance."
        return
    fi

    local CACHED_FILE="${CF_CACHE_DIR}/${FILENAME}"

    # 4. Download if not in global cache (follows 307 redirect to CDN)
    if [ "$FORCE_UPDATE_DEPS" = true ] || [ ! -f "$CACHED_FILE" ]; then
        echo "  -> Downloading $SLUG ($FILENAME) from CurseForge..."
        local DOWNLOAD_URL="https://www.curseforge.com/api/v1/mods/${PROJECT_ID}/files/${FILE_ID}/download"
        curl -s -L -o "$CACHED_FILE" "$DOWNLOAD_URL"
    else
        echo "  -> Found $SLUG in global cache."
    fi

    # 5. Copy to instance folder
    cp "$CACHED_FILE" "$DEST_DIR/"
}

# ==============================================================================
# MAIN EXECUTION
# ==============================================================================

download_launcher
echo ">>> Bootstrapping environments for player: $PLAYER_NAME using $CMD_LAUNCHER"

INSTANCE_COUNT=$(jq '. | length' "$INSTANCES_FILE")

for (( i=0; i<$INSTANCE_COUNT; i++ )); do
    NAME=$(jq -r ".[$i].name" "$INSTANCES_FILE")
    GAME_VER=$(jq -r ".[$i].game_version" "$INSTANCES_FILE")
    LOADER=$(jq -r ".[$i].loader" "$INSTANCES_FILE")
    JAR_DIR=$(jq -r ".[$i].jar_dir" "$INSTANCES_FILE")

    echo -e "\n>>> Configuring instance: $NAME"

    MAIN_DIR="$TEST_ENV_DIR/instances/$NAME/.minecraft"
    MODS_DIR="$MAIN_DIR/mods"
    mkdir -p "$MODS_DIR"
    
    # Copy newly compiled mod JAR (Targeted cleanup replaces the old blanket jar deletion)
    if [ -d "$JAR_DIR" ]; then
        COMPILED_JAR=$(ls "$JAR_DIR"/*.jar 2>/dev/null | grep -Ev "sources|javadoc|dev" | head -n 1)
        if [ -n "$COMPILED_JAR" ] && [ -f "$COMPILED_JAR" ]; then
            # Grab the base name of your mod (e.g. 'wands' from 'wands-1.0.jar')
            MOD_BASENAME=$(basename "$COMPILED_JAR" | sed -E 's/-[0-9].*//')
            
            # Delete ONLY previous builds of your mod to prevent duplicate loading crashes
            rm -f "$MODS_DIR"/${MOD_BASENAME}*.jar
            
            cp "$COMPILED_JAR" "$MODS_DIR/"
            echo "  -> Copied your mod: $(basename "$COMPILED_JAR")"
        fi
    fi

    # Download Modrinth Dependencies (Required)
    DEPS_LENGTH=$(jq ".[$i].dependencies | length" "$INSTANCES_FILE")
    for (( d=0; d<$DEPS_LENGTH; d++ )); do
        DEP_SLUG=$(jq -r ".[$i].dependencies[$d]" "$INSTANCES_FILE")
        download_modrinth_dep "$DEP_SLUG" "$GAME_VER" "$LOADER" "$MODS_DIR"
    done

    # Download Modrinth Dependencies (Optional / Compat mods)
    if [ "$WITH_OPTIONAL" = true ]; then
        OPT_DEPS_LENGTH=$(jq ".[$i].optional_dependencies // [] | length" "$INSTANCES_FILE")
        for (( d=0; d<$OPT_DEPS_LENGTH; d++ )); do
            DEP_SLUG=$(jq -r ".[$i].optional_dependencies[$d]" "$INSTANCES_FILE")
            download_modrinth_dep "$DEP_SLUG" "$GAME_VER" "$LOADER" "$MODS_DIR"
        done
    fi

    # Download CurseForge Dependencies (Required)
    CF_DEPS_LENGTH=$(jq ".[$i].curseforge_dependencies // [] | length" "$INSTANCES_FILE")
    for (( d=0; d<$CF_DEPS_LENGTH; d++ )); do
        CF_PROJECT_ID=$(jq -r ".[$i].curseforge_dependencies[$d].project_id" "$INSTANCES_FILE")
        CF_SLUG=$(jq -r ".[$i].curseforge_dependencies[$d].slug" "$INSTANCES_FILE")
        download_curseforge_dep "$CF_PROJECT_ID" "$CF_SLUG" "$GAME_VER" "$LOADER" "$MODS_DIR"
    done

    # Download CurseForge Dependencies (Optional / Compat mods)
    if [ "$WITH_OPTIONAL" = true ]; then
        OPT_CF_DEPS_LENGTH=$(jq ".[$i].optional_curseforge_dependencies // [] | length" "$INSTANCES_FILE")
        for (( d=0; d<$OPT_CF_DEPS_LENGTH; d++ )); do
            CF_PROJECT_ID=$(jq -r ".[$i].optional_curseforge_dependencies[$d].project_id" "$INSTANCES_FILE")
            CF_SLUG=$(jq -r ".[$i].optional_curseforge_dependencies[$d].slug" "$INSTANCES_FILE")
            download_curseforge_dep "$CF_PROJECT_ID" "$CF_SLUG" "$GAME_VER" "$LOADER" "$MODS_DIR"
        done
    fi

    if [[ "$CMD_LAUNCHER" == "$TEST_ENV_DIR/"* ]]; then
        LAUNCHER_CALL="\$SCRIPT_DIR/$(basename "$CMD_LAUNCHER")"
    else
        LAUNCHER_CALL="$CMD_LAUNCHER"
    fi

    # Generate the self-contained launch script
    LAUNCH_SCRIPT="$TEST_ENV_DIR/launch-$NAME.sh"
    cat <<EOF > "$LAUNCH_SCRIPT"
#!/usr/bin/env bash
echo "Starting $NAME Test Environment..."

SCRIPT_DIR="\$(cd "\$(dirname "\$0")" && pwd)"
MAIN_DIR="\$SCRIPT_DIR/instances/$NAME/.minecraft"

# Execute PortableMC
$LAUNCHER_CALL --main-dir "\$MAIN_DIR" start "${LOADER}:${GAME_VER}" -u "$PLAYER_NAME"
EOF

    chmod +x "$LAUNCH_SCRIPT"
    echo "  -> Generated launch script: $(basename "$LAUNCH_SCRIPT")"

    # Generate guest instance (separate game dir, same mods, different player)
    GUEST_DIR="$TEST_ENV_DIR/instances/$NAME-guest/.minecraft"
    GUEST_MODS_DIR="$GUEST_DIR/mods"
    mkdir -p "$GUEST_MODS_DIR"

    rm -f "$GUEST_MODS_DIR"/*.jar
    # Copy all mods from the main instance into the guest instance
    cp -u "$MODS_DIR"/*.jar "$GUEST_MODS_DIR/" 2>/dev/null

    GUEST_SCRIPT="$TEST_ENV_DIR/launch-$NAME-guest.sh"
    cat <<EOF > "$GUEST_SCRIPT"
#!/usr/bin/env bash
echo "Starting $NAME Guest (${GUEST_NAME}) Test Environment..."

SCRIPT_DIR="\$(cd "\$(dirname "\$0")" && pwd)"
MAIN_DIR="\$SCRIPT_DIR/instances/$NAME-guest/.minecraft"

# Execute PortableMC (different player, separate game dir for LAN testing)
$LAUNCHER_CALL --main-dir "\$MAIN_DIR" start "${LOADER}:${GAME_VER}" -u "$GUEST_NAME"
EOF

    chmod +x "$GUEST_SCRIPT"
    echo "  -> Generated guest launch script: $(basename "$GUEST_SCRIPT")"

done

echo -e "\n>>> Test environment setup complete!"