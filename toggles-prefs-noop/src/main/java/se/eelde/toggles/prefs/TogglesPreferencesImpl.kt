package se.eelde.toggles.prefs

import android.content.Context

class TogglesPreferencesImpl(context: Context) : TogglesPreferences {

    override fun getBoolean(key: String, defValue: Boolean): Boolean = defValue

    override fun getInt(key: String, defValue: Int): Int = defValue

    override fun getString(key: String, defValue: String): String = defValue

    override fun <T : Enum<T>> getEnum(key: String, type: Class<T>, defValue: T): T = defValue
}
