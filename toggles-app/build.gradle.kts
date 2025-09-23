import com.github.triplet.gradle.androidpublisher.ReleaseStatus
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.toggles.android.application)
    alias(libs.plugins.toggles.android.compose.application)
    id("com.google.gms.google-services")
    id("kotlin-parcelize")
    alias(libs.plugins.toggles.hilt)
    id("com.github.triplet.play")
    id("app.cash.licensee")
    alias(libs.plugins.se.premex.gross)
}

val versionFile = File("versions.properties")
val versions = Properties().apply {
    if (versionFile.exists()) {
        FileInputStream(versionFile).use {
            load(it)
        }
    }
}

licensee {
    allow("Apache-2.0")
}

play {
    serviceAccountCredentials.set(file("service_account.json"))
    defaultToAppBundles.set(true)
    releaseStatus.set(ReleaseStatus.DRAFT)
//    resolutionStrategy.set(com.github.triplet.gradle.androidpublisher.ResolutionStrategy.AUTO)
}

val keystorePropertiesFile: File = project.file("keystore.properties")
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
            storeFile = file("toggles_keystore.jks")
            storePassword = keystoreProperties["storePassword"] as String?
        }
    }

    buildFeatures {
        buildConfig = true
    }

    namespace = "se.eelde.toggles"

    defaultConfig {
        applicationId = "se.eelde.toggles"
        versionName = versions.getProperty("V_VERSION", "0.0.1")
        versionCode = versions.getProperty("V_VERSION_CODE", "1").toInt()

        vectorDrawables.useSupportLibrary = true

        val togglesAuthority = "se.eelde.toggles.configprovider"
        val togglesPermission = "se.eelde.toggles.provider_permission"

        manifestPlaceholders["togglesAuthority"] = togglesAuthority
        manifestPlaceholders["togglesPermission"] = togglesPermission

        buildConfigField("String", "CONFIG_AUTHORITY", "\"$togglesAuthority\"")

        testInstrumentationRunner = "se.eelde.toggles.CustomTestRunner"
    }
    packaging {
        resources {
            excludes += setOf("META-INF/main.kotlin_module", "META-INF/atomicfu.kotlin_module")
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
            signingConfig = signingConfigs["release"]
        }
        getByName("debug") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
            versionNameSuffix = " debug"
        }
    }
}

dependencies {
    implementation(projects.modules.composeTheme)
    implementation(projects.modules.database.implementation)
    implementation(projects.modules.provider.implementation)
    implementation(projects.modules.applications)
    implementation(projects.modules.configurations)
    implementation(projects.modules.oss)
    implementation(projects.modules.help)
    implementation(projects.modules.booleanconfiguration)
    implementation(projects.modules.integerconfiguration)
    implementation(projects.modules.stringconfiguration)
    implementation(projects.modules.enumconfiguration)
    implementation(projects.modules.coroutines.wiring)
    implementation(projects.modules.coroutines.api)
    implementation(projects.modules.provider.wiring)
    implementation(projects.modules.database.wiring)

    implementation(libs.androidx.compose.ui.ui.tooling)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.activity.activity.compose)
    implementation(libs.androidx.core.core.splashscreen)
    implementation(libs.androidx.compose.foundation.foundation.layout)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.material.icons.extended)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.ui.ui.tooling)
    implementation(libs.androidx.lifecycle.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.hilt.hilt.navigation.compose)
    implementation(libs.androidx.startup.startup.runtime)
    implementation(projects.modules.routes.api)
    ksp(libs.com.google.dagger.hilt.android.compiler)

    implementation(libs.androidx.lifecycle.lifecycle.common.java8)

    implementation(libs.com.google.dagger)

    implementation(libs.androidx.lifecycle.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.lifecycle.runtime.ktx)
    implementation(libs.androidx.room.room.ktx)

    implementation(projects.togglesCore)
    implementation(projects.togglesFlow)

    implementation(platform(libs.org.jetbrains.kotlinx.kotlinx.coroutines.bom))
    implementation(libs.org.jetbrains.kotlinx.kotlinx.coroutines.android)

    debugImplementation(libs.com.squareup.leakcanary.leakcanary.android)

    androidTestImplementation(libs.androidx.test.core.ktx)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.ui.test.junit4)
    androidTestImplementation(libs.androidx.arch.core.core.testing)
    androidTestImplementation(libs.com.google.dagger.hilt.android.testing)
    androidTestImplementation(libs.app.cash.turbine)
    kspAndroidTest(libs.com.google.dagger.hilt.android.compiler)

    testFixturesImplementation(libs.org.jetbrains.kotlin.kotlin.stdlib)
}
