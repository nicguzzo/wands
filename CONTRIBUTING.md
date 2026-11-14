# Contributing to Building Wands

## Project structure and builds

Stonecutter generates each Minecraft and loader target from the shared Java sources in `src/main/java`. Put version-specific API calls in the existing `compat` package when callers can share one implementation. Use the surrounding Stonecutter directives for inline version and loader differences. Shared assets live in `src/main/resources` and Minecraft-specific resources in `src/resources_per_version`.

Each `versions/<minecraft>-<loader>/gradle.properties` defines a build target and its dependencies. Use JDK 25 for Gradle. The target determines the Java version of the shipped mod. For build and test commands, see [BUILD.md](BUILD.md).

## CI workflow

Pushes to `main` and pull requests targeting `main` run one build per discovered target. Each build verifies its production JAR and uploads it as `BuildingWands-{target}-{sha}`.

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
2. Reads `mod_version` and `release_channel` from the root `gradle.properties`
3. Builds every target and verifies the production JARs
4. Publishes to CurseForge and Modrinth with `./gradlew publishMods` when publishing is enabled
5. Uploads the JARs as a GitHub Actions artifact
6. Creates a GitHub Release at the selected commit and attaches the JARs when publishing is enabled

### Publishing

The Modstitch publishing plugin reads `CF_TOKEN` and `MODRINTH_TOKEN` from repository secrets. The workflow passes release notes in `WANDS_CHANGELOG`. The `publishMods` block in `build.gradle.kts` configures both platforms.

### Versioning

Set `mod_version` and `release_channel` in the root `gradle.properties` before releasing. Supported channels are `release`, `beta`, and `alpha`.

```properties
mod_version=3.2.1
release_channel=release
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
