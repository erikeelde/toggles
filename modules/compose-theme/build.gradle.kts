plugins {
    id("toggles.android.module-conventions")
    id("toggles.ownership-conventions")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "se.eelde.toggles.composetheme"
}
dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.core.core.ktx)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.hilt.hilt.navigation.compose)
}