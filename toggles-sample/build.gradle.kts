import java.io.FileInputStream
import java.util.Properties

plugins {
    id("toggles.android.application-conventions")
    id("dagger.hilt.android.plugin")
    id("toggles.ownership-conventions")
    id("app.cash.licensee")
    alias(libs.plugins.se.premex.gross)
    alias(libs.plugins.com.google.devtools.ksp)
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
            proguardFiles(getDefaultProguardFile("proguard-android.txt"))
        }
        getByName("debug") {
            versionNameSuffix = " debug"
            applicationIdSuffix = ".debug"
        }
    }
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(JavaVersion.VERSION_17.toString()))
    }
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

    implementation(libs.com.google.dagger.hilt.android)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3.android)
    ksp(libs.com.google.dagger.hilt.android.compiler)
    kspTest(libs.com.google.dagger.hilt.android.compiler)

    implementation(projects.togglesCore)
    implementation(projects.togglesFlow)

    implementation(platform(libs.org.jetbrains.kotlinx.kotlinx.coroutines.bom))
    implementation(libs.org.jetbrains.kotlinx.kotlinx.coroutines.android)

    debugImplementation(libs.com.squareup.leakcanary.leakcanary.android)

    testFixturesImplementation(libs.org.jetbrains.kotlin.kotlin.stdlib)
}
