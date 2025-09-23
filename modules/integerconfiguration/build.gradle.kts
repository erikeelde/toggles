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
    implementation(projects.modules.composeTheme)
    implementation(projects.modules.database.implementation)
    implementation(projects.modules.provider.implementation)
    implementation(projects.modules.routes.api)
    implementation(libs.androidx.core.core.ktx)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(projects.modules.coroutines.api)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.hilt.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.lifecycle.runtime.compose)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.ui.tooling)
    implementation(libs.androidx.compose.ui.ui.tooling.preview)
    implementation(libs.androidx.startup.startup.runtime)
    implementation(libs.com.google.dagger.hilt.android)
    implementation(projects.togglesCore)
    ksp(libs.com.google.dagger.hilt.compiler)
}