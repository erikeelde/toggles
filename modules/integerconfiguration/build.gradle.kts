plugins {
    id("toggles.android.module-conventions")
    id("toggles.ownership-conventions")
    id("com.google.devtools.ksp")
}

android {
    namespace = "se.eelde.toggles.integerconfiguration"
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.androidx.compose.compiler.get()
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(projects.modules.composeTheme)
    implementation(projects.modules.database)
    implementation(projects.modules.provider)
    implementation(libs.androidx.core.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.navigation.navigation.compose)
    implementation(libs.androidx.hilt.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.lifecycle.runtime.compose)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.ui.tooling)
    implementation(libs.androidx.compose.ui.ui.tooling.preview)
    implementation(libs.androidx.startup.startup.runtime)
    implementation(libs.com.google.dagger.hilt.android)
    implementation(libs.se.eelde.toggles.toggles.core)
    ksp(libs.com.google.dagger.hilt.compiler)
}