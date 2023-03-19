enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

plugins {
    id("com.gradle.enterprise") version "3.10.2"
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

rootProject.name = "Toggles"
includeBuild("build-logic")
includeBuild("toggles-core")
includeBuild("toggles-flow")
includeBuild("toggles-flow-noop")
includeBuild("toggles-prefs")
includeBuild("toggles-prefs-noop")
include(
    ":toggles-app",
    ":toggles-sample",
    ":modules:compose-theme",
    ":modules:database",
    ":modules:applications",
    ":modules:wrench-core",
    ":modules:oss",
)

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
