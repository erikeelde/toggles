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
    implementation(libs.kotlinx.datetime)
    api(projects.modules.database.implementation)
    implementation(libs.androidx.appcompat)
    implementation(projects.togglesCore)
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
    testFixturesApi(projects.modules.database.implementation)
    testFixturesApi(projects.togglesFlow)
    implementation(libs.androidx.annotation)
    testImplementation(libs.com.google.dagger)
    testImplementation(libs.javax.inject)
    testFixturesApi(libs.org.jetbrains.kotlinx.kotlinx.coroutines.core)
    testImplementation(libs.org.jetbrains.kotlinx.kotlinx.coroutines.core)
    testImplementation(libs.androidx.fragment)
    testImplementation(libs.androidx.lifecycle.lifecycle.viewmodel.savedstate)
    testImplementation(libs.androidx.test.core)
    implementation(libs.com.google.dagger.hilt.core)
    testImplementation(libs.org.robolectric.annotations)
    testFixturesImplementation(libs.org.robolectric.shadows.framework)
    testImplementation(libs.org.robolectric.shadows.framework)
    testImplementation(libs.com.google.errorprone.error.prone.annotations)
    testImplementation(libs.com.google.guava.guava)
}
dependencies {
    testFixturesImplementation(libs.androidx.room.room.runtime)
}
