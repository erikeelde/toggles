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
    implementation(libs.androidx.navigation.navigation.runtime.ktx)
    implementation(libs.androidx.navigation.navigation.compose)
}