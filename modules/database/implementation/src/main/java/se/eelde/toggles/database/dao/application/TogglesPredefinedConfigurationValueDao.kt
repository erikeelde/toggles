@file:Suppress("MaxLineLength")

package se.eelde.toggles.database.dao.application

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import se.eelde.toggles.database.TogglesPredefinedConfigurationValue
import se.eelde.toggles.database.tables.PredefinedConfigurationValueTable

@Dao
interface TogglesPredefinedConfigurationValueDao {

    @Query(
        """SELECT * FROM ${PredefinedConfigurationValueTable.TABLE_NAME} WHERE ${PredefinedConfigurationValueTable.COL_CONFIG_ID} = (:configurationId)"""
    )
    fun getByConfigurationId(configurationId: Long): Flow<List<TogglesPredefinedConfigurationValue>>

    @Query(
        """SELECT * FROM ${PredefinedConfigurationValueTable.TABLE_NAME} WHERE ${PredefinedConfigurationValueTable.COL_CONFIG_ID} = (:configurationId) AND ${PredefinedConfigurationValueTable.COL_VALUE} = (:value) """
    )
    fun getByConfigurationAndValueId(
        configurationId: Long,
        value: String
    ): TogglesPredefinedConfigurationValue

    @Insert
    fun insert(fullConfig: TogglesPredefinedConfigurationValue): Long
}
