package se.eelde.toggles.agent.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import se.eelde.toggles.agent.AgentDescription
import se.eelde.toggles.agent.AgentUriMatcher
import se.eelde.toggles.agent.CallerAuthorization
import se.eelde.toggles.database.TogglesDatabase
import se.eelde.toggles.database.dao.agent.AgentDao
import javax.inject.Singleton

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
}
