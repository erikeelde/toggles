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
}