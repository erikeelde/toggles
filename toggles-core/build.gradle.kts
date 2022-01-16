plugins {
    id("com.android.library")
    kotlin("android")
}

apply(plugin = "com.vanniktech.maven.publish")

android {
    compileSdk = 32

    defaultConfig {
        minSdk = 21
        targetSdk = 32

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val togglesProviderAuthority = "se.eelde.toggles.configprovider"
        manifestPlaceholders["togglesProviderAuthority"] = togglesProviderAuthority
        buildConfigField("String", "TOGGLES_AUTHORITY", "\"${togglesProviderAuthority}\"")

        buildConfigField("int", "TOGGLES_API_VERSION", "1")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    lint {
//        baselineFile = file("lint-baseline.xml")
//        isCheckReleaseBuilds = true
//        isAbortOnError = true
//        isWarningsAsErrors = true
//        lintConfig = File("../lint.xml")
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
kotlin {
    // https://kotlinlang.org/docs/whatsnew14.html#explicit-api-mode-for-library-authors
    explicitApi()
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    implementation("androidx.annotation:annotation:1.3.0")
}
