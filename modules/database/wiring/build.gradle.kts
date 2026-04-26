plugins {
    alias(libs.plugins.toggles.android.module)
    alias(libs.plugins.toggles.hilt)
}

android {
    namespace = "se.eelde.toggles.database.wiring"
}

dependencies {
    implementation(projects.modules.database.implementation)

    implementation(libs.androidx.room.room.runtime)

    implementation(libs.com.google.dagger.hilt.android)
    ksp(libs.com.google.dagger.hilt.compiler)

    androidTestImplementation(libs.androidx.test.ext.junit)
    api(libs.com.google.dagger)
    implementation(libs.com.google.dagger.hilt.core)
    api(libs.javax.inject)
    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.core)
}
