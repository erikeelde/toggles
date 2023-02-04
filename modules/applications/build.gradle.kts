plugins {
    id("toggles.android.module-conventions")
    id("toggles.ownership-conventions")
}

android {
    namespace = "se.eelde.toggles.applications"
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.3.2"
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(projects.modules.navigation)
    implementation(projects.modules.composeTheme)
    implementation(projects.modules.database)
    implementation(libs.androidx.core.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.navigation.navigation.compose)
    implementation(libs.androidx.hilt.hilt.navigation.compose)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.ui.tooling)
    implementation(libs.androidx.compose.ui.ui.tooling.preview)
    implementation(libs.androidx.startup.startup.runtime)
    implementation(libs.com.google.dagger.hilt.android)
    kapt(libs.com.google.dagger.hilt.compiler)
}