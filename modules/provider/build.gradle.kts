plugins {
    id("toggles.android.module-conventions")
    id("toggles.ownership-conventions")
    alias(libs.plugins.com.google.devtools.ksp)
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
    implementation(projects.togglesFlow)
    implementation(libs.androidx.startup.startup.runtime)
    implementation(libs.com.google.dagger.hilt.android)
    ksp(libs.com.google.dagger.hilt.compiler)

    testFixturesImplementation(libs.org.robolectric)
    testFixturesImplementation(libs.androidx.core.core.ktx)
    testFixturesImplementation(projects.modules.database)
    testFixturesImplementation(libs.androidx.room.room.runtime)
    testFixturesImplementation(libs.androidx.room.room.ktx)
    testFixturesImplementation(projects.togglesFlow)
}