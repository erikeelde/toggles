plugins {
    alias(libs.plugins.toggles.android.module)
    alias(libs.plugins.toggles.hilt)
}

android {
    namespace = "se.eelde.toggles.database.wiring"
}

dependencies {
    implementation(projects.modules.database.api)
    implementation(projects.modules.database.implementation)

    implementation(libs.androidx.room.room.runtime)
    implementation(libs.androidx.room.room.ktx)

    implementation(libs.com.google.dagger.hilt.android)
    ksp(libs.com.google.dagger.hilt.compiler)

    androidTestImplementation(libs.androidx.test.core.ktx)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.arch.core.core.testing)
}
