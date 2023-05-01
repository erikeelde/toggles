import java.io.FileInputStream
import java.util.Properties
//import se.eelde.toggles.licenseeassetplugin.CopyLicenseeReportPlugin

plugins {
    id("toggles.android.application-conventions")
    id("com.google.gms.google-services")
    id("kotlin-parcelize")
    id("dagger.hilt.android.plugin")
    id("com.github.triplet.play")
    id("com.gladed.androidgitversion") version "0.4.14"
    id("com.google.firebase.crashlytics")
    id("app.cash.licensee")
    id("se.eelde.toggles.licenseeassetplugin")
}

licensee {
    allow("Apache-2.0")

    allowUrl("https://developer.android.com/studio/terms.html")
    //allowUrl("https://cloud.google.com/maps-platform/terms/")

    // try remove or ping developer later
    // allowUrl("http://www.opensource.org/licenses/mit-license.php")
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
    signingConfigs {
        create("release") {
            keyAlias = keystoreProperties["keyAlias"] as String?
            keyPassword = keystoreProperties["keyPassword"] as String?
            storeFile = file("../toggles_keystore.jks")
            storePassword = keystoreProperties["storePassword"] as String?
        }
    }

    buildFeatures {
        buildConfig = true
    }

    namespace = "se.eelde.toggles"

    defaultConfig {
        applicationId = "se.eelde.toggles"
        versionName = androidGitVersion.name()
        versionCode = androidGitVersion.code()

        vectorDrawables.useSupportLibrary = true

        val togglesAuthority = "se.eelde.toggles.configprovider"
        val togglesPermission = "se.eelde.toggles.provider_permission"

        manifestPlaceholders["togglesAuthority"] = togglesAuthority
        manifestPlaceholders["togglesPermission"] = togglesPermission

        buildConfigField("String", "CONFIG_AUTHORITY", "\"$togglesAuthority\"")
    }
    packaging {
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
}

dependencies {
    implementation(projects.modules.composeTheme)
    implementation(projects.modules.database)
    implementation(projects.modules.applications)
    implementation(projects.modules.oss)

    implementation(libs.androidx.ui.ui.tooling)
    implementation(platform(libs.androidx.compose.bom))
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
    implementation(libs.androidx.lifecycle.lifecycle.runtime.compose)
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

    implementation(libs.androidx.lifecycle.lifecycle.runtime.ktx)
    implementation(platform(libs.com.google.firebase.firebase.bom))
    implementation(libs.com.google.firebase.firebase.crashlytics.ktx)
    implementation(libs.com.google.firebase.firebase.analytics.ktx)

    implementation(libs.com.google.dagger.hilt.android)
    kapt(libs.com.google.dagger.hilt.android.compiler)
    kapt(libs.androidx.hilt.hilt.compiler)

    testImplementation(libs.com.google.dagger.hilt.android.testing)
    kaptTest(libs.com.google.dagger.hilt.android.compiler)

    implementation(libs.androidx.lifecycle.lifecycle.common.java8)

    implementation(libs.com.google.dagger)
    kapt(libs.com.google.dagger.dagger.compiler)

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.recyclerview)

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

    implementation(libs.se.eelde.toggles.toggles.core)
    implementation(libs.se.eelde.toggles.toggles.flow)
    implementation(libs.se.eelde.toggles.toggles.prefs)

    implementation(libs.org.jetbrains.kotlinx.kotlinx.coroutines.android)

    implementation(libs.androidx.core.core.ktx)
    // https://mvnrepository.com/artifact/com.squareup.okio/okio
    implementation(libs.com.squareup.okio)

    debugImplementation(libs.com.squareup.leakcanary.leakcanary.android)
}
