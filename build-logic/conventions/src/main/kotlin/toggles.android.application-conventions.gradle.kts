
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.kotlin.dsl.kotlin

val libs = the<LibrariesForLibs>()

plugins {
    id("com.android.application")
    kotlin("android")
    id("kotlin-android")
    id("toggles.detekt-conventions")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    compileSdk = 35

    defaultConfig {
        minSdk = 21
        targetSdk = 35

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    lint {
        baseline = file("lint-baseline.xml")
        checkReleaseBuilds = true
        abortOnError = true
        warningsAsErrors = true
        lintConfig = File("../lint.xml")
    }

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

    sourceSets {
//        val sharedTestDir = "src/sharedTest/"
//        if(project.file(sharedTestDir).exists()) {
//            val sharedTestSourceDir = sharedTestDir + "java"
//            val sharedTestResourceDir = sharedTestDir + "resources"
//            val sharedIsAndroid =
//                providers.gradleProperty("shared-tests-are-android-tests").get().toBoolean()
//            if (sharedIsAndroid) {
//                logger.lifecycle("Shared tests are androidTests")
//                named("androidTest") {
//                    java.srcDir(sharedTestSourceDir)
//                    resources.srcDir(sharedTestResourceDir)
//                }
//            } else {
//                logger.lifecycle("Shared tests are unitTests")
//                named("test") {
//                    java.srcDir(sharedTestSourceDir)
//                    resources.srcDir(sharedTestResourceDir)
//                }
//            }
//        }
    }
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
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
