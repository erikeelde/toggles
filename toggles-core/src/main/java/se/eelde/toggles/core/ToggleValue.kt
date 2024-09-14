package se.eelde.toggles.core

import android.content.ContentValues

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