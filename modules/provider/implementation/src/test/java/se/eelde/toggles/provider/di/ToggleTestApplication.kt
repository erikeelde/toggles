package se.eelde.toggles.provider.di

import android.app.Application
import dagger.hilt.android.testing.CustomTestApplication

@CustomTestApplication(Application::class)
interface ToggleTestApplication
