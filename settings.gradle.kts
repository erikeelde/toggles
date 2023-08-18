enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}

plugins {
    id("com.gradle.enterprise") version "3.13.4"
    id("org.gradle.toolchains.foojay-resolver-convention") version ("0.4.0")
}

buildCache {
    local {
        isEnabled = true
        directory = File(rootDir, "build-cache")
        removeUnusedEntriesAfterDays = 30
    }
    remote<HttpBuildCache> {
        isEnabled = false
    }
}

private val localLibraries = false

rootProject.name = "Toggles"
includeBuild("build-logic/conventions")
includeBuild("toggles-core") {
    dependencySubstitution {
        if (localLibraries) {
            substitute(module("se.eelde.toggles:toggles-core")).using(project(":"))
        }
    }
}
includeBuild("toggles-flow") {
    dependencySubstitution {
        if (localLibraries) {
            substitute(module("se.eelde.toggles:toggles-flow")).using(project(":"))
        }
    }
}
includeBuild("toggles-flow-noop") {
    dependencySubstitution {
        if (localLibraries) {
            substitute(module("se.eelde.toggles:toggles-flow-noop")).using(project(":"))
        }
    }
}
includeBuild("toggles-prefs") {
    dependencySubstitution {
        if (localLibraries) {
            substitute(module("se.eelde.toggles:toggles-prefs")).using(project(":"))
        }
    }
}
includeBuild("toggles-prefs-noop") {
    dependencySubstitution {
        if (localLibraries) {
            substitute(module("se.eelde.toggles:toggles-prefs-noop")).using(project(":"))
        }
    }
}
include(
    ":toggles-app",
    ":toggles-sample",
    ":modules:compose-theme",
    ":modules:database",
    ":modules:applications",
    ":modules:oss",
)

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
