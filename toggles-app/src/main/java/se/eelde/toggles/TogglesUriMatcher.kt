package se.eelde.toggles

import android.content.UriMatcher
import com.izettle.wrench.core.WrenchProviderContract
import se.eelde.toggles.core.TogglesProviderContract

class TogglesUriMatcher {
    companion object {
        internal const val CURRENT_CONFIGURATION_ID = 1
        internal const val CURRENT_CONFIGURATION_KEY = 2
        internal const val CURRENT_CONFIGURATIONS = 3
        internal const val PREDEFINED_CONFIGURATION_VALUES = 5
        internal const val APPLICATION_ID = 6

        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)

        fun getTogglesUriMatcher() = uriMatcher

        init {
            uriMatcher.addURI(
                TogglesProviderContract.TOGGLES_AUTHORITY,
                "application/#",
                APPLICATION_ID
            )
            uriMatcher.addURI(
                TogglesProviderContract.TOGGLES_AUTHORITY,
                "currentConfiguration/#",
                CURRENT_CONFIGURATION_ID
            )
            uriMatcher.addURI(
                TogglesProviderContract.TOGGLES_AUTHORITY,
                "currentConfiguration/*",
                CURRENT_CONFIGURATION_KEY
            )
            uriMatcher.addURI(
                TogglesProviderContract.TOGGLES_AUTHORITY,
                "currentConfiguration",
                CURRENT_CONFIGURATIONS
            )
            uriMatcher.addURI(
                TogglesProviderContract.TOGGLES_AUTHORITY,
                "predefinedConfigurationValue",
                PREDEFINED_CONFIGURATION_VALUES
            )

            uriMatcher.addURI(
                WrenchProviderContract.WRENCH_AUTHORITY,
                "currentConfiguration/#",
                CURRENT_CONFIGURATION_ID
            )
            uriMatcher.addURI(
                WrenchProviderContract.WRENCH_AUTHORITY,
                "currentConfiguration/*",
                CURRENT_CONFIGURATION_KEY
            )
            uriMatcher.addURI(
                WrenchProviderContract.WRENCH_AUTHORITY,
                "currentConfiguration",
                CURRENT_CONFIGURATIONS
            )
            uriMatcher.addURI(
                WrenchProviderContract.WRENCH_AUTHORITY,
                "predefinedConfigurationValue",
                PREDEFINED_CONFIGURATION_VALUES
            )
        }

    }
}