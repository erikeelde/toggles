package se.eelde.toggles.core

import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import androidx.annotation.StringDef
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull

class ColumnNames {
    object Toggle {
        const val COL_KEY = "configurationKey"
        const val COL_ID = "id"
        const val COL_VALUE = "value"
        const val COL_TYPE = "configurationType"
    }

    object ToggleValue {
        const val COL_ID = "id"
        const val COL_VALUE = "value"
        const val COL_CONFIG_ID = "configurationId"
    }
}

data class ToggleValue(
    val id: Long = 0,
    val configurationId: Long = 0,
    val value: String? = null
) {

    constructor(configurationId: Long, value: String?) : this(0, configurationId, value)

    fun toContentValues(): ContentValues {
        val contentValues = ContentValues()
        if (id > 0) {
            contentValues.put(ColumnNames.ToggleValue.COL_ID, id)
        }
        contentValues.put(ColumnNames.ToggleValue.COL_CONFIG_ID, configurationId)
        contentValues.put(ColumnNames.ToggleValue.COL_VALUE, value)

        return contentValues
    }
}

data class Toggle(
    var id: Long = 0,
    @ToggleType val type: String,
    val key: String = "",
    val value: String? = null
) {

    fun toContentValues(): ContentValues {
        val contentValues = ContentValues()

        contentValues.put(ColumnNames.Toggle.COL_ID, id)
        contentValues.put(ColumnNames.Toggle.COL_KEY, key)
        contentValues.put(ColumnNames.Toggle.COL_VALUE, value)
        contentValues.put(ColumnNames.Toggle.COL_TYPE, type)

        return contentValues
    }

    @StringDef(TYPE.BOOLEAN, TYPE.STRING, TYPE.INTEGER, TYPE.ENUM)
    @Retention(AnnotationRetention.SOURCE)
    annotation class ToggleType

    object TYPE {
        const val BOOLEAN = "boolean"
        const val STRING = "string"
        const val INTEGER = "integer"
        const val ENUM = "enum"
    }

    companion object {
        @JvmStatic
        fun fromContentValues(values: ContentValues): Toggle {
            return Toggle(
                id = values.getAsLong(ColumnNames.Toggle.COL_ID) ?: 0,
                type = values.getAsString(ColumnNames.Toggle.COL_TYPE),
                key = values.getAsString(ColumnNames.Toggle.COL_KEY),
                value = values.getAsString(ColumnNames.Toggle.COL_VALUE)
            )
        }

        @JvmStatic
        fun fromCursor(cursor: Cursor): Toggle {
            return Toggle(
                id = cursor.getLongOrNull(cursor.getColumnIndexOrThrow(ColumnNames.Toggle.COL_ID))!!,
                type = cursor.getStringOrNull(cursor.getColumnIndexOrThrow(ColumnNames.Toggle.COL_TYPE))!!,
                key = cursor.getStringOrNull(cursor.getColumnIndexOrThrow(ColumnNames.Toggle.COL_KEY))!!,
                value = cursor.getStringOrNull(cursor.getColumnIndexOrThrow(ColumnNames.Toggle.COL_VALUE))
            )
        }
    }
}

object TogglesProviderContract {
    private const val TOGGLES_AUTHORITY = BuildConfig.TOGGLES_AUTHORITY
    private const val TOGGLES_API_VERSION_QUERY_PARAM = "API_VERSION"
    private const val TOGGLES_API_VERSION = BuildConfig.TOGGLES_API_VERSION.toString()

    private val applicationUri = Uri.parse("content://$TOGGLES_AUTHORITY/application")
    private val configurationUri = Uri.parse("content://$TOGGLES_AUTHORITY/currentConfiguration")
    private val configurationValueUri = Uri.parse("content://$TOGGLES_AUTHORITY/predefinedConfigurationValue")

    @JvmStatic
    fun applicationUri(id: Long): Uri {
        return applicationUri
            .buildUpon()
            .appendPath(id.toString())
            .appendQueryParameter(TOGGLES_API_VERSION_QUERY_PARAM, TOGGLES_API_VERSION)
            .build()
    }

    @JvmStatic
    fun toggleUri(id: Long): Uri {
        return configurationUri
            .buildUpon()
            .appendPath(id.toString())
            .appendQueryParameter(TOGGLES_API_VERSION_QUERY_PARAM, TOGGLES_API_VERSION)
            .build()
    }

    @JvmStatic
    fun toggleUri(key: String): Uri {
        return configurationUri
            .buildUpon()
            .appendPath(key)
            .appendQueryParameter(TOGGLES_API_VERSION_QUERY_PARAM, TOGGLES_API_VERSION)
            .build()
    }

    @JvmStatic
    fun toggleUri(): Uri {
        return configurationUri
            .buildUpon()
            .appendQueryParameter(TOGGLES_API_VERSION_QUERY_PARAM, TOGGLES_API_VERSION)
            .build()
    }

    @JvmStatic
    fun toggleValueUri(): Uri {
        return configurationValueUri
            .buildUpon()
            .appendQueryParameter(TOGGLES_API_VERSION_QUERY_PARAM, TOGGLES_API_VERSION)
            .build()
    }
}
