enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

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

rootProject.name = "toggles-core"

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

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
    @Suppress("UnstableApiUsage")
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    @Suppress("UnstableApiUsage")
    repositories {
        google()
        mavenCentral()
        maven {
            name = "SonatypeReleases"
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
        }
        maven {
            name = "SonatypeSnapshots"
            url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        }
    }
}

includeBuild("../build-logic/conventions")

