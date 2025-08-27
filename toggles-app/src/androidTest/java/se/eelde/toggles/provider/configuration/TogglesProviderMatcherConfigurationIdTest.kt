package se.eelde.toggles.provider.configuration

import android.app.Application
import android.content.Context
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
@UninstallModules(DatabaseModule::class)
class TogglesProviderMatcherConfigurationIdTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private val context = ApplicationProvider.getApplicationContext<Application>()
    private val contentResolver = context.contentResolver

    val togglesConfiguration = TogglesConfiguration {
        type = Toggle.TYPE.BOOLEAN
        key = "myConfigurationkey"
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object TestModule {
        @Singleton
        @Provides
        fun provideTogglesDb(@ApplicationContext context: Context): TogglesDatabase {
            return Room.inMemoryDatabaseBuilder(context, TogglesDatabase::class.java)
                .allowMainThreadQueries().build()
        }
    }

    @Inject
    lateinit var togglesDatabase: TogglesDatabase

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun testGetTypePredefinedConfigurationValue() {
        val type = contentResolver.getType(TogglesProviderContract.configurationUri(0))
        assertEquals("vnd.android.cursor.item/vnd.se.eelde.toggles.configuration", type)
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testInsert() {
        contentResolver.insert(
            TogglesProviderContract.configurationUri(0),
            null
        )
    }

    @Test
    fun testUpdate() {
        val uri = contentResolver.insert(
            TogglesProviderContract.configurationUri(),
            togglesConfiguration.toContentValues(),
        )!!

        val updatedConfiguration =
            togglesConfiguration.copy(key = "newKey", type = Toggle.TYPE.STRING)

        val rowsUpdated = contentResolver.update(
            TogglesProviderContract.configurationUri(uri.lastPathSegment!!),
            updatedConfiguration.toContentValues(),
            null,
            null
        )

        val query = contentResolver.query(uri, null, null, null, null)!!
        assertTrue(query.moveToFirst())
        val fromCursor = TogglesConfiguration.fromCursor(query)

        assertEquals(1, rowsUpdated)
        assertEquals(1, fromCursor.id)
    }

    @Test
    fun testQuery() {
        val uri = contentResolver.insert(
            TogglesProviderContract.configurationUri(),
            togglesConfiguration.toContentValues(),
        )!!

        val configurationUri = TogglesProviderContract.configurationUri(uri.lastPathSegment!!)

        val cursor = contentResolver.query(configurationUri, null, null, null, null)!!
        assertTrue(cursor.moveToFirst())
        TogglesConfiguration.fromCursor(cursor).also { cursorConfiguration ->
            assertEquals(togglesConfiguration.key, cursorConfiguration.key)
            assertEquals(togglesConfiguration.type, cursorConfiguration.type)
        }
    }

    @Test
    fun testDelete() {
        val uri = contentResolver.insert(
            TogglesProviderContract.configurationUri(),
            togglesConfiguration.toContentValues(),
        )!!

        val rowsDeleted = contentResolver.delete(
            TogglesProviderContract.configurationUri(uri.lastPathSegment!!),
            null,
            null
        )
        assertEquals(1, rowsDeleted)
    }
}
