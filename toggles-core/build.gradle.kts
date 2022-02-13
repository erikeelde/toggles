plugins {
    id("toggles.android.library-conventions")
    id("toggles.ownership-conventions")
}

apply(plugin = "com.vanniktech.maven.publish")

android {
    defaultConfig {
        val togglesProviderAuthority = "se.eelde.toggles.configprovider"
        manifestPlaceholders["togglesProviderAuthority"] = togglesProviderAuthority
        buildConfigField("String", "TOGGLES_AUTHORITY", "\"${togglesProviderAuthority}\"")

        buildConfigField("int", "TOGGLES_API_VERSION", "1")
    }
}

dependencies {
    implementation("androidx.annotation:annotation:1.4.0")
}
