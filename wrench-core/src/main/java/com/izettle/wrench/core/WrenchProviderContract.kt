package com.izettle.wrench.core

import android.net.Uri

object WrenchProviderContract {
    const val WRENCH_AUTHORITY = "com.izettle.wrench.configprovider"
    const val WRENCH_API_VERSION_QUERY_PARAM = "API_VERSION"
    private const val WRENCH_API_VERSION = 1.toString()
    private val boltUri = Uri.parse("content://$WRENCH_AUTHORITY/currentConfiguration")
    private val nutUri = Uri.parse("content://$WRENCH_AUTHORITY/predefinedConfigurationValue")

    fun boltUri(id: Long): Uri {
        return boltUri
            .buildUpon()
            .appendPath(id.toString())
            .appendQueryParameter(WRENCH_API_VERSION_QUERY_PARAM, WRENCH_API_VERSION)
            .build()
    }

    fun boltUri(key: String?): Uri {
        return boltUri
            .buildUpon()
            .appendPath(key)
            .appendQueryParameter(WRENCH_API_VERSION_QUERY_PARAM, WRENCH_API_VERSION)
            .build()
    }

    fun boltUri(): Uri {
        return boltUri
            .buildUpon()
            .appendQueryParameter(WRENCH_API_VERSION_QUERY_PARAM, WRENCH_API_VERSION)
            .build()
    }

    fun nutUri(): Uri {
        return nutUri
            .buildUpon()
            .appendQueryParameter(WRENCH_API_VERSION_QUERY_PARAM, WRENCH_API_VERSION)
            .build()
    }
}
