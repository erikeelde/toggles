@file:Suppress("MaxLineLength")

package se.eelde.toggles.database.dao.application

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import se.eelde.toggles.database.WrenchPredefinedConfigurationValue
import se.eelde.toggles.database.tables.PredefinedConfigurationValueTable

@Dao
interface TogglesPredefinedConfigurationValueDao {

    @Query(
        """SELECT * FROM ${PredefinedConfigurationValueTable.TABLE_NAME} WHERE ${PredefinedConfigurationValueTable.COL_CONFIG_ID} = (:configurationId)"""
    )
    fun getByConfigurationId(configurationId: Long): Flow<List<WrenchPredefinedConfigurationValue>>

    @Query(
        """SELECT * FROM ${PredefinedConfigurationValueTable.TABLE_NAME} WHERE ${PredefinedConfigurationValueTable.COL_CONFIG_ID} = (:configurationId) AND ${PredefinedConfigurationValueTable.COL_VALUE} = (:value) """
    )
    fun getByConfigurationAndValueId(
        configurationId: Long,
        value: String
    ): WrenchPredefinedConfigurationValue

    @Insert
    fun insert(fullConfig: WrenchPredefinedConfigurationValue): Long
}
