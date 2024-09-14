package se.eelde.toggles.core

import android.content.ContentValues
import android.database.Cursor
import androidx.annotation.StringDef

@Suppress("LibraryEntitiesShouldNotBePublic")
public class Toggle private constructor(
    public var id: Long = 0,
    @ToggleType public val type: String,
    public val key: String = "",
    public val value: String? = null,
    public val scope: String? = null,
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
