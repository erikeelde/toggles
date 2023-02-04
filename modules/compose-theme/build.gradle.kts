plugins {
    id("toggles.android.module-conventions")
    id("toggles.ownership-conventions")
}

android {
    namespace = "se.eelde.toggles.composetheme"
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.3.2"
    }
}
dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.core.core.ktx)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.navigation.runtime.ktx)
    implementation(libs.androidx.navigation.navigation.compose)
}