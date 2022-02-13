plugins {
    id("toggles.android.module-conventions")
    id("toggles.ownership-conventions")
}

val composeVersion = "1.2.0-alpha03"

android {
    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = composeVersion
    }
}
dependencies {

    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.compose.ui:ui:$composeVersion")
    implementation("androidx.compose.material3:material3:1.0.0-alpha05")
}