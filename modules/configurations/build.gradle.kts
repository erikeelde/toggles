plugins {
    alias(libs.plugins.toggles.android.module)
    alias(libs.plugins.toggles.android.compose.module)
    alias(libs.plugins.com.google.devtools.ksp)
}

android {
    namespace = "se.eelde.toggles.configurations"
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(projects.modules.composeTheme)
    implementation(projects.modules.database.implementation)
    implementation(projects.togglesCore)
    implementation(libs.androidx.core.core.ktx)
    implementation(libs.androidx.lifecycle.lifecycle.runtime.compose)
    implementation(libs.androidx.appcompat)
    implementation(projects.modules.routes.api)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.hilt.hilt.lifecycle.viewmodel.compose)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.ui.tooling)
    implementation(libs.androidx.compose.material.material.icons.extended)
    runtimeOnly(libs.androidx.startup.startup.runtime)
    implementation(libs.com.google.dagger.hilt.android)
    ksp(libs.com.google.dagger.hilt.compiler)
    implementation(libs.androidx.activity.activity.compose)
    implementation(libs.androidx.compose.foundation.foundation.layout)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.compose.ui.ui.text)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.lifecycle.lifecycle.viewmodel.compose)
    api(libs.com.google.dagger)
    implementation(libs.com.google.dagger.hilt.core)
    api(libs.javax.inject)
    api(libs.org.jetbrains.kotlinx.kotlinx.coroutines.core)
}