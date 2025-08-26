plugins {
    id("com.android.library")
    kotlin("android")
    id("io.gitlab.arturbosch.detekt")
    id("toggles.detekt-library-conventions")
}

android {
    compileSdk = 36

    defaultConfig {
        minSdk = 23

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    lint {
        baseline = file("lint-baseline.xml")
        checkReleaseBuilds = true
        abortOnError = true
        warningsAsErrors = true
        lintConfig = File("../lint.xml")
    }

    publishing {
        multipleVariants {
            allVariants()
            withSourcesJar()
            withJavadocJar()
        }
    }

    @Suppress("UnstableApiUsage")
    testFixtures {
        enable = true
    }
}

kotlin {
    jvmToolchain(21)
    explicitApi()
}
