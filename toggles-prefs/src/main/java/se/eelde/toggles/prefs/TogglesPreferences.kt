package se.eelde.toggles.prefs

@Suppress("LibraryEntitiesShouldNotBePublic")
public interface TogglesPreferences {
    public fun getBoolean(key: String, defaultValue: Boolean): Boolean
    public fun getInt(key: String, defaultValue: Int): Int
    public fun getString(key: String, defaultValue: String): String
    public fun <T : Enum<T>> getEnum(key: String, type: Class<T>, defaultValue: T): T
}
