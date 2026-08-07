package se.eelde.toggles.agent.di

import android.content.Context
import android.content.pm.PackageManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import se.eelde.toggles.agent.AgentChangeNotifier
import se.eelde.toggles.agent.AgentDescription
import se.eelde.toggles.agent.AgentUriMatcher
import se.eelde.toggles.agent.CallerAuthorization
import se.eelde.toggles.agent.ContentResolverAgentChangeNotifier
import se.eelde.toggles.database.TogglesDatabase
import se.eelde.toggles.database.dao.agent.AgentDao
import se.eelde.toggles.database.dao.agent.AgentMutationDao
import javax.inject.Singleton
import kotlin.time.Clock

@Module
@InstallIn(SingletonComponent::class)
object TestAgentModule {
    @Singleton
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
    fun provideClock(): Clock = Clock.System

    @Provides
    fun providePackageManager(@ApplicationContext context: Context): PackageManager =
        context.packageManager
}
