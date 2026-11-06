#!/usr/bin/env bash

# ==============================================================================
# Script: generate_version_properties.sh
# Purpose: Fetch dependencies from Modrinth, Fabric/NeoForge/Forge webs and
#          CurseForge APIs, and generate gradle.properties for the target MC version.
# Usage: ./generate_version_properties.sh [MC_VERSION]
# Example: ./generate_version_properties.sh 26.3
# ==============================================================================

set -euo pipefail

MC_VERSION="${1:-26.3}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
VERSIONS_DIR="${SCRIPT_DIR}/versions"

USER_AGENT="BuildingWands-VersionGen/1.0 (contact: nicguzzo@gmail.com)"

echo "=========================================================="
echo " Generating gradle.properties for Minecraft: ${MC_VERSION}"
echo "=========================================================="

# ------------------------------------------------------------------------------
# 1. Fetch Fabric API
# Web: https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/maven-metadata.xml
# Modrinth: https://api.modrinth.com/v2/project/fabric-api/version
# ------------------------------------------------------------------------------
echo ">>> [1/7] Fetching Fabric API version..."
FABRIC_API_VER=""

FABRIC_MAVEN_URL="https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/maven-metadata.xml"
FABRIC_XML=$(curl -s -A "$USER_AGENT" "$FABRIC_MAVEN_URL" || true)

if [ -n "$FABRIC_XML" ]; then
    FABRIC_API_VER=$(python -c "
import sys, xml.etree.ElementTree as ET
try:
    content = sys.stdin.read()
    root = ET.fromstring(content)
    target = '+' + sys.argv[1]
    matching = [v.text for v in root.findall('.//version') if v.text and target in v.text]
    if matching:
        print(matching[-1])
except Exception:
    pass
" "$MC_VERSION" <<< "$FABRIC_XML")
fi

if [ -z "$FABRIC_API_VER" ]; then
    echo "    Maven didn't return version, querying Modrinth..."
    MODRINTH_URL="https://api.modrinth.com/v2/project/fabric-api/version?game_versions=%5B%22${MC_VERSION}%22%5D&loaders=%5B%22fabric%22%5D"
    MODRINTH_JSON=$(curl -s -A "$USER_AGENT" "$MODRINTH_URL" || true)
    if [ -n "$MODRINTH_JSON" ]; then
        FABRIC_API_VER=$(python -c "
import sys, json
try:
    d = json.loads(sys.stdin.read())
    if d and isinstance(d, list) and len(d) > 0:
        print(d[0].get('version_number', ''))
except Exception:
    pass
" <<< "$MODRINTH_JSON")
    fi
fi

echo "    -> Fabric API: ${FABRIC_API_VER:-NOT FOUND}"

# ------------------------------------------------------------------------------
# 2. Fetch NeoForge
# Web: https://maven.neoforged.net/releases/net/neoforged/neoforge/maven-metadata.xml
# ------------------------------------------------------------------------------
echo ">>> [2/7] Fetching NeoForge version..."
NEOFORGE_VER=""
NEOFORGE_MAVEN_URL="https://maven.neoforged.net/releases/net/neoforged/neoforge/maven-metadata.xml"
NEOFORGE_XML=$(curl -s -A "$USER_AGENT" "$NEOFORGE_MAVEN_URL" || true)

if [ -n "$NEOFORGE_XML" ]; then
    NEOFORGE_VER=$(python -c "
import sys, xml.etree.ElementTree as ET
try:
    content = sys.stdin.read()
    root = ET.fromstring(content)
    prefix = sys.argv[1] + '.'
    matching = [v.text for v in root.findall('.//version') if v.text and v.text.startswith(prefix)]
    if matching:
        # Prefer non-beta/non-alpha release if available, otherwise latest
        releases = [v for v in matching if 'beta' not in v.lower() and 'alpha' not in v.lower()]
        print(releases[-1] if releases else matching[-1])
except Exception:
    pass
" "$MC_VERSION" <<< "$NEOFORGE_XML")
fi

echo "    -> NeoForge: ${NEOFORGE_VER:-NOT FOUND}"

# ------------------------------------------------------------------------------
# 3. Fetch Forge (optional, if exists for this version)
# Web: https://files.minecraftforge.net/net/minecraftforge/forge/promotions_slim.json
# ------------------------------------------------------------------------------
echo ">>> [3/7] Fetching Minecraft Forge version (optional)..."
FORGE_VER=""
FORGE_PROMO_URL="https://files.minecraftforge.net/net/minecraftforge/forge/promotions_slim.json"
FORGE_JSON=$(curl -s -A "$USER_AGENT" "$FORGE_PROMO_URL" || true)

if [ -n "$FORGE_JSON" ]; then
    FORGE_VER=$(python -c "
import sys, json
try:
    d = json.loads(sys.stdin.read())
    mc = sys.argv[1]
    promos = d.get('promos', {})
    ver = promos.get(mc + '-recommended') or promos.get(mc + '-latest')
    if ver:
        print(mc + '-' + ver)
except Exception:
    pass
" "$MC_VERSION" <<< "$FORGE_JSON")
fi

echo "    -> Forge: ${FORGE_VER:-NOT FOUND / N/A}"

# ------------------------------------------------------------------------------
# 4. Fetch ModMenu (Fabric)
# Modrinth: https://api.modrinth.com/v2/project/modmenu/version
# ------------------------------------------------------------------------------
echo ">>> [4/7] Fetching ModMenu version from Modrinth..."
MODMENU_VER=""
MODMENU_URL="https://api.modrinth.com/v2/project/modmenu/version?game_versions=%5B%22${MC_VERSION}%22%5D&loaders=%5B%22fabric%22%5D"
MODMENU_JSON=$(curl -s -A "$USER_AGENT" "$MODMENU_URL" || true)

if [ -n "$MODMENU_JSON" ]; then
    MODMENU_VER=$(python -c "
import sys, json
try:
    d = json.loads(sys.stdin.read())
    if d and isinstance(d, list) and len(d) > 0:
        releases = [v for v in d if v.get('version_type') == 'release']
        ver = (releases[0] if releases else d[0]).get('version_number', '')
        if ver.startswith('v') or ver.startswith('V'):
            ver = ver[1:]
        print(ver)
except Exception:
    pass
" <<< "$MODMENU_JSON")
fi

# Fallback: Terraformers Maven
if [ -z "$MODMENU_VER" ]; then
    MODMENU_MAVEN_URL="https://maven.terraformersmc.com/releases/com/terraformersmc/modmenu/maven-metadata.xml"
    MODMENU_XML=$(curl -s -A "$USER_AGENT" "$MODMENU_MAVEN_URL" || true)
    if [ -n "$MODMENU_XML" ]; then
        MODMENU_VER=$(python -c "
import sys, xml.etree.ElementTree as ET
try:
    root = ET.fromstring(sys.stdin.read())
    versions = [v.text for v in root.findall('.//version') if v.text]
    if versions:
        print(versions[-1])
except Exception:
    pass
" <<< "$MODMENU_XML")
    fi
fi

echo "    -> ModMenu: ${MODMENU_VER:-NOT FOUND}"

# ------------------------------------------------------------------------------
# 5. Fetch Cloth Config
# Shedaniel Maven: https://maven.shedaniel.me/me/shedaniel/cloth/cloth-config-fabric/maven-metadata.xml
# Modrinth: https://api.modrinth.com/v2/project/cloth-config/version
# ------------------------------------------------------------------------------
echo ">>> [5/7] Fetching Cloth Config version..."
CLOTH_CONFIG_VER=""

CLOTH_MAVEN_URL="https://maven.shedaniel.me/me/shedaniel/cloth/cloth-config-fabric/maven-metadata.xml"
CLOTH_XML=$(curl -s -A "$USER_AGENT" "$CLOTH_MAVEN_URL" || true)

if [ -n "$CLOTH_XML" ]; then
    CLOTH_CONFIG_VER=$(python -c "
import sys, xml.etree.ElementTree as ET
try:
    root = ET.fromstring(sys.stdin.read())
    prefix = sys.argv[1] + '.'
    matching = [v.text for v in root.findall('.//version') if v.text and v.text.startswith(prefix)]
    if matching:
        print(matching[-1])
except Exception:
    pass
" "$MC_VERSION" <<< "$CLOTH_XML")
fi

if [ -z "$CLOTH_CONFIG_VER" ]; then
    CLOTH_MODRINTH_URL="https://api.modrinth.com/v2/project/cloth-config/version?game_versions=%5B%22${MC_VERSION}%22%5D"
    CLOTH_JSON=$(curl -s -A "$USER_AGENT" "$CLOTH_MODRINTH_URL" || true)
    if [ -n "$CLOTH_JSON" ]; then
        CLOTH_CONFIG_VER=$(python -c "
import sys, json
try:
    d = json.loads(sys.stdin.read())
    if d and isinstance(d, list) and len(d) > 0:
        ver = d[0].get('version_number', '')
        if '+' in ver:
            ver = ver.split('+')[0]
        print(ver)
except Exception:
    pass
" <<< "$CLOTH_JSON")
    fi
fi

echo "    -> Cloth Config: ${CLOTH_CONFIG_VER:-NOT FOUND}"

# ------------------------------------------------------------------------------
# 6. Fetch CurseForge Dependencies (OPAC, FTB Chunks, FTB Library, Flan, GOML)
# ------------------------------------------------------------------------------
echo ">>> [6/7] Fetching CurseForge dependencies (OPAC, FTB, Flan, GOML)..."

fetch_cf_file_id() {
    local project_id="$1"
    local slug="$2"
    local loader="$3"
    local mc_ver="$4"
    local fallback_prefix="$5"

    local cf_url="https://www.curseforge.com/api/v1/mods/${project_id}/files?pageSize=50"
    local cf_json
    cf_json=$(curl -s -A "$USER_AGENT" "$cf_url" || true)

    if [ -z "$cf_json" ]; then
        echo ""
        return
    fi

    python -c "
import sys, json

try:
    d = json.loads(sys.stdin.read())
    loader = sys.argv[1].lower()
    mc_ver = sys.argv[2]
    fallback_prefix = sys.argv[3]

    files = d.get('data', [])

    def matches_loader(f):
        gvers = [v.lower() for v in f.get('gameVersions', [])]
        if loader == 'fabric':
            return 'fabric' in gvers or 'quilt' in gvers
        elif loader == 'neoforge':
            return 'neoforge' in gvers
        elif loader == 'forge':
            return 'forge' in gvers
        return True

    # 1. Exact match for target MC version + loader
    exact = [f for f in files if mc_ver in f.get('gameVersions', []) and matches_loader(f)]
    if exact:
        exact.sort(key=lambda x: x.get('id', 0), reverse=True)
        releases = [f for f in exact if f.get('releaseType') == 1]
        chosen = releases[0] if releases else exact[0]
        print(chosen.get('id', ''))
        sys.exit(0)

    # 2. Match for major branch (e.g. '26.') + loader
    if fallback_prefix:
        major_matches = [f for f in files if any(v.startswith(fallback_prefix) for v in f.get('gameVersions', [])) and matches_loader(f)]
        if major_matches:
            major_matches.sort(key=lambda x: x.get('id', 0), reverse=True)
            releases = [f for f in major_matches if f.get('releaseType') == 1]
            chosen = releases[0] if releases else major_matches[0]
            print(chosen.get('id', ''))
            sys.exit(0)

    # 3. Fallback: latest file matching loader
    loader_matches = [f for f in files if matches_loader(f)]
    if loader_matches:
        loader_matches.sort(key=lambda x: x.get('id', 0), reverse=True)
        print(loader_matches[0].get('id', ''))
except Exception:
    pass
" "$loader" "$mc_ver" "$fallback_prefix" <<< "$cf_json"
}

# Major version prefix for fallback (e.g. '26.' for 26.3)
MAJOR_PREFIX="$(echo "$MC_VERSION" | cut -d'.' -f1)."

# OPAC (636608)
OPAC_FABRIC=$(fetch_cf_file_id 636608 "open-parties-and-claims" "fabric" "$MC_VERSION" "$MAJOR_PREFIX")
OPAC_NEOFORGE=$(fetch_cf_file_id 636608 "open-parties-and-claims" "neoforge" "$MC_VERSION" "$MAJOR_PREFIX")
echo "    -> OPAC Fabric: ${OPAC_FABRIC:-NOT FOUND}, NeoForge: ${OPAC_NEOFORGE:-NOT FOUND}"

# FTB Chunks (Fabric: 472657, NeoForge/Forge: 314906)
FTB_CHUNKS_FABRIC=$(fetch_cf_file_id 472657 "ftb-chunks-fabric" "fabric" "$MC_VERSION" "$MAJOR_PREFIX")
FTB_CHUNKS_NEOFORGE=$(fetch_cf_file_id 314906 "ftb-chunks-forge" "neoforge" "$MC_VERSION" "$MAJOR_PREFIX")
echo "    -> FTB Chunks Fabric: ${FTB_CHUNKS_FABRIC:-NOT FOUND}, NeoForge: ${FTB_CHUNKS_NEOFORGE:-NOT FOUND}"

# FTB Library (Fabric: 438495, NeoForge/Forge: 404465)
FTB_LIB_FABRIC=$(fetch_cf_file_id 438495 "ftb-library-fabric" "fabric" "$MC_VERSION" "$MAJOR_PREFIX")
FTB_LIB_NEOFORGE=$(fetch_cf_file_id 404465 "ftb-library-forge" "neoforge" "$MC_VERSION" "$MAJOR_PREFIX")
echo "    -> FTB Library Fabric: ${FTB_LIB_FABRIC:-NOT FOUND}, NeoForge: ${FTB_LIB_NEOFORGE:-NOT FOUND}"

# Flan (Fabric: 404578, NeoForge/Forge: 493246)
FLAN_FABRIC=$(fetch_cf_file_id 404578 "flan" "fabric" "$MC_VERSION" "$MAJOR_PREFIX")
FLAN_NEOFORGE=$(fetch_cf_file_id 493246 "flan-forge" "neoforge" "$MC_VERSION" "$MAJOR_PREFIX")
echo "    -> Flan Fabric: ${FLAN_FABRIC:-NOT FOUND}, NeoForge: ${FLAN_NEOFORGE:-NOT FOUND}"

# GOML (Fabric only: 572651)
GOML_FABRIC=$(fetch_cf_file_id 572651 "get-off-my-lawn-reserved" "fabric" "$MC_VERSION" "$MAJOR_PREFIX")
echo "    -> GOML Fabric: ${GOML_FABRIC:-NOT FOUND}"

# ------------------------------------------------------------------------------
# 7. Generate gradle.properties Files
# ------------------------------------------------------------------------------
echo ">>> [7/7] Generating gradle.properties files..."

FABRIC_DIR="${VERSIONS_DIR}/${MC_VERSION}-fabric"
NEOFORGE_DIR="${VERSIONS_DIR}/${MC_VERSION}-neoforge"

mkdir -p "$FABRIC_DIR"
mkdir -p "$NEOFORGE_DIR"

# Fabric gradle.properties
cat <<EOF > "${FABRIC_DIR}/gradle.properties"
modstitch.platform=fabric-loom
deps.fabric-api=${FABRIC_API_VER}

deps.minecraft=${MC_VERSION}

modmenu_version=${MODMENU_VER}
cloth_config_version=${CLOTH_CONFIG_VER}

opac         = curse.maven:open-parties-and-claims-636608:${OPAC_FABRIC}
ftb_chunks   = curse.maven:ftb-chunks-fabric-472657:${FTB_CHUNKS_FABRIC}
ftb_library  = curse.maven:ftb-library-fabric-438495:${FTB_LIB_FABRIC}
flan         = curse.maven:flan-404578:${FLAN_FABRIC}
goml         = curse.maven:get-off-my-lawn-reserved-572651:${GOML_FABRIC}

rtree_version = 0.3.0
EOF
echo "  [CREATED] ${FABRIC_DIR}/gradle.properties"

# NeoForge gradle.properties
cat <<EOF > "${NEOFORGE_DIR}/gradle.properties"
modstitch.platform=moddevgradle

deps.minecraft=${MC_VERSION}
deps.neoforge=${NEOFORGE_VER}

cloth_config_version=${CLOTH_CONFIG_VER}
opac         = curse.maven:open-parties-and-claims-636608:${OPAC_NEOFORGE}
ftb_chunks   = curse.maven:ftb-chunks-forge-314906:${FTB_CHUNKS_NEOFORGE}
ftb_library  = curse.maven:ftb-library-forge-404465:${FTB_LIB_NEOFORGE}
flan         = curse.maven:flan-forge-493246:${FLAN_NEOFORGE}
goml         =

rtree_version = 0.3.0
EOF
echo "  [CREATED] ${NEOFORGE_DIR}/gradle.properties"

# Optional: Forge gradle.properties (if Forge exists for this version)
if [ -n "$FORGE_VER" ]; then
    FORGE_DIR="${VERSIONS_DIR}/${MC_VERSION}-forge"
    OPAC_FORGE=$(fetch_cf_file_id 636608 "open-parties-and-claims" "forge" "$MC_VERSION" "$MAJOR_PREFIX")
    FTB_CHUNKS_FORGE=$(fetch_cf_file_id 314906 "ftb-chunks-forge" "forge" "$MC_VERSION" "$MAJOR_PREFIX")
    FTB_LIB_FORGE=$(fetch_cf_file_id 404465 "ftb-library-forge" "forge" "$MC_VERSION" "$MAJOR_PREFIX")
    FLAN_FORGE=$(fetch_cf_file_id 493246 "flan-forge" "forge" "$MC_VERSION" "$MAJOR_PREFIX")

    mkdir -p "$FORGE_DIR"
    cat <<EOF > "${FORGE_DIR}/gradle.properties"
modstitch.platform=moddevgradle-legacy

deps.minecraft=${MC_VERSION}
deps.forge=${FORGE_VER}

cloth_config_version=${CLOTH_CONFIG_VER}

opac        = curse.maven:open-parties-and-claims-636608:${OPAC_FORGE}
ftb_chunks  = curse.maven:ftb-chunks-forge-314906:${FTB_CHUNKS_FORGE}
ftb_library = curse.maven:ftb-library-forge-404465:${FTB_LIB_FORGE}
flan        = curse.maven:flan-forge-493246:${FLAN_FORGE}
goml        =
rtree_version = 0.3.0
EOF
    echo "  [CREATED] ${FORGE_DIR}/gradle.properties"
fi

echo ""
echo "=========================================================="
echo " Generation completed successfully for ${MC_VERSION}!"
echo "=========================================================="
