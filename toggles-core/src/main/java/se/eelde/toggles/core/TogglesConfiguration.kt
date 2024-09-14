package se.eelde.toggles.core

import android.content.ContentValues
import android.database.Cursor

@Suppress("LibraryEntitiesShouldNotBePublic")
public class TogglesConfiguration private constructor(
    public var id: Long = 0,
    @Toggle.ToggleType public val type: String,
    public val key: String = "",
) {
    public class Builder {
        @set:JvmSynthetic
        public var id: Long = 0

        @set:JvmSynthetic
        @Toggle.ToggleType
        public var type: String = ""

        @set:JvmSynthetic
        public var key: String = ""

        public fun setId(id: Long): Builder = apply { this.id = id }
        public fun setType(@Toggle.ToggleType type: String): Builder =
            apply { this.type = type }

        public fun setKey(key: String): Builder = apply { this.key = key }

        public fun build(): TogglesConfiguration =
            TogglesConfiguration(id = id, type = type, key = key)
    }

    public fun copy(
        id: Long = this.id,
        type: String = this.type,
        key: String = this.key,
    ): TogglesConfiguration =
        TogglesConfiguration(id = id, type = type, key = key)

    public fun toContentValues(): ContentValues = ContentValues().apply {
        put(ColumnNames.Configuration.COL_ID, id)
        put(ColumnNames.Configuration.COL_KEY, key)
        put(ColumnNames.Configuration.COL_TYPE, type)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TogglesConfiguration

        if (id != other.id) return false
        if (type != other.type) return false
        if (key != other.key) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + key.hashCode()
        return result
    }

    override fun toString(): String {
        return "TogglesConfiguration(id=$id, type='$type', key='$key')"
    }

    public companion object {
        @JvmStatic
        public fun fromContentValues(values: ContentValues): TogglesConfiguration {
            return TogglesConfiguration(
                id = values.getAsLong(ColumnNames.Configuration.COL_ID) ?: 0,
                type = values.getAsString(ColumnNames.Configuration.COL_TYPE),
                key = values.getAsString(ColumnNames.Configuration.COL_KEY),
            )
        }

        @JvmStatic
        public fun fromCursor(cursor: Cursor): TogglesConfiguration {
            return TogglesConfiguration(
                id = cursor.getLongOrThrow(ColumnNames.Configuration.COL_ID),
                type = cursor.getStringOrThrow(ColumnNames.Configuration.COL_TYPE),
                key = cursor.getStringOrThrow(ColumnNames.Configuration.COL_KEY),
            )
        }
    }
}

@JvmSynthetic
@Suppress("LibraryEntitiesShouldNotBePublic")
public fun TogglesConfiguration(initializer: TogglesConfiguration.Builder.() -> Unit): TogglesConfiguration {
    return TogglesConfiguration.Builder().apply(initializer).build()
}