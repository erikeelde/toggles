package se.eelde.toggles.provider

import android.content.Context
import android.graphics.drawable.ColorDrawable
import androidx.room.Room
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import se.eelde.toggles.database.WrenchDatabase
import se.eelde.toggles.database.dao.provider.ProviderApplicationDao
import se.eelde.toggles.database.dao.provider.ProviderConfigurationDao
import se.eelde.toggles.database.dao.provider.ProviderConfigurationValueDao
import se.eelde.toggles.database.dao.provider.ProviderPredefinedConfigurationValueDao
import se.eelde.toggles.database.dao.provider.ProviderScopeDao
import se.eelde.toggles.flow.Toggles
import se.eelde.toggles.flow.TogglesImpl

object RobolectricTogglesProvider {
    fun create(context: Context): TogglesProvider {
        val database = Room.inMemoryDatabaseBuilder(context, WrenchDatabase::class.java)
            .allowMainThreadQueries().build()

        val contentProviderController =
            Robolectric.buildContentProvider(TogglesProvider::class.java)
                .create("se.eelde.toggles.configprovider")

        val provider = contentProviderController.get()

        val appIcon = ColorDrawable(0x00FF00).apply {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
        }
        shadowOf(context.packageManager).setApplicationIcon(
            context.applicationInfo.packageName,
            appIcon
        )

        provider.entryPointBuilder = object : TogglesProvider.EntryPointBuilder {
            override fun build(context: Context): TogglesProvider.TogglesProviderEntryPoint {
                return object : TogglesProvider.TogglesProviderEntryPoint {
                    override fun provideProviderApplicationDao(): ProviderApplicationDao =
                        database.providerApplicationDao()

                    override fun provideProviderConfigurationDao(): ProviderConfigurationDao =
                        database.providerConfigurationDao()

                    override fun provideProviderConfigurationValueDao(): ProviderConfigurationValueDao =
                        database.providerConfigurationValueDao()

                    override fun provideProviderScopeDao(): ProviderScopeDao =
                        database.providerScopeDao()

                    override fun providePredefinedConfigurationValueDao(): ProviderPredefinedConfigurationValueDao =
                        database.providerPredefinedConfigurationValueDao()

                    override fun providePackageManagerWrapper(): IPackageManagerWrapper {
                        return object : IPackageManagerWrapper {
                            override val applicationLabel: String
                                get() = "Test"

                            override val callingApplicationPackageName: String?
                                get() = "Test"
                        }
                    }

                    override fun provideToggles(): Toggles {
                        return object : Toggles {
                            override fun toggle(key: String, defaultValue: Boolean): Flow<Boolean> {
                                return flowOf(defaultValue)
                            }

                            override fun toggle(key: String, defaultValue: String): Flow<String> {
                                TODO("Not yet implemented")
                            }

                            override fun toggle(key: String, defaultValue: Int): Flow<Int> {
                                TODO("Not yet implemented")
                            }

                            override fun <T : Enum<T>> toggle(
                                key: String,
                                type: Class<T>,
                                defaultValue: T
                            ): Flow<T> {
                                TODO("Not yet implemented")
                            }
                        }
                    }

                    override fun provideTogglesUriMatcher(): TogglesUriMatcher =
                        TogglesUriMatcher("se.eelde.toggles.configprovider")
                }
            }
        }

        return provider
    }
}
