plugins {
    id("toggles.android.module-conventions")
    id("toggles.ownership-conventions")
    id("dagger.hilt.android.plugin")
    alias(libs.plugins.com.google.devtools.ksp)
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
}
