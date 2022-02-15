plugins {
    id("toggles.android.module-conventions")
    id("toggles.ownership-conventions")
}

val composeVersion: String by rootProject.extra

android {
    namespace = "se.eelde.toggles.applications"
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = composeVersion
    }
}

dependencies {
    implementation(project(":modules:navigation"))
    implementation(project(":modules:database"))
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.appcompat:appcompat:1.4.1")
    implementation("com.google.android.material:material:1.5.0")
    implementation("androidx.hilt:hilt-navigation-compose:1.0.0")
    implementation("androidx.compose.runtime:runtime:$composeVersion")
    implementation("androidx.compose.material:material:$composeVersion")
    implementation("androidx.compose.material3:material3:1.0.0-alpha05")
    implementation("androidx.startup:startup-runtime:1.1.1")
    implementation("com.google.dagger:hilt-android:2.38.1")
    kapt("com.google.dagger:hilt-compiler:2.38.1")
}