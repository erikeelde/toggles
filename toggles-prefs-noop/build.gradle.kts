plugins {
    id("toggles.android.library-conventions")
    id("toggles.ownership-conventions")
}

apply(plugin = "com.vanniktech.maven.publish")

dependencies {
    implementation("se.eelde.toggles:toggles-core:0.0.2")
    //implementation("se.eelde.toggles:toggles-core:0.0.2-SNAPSHOT")
    //implementation(project(":toggles-core"))

    implementation("androidx.annotation:annotation:1.4.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.3")
}
