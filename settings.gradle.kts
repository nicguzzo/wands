pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()

        // Modstitch
        maven("https://maven.isxander.dev/releases/")

        // Loom platform
        maven("https://maven.fabricmc.net/")

        // MDG platform
        maven("https://maven.neoforged.net/releases/")

        // Stonecutter
        maven("https://maven.kikugie.dev/releases")
        maven("https://maven.kikugie.dev/snapshots")

        // Modstitch
        maven("https://maven.isxander.dev/releases")
        // clothconfig
        maven("https://maven.shedaniel.me/")
        maven("https://maven.terraformersmc.com/releases")
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9+"
//    id("fabric-loom") version "1.16-SNAPSHOT"
}

stonecutter {
    kotlinController = true
    centralScript = "build.gradle.kts"

    create(rootProject) {
        /**
         * @param mcVersion The base minecraft version.
         * @param loaders A list of loaders to target, supports "fabric" (1.14+), "neoforge"(1.20.6+), "vanilla"(any) or "forge"(<=1.20.1)
         */
        fun mc(mcVersion: String, name: String = mcVersion, loaders: Iterable<String>) =
            loaders.forEach { version("$name-$it", mcVersion) }

        // Configure your targets here!
        mc("1.20.1", loaders = listOf("fabric","forge"))
        mc("1.21.1", loaders = listOf("fabric","neoforge"))
        mc("1.21.11", loaders = listOf("fabric","neoforge"))
        mc("26.1.2", loaders = listOf("fabric", "neoforge"))

        // This is the default target.
        // https://stonecutter.kikugie.dev/stonecutter/guide/setup#settings-settings-gradle-kts
        vcsVersion = "26.1.2-fabric"
    }
}

gradle.beforeProject {
    val gitDir = rootDir.resolve(".git")
    if (gitDir.exists() && gitDir.isDirectory) {
        val hooksDir = gitDir.resolve("hooks")
        val preCommitHook = hooksDir.resolve("pre-commit")

        if (!preCommitHook.exists()) {
            hooksDir.mkdirs()
            preCommitHook.writeText(
                """
                #!/bin/bash
                
                vcs_version=$(ggrep -oP 'vcsVersion\s*=\s*"\K[^"]+' settings.gradle.kts)
                active_version=$(ggrep -oP 'stonecutter\s+active\s+"\K[^"]+' stonecutter.gradle.kts)
                
                echo "Detected vcsVersion: ${'$'}vcs_version"
                echo "Detected active version: ${'$'}active_version"
                
                if [ "${'$'}vcs_version" != "${'$'}active_version" ]; then
                  echo "Please run './gradlew \"Reset active project\"' to set the stonecutter branch to the version control version."
                  exit 1
                else
                  echo "Versions match. No action needed."
                fi
                """.trimIndent()
            )
            preCommitHook.setExecutable(true)
            println("Git pre-commit hook installed.")
        }
    } else {
        println("Not a Git repository. Skipping hook installation.")
    }
}

rootProject.name = "wands"
