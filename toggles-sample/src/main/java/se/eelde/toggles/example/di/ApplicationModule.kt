package se.eelde.toggles.example.di

import android.app.Application
import android.util.Log
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import se.eelde.toggles.core.ToggleState
import se.eelde.toggles.flow.Toggles
import se.eelde.toggles.flow.TogglesImpl

@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {
    @Provides
    @Suppress("InjectDispatcher")
    fun provideIoDispatcher() = Dispatchers.IO

    @Provides
    fun provideTogglesFlow(
        application: Application,
        ioDispatcher: CoroutineDispatcher
    ): Toggles =
        TogglesImpl(
            context = application,
            ioDispatcher = ioDispatcher,
            addDefaultAutomatically = true,
            updateDefaultAutomatically = true,
            onMissingToggle = { key: String,
                                defaultValue: String,
                                toggleState: ToggleState ->
                Log.w("TogglesSample", "Missing toggle for '$key': $defaultValue\n$toggleState")
            },
            onDefaultMismatch = { key: String,
                                  storedDefault: String,
                                  requestedDefault: String,
                                  toggleState: ToggleState ->
                Log.w(
                    "TogglesSample",
                    "Default mismatch for '$key': stored=$storedDefault, requested=$requestedDefault\n$toggleState"
                )
            }
        )
}
