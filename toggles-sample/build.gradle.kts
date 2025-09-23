import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.toggles.android.application)
    alias(libs.plugins.toggles.android.compose.application)
    alias(libs.plugins.toggles.hilt)
    id("app.cash.licensee")
    alias(libs.plugins.se.premex.gross)
    alias(libs.plugins.org.jetbrains.kotlin.plugin.serialization)
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

android {
    namespace = "se.eelde.toggles.example"

    defaultConfig {
        versionName = versions.getProperty("V_VERSION", "0.0.1")
        versionCode = versions.getProperty("V_VERSION_CODE", "1").toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
        getByName("debug") {
            versionNameSuffix = " debug"
            applicationIdSuffix = ".debug"
        }
    }
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation.foundation.layout)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.material.icons.extended)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.ui.ui.tooling)
    implementation(libs.androidx.core.core.splashscreen)
    implementation(libs.androidx.lifecycle.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.hilt.hilt.navigation.compose)
    implementation(libs.co.touchlab.kermit)
    implementation(libs.org.jetbrains.kotlinx.kotlinx.serialization.json)
    implementation(libs.org.jetbrains.kotlinx.kotlinx.collections.immutable)
    implementation(projects.modules.composeTheme)
    implementation(projects.modules.oss)

    implementation(libs.androidx.lifecycle.viewmodel.navigation3)

    implementation(projects.togglesCore)
    implementation(projects.togglesFlow)

    implementation(platform(libs.org.jetbrains.kotlinx.kotlinx.coroutines.bom))
    implementation(libs.org.jetbrains.kotlinx.kotlinx.coroutines.android)

    debugImplementation(libs.com.squareup.leakcanary.leakcanary.android)

    androidTestImplementation(libs.androidx.test.core.ktx)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
}
