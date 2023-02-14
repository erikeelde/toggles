import gradle.kotlin.dsl.accessors._dfb77a4bc97bf67c314166dbbff82491.lintChecks
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.JavaVersion
import org.gradle.kotlin.dsl.kotlin

val libs = the<LibrariesForLibs>()

plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
    id("toggles.detekt-conventions")
}

android {
    compileSdk = 33

    defaultConfig {
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
        freeCompilerArgs = listOfNotNull(
            "-opt-in=kotlin.RequiresOptIn"
        )
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    lint {
        baseline = file("lint-baseline.xml")
        checkReleaseBuilds = true
        abortOnError = true
        warningsAsErrors = true
        lintConfig = File("../../lint.xml")
    }
}

dependencies {
    lintChecks(libs.com.slack.lint.compose.compose.lint.checks)
}