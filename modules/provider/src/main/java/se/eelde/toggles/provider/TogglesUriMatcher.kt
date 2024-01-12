package se.eelde.toggles.provider

import android.content.UriMatcher
import android.net.Uri

class TogglesUriMatcher constructor(providerAuthority: String) {
    @Suppress("MagicNumber")
    internal val currentConfigurationId = 1

    @Suppress("MagicNumber")
    internal val currentConfigurationKey = 2

    @Suppress("MagicNumber")
    internal val currentConfigurations = 3

    @Suppress("MagicNumber")
    internal val predefinedConfigurationValues = 5

    @Suppress("MagicNumber")
    private val applicationId = 6

    private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)

    fun match(uri: Uri) = uriMatcher.match(uri)

    init {
        uriMatcher.addURI(
            providerAuthority,
            "application/#",
            applicationId
        )
        uriMatcher.addURI(
            providerAuthority,
            "currentConfiguration/#",
            currentConfigurationId
        )
        uriMatcher.addURI(
            providerAuthority,
            "currentConfiguration/*",
            currentConfigurationKey
        )
        uriMatcher.addURI(
            providerAuthority,
            "currentConfiguration",
            currentConfigurations
        )
        uriMatcher.addURI(
            providerAuthority,
            "predefinedConfigurationValue",
            predefinedConfigurationValues
        )
    }
}
