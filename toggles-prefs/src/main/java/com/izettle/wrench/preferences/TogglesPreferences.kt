package com.izettle.wrench.preferences

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import se.eelde.toggles.core.Bolt
import se.eelde.toggles.core.Nut
import se.eelde.toggles.core.TogglesProviderContract

interface ITogglesPreferences {
    fun <T : Enum<T>> getEnum(key: String, type: Class<T>, defValue: T): T
    fun getString(key: String, defValue: String?): String?
    fun getBoolean(key: String, defValue: Boolean): Boolean
    fun getInt(key: String, defValue: Int): Int
}

class TogglesPreferences(context: Context) : ITogglesPreferences {
    private val contentResolver: ContentResolver = context.contentResolver

    private fun insertNut(contentResolver: ContentResolver, nut: Nut) {
        contentResolver.insert(TogglesProviderContract.nutUri(), nut.toContentValues())
    }

    @Suppress("ReturnCount")
    private fun getBolt(contentResolver: ContentResolver, @Bolt.BoltType boltType: String, key: String): Bolt? {
        val cursor = contentResolver.query(TogglesProviderContract.boltUri(key), null, null, null, null)
        cursor.use {
            if (cursor == null) {
                return null
            }

            if (cursor.moveToFirst()) {
                return Bolt.fromCursor(cursor)
            }
        }

        return Bolt(0, boltType, key, null)
    }

    private fun insertBolt(contentResolver: ContentResolver, bolt: Bolt): Uri? {
        return contentResolver.insert(TogglesProviderContract.boltUri(), bolt.toContentValues())
    }

    override fun <T : Enum<T>> getEnum(key: String, type: Class<T>, defValue: T): T {
        var bolt = getBolt(contentResolver = contentResolver, boltType = Bolt.TYPE.ENUM, key = key)
            ?: return defValue

        if (bolt.id == 0L) {
            bolt = bolt.copy(id = bolt.id, key = key, type = Bolt.TYPE.ENUM, value = defValue.toString())
            val uri = insertBolt(contentResolver, bolt)
            bolt.id = uri!!.lastPathSegment!!.toLong()

            for (enumConstant in type.enumConstants!!) {
                insertNut(contentResolver = contentResolver, nut = Nut(configurationId = bolt.id, value = enumConstant.toString()))
            }
        }

        return java.lang.Enum.valueOf(type, bolt.value!!)
    }

    override fun getString(key: String, defValue: String?): String? {

        var bolt = getBolt(contentResolver = contentResolver, boltType = Bolt.TYPE.STRING, key = key)
            ?: return defValue

        if (bolt.id == 0L) {
            bolt = bolt.copy(id = bolt.id, key = key, type = Bolt.TYPE.STRING, value = defValue)
            insertBolt(contentResolver, bolt)
        }

        return bolt.value
    }

    override fun getBoolean(key: String, defValue: Boolean): Boolean {
        var bolt = getBolt(contentResolver = contentResolver, boltType = Bolt.TYPE.BOOLEAN, key = key)
            ?: return defValue

        if (bolt.id == 0L) {
            bolt = bolt.copy(id = bolt.id, key = key, type = Bolt.TYPE.BOOLEAN, value = defValue.toString())
            insertBolt(contentResolver, bolt)
        }

        return bolt.value!!.toBoolean()
    }

    override fun getInt(key: String, defValue: Int): Int {
        var bolt = getBolt(contentResolver = contentResolver, boltType = Bolt.TYPE.INTEGER, key = key)
            ?: return defValue

        if (bolt.id == 0L) {
            bolt = bolt.copy(id = bolt.id, key = key, type = Bolt.TYPE.INTEGER, value = defValue.toString())
            insertBolt(contentResolver, bolt)
        }

        return bolt.value!!.toInt()
    }
}
