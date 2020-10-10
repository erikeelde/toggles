plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("android.extensions")
    id("org.jlleitschuh.gradle.ktlint")
    id("io.gitlab.arturbosch.detekt")
}

dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.14.1")
}

detekt {
    autoCorrect = true
}

android {
    compileSdk = 30

    defaultConfig {
        minSdk = 14
        targetSdk = 30

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val wrenchProviderAuthority = "com.izettle.wrench.configprovider"
        manifestPlaceholders["wrenchProviderAuthority"] = wrenchProviderAuthority
        buildConfigField("String", "WRENCH_AUTHORITY", "\"${wrenchProviderAuthority}\"")

        buildConfigField("int", "WRENCH_API_VERSION", "1")
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
    testImplementation("junit:junit:4.13")
    implementation("androidx.annotation:annotation:1.2.0-alpha01")
}

apply(rootProject.file("gradle/gradle-mvn-push.gradle"))
