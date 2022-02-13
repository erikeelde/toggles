plugins {
    id("toggles.android.library-conventions")
    id("toggles.ownership-conventions")
}

apply(plugin = "com.vanniktech.maven.publish")

dependencies {
    testImplementation("junit:junit:4.13.2")

    testImplementation("androidx.test:core:1.4.0")
    testImplementation("androidx.test.ext:truth:1.4.0")
    testImplementation("androidx.test:rules:1.4.0")
    testImplementation("androidx.test:runner:1.4.0")
    testImplementation("androidx.test.ext:junit:1.1.3")
    testImplementation("org.robolectric:robolectric:4.8.1")

    implementation("se.eelde.toggles:toggles-core:0.0.2")
    //implementation("se.eelde.toggles:toggles-core:0.0.2-SNAPSHOT")
    //implementation(project(":toggles-core"))

    implementation("androidx.annotation:annotation:1.4.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.3")
}
