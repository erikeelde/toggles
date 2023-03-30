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
    // try remove or ping developer later
    allowUrl("https://raw.githubusercontent.com/erikeelde/toggles/master/LICENCE")
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
    namespace = "com.example.wrench"
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.legacy.legacy.support.v4)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.ext.truth)
    testImplementation(libs.androidx.test.rules)
    testImplementation(libs.androidx.test.runner)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.org.robolectric)

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.coordinatorlayout)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.lifecycle.extensions)

    implementation(libs.com.google.dagger.hilt.android)
    kapt(libs.com.google.dagger.hilt.android.compiler)
    kapt(libs.androidx.hilt.hilt.compiler)
    testImplementation(libs.com.google.dagger.hilt.android.testing)
    kaptTest(libs.com.google.dagger.hilt.android.compiler)
    implementation(libs.androidx.lifecycle.lifecycle.common.java8)

    implementation(libs.androidx.navigation.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.navigation.ui.ktx)

//    implementation(project(":toggles-prefs"))
//    implementation(project(":toggles-flow"))

//    implementation("se.eelde.toggles:toggles-flow-noop:0.0.1")
//    implementation("se.eelde.toggles:toggles-prefs-noop:0.0.1")
    implementation(libs.se.eelde.toggles.toggles.flow)
    implementation(libs.se.eelde.toggles.toggles.prefs)

    implementation(libs.org.jetbrains.kotlinx.kotlinx.coroutines.android)

    implementation(libs.com.google.dagger)
    kapt(libs.com.google.dagger.dagger.compiler)

    implementation(libs.com.google.android.gms.play.services.oss.licenses)
    implementation(libs.androidx.core.core.ktx)

    debugImplementation(libs.com.squareup.leakcanary.leakcanary.android)
}
