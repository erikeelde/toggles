plugins {
    id("toggles.android.module-conventions")
    id("toggles.ownership-conventions")
}

val composeVersion: String by rootProject.extra

android {
    namespace = "se.eelde.toggles.navigation"
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = composeVersion
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.appcompat:appcompat:1.4.1")
    implementation("com.google.android.material:material:1.5.0")
    implementation("androidx.hilt:hilt-navigation-compose:1.0.0")
    implementation("androidx.compose.runtime:runtime:$composeVersion")
}