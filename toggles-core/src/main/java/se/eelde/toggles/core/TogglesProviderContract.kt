package se.eelde.toggles.core

import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import androidx.annotation.StringDef

@Suppress("LibraryEntitiesShouldNotBePublic")
public object ColumnNames {
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

@Suppress("LibraryEntitiesShouldNotBePublic")
public class ToggleValue private constructor(
    public val id: Long = 0,
    public val configurationId: Long = 0,
    public val value: String? = null
) {

    public class Builder {
        @set:JvmSynthetic
        public var id: Long = 0

        @set:JvmSynthetic
        public var configurationId: Long = 0

        @set:JvmSynthetic
        public var value: String? = null

        public fun setId(id: Long): Builder = apply { this.id = id }
        public fun setConfigurationId(configurationId: Long): Builder =
            apply { this.configurationId = configurationId }

        public fun setValue(value: String?): Builder = apply { this.value = value }

        public fun build(): ToggleValue =
            ToggleValue(id = id, configurationId = configurationId, value = value)
    }

    public fun toContentValues(): ContentValues = ContentValues().apply {
        if (id > 0) {
            put(ColumnNames.ToggleValue.COL_ID, id)
        }
        put(ColumnNames.ToggleValue.COL_CONFIG_ID, configurationId)
        put(ColumnNames.ToggleValue.COL_VALUE, value)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ToggleValue

        if (id != other.id) return false
        if (configurationId != other.configurationId) return false
        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + configurationId.hashCode()
        result = 31 * result + (value?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "ToggleValue(id=$id, configurationId=$configurationId, value=$value)"
    }
}

@JvmSynthetic
@Suppress("LibraryEntitiesShouldNotBePublic")
public fun ToggleValue(initializer: ToggleValue.Builder.() -> Unit): ToggleValue {
    return ToggleValue.Builder().apply(initializer).build()
}

@Suppress("LibraryEntitiesShouldNotBePublic")
public class Toggle private constructor(
    public var id: Long = 0,
    @ToggleType public val type: String,
    public val key: String = "",
    public val value: String? = null,
) {
    public class Builder {
        @set:JvmSynthetic
        public var id: Long = 0

        @set:JvmSynthetic
        @ToggleType
        public var type: String = ""

        @set:JvmSynthetic
        public var key: String = ""

        @set:JvmSynthetic
        public var value: String? = null

        public fun setId(id: Long): Builder = apply { this.id = id }
        public fun setType(@ToggleType type: String): Builder =
            apply { this.type = type }

        public fun setKey(key: String): Builder = apply { this.key = key }
        public fun setValue(value: String?): Builder = apply { this.value = value }

        public fun build(): Toggle =
            Toggle(id = id, type = type, key = key, value = value)
    }

    public fun copy(
        id: Long = this.id,
        type: String = this.type,
        key: String = this.key,
        value: String? = this.value
    ): Toggle =
        Toggle(id = id, type = type, key = key, value = value)

    public fun toContentValues(): ContentValues = ContentValues().apply {
        put(ColumnNames.Toggle.COL_ID, id)
        put(ColumnNames.Toggle.COL_KEY, key)
        put(ColumnNames.Toggle.COL_VALUE, value)
        put(ColumnNames.Toggle.COL_TYPE, type)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Toggle

        if (id != other.id) return false
        if (type != other.type) return false
        if (key != other.key) return false
        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + key.hashCode()
        result = 31 * result + (value?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "Toggle(id=$id, type='$type', key='$key', value=$value)"
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
            )
        }
    }
}

@JvmSynthetic
@Suppress("LibraryEntitiesShouldNotBePublic")
public fun Toggle(initializer: Toggle.Builder.() -> Unit): Toggle {
    return Toggle.Builder().apply(initializer).build()
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
    private val configurationValueUri =
        Uri.parse("content://$TOGGLES_AUTHORITY/predefinedConfigurationValue")

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
