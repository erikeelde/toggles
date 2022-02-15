import gradle.kotlin.dsl.accessors._f6fccc4cd1db97dcfb2ceed9d5ddc56a.kotlinOptions
import org.gradle.api.JavaVersion
import org.gradle.kotlin.dsl.kotlin

plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
    id("toggles.detekt-conventions")
}

val composeVersion: String by rootProject.extra

dependencies {
    implementation("androidx.compose.runtime:runtime:$composeVersion")
}

android {
    compileSdk = 32

    defaultConfig {
        minSdk = 21
        targetSdk = 32

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
        freeCompilerArgs = listOfNotNull(
            "-Xopt-in=kotlin.RequiresOptIn"
        )
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true

        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    lint {
        baseline = file("lint-baseline.xml")
        checkReleaseBuilds = true
        abortOnError = true
        warningsAsErrors = true
        lintConfig = File("../lint.xml")
    }
}

repositories {
    google()
    mavenCentral()
}