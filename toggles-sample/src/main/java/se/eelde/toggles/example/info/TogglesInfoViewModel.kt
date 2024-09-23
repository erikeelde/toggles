package se.eelde.toggles.example.info

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import se.eelde.toggles.core.TogglesConfiguration
import se.eelde.toggles.core.TogglesConfigurationValue
import se.eelde.toggles.core.TogglesProviderContract
import javax.inject.Inject

data class SampleConfigurationValue(
    val id: Long,
    val configurationId: Long,
    val value: String?,
    val scope: Long,
)

data class SampleConfiguration(
    val id: Long,
    val type: String,
    val key: String,
    val values: ImmutableList<SampleConfigurationValue>
)

data class InfoViewState(
    val configurations: ImmutableList<SampleConfiguration> = persistentListOf(),
)

@HiltViewModel
class TogglesInfoViewModel @Inject internal constructor(
    application: Application,
) : ViewModel() {

    private val contentResolver = application.contentResolver

    private val _viewState: MutableStateFlow<InfoViewState> = MutableStateFlow(InfoViewState())
    internal val viewState: StateFlow<InfoViewState> = _viewState.asStateFlow()

    init {
        updateViewState()
    }

    private fun updateViewState() {
        viewModelScope.launch {
            val configurations = contentResolver.query(
                TogglesProviderContract.configurationUri(),
                null,
                null,
                null,
                null,
                null
            ).use { cursor ->
                val mutableList = mutableListOf<TogglesConfiguration>()
                while (cursor?.moveToNext() == true) {
                    mutableList.add(TogglesConfiguration.fromCursor(cursor))
                }
                mutableList.toImmutableList()
            }

            val sampleConfigurations = mutableListOf<SampleConfiguration>()
            configurations.forEach { configuration ->
                contentResolver.query(
                    TogglesProviderContract.configurationValueUri(configuration.id),
                    null,
                    null,
                    null,
                    null
                ).use { cursor ->
                    val values: MutableList<SampleConfigurationValue> = mutableListOf()
                    while (cursor?.moveToNext() == true) {
                        TogglesConfigurationValue.fromCursor(cursor).also {
                            values.add(
                                SampleConfigurationValue(
                                    id = it.id,
                                    configurationId = it.configurationId,
                                    value = it.value,
                                    scope = it.scope,
                                )
                            )
                        }
                    }
                    sampleConfigurations.add(
                        SampleConfiguration(
                            id = configuration.id,
                            type = configuration.type,
                            key = configuration.key,
                            values = values.toImmutableList(),
                        )
                    )
                }
            }
            _viewState.update { value ->
                value.copy(configurations = sampleConfigurations.toImmutableList())
            }
        }
    }
}
