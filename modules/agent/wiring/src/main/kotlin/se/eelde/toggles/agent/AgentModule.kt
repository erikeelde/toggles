package se.eelde.toggles.agent

import android.content.Context
import android.content.pm.PackageManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import se.eelde.toggles.database.TogglesDatabase
import se.eelde.toggles.database.dao.agent.AgentDao
import se.eelde.toggles.database.dao.agent.AgentMutationDao

@Module
@InstallIn(SingletonComponent::class)
object AgentModule {
    @Provides
    fun provideAgentUriMatcher() = AgentUriMatcher(AgentDescription.AGENT_AUTHORITY)

    @Provides
    fun provideCallerAuthorization() = CallerAuthorization()

    @Provides
    fun provideAgentDao(togglesDatabase: TogglesDatabase): AgentDao = togglesDatabase.agentDao()

    @Provides
    fun provideAgentMutationDao(togglesDatabase: TogglesDatabase): AgentMutationDao =
        togglesDatabase.agentMutationDao()

    @Provides
    fun provideAgentChangeNotifier(@ApplicationContext context: Context): AgentChangeNotifier =
        ContentResolverAgentChangeNotifier(context.contentResolver)

    @Provides
    fun providePackageManager(@ApplicationContext context: Context): PackageManager =
        context.packageManager

    // Clock is deliberately NOT provided here: toggles-app's ClockModule already binds
    // kotlin.time.Clock into this same SingletonComponent, and both AgentModule and ClockModule
    // are installed together in the real app. A second binding here would be a duplicate-binding
    // compile error. This mirrors ProviderModule, which provides Clock in TestProviderModule only
    // and relies on the app's ClockModule in production.
}
