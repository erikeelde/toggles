plugins {
    alias(libs.plugins.toggles.android.module)
    alias(libs.plugins.toggles.hilt)
}

android {
    namespace = "se.eelde.toggles.provider.wiring"
}
dependencies {
    api(projects.modules.provider.implementation)

    api(libs.com.google.dagger.hilt.android)
    ksp(libs.com.google.dagger.hilt.compiler)

    androidTestImplementation(libs.androidx.test.ext.junit)
    api(libs.com.google.dagger)
    implementation(libs.com.google.dagger.hilt.core)
    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.core)
}