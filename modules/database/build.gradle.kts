plugins {
    id("toggles.android.module-conventions")
    id("toggles.ownership-conventions")
}

android {
    namespace = "se.eelde.toggles.database"
}

kapt {
    arguments {
        arg("room.schemaLocation", "$projectDir/schemas")
        arg("room.incremental", "true")
        arg("room.expandProjection", "true")
    }
}

dependencies {
    implementation(libs.androidx.room.room.paging)
    implementation(libs.androidx.room.room.runtime)
    implementation(libs.androidx.room.room.ktx)
    kapt(libs.androidx.room.room.compiler)
    implementation(libs.se.eelde.toggles.toggles.core)
    testImplementation(libs.androidx.room.room.testing)
    testImplementation(libs.androidx.test.ext.junit)
    implementation(libs.androidx.core.core.ktx)
    implementation(libs.androidx.appcompat)
    testImplementation(libs.org.robolectric)
    implementation(libs.com.google.android.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.espresso.core)
}