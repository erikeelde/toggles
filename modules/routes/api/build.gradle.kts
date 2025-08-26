plugins {
    id("toggles.android.module-conventions")
    id("toggles.ownership-conventions")
    alias(libs.plugins.org.jetbrains.kotlin.plugin.serialization)
}

android {
    namespace = "se.eelde.toggles.routes"
}

dependencies {
    implementation(libs.org.jetbrains.kotlinx.kotlinx.serialization.json)
}