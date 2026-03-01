# Contributing to Building Wands

## Project structure

```
├── core/                    Shared mod source code (all MC versions)
│   ├── src/main/java/       Java source with preprocessor directives
│   ├── lang/                Language files
│   └── textures/            Shared texture assets
│
├── compat/                  Version compatibility layer
│   └── src/main/java/       Abstracts APIs that differ between MC versions
│
├── mc1.20.1/                MC 1.20.1 build targets
│   ├── gradle.properties    Version-specific dependencies
│   ├── common/              Architectury common module
│   ├── fabric/              Fabric loader target
│   └── forge/               Forge loader target
│
├── mc1.21.1/                MC 1.21.1 build targets
│   ├── common/
│   ├── fabric/
│   └── neoforge/
│
├── mc1.21.11/               MC 1.21.11 build targets
│   ├── common/
│   ├── fabric/
│   └── neoforge/
│
├── build.gradle             Root build config, plugin setup, publishing
├── settings.gradle          Conditional subproject inclusion
├── shared-sources.gradle    Links core/ and compat/ into each mc target
└── gradle.properties        Mod version, release type, maven group
```

### How code flows from core/ to build targets

`core/` and `compat/` are **not** Gradle subprojects — they have no build tasks. Instead, `shared-sources.gradle` creates symlinks (Windows junctions) from each `mc*/common/` module into the shared source trees:

```
core/src/main/java       →  mc1.20.1/common/src/main/core-link
compat/src/main/java     →  mc1.20.1/common/src/main/compat-link
```

Each `mc*/common/` module compiles core + compat against its MC version's dependencies. The `mc*/fabric/` and `mc*/forge/` (or `neoforge/`) modules depend on their sibling `common/` module and add loader-specific code.

## Build system

The project uses the [Architectury](https://github.com/architectury) toolchain with [Manifold](https://github.com/nicguzzo/wands/wiki) preprocessor for multi-version support.

### Build commands

```bash
# Build all 6 targets
./gradlew build

# Build a single MC version (both loaders)
./gradlew -Ptarget=mc1.20.1 build

# Build a specific target
./gradlew -Ptarget=mc1.20.1 :mc1.20.1:fabric:build

# Run a development client
./gradlew -Ptarget=mc1.20.1 :mc1.20.1:fabric:runClient
```

> **Windows note:** Use `bash gradlew build` from Git Bash, not `.\gradlew` or `gradlew.bat`.

### The 6 build targets

| MC Version | Loaders          | Java |
|------------|------------------|------|
| 1.20.1     | Fabric, Forge    | 17   |
| 1.21.1     | Fabric, NeoForge | 21   |
| 1.21.11    | Fabric, NeoForge | 21   |

### Conditional compilation with `-Ptarget`

`settings.gradle` reads the `target` property to include only the relevant subprojects:

- `./gradlew build` — includes all MC versions
- `./gradlew -Ptarget=mc1.21.1 build` — includes only mc1.21.1 subprojects

CI uses `-Ptarget` to build each target in a separate parallel job.

## Preprocessor directives

The Manifold preprocessor handles API differences across MC versions. Directives are used in `core/` and `compat/` source files and evaluated at compile time.

### MC_VERSION encoding

| MC Version | MC_VERSION value |
|------------|-----------------|
| 1.20.1     | `12001`          |
| 1.21.1     | `12101`          |
| 1.21.11    | `12111`          |

### Usage

```java
#if MC_VERSION >= 12111
    // 1.21.11+ only
    float pt = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);
#else
    // 1.20.1 and 1.21.1
    float pt = Minecraft.getInstance().getFrameTime();
#endif
```

### When to use preprocessor vs compat/

- **Preprocessor (`#if`):** Small inline differences — a method name changed, a parameter added, an import differs.
- **Compat class:** Larger behavioral differences that benefit from a clean method abstraction. The `Compat` class in `compat/src/` uses preprocessor directives internally but exposes a stable API to core code.

```java
// In compat/src/.../Compat.java (uses preprocessor internally)
static public float getPartialTick() {
    #if MC_VERSION >= 12111
        return Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);
    #else
        return Minecraft.getInstance().getFrameTime();
    #endif
}

// In core/ code (clean call, no preprocessor needed)
float pt = Compat.getPartialTick();
```

## CI workflow

**Trigger:** Push to `main` or pull request targeting `main`.

The CI workflow (`.github/workflows/ci.yml`) runs 6 parallel build jobs — one per MC version + loader combination:

1. **Discover** — scans `mc*/` directories to find all target/loader pairs (excluding `common/`)
2. **Build matrix** — each pair gets its own job: checkout, JDK 21 setup, Gradle build
3. **Artifact upload** — each job uploads its JAR as a separate artifact named `BuildingWands-{mcVersion}-{loader}-{sha}`

Gradle caching (`gradle/actions/setup-gradle`) persists dependencies across runs.

## Release workflow

**Trigger:** Manual dispatch (`workflow_dispatch`) from the GitHub Actions UI.

### Inputs

| Input       | Required | Description |
|-------------|----------|-------------|
| `ref`       | Yes      | Branch, tag, or commit SHA to release |
| `changelog` | No       | Markdown changelog text. Falls back to `CHANGELOG.md` |
| `dry_run`   | No       | Build and upload artifacts without publishing |

### What it does

1. Checks out the specified ref
2. Reads `mod_version` and `release_type` from `gradle.properties`
3. Builds all 6 targets (`./gradlew build`)
4. Publishes to CurseForge and Modrinth (`./gradlew publishUnified`)
5. Uploads JARs as a GitHub Actions artifact
6. Creates a GitHub Release with tag `v{mod_version}` and attaches JARs

### Publishing

The `me.shedaniel.unified-publishing` Gradle plugin handles CurseForge and Modrinth uploads. It reads:

- `CF_TOKEN` and `MODRINTH_TOKEN` from repository secrets
- Game versions, loaders, and dependencies from `gradle.properties` and `build.gradle`
- Changelog text passed from the workflow via the `WANDS_CHANGELOG` environment variable (see `build.gradle` `unifiedPublishing` block)

### Versioning

Set `mod_version` and `release_type` in the root `gradle.properties` before releasing:

```properties
mod_version=3.0
release_type=beta    # "release", "beta", or "alpha"
```

The release name is formatted as `Building Wands {version}` (release) or `Building Wands {version}-{type}` (pre-release).

### Changelog

The release workflow reads changelog entries from `CHANGELOG.md`. Each version gets a section with a heading that exactly matches `mod_version`:

```markdown
## 3.0
- Added reach distance setting
- Axe oxidation/wax removal in Use mode

## 2.9
- Previous version changes...
```

The heading must match exactly (e.g. `## 3.0`, not `## v3.0`). If a `changelog` input is provided in the dispatch form, it overrides the file.

## Testing

See [TESTING.md](TESTING.md) for instructions on setting up test environments and testing mod compatibility (claim mods, JEI, etc.).
