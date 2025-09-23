plugins {
    alias(libs.plugins.toggles.android.module)
}

android {
    namespace = "se.eelde.toggles.coroutines"
}

dependencies {
    api(libs.javax.inject)
}