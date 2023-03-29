plugins {
    id("toggles.android.library-conventions")
    id("toggles.ownership-conventions")
    alias(libs.plugins.com.vanniktech.maven.publish)
    alias(libs.plugins.org.jetbrains.kotlinx.binary.compatibility.validator)
    alias(libs.plugins.org.jetbrains.dokka)
}

android {
    namespace = "se.eelde.toggles.prefs"

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    testImplementation(libs.junit)

    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.ext.truth)
    testImplementation(libs.androidx.test.rules)
    testImplementation(libs.androidx.test.runner)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.org.robolectric)

    implementation(libs.se.eelde.toggles.toggles.core)
    //implementation("se.eelde.toggles:toggles-core:0.0.2-SNAPSHOT")
    //implementation(project(":toggles-core"))

    implementation(libs.androidx.annotation)
}
