plugins {
    alias(libs.plugins.toggles.android.module)
    alias(libs.plugins.toggles.hilt)
}

android {
    namespace = "se.eelde.toggles.coroutines.wiring"
}

dependencies {
    implementation(projects.modules.coroutines.api)
    implementation(libs.com.google.dagger.hilt.android)
    ksp(libs.com.google.dagger.hilt.compiler)
    api(libs.com.google.dagger)
    implementation(libs.com.google.dagger.hilt.core)
    runtimeOnly(libs.org.jetbrains.kotlinx.kotlinx.coroutines.android)
    api(libs.org.jetbrains.kotlinx.kotlinx.coroutines.core)
    runtimeOnly(libs.com.google.dagger.dagger.lint.aar)
}