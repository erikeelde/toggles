package se.eelde.toggles.provider

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import se.eelde.toggles.core.Toggle
import se.eelde.toggles.core.ToggleValue
import se.eelde.toggles.core.TogglesProviderContract
import se.eelde.toggles.database.DatabaseModule
import se.eelde.toggles.database.TogglesDatabase
import javax.inject.Inject
import javax.inject.Singleton

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@UninstallModules(DatabaseModule::class)
class TogglesProviderPredefinedConfigurationValuesTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)


    private val context = ApplicationProvider.getApplicationContext<Application>()
    private val contentResolver = context.contentResolver

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
        val type = contentResolver.getType(TogglesProviderContract.toggleValueUri())
        assertEquals(
            "vnd.android.cursor.dir/vnd.se.eelde.toggles.predefinedConfigurationValue",
            type
        )
    }

    @Test
    fun testInsertPredefinedConfigurationValueType() = runTest {
        val insertToggleKey = "insertToggleKey"

        val toggleUri = TogglesProviderContract.toggleUri()
        val insertToggle = getToggle(insertToggleKey)
        val insertToggleUri = contentResolver.insert(toggleUri, insertToggle.toContentValues())!!

        val configId = insertToggleUri.lastPathSegment!!.toLong()
        val toggleValue = ToggleValue {
            configurationId = configId
            value = "FIRST"
        }

        contentResolver.insert(
            TogglesProviderContract.toggleValueUri(),
            toggleValue.toContentValues()
        )
        togglesDatabase.togglesPredefinedConfigurationValueDao().getByConfigurationId(configId)
            .test {
                val togglesPredefinedConfigurationValueList = awaitItem()
                assertEquals(1, togglesPredefinedConfigurationValueList.size)
                togglesPredefinedConfigurationValueList[0].let {
                    assertEquals(toggleValue.value, it.value)
                    assertEquals(toggleValue.configurationId, it.configurationId)
                }
            }
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testMissingQueryForToggleValues() {
        contentResolver.update(
            TogglesProviderContract.toggleValueUri(),
            getToggleValue("dummyToggleValue").toContentValues(),
            null,
            null
        )
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testMissingUpdateForToggleValues() {
        contentResolver.update(
            TogglesProviderContract.toggleValueUri(),
            getToggleValue("dummyToggleValue").toContentValues(),
            null,
            null
        )
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testMissingDeleteForToggleValues() {
        contentResolver.delete(TogglesProviderContract.toggleValueUri(), null, null)
    }

    private fun getToggleValue(value: String): ToggleValue {
        return ToggleValue {
            id = 0
            this.value = value
        }
    }

    private fun getToggle(key: String): Toggle {
        return Toggle {
            id = 0L
            type = "toggletype"
            this.key = key
            value = "togglevalue"
        }
    }
}
