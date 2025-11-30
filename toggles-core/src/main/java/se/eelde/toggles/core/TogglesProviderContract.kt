package se.eelde.toggles.core

import android.net.Uri

public object TogglesProviderContract {
    private const val TOGGLES_AUTHORITY = "se.eelde.toggles.configprovider"
    private const val TOGGLES_API_VERSION_QUERY_PARAM = "API_VERSION"
    private const val TOGGLES_API_VERSION = 1
    private const val TOGGLES_SCOPE_QUERY_PARAM = "SCOPE"

    private val configurationUri: Uri = Uri.parse("content://$TOGGLES_AUTHORITY/configuration")

    private val configurationValueUri: Uri =
        Uri.parse("content://$TOGGLES_AUTHORITY/currentConfiguration")
    private val predefinedConfigurationvalueUri: Uri =
        Uri.parse("content://$TOGGLES_AUTHORITY/predefinedConfigurationValue")
    private val baseScopeUri: Uri =
        Uri.parse("content://$TOGGLES_AUTHORITY/scope")

    @JvmStatic
    public fun toggleUri(id: Long): Uri {
        return configurationValueUri
            .buildUpon()
            .appendPath(id.toString())
            .appendQueryParameter(TOGGLES_API_VERSION_QUERY_PARAM, TOGGLES_API_VERSION.toString())
            .build()
    }

    @JvmStatic
    public fun toggleUri(id: Long, scope: String?): Uri {
        val builder = configurationValueUri
            .buildUpon()
            .appendPath(id.toString())
            .appendQueryParameter(TOGGLES_API_VERSION_QUERY_PARAM, TOGGLES_API_VERSION.toString())
        if (scope != null) {
            builder.appendQueryParameter(TOGGLES_SCOPE_QUERY_PARAM, scope)
        }
        return builder.build()
    }

    @JvmStatic
    public fun toggleUri(key: String): Uri {
        return configurationValueUri
            .buildUpon()
            .appendPath(key)
            .appendQueryParameter(TOGGLES_API_VERSION_QUERY_PARAM, TOGGLES_API_VERSION.toString())
            .build()
    }

    @JvmStatic
    public fun toggleUri(key: String, scope: String?): Uri {
        val builder = configurationValueUri
            .buildUpon()
            .appendPath(key)
            .appendQueryParameter(TOGGLES_API_VERSION_QUERY_PARAM, TOGGLES_API_VERSION.toString())
        if (scope != null) {
            builder.appendQueryParameter(TOGGLES_SCOPE_QUERY_PARAM, scope)
        }
        return builder.build()
    }

    @JvmStatic
    public fun toggleUri(): Uri {
        return configurationValueUri
            .buildUpon()
            .appendQueryParameter(TOGGLES_API_VERSION_QUERY_PARAM, TOGGLES_API_VERSION.toString())
            .build()
    }

    @JvmStatic
    public fun toggleUri(scope: String?): Uri {
        val builder = configurationValueUri
            .buildUpon()
            .appendQueryParameter(TOGGLES_API_VERSION_QUERY_PARAM, TOGGLES_API_VERSION.toString())
        if (scope != null) {
            builder.appendQueryParameter(TOGGLES_SCOPE_QUERY_PARAM, scope)
        }
        return builder.build()
    }

    @JvmStatic
    public fun toggleValueUri(): Uri {
        return predefinedConfigurationvalueUri
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

    @JvmStatic
    public fun configurationValueUri(key: String): Uri {
        return configurationUri
            .buildUpon()
            .appendPath(key)
            .appendPath("values")
            .appendQueryParameter(TOGGLES_API_VERSION_QUERY_PARAM, TOGGLES_API_VERSION.toString())
            .build()
    }

    @JvmStatic
    public fun configurationValueUri(id: Long): Uri {
        return configurationUri
            .buildUpon()
            .appendPath(id.toString())
            .appendPath("values")
            .appendQueryParameter(TOGGLES_API_VERSION_QUERY_PARAM, TOGGLES_API_VERSION.toString())
            .build()
    }

    @JvmStatic
    public fun scopeUri(): Uri {
        return baseScopeUri
            .buildUpon()
            .appendQueryParameter(TOGGLES_API_VERSION_QUERY_PARAM, TOGGLES_API_VERSION.toString())
            .build()
    }
}
