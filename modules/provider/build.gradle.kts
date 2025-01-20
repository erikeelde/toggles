plugins {
    id("toggles.android.module-conventions")
    id("toggles.ownership-conventions")
    alias(libs.plugins.com.google.devtools.ksp)
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "se.eelde.toggles.provider"

    packaging {
        resources.excludes.add("META-INF/versions/9/OSGI-INF/MANIFEST.MF")
    }
}
dependencies {
    implementation(projects.modules.database)
    implementation(libs.androidx.core.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(projects.togglesCore)
    implementation(projects.togglesPrefs)
    implementation(libs.androidx.startup.startup.runtime)
    implementation(libs.com.google.dagger.hilt.android)
    ksp(libs.com.google.dagger.hilt.compiler)

    testFixturesImplementation(platform(libs.androidx.compose.bom))
    testFixturesImplementation(libs.androidx.compose.runtime)

    testFixturesImplementation(libs.org.robolectric)
    testFixturesImplementation(libs.androidx.core.core.ktx)
    testFixturesImplementation(projects.modules.database)
    testFixturesImplementation(libs.androidx.room.room.runtime)
    testFixturesImplementation(libs.androidx.room.room.ktx)
    testFixturesImplementation(projects.togglesPrefs)
}