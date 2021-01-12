plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.gms.oss.licenses.plugin")
    id("dagger.hilt.android.plugin")
    id("io.gitlab.arturbosch.detekt")
}

dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.15.0")
}

detekt {
    autoCorrect = true
}

android {
    buildFeatures {
        viewBinding = true
    }
    compileSdk = 30
    defaultConfig {
        applicationId = "se.eelde.toggles.example"
        minSdk = 16
        targetSdk = 30
        versionCode = 2
        versionName = "1.00.01"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
        freeCompilerArgs = listOfNotNull(
            "-Xopt-in=kotlin.RequiresOptIn"
        )
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"))
        }
        getByName("debug") {
            versionNameSuffix = " debug"
            applicationIdSuffix = ".debug"
        }
    }
    lintOptions {
        baselineFile = file("lint-baseline.xml")
        isCheckReleaseBuilds = true
        isAbortOnError = true
        isWarningsAsErrors = true
        lintConfig = File("../lint.xml")
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    testImplementation("androidx.test:core:1.3.0")
    testImplementation("androidx.test.ext:truth:1.3.0")
    testImplementation("androidx.test:rules:1.3.0")
    testImplementation("androidx.test:runner:1.3.0")
    testImplementation("androidx.test.ext:junit:1.1.2")
    testImplementation("org.robolectric:robolectric:4.4")

    implementation("androidx.appcompat:appcompat:1.3.0-alpha02")
    implementation("com.google.android.material:material:1.3.0-beta01")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.1.0")
    implementation("androidx.constraintlayout:constraintlayout:2.0.4")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.2.0")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")

    implementation("com.google.dagger:hilt-android:2.30.1-alpha")
    kapt("com.google.dagger:hilt-android-compiler:2.30.1-alpha")
    implementation("androidx.hilt:hilt-lifecycle-viewmodel:1.0.0-alpha02")
    kapt("androidx.hilt:hilt-compiler:1.0.0-alpha02")
    testImplementation("com.google.dagger:hilt-android-testing:2.30.1-alpha")
    kaptTest("com.google.dagger:hilt-android-compiler:2.30.1-alpha")

    implementation("androidx.navigation:navigation-fragment-ktx:2.3.2")
    implementation("androidx.navigation:navigation-ui-ktx:2.3.2")

    implementation(project(":toggles-core"))
    implementation(project(":toggles-prefs"))
    implementation(project(":toggles-coroutines"))

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.4.21")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.4.2")

    implementation("com.google.dagger:dagger:2.30.1")
    kapt("com.google.dagger:dagger-compiler:2.30.1")

    implementation("com.google.android.gms:play-services-oss-licenses:17.0.0")
    implementation("androidx.core:core-ktx:1.5.0-alpha05")

    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.6")
}
