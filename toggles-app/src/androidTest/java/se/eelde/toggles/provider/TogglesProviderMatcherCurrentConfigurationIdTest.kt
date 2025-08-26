package se.eelde.toggles.provider

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
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import se.eelde.toggles.core.Toggle
import se.eelde.toggles.core.TogglesProviderContract
import se.eelde.toggles.database.DatabaseModule
import se.eelde.toggles.database.TogglesDatabase
import javax.inject.Inject
import javax.inject.Singleton

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@UninstallModules(DatabaseModule::class)
class TogglesProviderMatcherCurrentConfigurationIdTest {
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
        val type = contentResolver.getType(TogglesProviderContract.toggleUri(0))
        assertEquals("vnd.android.cursor.item/vnd.se.eelde.toggles.currentConfiguration", type)
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testMissingInsertForToggleWithId() {
        contentResolver.insert(
            TogglesProviderContract.toggleUri(0),
            getToggle("dummyToggle").toContentValues()
        )
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testMissingDeleteForToggleWithId() {
        contentResolver.delete(TogglesProviderContract.toggleUri(0), null, null)
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
