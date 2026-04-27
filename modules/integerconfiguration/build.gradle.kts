plugins {
    alias(libs.plugins.toggles.android.module)
    alias(libs.plugins.toggles.android.compose.module)
    alias(libs.plugins.com.google.devtools.ksp)
}

android {
    namespace = "se.eelde.toggles.integerconfiguration"
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    api(projects.modules.database.implementation)
    implementation(projects.modules.provider.implementation)
    api(projects.modules.routes.api)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(projects.modules.coroutines.api)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.lifecycle.runtime.compose)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.ui.tooling)
    implementation(libs.androidx.compose.ui.ui.tooling.preview)
    runtimeOnly(libs.androidx.startup.startup.runtime)
    implementation(libs.com.google.dagger.hilt.android)
    implementation(projects.togglesCore)
    ksp(libs.com.google.dagger.hilt.compiler)
    implementation(libs.androidx.compose.foundation.foundation.layout)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.compose.ui.ui.text)
    implementation(libs.androidx.compose.ui)
    api(libs.com.google.dagger)
    implementation(libs.com.google.dagger.hilt.core)
    api(libs.javax.inject)
    api(libs.org.jetbrains.kotlinx.kotlinx.coroutines.core)
    implementation(libs.androidx.compose.ui.ui.unit)
    implementation(libs.androidx.lifecycle.lifecycle.common)
}