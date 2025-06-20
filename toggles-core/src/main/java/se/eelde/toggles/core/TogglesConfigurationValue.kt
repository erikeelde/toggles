package se.eelde.toggles.core

import android.content.ContentValues
import android.database.Cursor

@Suppress("LibraryEntitiesShouldNotBePublic")
public class TogglesConfigurationValue private constructor(
    public var id: Long = 0,
    public val configurationId: Long,
    public val value: String? = null,
    public val scope: Long,
) {
    public class Builder {
        @set:JvmSynthetic
        public var id: Long = 0

        @set:JvmSynthetic
        public var configurationId: Long = 0

        @set:JvmSynthetic
        public var value: String? = null

        @set:JvmSynthetic
        public var scope: Long = 0

        public fun setId(id: Long): Builder = apply { this.id = id }
        public fun setConfigurationId(configurationId: Long): Builder =
            apply { this.configurationId = configurationId }

        public fun setValue(value: String): Builder = apply { this.value = value }
        public fun setScope(scope: Long): Builder = apply { this.scope = scope }

        public fun build(): TogglesConfigurationValue =
            TogglesConfigurationValue(
                id = id,
                configurationId = configurationId,
                value = value,
                scope = scope
            )
    }

    public fun copy(
        id: Long = this.id,
        configurationId: Long = this.configurationId,
        value: String? = this.value,
        scope: Long = this.scope
    ): TogglesConfigurationValue =
        TogglesConfigurationValue(
            id = id,
            configurationId = configurationId,
            value = value,
            scope = scope
        )

    public fun toContentValues(): ContentValues = ContentValues().apply {
        put(ColumnNames.ConfigurationValue.COL_ID, id)
        put(ColumnNames.ConfigurationValue.COL_CONFIG_ID, configurationId)
        put(ColumnNames.ConfigurationValue.COL_VALUE, value)
        put(ColumnNames.ConfigurationValue.COL_SCOPE, scope)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TogglesConfigurationValue

        if (id != other.id) return false
        if (configurationId != other.configurationId) return false
        if (value != other.value) return false
        if (scope != other.scope) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + configurationId.hashCode()
        result = 31 * result + (value?.hashCode() ?: 0)
        result = 31 * result + scope.hashCode()
        return result
    }

    override fun toString(): String {
        return "TogglesConfigurationValue(id=$id, configurationId='$configurationId', value=$value, scope='$scope')"
    }

    public companion object {
        @JvmStatic
        public fun fromContentValues(values: ContentValues): TogglesConfigurationValue {
            return TogglesConfigurationValue(
                id = values.getAsLong(ColumnNames.ConfigurationValue.COL_ID) ?: 0,
                configurationId = values.getAsLong(ColumnNames.ConfigurationValue.COL_CONFIG_ID),
                value = values.getAsString(ColumnNames.ConfigurationValue.COL_VALUE),
                scope = values.getAsLong(ColumnNames.ConfigurationValue.COL_SCOPE),
            )
        }

        @JvmStatic
        public fun fromCursor(cursor: Cursor): TogglesConfigurationValue {
            return TogglesConfigurationValue(
                id = cursor.getLongOrThrow(ColumnNames.ConfigurationValue.COL_ID),
                configurationId = cursor.getLongOrThrow(ColumnNames.ConfigurationValue.COL_CONFIG_ID),
                value = cursor.getStringOrNull(ColumnNames.ConfigurationValue.COL_VALUE),
                scope = cursor.getLongOrThrow(ColumnNames.ConfigurationValue.COL_SCOPE),
            )
        }
    }
}

@JvmSynthetic
@Suppress("LibraryEntitiesShouldNotBePublic")
public fun TogglesConfigurationValue(initializer: TogglesConfigurationValue.Builder.() -> Unit):
    TogglesConfigurationValue {
    return TogglesConfigurationValue.Builder().apply(initializer).build()
}
