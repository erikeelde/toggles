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
    id("com.gradle.develocity") version "3.18"
    id("org.gradle.toolchains.foojay-resolver-convention") version ("0.8.0")
}

develocity {
    buildScan {
        termsOfUseUrl.set("https://gradle.com/help/legal-terms-of-use")
        termsOfUseAgree.set("yes")
    }
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

val localLibaries = providers.gradleProperty("se-eelde-toggles-use-local-libraries")
    .map { it.toBoolean() }
    .orElse(false)

rootProject.name = "Toggles"
includeBuild("build-logic/conventions")
includeBuild("toggles-core") {
    dependencySubstitution {
        if (localLibaries.get()) {
            substitute(module("se.eelde.toggles:toggles-core")).using(project(":"))
        }
    }
}
includeBuild("toggles-flow") {
    dependencySubstitution {
        if (localLibaries.get()) {
            substitute(module("se.eelde.toggles:toggles-flow")).using(project(":"))
        }
    }
}
includeBuild("toggles-flow-noop") {
    dependencySubstitution {
        if (localLibaries.get()) {
            substitute(module("se.eelde.toggles:toggles-flow-noop")).using(project(":"))
        }
    }
}
includeBuild("toggles-prefs") {
    dependencySubstitution {
        if (localLibaries.get()) {
            substitute(module("se.eelde.toggles:toggles-prefs")).using(project(":"))
        }
    }
}
includeBuild("toggles-prefs-noop") {
    dependencySubstitution {
        if (localLibaries.get()) {
            substitute(module("se.eelde.toggles:toggles-prefs-noop")).using(project(":"))
        }
    }
}
include(
    ":toggles-app",
    ":toggles-sample",
    ":modules:compose-theme",
    ":modules:database",
    ":modules:provider",
    ":modules:applications",
    ":modules:configurations",
    ":modules:oss",
    ":modules:help",
    ":modules:routes",
    ":modules:stringconfiguration",
    ":modules:booleanconfiguration",
    ":modules:integerconfiguration",
    ":modules:enumconfiguration",
)

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
