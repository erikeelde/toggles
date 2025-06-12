import org.gradle.accessors.dm.LibrariesForLibs

val libs = the<LibrariesForLibs>()

plugins {
    id("com.android.library")
    kotlin("android")
    id("toggles.detekt-conventions")
}

android {
    compileSdk = 36

    defaultConfig {
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    lint {
        baseline = file("lint-baseline.xml")
        checkReleaseBuilds = true
        abortOnError = true
        warningsAsErrors = true
        lintConfig = File("../../lint.xml")
    }

    @Suppress("UnstableApiUsage")
    testOptions {
        animationsDisabled = true
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
        unitTests {
            isReturnDefaultValues = true
            isIncludeAndroidResources = true
        }
        managedDevices {
            devices {
                maybeCreate<com.android.build.api.dsl.ManagedVirtualDevice>("pixel2api30").apply {
                    // Use device profiles you typically see in Android Studio.
                    device = "Pixel 2"
                    // Use only API levels 30 and higher.
                    apiLevel = 30
                    // To include Google services, use "google".
                    systemImageSource = "aosp"
                }
            }
        }
    }

    @Suppress("UnstableApiUsage")
    testFixtures {
        enable = true
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    lintChecks(libs.com.slack.lint.compose.compose.lint.checks)

    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.org.robolectric)
    testImplementation(libs.androidx.test.runner)

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)

    androidTestUtil(libs.androidx.test.orchestrator)
}