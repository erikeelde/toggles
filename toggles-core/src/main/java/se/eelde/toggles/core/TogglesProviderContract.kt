package se.eelde.toggles.core

import android.net.Uri

public object TogglesProviderContract {
    private const val TOGGLES_AUTHORITY = "se.eelde.toggles.configprovider"
    private const val TOGGLES_API_VERSION_QUERY_PARAM = "API_VERSION"
    private const val TOGGLES_API_VERSION = 1

    private val configurationUri = Uri.parse("content://$TOGGLES_AUTHORITY/configuration")
    private val currentConfigurationUri =
        Uri.parse("content://$TOGGLES_AUTHORITY/currentConfiguration")
    private val configurationValueUri =
        Uri.parse("content://$TOGGLES_AUTHORITY/predefinedConfigurationValue")

    @JvmStatic
    public fun toggleUri(id: Long): Uri {
        return currentConfigurationUri
            .buildUpon()
            .appendPath(id.toString())
            .appendQueryParameter(TOGGLES_API_VERSION_QUERY_PARAM, TOGGLES_API_VERSION.toString())
            .build()
    }

    @JvmStatic
    public fun toggleUri(key: String): Uri {
        return currentConfigurationUri
            .buildUpon()
            .appendPath(key)
            .appendQueryParameter(TOGGLES_API_VERSION_QUERY_PARAM, TOGGLES_API_VERSION.toString())
            .build()
    }

    @JvmStatic
    public fun toggleUri(): Uri {
        return currentConfigurationUri
            .buildUpon()
            .appendQueryParameter(TOGGLES_API_VERSION_QUERY_PARAM, TOGGLES_API_VERSION.toString())
            .build()
    }

    @JvmStatic
    public fun toggleValueUri(): Uri {
        return configurationValueUri
            .buildUpon()
            .appendQueryParameter(TOGGLES_API_VERSION_QUERY_PARAM, TOGGLES_API_VERSION.toString())
            .build()
    }

    @JvmStatic
    public fun configurationUri(): Uri {
        return configurationUri
            .buildUpon()
            .appendQueryParameter(TOGGLES_API_VERSION_QUERY_PARAM, TOGGLES_API_VERSION.toString())
            .build()
    }

    @JvmStatic
    public fun configurationUri(key: String): Uri {
        return configurationUri
            .buildUpon()
            .appendPath(key)
            .appendQueryParameter(TOGGLES_API_VERSION_QUERY_PARAM, TOGGLES_API_VERSION.toString())
            .build()
    }

    @JvmStatic
    public fun configurationUri(id: Long): Uri {
        return configurationUri
            .buildUpon()
            .appendPath(id.toString())
            .appendQueryParameter(TOGGLES_API_VERSION_QUERY_PARAM, TOGGLES_API_VERSION.toString())
            .build()
    }
}
