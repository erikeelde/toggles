package se.eelde.toggles

import android.content.UriMatcher

class TogglesUriMatcher private constructor() {
    companion object {
        internal const val CURRENT_CONFIGURATION_ID = 1
        internal const val CURRENT_CONFIGURATION_KEY = 2
        internal const val CURRENT_CONFIGURATIONS = 3
        internal const val PREDEFINED_CONFIGURATION_VALUES = 5
        private const val APPLICATION_ID = 6

        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)

        fun getTogglesUriMatcher() = uriMatcher

        init {
            uriMatcher.addURI(
                BuildConfig.CONFIG_AUTHORITY,
                "application/#",
                APPLICATION_ID
            )
            uriMatcher.addURI(
                BuildConfig.CONFIG_AUTHORITY,
                "currentConfiguration/#",
                CURRENT_CONFIGURATION_ID
            )
            uriMatcher.addURI(
                BuildConfig.CONFIG_AUTHORITY,
                "currentConfiguration/*",
                CURRENT_CONFIGURATION_KEY
            )
            uriMatcher.addURI(
                BuildConfig.CONFIG_AUTHORITY,
                "currentConfiguration",
                CURRENT_CONFIGURATIONS
            )
            uriMatcher.addURI(
                BuildConfig.CONFIG_AUTHORITY,
                "predefinedConfigurationValue",
                PREDEFINED_CONFIGURATION_VALUES
            )
        }
    }
}
