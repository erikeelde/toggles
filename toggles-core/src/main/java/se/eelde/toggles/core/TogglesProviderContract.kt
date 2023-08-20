package se.eelde.toggles.core

import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import androidx.annotation.StringDef

@Suppress("LibraryEntitiesShouldNotBePublic")
public class ColumnNames {
    public object Toggle {
        public const val COL_KEY: String = "configurationKey"
        public const val COL_ID: String = "id"
        public const val COL_VALUE: String = "value"
        public const val COL_TYPE: String = "configurationType"
    }

    public object ToggleValue {
        public const val COL_ID: String = "id"
        public const val COL_VALUE: String = "value"
        public const val COL_CONFIG_ID: String = "configurationId"
    }
}

@Suppress("ForbiddenPublicDataClass", "LibraryEntitiesShouldNotBePublic")
public data class ToggleValue(
    val id: Long = 0,
    val configurationId: Long = 0,
    val value: String? = null
) {

    public constructor(configurationId: Long, value: String?) : this(0, configurationId, value)

    public fun toContentValues(): ContentValues {
        val contentValues = ContentValues()
        if (id > 0) {
            contentValues.put(ColumnNames.ToggleValue.COL_ID, id)
        }
        contentValues.put(ColumnNames.ToggleValue.COL_CONFIG_ID, configurationId)
        contentValues.put(ColumnNames.ToggleValue.COL_VALUE, value)

        return contentValues
    }
}

@Suppress("ForbiddenPublicDataClass", "LibraryEntitiesShouldNotBePublic")
public data class Toggle(
    var id: Long = 0,
    @ToggleType val type: String,
    val key: String = "",
    val value: String? = null,
//     val scope: String? = null,
) {

    public fun toContentValues(): ContentValues = ContentValues().apply {
        put(ColumnNames.Toggle.COL_ID, id)
        put(ColumnNames.Toggle.COL_KEY, key)
        put(ColumnNames.Toggle.COL_VALUE, value)
        put(ColumnNames.Toggle.COL_TYPE, type)
    }

    @StringDef(TYPE.BOOLEAN, TYPE.STRING, TYPE.INTEGER, TYPE.ENUM)
    @Retention(AnnotationRetention.SOURCE)
    public annotation class ToggleType

    public object TYPE {
        public const val BOOLEAN: String = "boolean"
        public const val STRING: String = "string"
        public const val INTEGER: String = "integer"
        public const val ENUM: String = "enum"
    }

    public companion object {
        @JvmStatic
        public fun fromContentValues(values: ContentValues): Toggle {
            return Toggle(
                id = values.getAsLong(ColumnNames.Toggle.COL_ID) ?: 0,
                type = values.getAsString(ColumnNames.Toggle.COL_TYPE),
                key = values.getAsString(ColumnNames.Toggle.COL_KEY),
                value = values.getAsString(ColumnNames.Toggle.COL_VALUE)
            )
        }

        @JvmStatic
        public fun fromCursor(cursor: Cursor): Toggle {
            return Toggle(
                id = cursor.getLongOrThrow(ColumnNames.Toggle.COL_ID),
                type = cursor.getStringOrThrow(ColumnNames.Toggle.COL_TYPE),
                key = cursor.getStringOrThrow(ColumnNames.Toggle.COL_KEY),
                value = cursor.getStringOrNull(ColumnNames.Toggle.COL_VALUE),
                // scope = cursor.getStringOrNull("scope"),
            )
        }
    }
}

private fun Cursor.getStringOrThrow(columnName: String): String = getStringOrNull(columnName)!!

private fun Cursor.getStringOrNull(columnName: String): String? {
    val index = getColumnIndexOrThrow(columnName)
    return if (isNull(index)) null else getString(index)
}

private fun Cursor.getLongOrThrow(columnName: String): Long = getLongOrNull(columnName)!!

private fun Cursor.getLongOrNull(columnName: String): Long? {
    val index = getColumnIndexOrThrow(columnName)
    return if (isNull(index)) null else getLong(index)
}

public object TogglesProviderContract {
    private const val TOGGLES_AUTHORITY = "se.eelde.toggles.configprovider"
    private const val TOGGLES_API_VERSION_QUERY_PARAM = "API_VERSION"
    private const val TOGGLES_API_VERSION = 1

    private val applicationUri = Uri.parse("content://$TOGGLES_AUTHORITY/application")
    private val configurationUri = Uri.parse("content://$TOGGLES_AUTHORITY/currentConfiguration")
    private val configurationValueUri = Uri.parse("content://$TOGGLES_AUTHORITY/predefinedConfigurationValue")

    @JvmStatic
    public fun applicationUri(id: Long): Uri {
        return applicationUri
            .buildUpon()
            .appendPath(id.toString())
            .appendQueryParameter(TOGGLES_API_VERSION_QUERY_PARAM, TOGGLES_API_VERSION.toString())
            .build()
    }

    @JvmStatic
    public fun toggleUri(id: Long): Uri {
        return configurationUri
            .buildUpon()
            .appendPath(id.toString())
            .appendQueryParameter(TOGGLES_API_VERSION_QUERY_PARAM, TOGGLES_API_VERSION.toString())
            .build()
    }

    @JvmStatic
    public fun toggleUri(key: String): Uri {
        return configurationUri
            .buildUpon()
            .appendPath(key)
            .appendQueryParameter(TOGGLES_API_VERSION_QUERY_PARAM, TOGGLES_API_VERSION.toString())
            .build()
    }

    @JvmStatic
    public fun toggleUri(): Uri {
        return configurationUri
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
}
