plugins {
    id("com.gradle.enterprise") version "3.12.1"
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

include(
    ":toggles-app",
    ":toggles-core",
    ":toggles-flow",
    ":toggles-prefs",
    ":toggles-sample",
    ":toggles-flow-noop",
    ":toggles-prefs-noop",
    ":wrench-core",
)
