plugins {
    id("toggles.android.module-conventions")
    id("toggles.ownership-conventions")
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
    implementation(projects.modules.database.implementation)
    implementation(libs.androidx.core.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(projects.togglesCore)
    implementation(projects.togglesFlow)
    implementation(libs.androidx.startup.startup.runtime)
    implementation(libs.com.google.dagger.hilt.android)
    ksp(libs.com.google.dagger.hilt.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.androidx.test.core.ktx)
    testImplementation(libs.androidx.test.ext.truth)
    testImplementation(libs.androidx.test.rules)
    testImplementation(libs.androidx.test.runner)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.org.robolectric)
    testImplementation(libs.androidx.test.espresso.espresso.core)
    testImplementation(libs.androidx.arch.core.core.testing)
    testImplementation(libs.com.google.dagger.hilt.android.testing)
    testImplementation(libs.org.jetbrains.kotlinx.kotlinx.coroutines.test)
    testImplementation(libs.app.cash.turbine)
    testImplementation(libs.androidx.room.room.runtime)
    testImplementation(projects.modules.database.wiring)
    kspTest(libs.com.google.dagger.hilt.android.compiler)

    testFixturesImplementation(libs.org.robolectric)
    testFixturesImplementation(libs.androidx.core.core.ktx)
    testFixturesImplementation(projects.modules.database.implementation)
    testFixturesImplementation(libs.androidx.room.room.runtime)
    testFixturesImplementation(libs.androidx.room.room.ktx)
    testFixturesImplementation(projects.togglesFlow)
}