package se.eelde.toggles.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import se.eelde.toggles.core.ColumnNames
import se.eelde.toggles.database.tables.ApplicationTable
import se.eelde.toggles.database.tables.ScopeTable
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

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
    var timeStamp: Instant
) {

    companion object {

        const val SCOPE_DEFAULT = ColumnNames.ToggleScope.DEFAULT_SCOPE
        const val SCOPE_USER = "Development scope"

        fun newScope(clock: Clock) = TogglesScope(0, 0, SCOPE_DEFAULT, clock.now())

        fun isDefaultScope(scope: TogglesScope): Boolean {
            return SCOPE_DEFAULT == scope.name
        }

        /**
         * The two scopes every application gets the moment Toggles creates its row — whether that
         * happens because the app itself first contacted the ContentProvider (TogglesProvider) or
         * because the agent API pre-created the application for a package that has not run yet.
         * Both call sites must build identical rows, so this is the one place either builds them:
         * the default scope's timestamp is anchored one second behind [clock] so it can never tie
         * with, or race ahead of, a scope created afterwards via createScope.
         */
        fun defaultScope(applicationId: Long, clock: Clock): TogglesScope = TogglesScope(
            id = 0,
            applicationId = applicationId,
            name = SCOPE_DEFAULT,
            timeStamp = clock.now().minus(1.seconds)
        )

        fun developmentScope(applicationId: Long, clock: Clock): TogglesScope = TogglesScope(
            id = 0,
            applicationId = applicationId,
            name = SCOPE_USER,
            timeStamp = clock.now()
        )
    }
}
