package se.eelde.toggles.agent.di

import android.app.Application
import dagger.hilt.android.testing.CustomTestApplication

@CustomTestApplication(Application::class)
interface AgentTestApplication
