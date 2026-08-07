plugins {
    alias(libs.plugins.toggles.android.module)
    alias(libs.plugins.com.google.devtools.ksp)
    alias(libs.plugins.org.jetbrains.kotlin.plugin.serialization)
}

android {
    namespace = "se.eelde.toggles.agent.implementation"

    packaging {
        resources.excludes.add("META-INF/versions/9/OSGI-INF/MANIFEST.MF")
    }
}

dependencies {
    api(projects.modules.database.implementation)
    // provider/api only — the agent module is a peer of TogglesProvider, not a consumer of it.
    api(projects.modules.provider.api)
    implementation(libs.org.jetbrains.kotlinx.kotlinx.serialization.json)
    implementation(libs.androidx.annotation)
    implementation(libs.com.google.dagger.hilt.android)
    implementation(libs.com.google.dagger.hilt.core)
    ksp(libs.com.google.dagger.hilt.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.org.robolectric)
    testImplementation(libs.org.robolectric.annotations)
    testImplementation(libs.org.robolectric.shadows.framework)
    testImplementation(libs.com.google.dagger)
    testImplementation(libs.javax.inject)
    testImplementation(libs.com.google.dagger.hilt.android.testing)
    testImplementation(libs.androidx.room.room.runtime)
    testImplementation(projects.modules.database.wiring)
    kspTest(libs.com.google.dagger.hilt.android.compiler)
}
