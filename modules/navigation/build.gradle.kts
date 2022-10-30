plugins {
    id("toggles.android.module-conventions")
    id("toggles.ownership-conventions")
}

val composeCompilerVersion: String by rootProject.extra

android {
    namespace = "se.eelde.toggles.navigation"
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = composeCompilerVersion
    }
}

dependencies {
    implementation(libs.androidx.core.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.com.google.android.material)
    implementation(libs.androidx.hilt.hilt.navigation.compose)
    implementation(libs.androidx.compose.runtime)
}