package com.izettle.wrench.provider

import com.izettle.wrench.preferences.ITogglesPreferences

class FakeTogglesPreferences : ITogglesPreferences {
    override fun <T : Enum<T>> getEnum(key: String, type: Class<T>, defValue: T): T = defValue

    override fun getString(key: String, defValue: String?): String? = defValue

    override fun getBoolean(key: String, defValue: Boolean): Boolean = defValue

    override fun getInt(key: String, defValue: Int): Int = defValue
}