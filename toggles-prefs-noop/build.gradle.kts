plugins {
    id("toggles.android.library-conventions")
    id("toggles.ownership-conventions")
}

apply(plugin = "com.vanniktech.maven.publish")

android {
    namespace = "se.eelde.toggles.prefs"
}

dependencies {
    implementation(libs.se.eelde.toggles.toggles.core)
    //implementation("se.eelde.toggles:toggles-core:0.0.2-SNAPSHOT")
    //implementation(project(":toggles-core"))

        implementation(libs.androidx.annotation)
    implementation(libs.org.jetbrains.kotlinx.kotlinx.coroutines.android)
}
