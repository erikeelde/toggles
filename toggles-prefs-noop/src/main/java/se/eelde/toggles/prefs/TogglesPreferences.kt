package se.eelde.toggles.prefs

@Suppress("LibraryEntitiesShouldNotBePublic")
public interface TogglesPreferences {
    public fun getBoolean(key: String, defValue: Boolean): Boolean
    public fun getInt(key: String, defValue: Int): Int
    public fun getString(key: String, defValue: String): String
    public fun <T : Enum<T>> getEnum(key: String, type: Class<T>, defValue: T): T
}
