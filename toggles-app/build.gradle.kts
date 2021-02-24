import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    kotlin("android")
    kotlin("kapt")
    id("kotlin-parcelize")
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.gms.oss.licenses.plugin")
    id("dagger.hilt.android.plugin")
    id("com.github.triplet.play") version "3.3.0-agp4.2"
    id("kotlin-android")
}

kapt {
    javacOptions {
        // Increase the max count of errors from annotation processors.
        // Default is 100.
        option("-Xmaxerrs", 500)
    }
}

play {
    serviceAccountCredentials.set(file("../service_account.json"))
    defaultToAppBundles.set(true)
}

val keystorePropertiesFile: File = rootProject.file("keystore.properties")
// Initialize a new Properties() object called keystoreProperties.
val keystoreProperties = Properties()
// Load your keystore.properties file into the keystoreProperties object.
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

android {
    signingConfigs {
        create("release") {
            keyAlias = keystoreProperties["keyAlias"] as String?
            keyPassword = keystoreProperties["keyPassword"] as String?
            storeFile = file("../toggles_keystore.jks")
            storePassword = keystoreProperties["storePassword"] as String?
        }
    }

    buildFeatures {
        viewBinding = true
//        compose = true
    }

//    composeOptions {
//        kotlinCompilerExtensionVersion = "1.0.0-alpha12"
//    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }

    compileSdk = 30

    defaultConfig {
        applicationId = "se.eelde.toggles"
        minSdk = 16
        targetSdk = 30
        versionCode = 5
        versionName = "1.01.01"

        multiDexEnabled = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true

        val wrenchAuthority = "com.izettle.wrench.configprovider"
        val wrenchPermission = "com.izettle.wrench.permission"
        val togglesAuthority = "se.eelde.toggles.configprovider"
        val togglesPermission = "se.eelde.toggles.provider_permission"

        manifestPlaceholders["wrenchAuthority"] = wrenchAuthority
        manifestPlaceholders["wrenchPermission"] = wrenchPermission
        manifestPlaceholders["togglesAuthority"] = togglesAuthority
        manifestPlaceholders["togglesPermission"] = togglesPermission

        buildConfigField("String", "CONFIG_AUTHORITY", "\"$togglesAuthority\"")
    }
    packagingOptions {
        exclude("META-INF/main.kotlin_module")
        exclude("META-INF/atomicfu.kotlin_module")
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
        freeCompilerArgs = listOfNotNull(
            "-Xopt-in=kotlin.RequiresOptIn"
        )
        useIR = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"))
            signingConfig = signingConfigs["release"]
        }
        getByName("debug") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"))
            versionNameSuffix = " debug"
//            applicationIdSuffix = ".debug"
        }
    }
    lintOptions {
        baselineFile = file("lint-baseline.xml")
        isCheckReleaseBuilds = true
        isAbortOnError = true
        isWarningsAsErrors = true
        lintConfig = File("../lint.xml")
    }
    sourceSets {
        // debug.assets.srcDirs => https://github.com/robolectric/robolectric/issues/3928
        // debug.assets.srcDirs += files("$projectDir/schemas".toString())
        getByName("debug") {
            assets.srcDirs(files("$projectDir/schemas"))
        }
    }
}

kapt {
    arguments {
        arg("room.schemaLocation", "$projectDir/schemas")
    }
}

dependencies {
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    testImplementation("junit:junit:4.13.2")

    testImplementation("androidx.test:core-ktx:1.3.0")
    testImplementation("androidx.test.ext:truth:1.3.0")
    testImplementation("androidx.test:rules:1.3.0")
    testImplementation("androidx.test:runner:1.3.0")
    testImplementation("androidx.test.ext:junit:1.1.2")
    testImplementation("androidx.room:room-testing:2.3.0-beta02")
    testImplementation("org.robolectric:robolectric:4.5.1")
    testImplementation("androidx.test.espresso:espresso-core:3.3.0")
    testImplementation("androidx.arch.core:core-testing:2.1.0")
    testImplementation("androidx.work:work-testing:2.5.0")

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.3.0")
    implementation(platform("com.google.firebase:firebase-bom:26.5.0"))

    implementation("com.google.dagger:hilt-android:2.32-alpha")
    kapt("com.google.dagger:hilt-android-compiler:2.32-alpha")
    implementation("androidx.hilt:hilt-lifecycle-viewmodel:1.0.0-alpha03")
    kapt("androidx.hilt:hilt-compiler:1.0.0-alpha03")
    implementation("androidx.hilt:hilt-work:1.0.0-alpha03")

    testImplementation("com.google.dagger:hilt-android-testing:2.32-alpha")
    kaptTest("com.google.dagger:hilt-android-compiler:2.32-alpha")

    implementation("androidx.lifecycle:lifecycle-common-java8:2.3.0")
    kapt("androidx.room:room-compiler:2.3.0-beta02")

    implementation("com.google.dagger:dagger:2.32")
    kapt("com.google.dagger:dagger-compiler:2.32")

    implementation("androidx.appcompat:appcompat:1.3.0-beta01")
    implementation("androidx.recyclerview:recyclerview:1.2.0-beta01")
    implementation("com.google.android.material:material:1.3.0")
    implementation("androidx.constraintlayout:constraintlayout:2.0.4")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.3.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.3.0")
    implementation("androidx.lifecycle:lifecycle-livedata-core-ktx:2.3.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.3.0")
    implementation("androidx.room:room-runtime:2.3.0-beta02")
    implementation("androidx.room:room-ktx:2.3.0-beta02")
    implementation("androidx.paging:paging-runtime-ktx:3.0.0-beta01")

    implementation("androidx.navigation:navigation-fragment-ktx:2.3.3")
    implementation("androidx.navigation:navigation-ui-ktx:2.3.3")

    implementation("com.izettle.wrench:wrench-core:0.3")
    implementation(project(":toggles-core"))
    implementation(project(":toggles-prefs"))
    implementation(project(":toggles-coroutines"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.4.2")

    implementation("androidx.core:core-ktx:1.5.0-beta01")
    implementation("androidx.work:work-runtime-ktx:2.5.0")

    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.6")
    implementation("androidx.multidex:multidex:2.0.1")
}
