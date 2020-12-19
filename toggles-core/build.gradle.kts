plugins {
    id("com.android.library")
    kotlin("android")
    id("org.jlleitschuh.gradle.ktlint")
    id("io.gitlab.arturbosch.detekt")
}

dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.14.2")
}

detekt {
    autoCorrect = true
}

android {
    compileSdk = 30

    defaultConfig {
        minSdk = 16
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
    implementation("androidx.core:core-ktx:1.3.2")
    testImplementation("junit:junit:4.13.1")
    implementation("androidx.annotation:annotation:1.2.0-alpha01")
}

apply(rootProject.file("gradle/gradle-mvn-push.gradle"))
