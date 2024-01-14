package se.eelde.toggles.provider

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import se.eelde.toggles.BuildConfig
import se.eelde.toggles.R
import se.eelde.toggles.core.TogglesProviderContract
import se.eelde.toggles.database.WrenchDatabase
import se.eelde.toggles.di.DatabaseModule
import javax.inject.Inject
import javax.inject.Singleton

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@UninstallModules(DatabaseModule::class)
@Config(application = HiltTestApplication::class, sdk = [Build.VERSION_CODES.P])
class TogglesProviderMatcherConfigurationKeyTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var togglesProvider: TogglesProvider

    @Module
    @InstallIn(SingletonComponent::class)
    object TestModule {
        @Singleton
        @Provides
        fun provideWrenchDb(@ApplicationContext context: Context): WrenchDatabase {
            return Room.inMemoryDatabaseBuilder(context, WrenchDatabase::class.java)
                .allowMainThreadQueries().build()
        }
    }

    @Inject
    lateinit var wrenchDatabase: WrenchDatabase

    @Before
    fun setUp() {
        hiltRule.inject()

        val contentProviderController =
            Robolectric.buildContentProvider(TogglesProvider::class.java)
                .create(BuildConfig.CONFIG_AUTHORITY)
        togglesProvider = contentProviderController.get()

        val context = ApplicationProvider.getApplicationContext<Application>()
        val appIcon = ContextCompat.getDrawable(context, R.drawable.ic_launcher_foreground)
        shadowOf(context.packageManager).setApplicationIcon(
            context.applicationInfo.packageName,
            appIcon
        )
    }

    @Test
    fun testGetTypePredefinedConfigurationValue() {
        val type = togglesProvider.getType(TogglesProviderContract.configurationUri("fakeKey"))
        assertEquals("vnd.android.cursor.item/vnd.se.eelde.toggles.configuration", type)
    }

    @Test
    fun testInsert() {
        TODO("To be implemented")
    }

    @Test
    fun testUpdate() {
        TODO("To be implemented")
    }

    @Test
    fun testQuery() {
        TODO("To be implemented")
    }

    @Test
    fun testDelete() {
        TODO("To be implemented")
    }
}
