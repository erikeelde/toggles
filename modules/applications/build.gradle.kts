plugins {
    id("toggles.android.module-conventions")
    id("toggles.ownership-conventions")
    alias(libs.plugins.com.google.devtools.ksp)
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "se.eelde.toggles.applications"
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(projects.modules.composeTheme)
    implementation(projects.modules.database)
    implementation(libs.androidx.core.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(projects.modules.routes)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.hilt.hilt.navigation.compose)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.ui.tooling)
    implementation(libs.androidx.compose.ui.ui.tooling.preview)
    implementation(libs.androidx.startup.startup.runtime)
    implementation(libs.com.google.dagger.hilt.android)
    ksp(libs.com.google.dagger.hilt.compiler)
}