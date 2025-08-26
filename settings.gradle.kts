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
    id("com.gradle.develocity") version "4.1"
    id("org.gradle.toolchains.foojay-resolver-convention") version ("1.0.0")
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
    }
    remote<HttpBuildCache> {
        isEnabled = false
    }
}

rootProject.name = "Toggles"
includeBuild("build-logic/conventions")
include(
    ":toggles-app",
    ":toggles-sample",
    ":modules:compose-theme",
    ":modules:database:api",
    ":modules:database:implementation",
    ":modules:database:wiring",
    ":modules:provider:api",
    ":modules:provider:implementation",
    ":modules:provider:wiring",
    ":modules:applications",
    ":modules:configurations",
    ":modules:oss",
    ":modules:help",
    ":modules:routes:api",
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
    @Suppress("UnstableApiUsage")
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    @Suppress("UnstableApiUsage")
    repositories {
        google()
        mavenCentral()
    }
}
