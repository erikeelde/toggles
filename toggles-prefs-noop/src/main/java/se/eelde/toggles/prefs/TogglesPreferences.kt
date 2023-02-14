package se.eelde.toggles.prefs

@Suppress("LibraryEntitiesShouldNotBePublic")
interface TogglesPreferences {
    fun getBoolean(key: String, defValue: Boolean): Boolean
    fun getInt(key: String, defValue: Int): Int
    fun getString(key: String, defValue: String): String
    fun <T : Enum<T>> getEnum(key: String, type: Class<T>, defValue: T): T
}
