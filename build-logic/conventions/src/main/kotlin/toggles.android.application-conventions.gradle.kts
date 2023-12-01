
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.kotlin.dsl.kotlin

val libs = the<LibrariesForLibs>()

plugins {
    id("com.android.application")
    kotlin("android")
    id("kotlin-android")
    id("toggles.detekt-conventions")
}

android {
    compileSdk = 34

    defaultConfig {
        minSdk = 21
        targetSdk = 34

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.androidx.compose.compiler.get()
    }

    @Suppress("UnstableApiUsage")
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }

    lint {
        baseline = file("lint-baseline.xml")
        checkReleaseBuilds = true
        abortOnError = true
        warningsAsErrors = true
        lintConfig = File("../lint.xml")
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
        vendor.set(JvmVendorSpec.AZUL)
    }
}

dependencies {
    lintChecks(libs.com.slack.lint.compose.compose.lint.checks)
}
