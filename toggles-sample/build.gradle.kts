plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.android.gms.oss-licenses-plugin")
    id("dagger.hilt.android.plugin")
}

android {
    buildFeatures {
        viewBinding = true
    }
    compileSdk = 30
    defaultConfig {
        applicationId = "se.eelde.toggles.example"
        minSdk = 21
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
    lint {
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

repositories {
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
}

dependencies {

    val composeVersion: String by rootProject.extra
    val roomVersion: String by rootProject.extra
    val lifecycleVersion: String by rootProject.extra

    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    testImplementation("androidx.test:core:1.4.0")
    testImplementation("androidx.test.ext:truth:1.4.0")
    testImplementation("androidx.test:rules:1.3.0")
    testImplementation("androidx.test:runner:1.3.0")
    testImplementation("androidx.test.ext:junit:1.1.2")
    testImplementation("org.robolectric:robolectric:4.5.1")

    implementation("androidx.appcompat:appcompat:1.3.0-rc01")
    implementation("com.google.android.material:material:1.3.0")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.1.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")

    implementation("com.google.dagger:hilt-android:2.35")
    kapt("com.google.dagger:hilt-android-compiler:2.35")
    implementation("androidx.hilt:hilt-lifecycle-viewmodel:1.0.0-alpha03")
    kapt("androidx.hilt:hilt-compiler:1.0.0")
    testImplementation("com.google.dagger:hilt-android-testing:2.35")
    kaptTest("com.google.dagger:hilt-android-compiler:2.35")
    implementation("androidx.lifecycle:lifecycle-common-java8:$lifecycleVersion")

    implementation("androidx.navigation:navigation-fragment-ktx:2.3.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.3.5")

//    implementation(project(":toggles-prefs"))
//    implementation(project(":toggles-flow"))
    implementation("se.eelde.toggles:toggles-flow:0.0.1")
    implementation("se.eelde.toggles:toggles-prefs:0.0.1")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.4.3")

    implementation("com.google.dagger:dagger:2.35")
    kapt("com.google.dagger:dagger-compiler:2.38.1")

    implementation("com.google.android.gms:play-services-oss-licenses:17.0.0")
    implementation("androidx.core:core-ktx:1.3.2")

    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.7")
}
