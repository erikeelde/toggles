plugins {
    id("toggles.android.library-conventions")
    id("toggles.ownership-conventions")
    alias(libs.plugins.com.vanniktech.maven.publish)
    alias(libs.plugins.org.jetbrains.kotlinx.binary.compatibility.validator)
    alias(libs.plugins.org.jetbrains.dokka)
}

android {
    namespace = "se.eelde.toggles.flow"
}

dependencies {
    implementation(libs.se.eelde.toggles.toggles.core)

    implementation(libs.androidx.annotation)
    implementation(platform(libs.org.jetbrains.kotlinx.kotlinx.coroutines.bom))
    implementation(libs.org.jetbrains.kotlinx.kotlinx.coroutines.android)
}
