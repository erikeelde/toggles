plugins {
    alias(libs.plugins.toggles.android.module)
    alias(libs.plugins.com.google.devtools.ksp)
}

android {
    namespace = "se.eelde.toggles.provider.implementation"

    packaging {
        resources.excludes.add("META-INF/versions/9/OSGI-INF/MANIFEST.MF")
    }
}
dependencies {
    implementation(projects.modules.provider.api)
    implementation(libs.kotlinx.datetime)
    implementation(projects.modules.database.implementation)
    implementation(libs.androidx.core.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(projects.togglesCore)
    implementation(projects.togglesFlow)
    implementation(projects.modules.coroutines.api)
    runtimeOnly(libs.androidx.startup.startup.runtime)
    implementation(libs.com.google.dagger.hilt.android)
    ksp(libs.com.google.dagger.hilt.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.org.robolectric)
    testImplementation(libs.com.google.dagger.hilt.android.testing)
    testImplementation(libs.org.jetbrains.kotlinx.kotlinx.coroutines.test)
    testImplementation(libs.app.cash.turbine)
    testImplementation(libs.androidx.room.room.runtime)
    testImplementation(projects.modules.database.wiring)
    kspTest(libs.com.google.dagger.hilt.android.compiler)

    testFixturesImplementation(libs.org.robolectric)
    testFixturesImplementation(libs.androidx.core.core.ktx)
    testFixturesImplementation(projects.modules.database.implementation)
    testFixturesImplementation(libs.androidx.room.room.ktx)
    testFixturesImplementation(projects.togglesFlow)
}