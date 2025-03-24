package se.eelde.toggles.prefs

import android.content.ContentResolver
import android.content.Context
import se.eelde.toggles.core.Toggle
import se.eelde.toggles.core.ToggleValue
import se.eelde.toggles.core.TogglesProviderContract.toggleUri
import se.eelde.toggles.core.TogglesProviderContract.toggleValueUri

@Suppress("LibraryEntitiesShouldNotBePublic")
public class TogglesPreferencesImpl(context: Context) : TogglesPreferences {
    private val context = context.applicationContext
    private val contentResolver: ContentResolver = this.context.contentResolver

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        val toggle = getToggle(
            contentResolver = contentResolver,
            toggleType = Toggle.TYPE.BOOLEAN,
            key = key
        )

        return if (toggle == null) {
            defaultValue
        } else if (toggle.id == 0L) {
            contentResolver.insert(
                toggleUri(),
                toggle.copy(value = defaultValue.toString()).toContentValues()
            )
            defaultValue
        } else {
            when (val value = toggle.value) {
                null -> defaultValue
                else -> value.toBoolean()
            }
        }
    }

    override fun getInt(key: String, defaultValue: Int): Int {
        val toggle = getToggle(
            contentResolver = contentResolver,
            toggleType = Toggle.TYPE.INTEGER,
            key = key
        )

        return if (toggle == null) {
            defaultValue
        } else if (toggle.id == 0L) {
            contentResolver.insert(
                toggleUri(),
                toggle.copy(value = defaultValue.toString()).toContentValues()
            )
            defaultValue
        } else {
            when (val value = toggle.value) {
                null -> defaultValue
                else -> value.toInt()
            }
        }
    }

    override fun getString(key: String, defaultValue: String): String {
        val toggle =
            getToggle(
                contentResolver = contentResolver,
                toggleType = Toggle.TYPE.STRING,
                key = key
            )

        return if (toggle == null) {
            defaultValue
        } else if (toggle.id == 0L) {
            contentResolver.insert(
                toggleUri(),
                toggle.copy(value = defaultValue).toContentValues()
            )
            defaultValue
        } else {
            when (val value = toggle.value) {
                null -> defaultValue
                else -> value
            }
        }
    }

    override fun <T : Enum<T>> getEnum(key: String, type: Class<T>, defaultValue: T): T {
        val toggle =
            getToggle(
                contentResolver = contentResolver,
                toggleType = Toggle.TYPE.ENUM,
                key = key
            )

        return if (toggle == null) {
            defaultValue
        } else if (toggle.id == 0L) {
            val uri = contentResolver.insert(
                toggleUri(),
                toggle.copy(value = defaultValue.toString()).toContentValues()
            )
            val configurationId = uri?.lastPathSegment?.toLong() ?: return defaultValue
            for (enumConstant in type.enumConstants!!) {
                contentResolver.insert(
                    toggleValueUri(),
                    ToggleValue {
                        this.configurationId = configurationId
                        value = enumConstant.toString()
                    }.toContentValues()
                )
            }
            defaultValue
        } else {
            when (val value = toggle.value) {
                null -> defaultValue
                else -> java.lang.Enum.valueOf(type, value)
            }
        }
    }

    @Suppress("ReturnCount")
    private fun getToggle(
        contentResolver: ContentResolver,
        @Toggle.ToggleType toggleType: String,
        key: String
    ): Toggle? {
        val cursor = contentResolver.query(toggleUri(key), null, null, null, null)
        cursor.use {
            if (cursor == null) {
                return null
            }

            if (cursor.moveToFirst()) {
                return Toggle.fromCursor(cursor)
            }
        }
        return Toggle {
            id = 0
            type = toggleType
            this.key = key
            value = null
        }
    }
}
