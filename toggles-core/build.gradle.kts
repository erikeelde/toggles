plugins {
    id("toggles.android.library-conventions")
    id("toggles.ownership-conventions")
    alias(libs.plugins.com.vanniktech.maven.publish)
    alias(libs.plugins.org.jetbrains.kotlinx.binary.compatibility.validator)
    alias(libs.plugins.org.jetbrains.dokka)
}

android {
    namespace = "se.eelde.toggles.core"
    defaultConfig {
        val togglesProviderAuthority = "se.eelde.toggles.configprovider"
        manifestPlaceholders["togglesProviderAuthority"] = togglesProviderAuthority
        buildConfigField("String", "TOGGLES_AUTHORITY", "\"${togglesProviderAuthority}\"")

        buildConfigField("int", "TOGGLES_API_VERSION", "1")
    }
}

dependencies {
    implementation(libs.androidx.annotation)
}
