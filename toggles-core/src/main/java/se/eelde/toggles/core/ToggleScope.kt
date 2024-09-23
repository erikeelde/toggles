package se.eelde.toggles.core

import android.content.ContentValues
import android.database.Cursor
import java.util.Date

@Suppress("LibraryEntitiesShouldNotBePublic")
public class ToggleScope private constructor(
    public val id: Long = 0,
    public val name: String,
    public val timeStamp: Date,
) {
    public class Builder {
        @set:JvmSynthetic
        public var id: Long = 0

        @set:JvmSynthetic
        public lateinit var name: String

        @set:JvmSynthetic
        public lateinit var timeStamp: Date

        public fun setId(id: Long): Builder = apply { this.id = id }

        public fun setName(name: String): Builder = apply { this.name = name }

        public fun setTimeStamp(timeStamp: Date): Builder = apply { this.timeStamp = timeStamp }

        public fun build(): ToggleScope = ToggleScope(id = id, name = name, timeStamp = timeStamp)
    }

    public fun copy(
        id: Long = this.id,
        name: String = this.name,
        timeStamp: Date = this.timeStamp,
    ): ToggleScope = ToggleScope(id = id, name = name, timeStamp = timeStamp)

    public fun toContentValues(): ContentValues = ContentValues().apply {
        put(ColumnNames.ToggleScope.COL_ID, id)
        put(ColumnNames.ToggleScope.COL_NAME, name)
        put(ColumnNames.ToggleScope.COL_SELECTED_TIMESTAMP, timeStamp.time)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ToggleScope

        if (id != other.id) return false
        if (name != other.name) return false
        if (timeStamp != other.timeStamp) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + timeStamp.hashCode()
        return result
    }

    override fun toString(): String {
        return "ToggleScope(id=$id, name='$name', timeStamp=$timeStamp)"
    }

    public companion object {
        @JvmStatic
        public fun fromContentValues(contentValues: ContentValues): ToggleScope {
            return ToggleScope(
                id = contentValues.getAsLong(ColumnNames.ToggleScope.COL_ID),
                name = contentValues.getAsString(ColumnNames.ToggleScope.COL_NAME),
                timeStamp = Date(contentValues.getAsLong(ColumnNames.ToggleScope.COL_SELECTED_TIMESTAMP))
            )
        }

        @JvmStatic
        public fun fromCursor(cursor: Cursor): ToggleScope {
            return ToggleScope(
                id = cursor.getLongOrThrow(ColumnNames.ToggleScope.COL_ID),
                name = cursor.getStringOrThrow(ColumnNames.ToggleScope.COL_NAME),
                timeStamp = Date(cursor.getLongOrThrow(ColumnNames.ToggleScope.COL_SELECTED_TIMESTAMP))
            )
        }
    }
}

@JvmSynthetic
@Suppress("LibraryEntitiesShouldNotBePublic")
public fun ToggleScope(initializer: ToggleScope.Builder.() -> Unit): ToggleScope {
    return ToggleScope.Builder().apply(initializer).build()
}
