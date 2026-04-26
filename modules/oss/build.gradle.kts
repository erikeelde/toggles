plugins {
    alias(libs.plugins.toggles.android.module)
    alias(libs.plugins.toggles.android.compose.module)
    alias(libs.plugins.com.google.devtools.ksp)
}

android {
    namespace = "se.eelde.toggles.oss"
}

dependencies {
    ksp(libs.com.squareup.moshi.moshi.kotlin.codegen)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation.foundation.layout)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.foundation)

    implementation(libs.androidx.compose.material.material.icons.extended)


    implementation(libs.androidx.compose.ui.ui.tooling)
    implementation(libs.androidx.compose.ui.ui.tooling.preview)
    implementation(libs.androidx.compose.ui.ui.text)
    implementation(libs.org.jetbrains.kotlinx.kotlinx.collections.immutable)

    implementation(libs.com.google.dagger.hilt.android)
    ksp(libs.com.google.dagger.hilt.compiler)
    implementation(libs.androidx.hilt.hilt.lifecycle.viewmodel.compose)

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.lifecycle.lifecycle.viewmodel.compose)
    api(libs.com.google.dagger)
    implementation(libs.com.google.dagger.hilt.core)
    api(libs.javax.inject)
    api(libs.org.jetbrains.kotlinx.kotlinx.coroutines.core)
    implementation(libs.androidx.compose.ui.ui.unit)
}
