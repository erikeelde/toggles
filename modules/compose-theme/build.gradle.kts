plugins {
    id("toggles.android.module-conventions")
    id("toggles.ownership-conventions")
}

val composeCompilerVersion: String by rootProject.extra

android {
    namespace = "se.eelde.toggles.composetheme"
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = composeCompilerVersion
    }
}
dependencies {
    implementation(platform(libs.androidx.compose.compose.bom))

    implementation(libs.androidx.core.core.ktx)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
}