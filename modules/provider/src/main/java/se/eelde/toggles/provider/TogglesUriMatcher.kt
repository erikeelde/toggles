package se.eelde.toggles.provider

import android.content.UriMatcher
import android.net.Uri

enum class UriMatch {
    CURRENT_CONFIGURATION_ID,
    CURRENT_CONFIGURATION_KEY,
    CURRENT_CONFIGURATIONS,
    CONFIGURATIONS,
    CONFIGURATION_ID,
    CONFIGURATION_KEY,
    CONFIGURATION_VALUE_ID,
    CONFIGURATION_VALUE_KEY,
    PREDEFINED_CONFIGURATION_VALUES,
    SCOPES,
    UNKNOWN,
}

class TogglesUriMatcher(providerAuthority: String) {
    private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)

    fun match(uri: Uri): UriMatch {
        val match = uriMatcher.match(uri)
        return if (match == -1) {
            UriMatch.UNKNOWN
        } else {
            UriMatch.entries[match]
        }
    }

    init {
        uriMatcher.addURI(
            providerAuthority,
            "currentConfiguration/#",
            UriMatch.CURRENT_CONFIGURATION_ID.ordinal
        )
        uriMatcher.addURI(
            providerAuthority,
            "currentConfiguration/*",
            UriMatch.CURRENT_CONFIGURATION_KEY.ordinal
        )
        uriMatcher.addURI(
            providerAuthority,
            "currentConfiguration",
            UriMatch.CURRENT_CONFIGURATIONS.ordinal
        )
        uriMatcher.addURI(
            providerAuthority,
            "configuration",
            UriMatch.CONFIGURATIONS.ordinal
        )
        uriMatcher.addURI(
            providerAuthority,
            "configuration/#",
            UriMatch.CONFIGURATION_ID.ordinal
        )
        uriMatcher.addURI(
            providerAuthority,
            "configuration/*",
            UriMatch.CONFIGURATION_KEY.ordinal
        )
        uriMatcher.addURI(
            providerAuthority,
            "configuration/#/values",
            UriMatch.CONFIGURATION_VALUE_ID.ordinal
        )
        uriMatcher.addURI(
            providerAuthority,
            "configuration/*/values",
            UriMatch.CONFIGURATION_VALUE_KEY.ordinal
        )
        uriMatcher.addURI(
            providerAuthority,
            "predefinedConfigurationValue",
            UriMatch.PREDEFINED_CONFIGURATION_VALUES.ordinal
        )
        uriMatcher.addURI(
            providerAuthority,
            "scope",
            UriMatch.SCOPES.ordinal
        )
    }
}
