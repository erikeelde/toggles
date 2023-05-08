plugins {
    id("toggles.android.application-conventions")
    id("dagger.hilt.android.plugin")
    id("com.gladed.androidgitversion") version "0.4.14"
    id("toggles.ownership-conventions")
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

android {
    buildFeatures {
        viewBinding = true
    }
    defaultConfig {
        applicationId = "se.eelde.toggles.example"
        versionName = androidGitVersion.name()
        versionCode = androidGitVersion.code()

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
    namespace = "com.example.toggles"
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
    implementation(libs.androidx.compose.runtime.runtime.livedata)
    implementation(libs.androidx.lifecycle.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.lifecycle.runtime.compose)
    implementation(libs.androidx.hilt.hilt.navigation.compose)
    implementation(libs.androidx.navigation.navigation.compose)

    implementation(projects.modules.composeTheme)
    implementation(projects.modules.oss)
    implementation(libs.com.squareup.okio)

    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.ext.truth)
    testImplementation(libs.androidx.test.rules)
    testImplementation(libs.androidx.test.runner)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.org.robolectric)

    implementation(libs.com.google.dagger.hilt.android)
    kapt(libs.com.google.dagger.hilt.android.compiler)
    testImplementation(libs.com.google.dagger.hilt.android.testing)
    kaptTest(libs.com.google.dagger.hilt.android.compiler)

    implementation(libs.se.eelde.toggles.toggles.flow)
    implementation(libs.se.eelde.toggles.toggles.prefs)

    implementation(libs.org.jetbrains.kotlinx.kotlinx.coroutines.android)

    implementation(libs.androidx.core.core.ktx)

    debugImplementation(libs.com.squareup.leakcanary.leakcanary.android)
}
