package se.eelde.toggles.database

import androidx.room.TypeConverter
import kotlinx.datetime.Instant

class RoomInstantConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Instant? {
        return if (value == null) null else Instant.fromEpochMilliseconds(value)
    }

    @TypeConverter
    fun instantToTimestamp(instant: Instant?): Long? {
        return instant?.toEpochMilliseconds()
    }
}
