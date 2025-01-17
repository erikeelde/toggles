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
    id("com.gradle.develocity") version "3.19"
    id("org.gradle.toolchains.foojay-resolver-convention") version ("0.9.0")
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
include(":toggles-core")
include(":toggles-flow")
include(":toggles-flow-noop")
include(":toggles-prefs")
include(":toggles-prefs-noop")


dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
