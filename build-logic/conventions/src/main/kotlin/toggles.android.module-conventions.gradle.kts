import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.kotlin.dsl.kotlin

val libs = the<LibrariesForLibs>()

plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
    id("toggles.detekt-conventions")
}

android {
    compileSdk = 34

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
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(JavaVersion.VERSION_17.toString()))
        vendor.set(JvmVendorSpec.AZUL)
    }
}

dependencies {
    lintChecks(libs.com.slack.lint.compose.compose.lint.checks)
}