import java.io.FileInputStream
import java.util.Properties

plugins {
    id("toggles.android.application-conventions")
    id("com.google.gms.google-services")
    id("kotlin-parcelize")
    id("com.google.android.gms.oss-licenses-plugin")
    id("com.google.dagger.hilt.android")
    id("com.github.triplet.play")
    id("com.gladed.androidgitversion") version "0.4.14"
    id("com.google.firebase.crashlytics")
}

androidGitVersion {
    tagPattern = "^v[0-9]+.*"
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
    namespace = "se.eelde.toggles"

    signingConfigs {
        create("release") {
            keyAlias = keystoreProperties["keyAlias"] as String?
            keyPassword = keystoreProperties["keyPassword"] as String?
            storeFile = file("../toggles_keystore.jks")
            storePassword = keystoreProperties["storePassword"] as String?
        }
    }


    defaultConfig {
        applicationId = "se.eelde.toggles"
        versionName = androidGitVersion.name()
        versionCode = androidGitVersion.code()

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
        resources {
            excludes += setOf("META-INF/main.kotlin_module", "META-INF/atomicfu.kotlin_module")
        }
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
        }
    }
    sourceSets {
        // debug.assets.srcDirs => https://github.com/robolectric/robolectric/issues/3928
        // debug.assets.srcDirs += files("$projectDir/schemas".toString())
        getByName("debug") {
            assets.srcDirs(files("$projectDir/schemas"))
        }
    }
}

dependencies {
    implementation(project(":modules:compose-theme"))
    implementation(project(":modules:database"))
    implementation(project(":modules:applications"))

    implementation(libs.androidx.ui.ui.tooling)
    implementation(platform(libs.androidx.compose.compose.bom))
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation.foundation.layout)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.material.icons.extended)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.ui.ui.tooling)
    implementation(libs.androidx.compose.runtime.runtime.livedata)
    implementation(libs.androidx.lifecycle.lifecycle.viewmodel.compose)
    implementation(libs.androidx.hilt.hilt.navigation.compose)
    implementation(libs.androidx.navigation.navigation.compose)
    implementation(libs.androidx.startup.startup.runtime)
    testImplementation(libs.junit)

    testImplementation(libs.androidx.test.core.ktx)
    testImplementation(libs.androidx.test.ext.truth)
    testImplementation(libs.androidx.test.rules)
    testImplementation(libs.androidx.test.runner)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.org.robolectric)
    testImplementation(libs.androidx.test.espresso.espresso.core)
    testImplementation(libs.androidx.arch.core.core.testing)
    testImplementation(libs.androidx.work.work.testing)

    implementation(libs.androidx.lifecycle.lifecycle.runtime.ktx)
    implementation(platform(libs.com.google.firebase.firebase.bom))
    implementation(libs.com.google.firebase.firebase.crashlytics.ktx)
    implementation(libs.com.google.firebase.firebase.analytics.ktx)

    implementation(libs.com.google.dagger.hilt.android)
    kapt(libs.com.google.dagger.hilt.android.compiler)
    kapt(libs.androidx.hilt.hilt.compiler)
    implementation(libs.androidx.hilt.hilt.work)

    testImplementation(libs.com.google.dagger.hilt.android.testing)
    kaptTest(libs.com.google.dagger.hilt.android.compiler)

    implementation(libs.androidx.lifecycle.lifecycle.common.java8)


    // implementation(libs.com.google.dagger)
    // kapt(libs.com.google.dagger.dagger.compiler)

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.recyclerview)
    // implementation("com.google.android.material:material:1.5.0)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.lifecycle.lifecycle.extensions)
    implementation(libs.androidx.lifecycle.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.lifecycle.livedata.core.ktx)
    implementation(libs.androidx.lifecycle.lifecycle.livedata.ktx)
    implementation(libs.androidx.room.room.ktx)
    implementation(libs.androidx.paging.paging.runtime.ktx)

    implementation(libs.androidx.paging.paging.runtime.ktx)

    implementation(project(":modules:wrench-core"))
//    implementation(project(":toggles-core"))
//    implementation(project(":toggles-prefs"))
//    implementation(project(":toggles-flow"))
    implementation(libs.se.eelde.toggles.toggles.core)
    implementation("se.eelde.toggles:toggles-flow:0.0.1")
    implementation("se.eelde.toggles:toggles-prefs:0.0.1")

    implementation(libs.org.jetbrains.kotlinx.kotlinx.coroutines.android)

    implementation(libs.androidx.core.core.ktx)
    implementation(libs.androidx.work.work.runtime)

    debugImplementation(libs.com.squareup.leakcanary.leakcanary.android)
}
