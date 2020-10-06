package com.izettle.wrench.database

import android.text.TextUtils
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey
import com.izettle.wrench.database.tables.ApplicationTable
import com.izettle.wrench.database.tables.ScopeTable
import java.util.Date

@Entity(
    tableName = ScopeTable.TABLE_NAME,
    indices = [Index(value = arrayOf(ScopeTable.COL_APP_ID, ScopeTable.COL_NAME), unique = true)],
    foreignKeys = [ForeignKey(entity = WrenchApplication::class, parentColumns = arrayOf(ApplicationTable.COL_ID), childColumns = arrayOf(ScopeTable.COL_APP_ID), onDelete = CASCADE)]
)
data class WrenchScope constructor(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = ScopeTable.COL_ID)
    var id: Long = 0,

    @ColumnInfo(name = ScopeTable.COL_APP_ID)
    var applicationId: Long = 0,

    @ColumnInfo(name = ScopeTable.COL_NAME)
    var name: String = SCOPE_DEFAULT,

    @ColumnInfo(name = ScopeTable.COL_SELECTED_TIMESTAMP)
    var timeStamp: Date = Date()
) {

    companion object {

        const val SCOPE_DEFAULT = "wrench_default"
        const val SCOPE_USER = "Development scope"

        fun isDefaultScope(scope: WrenchScope): Boolean {
            return TextUtils.equals(WrenchScope.SCOPE_DEFAULT, scope.name)
        }
    }
}
