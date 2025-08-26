plugins {
    id("toggles.android.module-conventions")
    id("toggles.ownership-conventions")
    id("dagger.hilt.android.plugin")
    alias(libs.plugins.com.google.devtools.ksp)
}

android {
    namespace = "se.eelde.toggles.provider.wiring"
}

dependencies {
    implementation(projects.modules.provider.api)
    implementation(projects.modules.provider.implementation)

    implementation(libs.com.google.dagger.hilt.android)
    ksp(libs.com.google.dagger.hilt.compiler)
}