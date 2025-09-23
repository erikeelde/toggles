package se.eelde.toggles.provider.configuration

import android.app.Application
import android.content.Context
import android.database.Cursor
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import se.eelde.toggles.core.Toggle
import se.eelde.toggles.core.TogglesConfiguration
import se.eelde.toggles.core.TogglesProviderContract
import se.eelde.toggles.database.DatabaseModule
import se.eelde.toggles.database.TogglesDatabase
import javax.inject.Inject
import javax.inject.Singleton

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class TogglesProviderMatcherConfigurationTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private val context = ApplicationProvider.getApplicationContext<Application>()
    private val contentResolver = context.contentResolver

    @Inject
    lateinit var togglesDatabase: TogglesDatabase

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun testGetTypePredefinedConfigurationValue() {
        val type = contentResolver.getType(TogglesProviderContract.configurationUri())
        assertEquals("vnd.android.cursor.dir/vnd.se.eelde.toggles.configuration", type)
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testUpdate() {
        contentResolver.update(
            TogglesProviderContract.configurationUri(),
            null,
            null,
            null
        )
    }

    @Test
    fun testInsert() {
        val togglesConfiguration = TogglesConfiguration {
            type = Toggle.TYPE.BOOLEAN
            key = "${this@TogglesProviderMatcherConfigurationTest::class.simpleName}Insertkey"
        }

        val uri = contentResolver.insert(
            TogglesProviderContract.configurationUri(),
            togglesConfiguration.toContentValues(),
        )!!

        assertEquals("content", uri.scheme)
        assertEquals("se.eelde.toggles.configprovider", uri.host)
        assertEquals("configuration", uri.pathSegments[0])
        assertTrue("Invalid id in path: ${uri.pathSegments[1]}", uri.pathSegments[1].toLong() > 0)
        assertEquals("1", uri.getQueryParameter("API_VERSION"))
    }

    @Test
    fun testQuery() {
        val togglesConfiguration = TogglesConfiguration {
            type = Toggle.TYPE.BOOLEAN
            key = "${this@TogglesProviderMatcherConfigurationTest::class.simpleName}Query"
        }

        contentResolver.insert(
            TogglesProviderContract.configurationUri(),
            togglesConfiguration.toContentValues(),
        )

        contentResolver.query(
            TogglesProviderContract.configurationUri(),
            null,
            null,
            null,
            null
        )!!.use { cursor ->
            val configurations =
                cursor.mapRows { cursor -> TogglesConfiguration.fromCursor(cursor) }

            assertTrue(
                configurations.any {
                    it.key == togglesConfiguration.key && it.type == togglesConfiguration.type
                })
        }
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testDelete() {
        contentResolver.delete(
            TogglesProviderContract.configurationUri(),
            null,
            null,
        )
    }
}

fun <T> Cursor.mapRows(converter: (Cursor) -> T): List<T> {
    moveToFirst()
    val objects = mutableListOf<T>()
    do {
        objects.add(converter(this))
    } while (moveToNext())

    return objects.toList()
}
