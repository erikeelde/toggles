package se.eelde.toggles.provider

import android.content.Context
import android.graphics.drawable.ColorDrawable
import androidx.core.graphics.drawable.toDrawable
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

object RobolectricTogglesProvider {
    fun create(
        context: Context,
        database: WrenchDatabase,
        toggles: Toggles
    ): TogglesProvider {
        val contentProviderController =
            Robolectric.buildContentProvider(TogglesProvider::class.java)
                .create("se.eelde.toggles.configprovider")

        val provider = contentProviderController.get()

        val appIcon = 0x00FF00.toDrawable().apply {
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

                    override fun provideTogglesUriMatcher(): TogglesUriMatcher =
                        TogglesUriMatcher("se.eelde.toggles.configprovider")
                }
            }
        }

        return provider
    }
}
