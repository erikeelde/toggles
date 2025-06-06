package se.eelde.toggles.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import se.eelde.toggles.core.ColumnNames
import se.eelde.toggles.database.tables.ApplicationTable
import se.eelde.toggles.database.tables.ScopeTable
import java.util.Date

@Entity(
    tableName = ScopeTable.TABLE_NAME,
    indices = [Index(value = arrayOf(ScopeTable.COL_APP_ID, ScopeTable.COL_NAME), unique = true)],
    foreignKeys = [
        ForeignKey(
            entity = TogglesApplication::class,
            parentColumns = arrayOf(
                ApplicationTable.COL_ID
            ),
            childColumns = arrayOf(ScopeTable.COL_APP_ID),
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TogglesScope constructor(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = ScopeTable.COL_ID)
    var id: Long,

    @ColumnInfo(name = ScopeTable.COL_APP_ID)
    var applicationId: Long,

    @ColumnInfo(name = ScopeTable.COL_NAME)
    var name: String,

    @ColumnInfo(name = ScopeTable.COL_SELECTED_TIMESTAMP)
    var timeStamp: Date
) {

    companion object {

        const val SCOPE_DEFAULT = ColumnNames.ToggleScope.DEFAULT_SCOPE
        const val SCOPE_USER = "Development scope"

        fun newScope() = TogglesScope(0, 0, SCOPE_DEFAULT, Date())

        fun isDefaultScope(scope: TogglesScope): Boolean {
            return SCOPE_DEFAULT == scope.name
        }
    }
}
