plugins {
    alias(libs.plugins.toggles.android.module)
    alias(libs.plugins.toggles.android.compose.module)
}

android {
    namespace = "se.eelde.toggles.composetheme"
}
dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.compose.ui.ui.text)
    implementation(libs.androidx.compose.ui.ui.unit)
}