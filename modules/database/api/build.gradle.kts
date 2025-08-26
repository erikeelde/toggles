plugins {
    id("toggles.android.module-conventions")
    id("toggles.ownership-conventions")
    alias(libs.plugins.com.google.devtools.ksp)
}
android {
    namespace = "se.eelde.toggles.database"
}
dependencies {

}
