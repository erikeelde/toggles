@file:Suppress("MaximumLineLength")

package se.eelde.toggles.database

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase.CONFLICT_FAIL
import androidx.sqlite.db.SupportSQLiteDatabase
import org.junit.Assert.assertNotNull
import se.eelde.toggles.database.tables.ApplicationTable
import se.eelde.toggles.database.tables.ConfigurationTable
import se.eelde.toggles.database.tables.ConfigurationValueTable
import se.eelde.toggles.database.tables.PredefinedConfigurationValueTable
import se.eelde.toggles.database.tables.ScopeTable
import java.util.Date

object DatabaseHelper {
    fun insertPredefinedConfigurationValue(
        db: SupportSQLiteDatabase,
        configurationId: Long,
        value: String,
    ): Long {
        val configurationValues = ContentValues()
        configurationValues.put(PredefinedConfigurationValueTable.COL_CONFIG_ID, configurationId)
        configurationValues.put(PredefinedConfigurationValueTable.COL_VALUE, value)
        configurationValues.put(PredefinedConfigurationValueTable.COL_VALUE, value)
        return db.insert(
            PredefinedConfigurationValueTable.TABLE_NAME,
            CONFLICT_FAIL,
            configurationValues
        )
    }

    fun getPredefinedConfigurationValueByConfigurationId(
        db: SupportSQLiteDatabase,
        configId: Long
    ): List<TogglesPredefinedConfigurationValue> {
        @Suppress("MaxLineLength")
        val query = db.query(
            "SELECT * FROM ${PredefinedConfigurationValueTable.TABLE_NAME} WHERE ${PredefinedConfigurationValueTable.COL_CONFIG_ID} = ?",
            arrayOf<Any>(configId)
        )
        assertNotNull(query)

        val values = mutableListOf<TogglesPredefinedConfigurationValue>()
        while (query.moveToNext()) {
            val id =
                query.getLong(query.getColumnIndexOrThrow(PredefinedConfigurationValueTable.COL_ID))
            val configurationId =
                query.getLong(query.getColumnIndexOrThrow(PredefinedConfigurationValueTable.COL_CONFIG_ID))
            val value =
                query.getString(query.getColumnIndexOrThrow(PredefinedConfigurationValueTable.COL_VALUE))
            values.add(TogglesPredefinedConfigurationValue(id, configurationId, value))
        }

        return values.toList()
    }

    fun insertConfigurationPre3(
        db: SupportSQLiteDatabase,
        applicationId: Long,
        key: String,
        type: String
    ): Long {
        val configurationValues = ContentValues()
        configurationValues.put(ConfigurationTable.COL_APP_ID, applicationId)
        configurationValues.put(ConfigurationTable.COL_KEY, key)
        configurationValues.put(ConfigurationTable.COL_TYPE, type)
        return db.insert(ConfigurationTable.TABLE_NAME, CONFLICT_FAIL, configurationValues)
    }

    fun insertConfiguration(
        db: SupportSQLiteDatabase,
        applicationId: Long,
        key: String,
        type: String,
        lastUse: Long
    ): Long {
        val configurationValues = ContentValues()
        configurationValues.put(ConfigurationTable.COL_APP_ID, applicationId)
        configurationValues.put(ConfigurationTable.COL_KEY, key)
        configurationValues.put(ConfigurationTable.COL_TYPE, type)
        configurationValues.put("lastUse", lastUse)
        return db.insert(ConfigurationTable.TABLE_NAME, CONFLICT_FAIL, configurationValues)
    }

    fun getConfigurationByKey(db: SupportSQLiteDatabase, key: String): Cursor {
        val query = db.query(
            "SELECT * FROM " + ConfigurationTable.TABLE_NAME + " WHERE " + ConfigurationTable.COL_KEY + "=?",
            arrayOf<Any>(key)
        )
        assertNotNull(query)
        return query
    }

    fun insertApplicationPre4(
        db: SupportSQLiteDatabase,
        applicationLabel: String,
        packageName: String
    ): Long {
        val applicationValues = ContentValues()
        applicationValues.put(ApplicationTable.COL_APP_LABEL, applicationLabel)
        applicationValues.put(ApplicationTable.COL_PACK_NAME, packageName)
        return db.insert(ApplicationTable.TABLE_NAME, CONFLICT_FAIL, applicationValues)
    }

    fun insertApplication(
        db: SupportSQLiteDatabase,
        applicationLabel: String,
        packageName: String,
        shortcutId: String,
    ): Long {
        val applicationValues = ContentValues()
        applicationValues.put(ApplicationTable.COL_APP_LABEL, applicationLabel)
        applicationValues.put(ApplicationTable.COL_PACK_NAME, packageName)
        applicationValues.put(ApplicationTable.COL_SHORTCUT_ID, shortcutId)
        return db.insert(ApplicationTable.TABLE_NAME, CONFLICT_FAIL, applicationValues)
    }

    fun getApplication(db: SupportSQLiteDatabase, applicationId: Long): TogglesApplication {
        val cursor = db.query(
            "SELECT * FROM " + ApplicationTable.TABLE_NAME + " WHERE " + ApplicationTable.COL_ID + "=?",
            arrayOf<Any>(applicationId)
        )

        cursor.moveToFirst()
        val id = cursor.getLong(cursor.getColumnIndex(ApplicationTable.COL_ID))
        val label = cursor.getString(cursor.getColumnIndex(ApplicationTable.COL_APP_LABEL))
        val packageName = cursor.getString(cursor.getColumnIndex(ApplicationTable.COL_PACK_NAME))
        val shortcutId = cursor.getString(cursor.getColumnIndex(ApplicationTable.COL_SHORTCUT_ID))

        return TogglesApplication(id, shortcutId, packageName, label)
    }

    fun insertScope(db: SupportSQLiteDatabase, applicationId: Long, scopeName: String): Long {
        val configurationValues = ContentValues()
        configurationValues.put(ScopeTable.COL_APP_ID, applicationId)
        configurationValues.put(ScopeTable.COL_NAME, scopeName)
        configurationValues.put(ScopeTable.COL_SELECTED_TIMESTAMP, 6457)
        return db.insert(
            ScopeTable.TABLE_NAME,
            CONFLICT_FAIL,
            configurationValues
        )
    }

    fun getScope(db: SupportSQLiteDatabase, scopeId: Long): TogglesScope {
        val cursor = db.query(
            "SELECT * FROM " + ScopeTable.TABLE_NAME + " WHERE " + ScopeTable.COL_ID + "=?",
            arrayOf<Any>(scopeId)
        )

        cursor.moveToFirst()
        val id = cursor.getLong(cursor.getColumnIndex(ScopeTable.COL_ID))
        val name = cursor.getString(cursor.getColumnIndex(ScopeTable.COL_NAME))
        val timeStamp = cursor.getLong(cursor.getColumnIndex(ScopeTable.COL_SELECTED_TIMESTAMP))

        return TogglesScope(id, 1, name, Date(timeStamp))
    }

    fun insertConfiguration(
        db: SupportSQLiteDatabase,
        applicationId: Long,
        key: String,
        type: String
    ): Long {
        val configurationValues = ContentValues()
        configurationValues.put(ConfigurationTable.COL_APP_ID, applicationId)
        configurationValues.put(ConfigurationTable.COL_KEY, key)
        configurationValues.put(ConfigurationTable.COL_TYPE, type)
        return db.insert(ConfigurationTable.TABLE_NAME, CONFLICT_FAIL, configurationValues)
    }

    fun insertConfigurationValue(
        db: SupportSQLiteDatabase,
        configurationId: Long,
        value: String,
        scopeId: Long
    ): Long {
        val configurationValues = ContentValues()
        configurationValues.put(ConfigurationValueTable.COL_CONFIG_ID, configurationId)
        configurationValues.put(ConfigurationValueTable.COL_VALUE, value)
        configurationValues.put(ConfigurationValueTable.COL_SCOPE, scopeId)
        return db.insert(ConfigurationValueTable.TABLE_NAME, CONFLICT_FAIL, configurationValues)
    }

    fun getConfigurationValues(
        db: SupportSQLiteDatabase,
        configurationId: Long,
        scopeId: Long
    ): List<TogglesConfigurationValue> {
        val query = db.query(
            "SELECT * FROM ${ConfigurationValueTable.TABLE_NAME} WHERE ${ConfigurationValueTable.COL_CONFIG_ID} = ? AND ${ConfigurationValueTable.COL_SCOPE} = ?",
            arrayOf<Any>(configurationId, scopeId)
        )
        assertNotNull(query)

        val values = mutableListOf<TogglesConfigurationValue>()
        while (query.moveToNext()) {
            val id = query.getLong(query.getColumnIndexOrThrow(ConfigurationValueTable.COL_ID))
            val configId =
                query.getLong(query.getColumnIndexOrThrow(ConfigurationValueTable.COL_CONFIG_ID))
            val value =
                query.getString(query.getColumnIndexOrThrow(ConfigurationValueTable.COL_VALUE))
            val scope = query.getLong(query.getColumnIndexOrThrow(ConfigurationValueTable.COL_SCOPE))
            values.add(TogglesConfigurationValue(id, configId, value, scope))
        }

        return values.toList()
    }
}
