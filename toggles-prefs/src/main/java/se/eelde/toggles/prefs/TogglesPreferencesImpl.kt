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

    override fun getBoolean(key: String, defValue: Boolean): Boolean {
        var toggle = getToggle(
            contentResolver = contentResolver,
            toggleType = Toggle.TYPE.BOOLEAN,
            key = key
        )
            ?: return defValue

        if (toggle.id == 0L) {
            toggle = toggle.copy(value = defValue.toString())
            contentResolver.insert(toggleUri(), toggle.toContentValues())
        }

        return toggle.value!!.toBoolean()
    }

    override fun getInt(key: String, defValue: Int): Int {
        var toggle = getToggle(
            contentResolver = contentResolver,
            toggleType = Toggle.TYPE.INTEGER,
            key = key
        )
            ?: return defValue

        if (toggle.id == 0L) {
            toggle = toggle.copy(value = defValue.toString())
            contentResolver.insert(toggleUri(), toggle.toContentValues())
        }

        return toggle.value!!.toInt()
    }

    override fun getString(key: String, defValue: String): String {
        var toggle =
            getToggle(
                contentResolver = contentResolver,
                toggleType = Toggle.TYPE.STRING,
                key = key
            )
                ?: return defValue

        if (toggle.id == 0L) {
            toggle = toggle.copy(value = defValue)
            contentResolver.insert(toggleUri(), toggle.toContentValues())
        }

        return toggle.value!!
    }

    override fun <T : Enum<T>> getEnum(key: String, type: Class<T>, defValue: T): T {
        var toggle =
            getToggle(
                contentResolver = contentResolver,
                toggleType = Toggle.TYPE.ENUM,
                key = key
            )
                ?: return defValue

        if (toggle.id == 0L) {
            toggle = toggle.copy(value = defValue.toString())
            val uri = contentResolver.insert(toggleUri(), toggle.toContentValues())
            toggle.id = uri!!.lastPathSegment!!.toLong()

            for (enumConstant in type.enumConstants!!) {
                contentResolver.insert(
                    toggleValueUri(),
                    ToggleValue(
                        configurationId = toggle.id,
                        value = enumConstant.toString()
                    )
                        .toContentValues()
                )
            }
        }

        return java.lang.Enum.valueOf(type, toggle.value!!)
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

        return Toggle(0, toggleType, key, null)
    }
}
