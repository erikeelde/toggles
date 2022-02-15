plugins {
    id("toggles.android.module-conventions")
    id("se.premex.ownership")
}
android {
    namespace = "com.izettle.wrench.core"
}
dependencies {
    implementation("androidx.appcompat:appcompat:1.4.1")
}