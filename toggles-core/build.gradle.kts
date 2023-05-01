plugins {
    id("toggles.android.library-conventions")
    id("toggles.ownership-conventions")
    alias(libs.plugins.com.vanniktech.maven.publish)
    alias(libs.plugins.org.jetbrains.kotlinx.binary.compatibility.validator)
    alias(libs.plugins.org.jetbrains.dokka)
}

android {
    namespace = "se.eelde.toggles.core"
}

dependencies {
    implementation(libs.androidx.annotation)
}
