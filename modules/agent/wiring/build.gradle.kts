plugins {
    alias(libs.plugins.toggles.android.module)
    alias(libs.plugins.toggles.hilt)
}

android {
    namespace = "se.eelde.toggles.agent.wiring"
}

dependencies {
    api(projects.modules.agent.implementation)

    api(libs.com.google.dagger.hilt.android)
    api(libs.com.google.dagger)
    implementation(libs.com.google.dagger.hilt.core)
    ksp(libs.com.google.dagger.hilt.compiler)
}
