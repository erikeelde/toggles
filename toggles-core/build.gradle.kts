plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    compileSdk = 30

    defaultConfig {
        minSdk = 21
        targetSdk = 30

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val wrenchProviderAuthority = "com.izettle.wrench.configprovider"
        manifestPlaceholders["wrenchProviderAuthority"] = wrenchProviderAuthority
        buildConfigField("String", "WRENCH_AUTHORITY", "\"${wrenchProviderAuthority}\"")

        buildConfigField("int", "WRENCH_API_VERSION", "1")

        val togglesProviderAuthority = "se.eelde.toggles.configprovider"
        manifestPlaceholders["togglesProviderAuthority"] = togglesProviderAuthority
        buildConfigField("String", "TOGGLES_AUTHORITY", "\"${togglesProviderAuthority}\"")

        buildConfigField("int", "TOGGLES_API_VERSION", "1")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"))
        }

        getByName("debug") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"))
        }
    }
    lintOptions {
        baselineFile = file("lint-baseline.xml")
        isCheckReleaseBuilds = true
        isAbortOnError = true
        isWarningsAsErrors = true
        lintConfig = File("../lint.xml")
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.5.0-beta01")
    testImplementation("junit:junit:4.13.2")
    implementation("androidx.annotation:annotation:1.2.0-beta01")
}

apply(rootProject.file("gradle/gradle-mvn-push.gradle"))
