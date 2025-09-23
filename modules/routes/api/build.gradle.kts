plugins {
    alias(libs.plugins.toggles.android.module)
    alias(libs.plugins.org.jetbrains.kotlin.plugin.serialization)
}

android {
    namespace = "se.eelde.toggles.routes"
}

dependencies {
    implementation(libs.org.jetbrains.kotlinx.kotlinx.serialization.json)
    api(libs.androidx.navigation3.runtime)
}