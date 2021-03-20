package com.izettle.wrench.preferences

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import se.eelde.toggles.core.Toggle
import se.eelde.toggles.core.ToggleValue
import se.eelde.toggles.core.TogglesProviderContract
import se.eelde.toggles.core.showDownloadNotification

interface ITogglesPreferences {
    fun <T : Enum<T>> getEnum(key: String, type: Class<T>, defValue: T): T
    fun getString(key: String, defValue: String?): String?
    fun getBoolean(key: String, defValue: Boolean): Boolean
    fun getInt(key: String, defValue: Int): Int
}

class TogglesPreferences(context: Context) : ITogglesPreferences {
    private val context = context.applicationContext
    private val contentResolver: ContentResolver = context.contentResolver

    private fun insertToggleValue(contentResolver: ContentResolver, toggleValue: ToggleValue) {
        contentResolver.insert(TogglesProviderContract.toggleValueUri(), toggleValue.toContentValues())
    }

    @Suppress("ReturnCount")
    private fun getToggle(contentResolver: ContentResolver, @Toggle.ToggleType toggleType: String, key: String): Toggle? {
        val cursor = contentResolver.query(TogglesProviderContract.toggleUri(key), null, null, null, null)
        cursor.use {
            if (cursor == null) {
                showDownloadNotification(context = context)
                return null
            }

            if (cursor.moveToFirst()) {
                return Toggle.fromCursor(cursor)
            }
        }

        return Toggle(0, toggleType, key, null)
    }

    private fun insertToggle(contentResolver: ContentResolver, toggle: Toggle): Uri? {
        return contentResolver.insert(TogglesProviderContract.toggleUri(), toggle.toContentValues())
    }

    override fun <T : Enum<T>> getEnum(key: String, type: Class<T>, defValue: T): T {
        var toggle = getToggle(contentResolver = contentResolver, toggleType = Toggle.TYPE.ENUM, key = key)
            ?: return defValue

        if (toggle.id == 0L) {
            toggle = toggle.copy(id = toggle.id, key = key, type = Toggle.TYPE.ENUM, value = defValue.toString())
            val uri = insertToggle(contentResolver, toggle)
            toggle.id = uri!!.lastPathSegment!!.toLong()

            for (enumConstant in type.enumConstants!!) {
                insertToggleValue(contentResolver = contentResolver, toggleValue = ToggleValue(configurationId = toggle.id, value = enumConstant.toString()))
            }
        }

        return java.lang.Enum.valueOf(type, toggle.value!!)
    }

    override fun getString(key: String, defValue: String?): String? {

        var toggle = getToggle(contentResolver = contentResolver, toggleType = Toggle.TYPE.STRING, key = key)
            ?: return defValue

        if (toggle.id == 0L) {
            toggle = toggle.copy(id = toggle.id, key = key, type = Toggle.TYPE.STRING, value = defValue)
            insertToggle(contentResolver, toggle)
        }

        return toggle.value
    }

    override fun getBoolean(key: String, defValue: Boolean): Boolean {
        var toggle = getToggle(contentResolver = contentResolver, toggleType = Toggle.TYPE.BOOLEAN, key = key)
            ?: return defValue

        if (toggle.id == 0L) {
            toggle = toggle.copy(id = toggle.id, key = key, type = Toggle.TYPE.BOOLEAN, value = defValue.toString())
            insertToggle(contentResolver, toggle)
        }

        return toggle.value!!.toBoolean()
    }

    override fun getInt(key: String, defValue: Int): Int {
        var toggle = getToggle(contentResolver = contentResolver, toggleType = Toggle.TYPE.INTEGER, key = key)
            ?: return defValue

        if (toggle.id == 0L) {
            toggle = toggle.copy(id = toggle.id, key = key, type = Toggle.TYPE.INTEGER, value = defValue.toString())
            insertToggle(contentResolver, toggle)
        }

        return toggle.value!!.toInt()
    }
}
