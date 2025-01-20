import java.io.FileInputStream
import java.util.Properties

plugins {
    id("toggles.android.application-conventions")
    id("com.google.gms.google-services")
    id("kotlin-parcelize")
    id("dagger.hilt.android.plugin")
    id("com.github.triplet.play")
    id("app.cash.licensee")
    alias(libs.plugins.se.premex.gross)
    alias(libs.plugins.com.google.devtools.ksp)
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
        versionName = versions.getProperty("V_VERSION", "0.0.1")
        versionCode = versions.getProperty("V_VERSION_CODE", "1").toInt()

        vectorDrawables.useSupportLibrary = true

        val togglesAuthority = "se.eelde.toggles.configprovider"
        val togglesPermission = "se.eelde.toggles.provider_permission"

        manifestPlaceholders["togglesAuthority"] = togglesAuthority
        manifestPlaceholders["togglesPermission"] = togglesPermission

        buildConfigField("String", "CONFIG_AUTHORITY", "\"$togglesAuthority\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    testOptions {
        animationsDisabled = true
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
        unitTests {
            isReturnDefaultValues = true
            isIncludeAndroidResources = true
        }
        managedDevices {
            devices {
                maybeCreate<com.android.build.api.dsl.ManagedVirtualDevice>("pixel2api30").apply {
                    // Use device profiles you typically see in Android Studio.
                    device = "Pixel 2"
                    // Use only API levels 30 and higher.
                    apiLevel = 30
                    // To include Google services, use "google".
                    systemImageSource = "aosp"
                }
            }
        }
    }
}

dependencies {
    implementation(projects.modules.composeTheme)
    implementation(projects.modules.database)
    implementation(projects.modules.provider)
    implementation(projects.modules.applications)
    implementation(projects.modules.configurations)
    implementation(projects.modules.oss)
    implementation(projects.modules.help)
    implementation(projects.modules.booleanconfiguration)
    implementation(projects.modules.integerconfiguration)
    implementation(projects.modules.stringconfiguration)
    implementation(projects.modules.enumconfiguration)

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
    implementation(libs.androidx.hilt.hilt.navigation.compose)
    implementation(libs.androidx.navigation.navigation.compose)
    implementation(libs.androidx.startup.startup.runtime)
    implementation(projects.modules.routes)
    implementation(libs.com.google.dagger.hilt.android)
    ksp(libs.com.google.dagger.hilt.android.compiler)
    ksp(libs.androidx.hilt.hilt.compiler)

    implementation(libs.androidx.lifecycle.lifecycle.common.java8)

    implementation(libs.com.google.dagger)
    ksp(libs.com.google.dagger.dagger.compiler)

    implementation(libs.androidx.lifecycle.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.lifecycle.runtime.ktx)
    implementation(libs.androidx.room.room.ktx)

    implementation(projects.togglesCore)
    implementation(projects.togglesFlow)

    implementation(platform(libs.org.jetbrains.kotlinx.kotlinx.coroutines.bom))
    implementation(libs.org.jetbrains.kotlinx.kotlinx.coroutines.android)

    debugImplementation(libs.com.squareup.leakcanary.leakcanary.android)

    testImplementation(libs.junit)


    testImplementation(libs.androidx.test.core.ktx)
    testImplementation(libs.androidx.test.ext.truth)
    testImplementation(libs.androidx.test.rules)
    testImplementation(libs.androidx.test.runner)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.org.robolectric)
    testImplementation(libs.androidx.test.espresso.espresso.core)
    testImplementation(libs.androidx.arch.core.core.testing)
    testImplementation(libs.com.google.dagger.hilt.android.testing)
    testImplementation(libs.org.jetbrains.kotlinx.kotlinx.coroutines.test)
    testImplementation(libs.app.cash.turbine)
    kspTest(libs.com.google.dagger.hilt.android.compiler)

    androidTestImplementation(libs.androidx.test.core.ktx)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.ui.test.junit4)

    testFixturesImplementation(libs.org.jetbrains.kotlin.kotlin.stdlib)
}
