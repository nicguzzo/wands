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
        maven("https://maven.jamieswhiteshirt.com/libs-release/") {
            content {
                includeGroup("com.jamieswhiteshirt")
            }
        }
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9.8"
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
//    id("fabric-loom") version "1.16-SNAPSHOT"
}

stonecutter {
    kotlinController = true
    centralScript = "build.gradle.kts"

    create(rootProject) {
        file("versions").listFiles().orEmpty()
            .filter { it.resolve("gradle.properties").isFile }
            .sortedBy { it.name }
            .forEach { version(it.name, it.name.substringBeforeLast('-')) }

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

                vcs_version=$(grep -oP 'vcsVersion\s*=\s*"\K[^"]+' settings.gradle.kts)
                active_version=$(grep -oP 'stonecutter\s+active\s+"\K[^"]+' stonecutter.gradle.kts)

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
