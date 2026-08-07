package se.eelde.toggles.agent

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import se.eelde.toggles.database.TogglesDatabase
import se.eelde.toggles.database.dao.agent.AgentDao

@Module
@InstallIn(SingletonComponent::class)
object AgentModule {
    @Provides
    fun provideAgentUriMatcher() = AgentUriMatcher(AgentDescription.AGENT_AUTHORITY)

    @Provides
    fun provideCallerAuthorization() = CallerAuthorization()

    @Provides
    fun provideAgentDao(togglesDatabase: TogglesDatabase): AgentDao = togglesDatabase.agentDao()
}
