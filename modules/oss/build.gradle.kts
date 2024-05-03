plugins {
    id("toggles.android.module-conventions")
    id("toggles.ownership-conventions")
    alias(libs.plugins.com.google.devtools.ksp)
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "se.eelde.toggles.oss"
}

dependencies {
    ksp(libs.com.squareup.moshi.moshi.kotlin.codegen)

    implementation(libs.androidx.lifecycle.lifecycle.viewmodel.ktx)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation.foundation.layout)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.foundation)

    implementation(libs.androidx.compose.material.material.icons.extended)

    implementation(libs.androidx.compose.animation)

    implementation(libs.androidx.compose.ui.ui.tooling)
    implementation(libs.androidx.compose.ui.ui.text)
    implementation(libs.org.jetbrains.kotlinx.kotlinx.collections.immutable)

    implementation(libs.androidx.navigation.navigation.compose)

    implementation(libs.com.google.dagger.hilt.android)
    ksp(libs.com.google.dagger.hilt.compiler)
    implementation(libs.androidx.hilt.hilt.navigation.compose)

    implementation(libs.com.squareup.moshi)
    implementation(libs.com.squareup.okio)
    implementation(libs.androidx.core.core.ktx)
    implementation(libs.androidx.appcompat)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.espresso.core)
}
