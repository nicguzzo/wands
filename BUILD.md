# Building Wands

Use JDK 25 to run the Gradle wrapper. Gradle resolves the Java toolchains required by each Minecraft target. Each directory under `versions/` containing a `gradle.properties` file defines one target.

```sh
./gradlew build
python .github/scripts/targets.py collect
```

On Windows, use `gradlew.bat`. To build one target, run a task such as `./gradlew :26.3-neoforge:build`. Production JARs are written to `versions/<target>/build/libs/`. The collection step checks that every expected JAR exists and contains its loader metadata, then copies them into `build/release-jars/`.
