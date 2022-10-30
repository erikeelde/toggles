plugins {
    id("toggles.android.module-conventions")
    id("toggles.ownership-conventions")
}

android {
    namespace = "se.eelde.toggles.applications"
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":modules:navigation"))
    implementation(project(":modules:database"))
    implementation(libs.androidx.core.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.com.google.android.material)
    implementation(libs.androidx.hilt.hilt.navigation.compose)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.startup.startup.runtime)
    implementation(libs.com.google.dagger.hilt.android)
    kapt(libs.com.google.dagger.hilt.compiler)
}