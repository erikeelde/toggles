package se.eelde.toggles.configurations

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import se.eelde.toggles.database.WrenchApplication
import se.eelde.toggles.database.WrenchConfigurationWithValues
import se.eelde.toggles.database.WrenchScope
import se.eelde.toggles.database.dao.application.TogglesApplicationDao
import se.eelde.toggles.database.dao.application.TogglesConfigurationDao
import se.eelde.toggles.database.dao.application.TogglesScopeDao

internal data class ViewState(
    val application: WrenchApplication? = null,
    val configurations: List<WrenchConfigurationWithValues> = listOf(),
    val defaultScope: WrenchScope? = null,
    val selectedScope: WrenchScope? = null,
)

internal sealed class PartialViewState {
    object Empty : PartialViewState()
    data class Configurations(val configurations: List<WrenchConfigurationWithValues>) :
        PartialViewState()

    data class SelectedScope(val scope: WrenchScope) : PartialViewState()
    data class DefaultScope(val scope: WrenchScope) : PartialViewState()
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel(assistedFactory = ConfigurationViewModel.Factory::class)
@Suppress("StaticFieldLeak")
class ConfigurationViewModel @AssistedInject internal constructor(
    @ApplicationContext private val context: Context,
    private val applicationDao: TogglesApplicationDao,
    configurationDao: TogglesConfigurationDao,
    scopeDao: TogglesScopeDao,
    val savedStateHandle: SavedStateHandle,
    @Assisted val applicationId: Long,
) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(applicationId: Long): ConfigurationViewModel
    }

    private val _state = MutableStateFlow(reduce(ViewState(), PartialViewState.Empty))

    internal val state: StateFlow<ViewState>
        get() = _state

    private val queryString: MutableStateFlow<String> = MutableStateFlow("")

    init {
        setQuery(savedStateHandle.get<String>("query") ?: "")

        viewModelScope.launch {
            queryString.value = (savedStateHandle.get<String>("query") ?: "")
        }

        viewModelScope.launch {
            combine(
                flowOf(applicationDao.getApplication(applicationId)!!),
                scopeDao.getSelectedScopeFlow(applicationId = applicationId),
                scopeDao.getDefaultScopeFlow(applicationId = applicationId),
            ) { application, selectedScope, defaultScope ->
                ViewState(
                    application = application,
                    defaultScope = defaultScope,
                    selectedScope = selectedScope
                )
            }.collect { newState ->
                _state.update {
                    it.copy(
                        application = newState.application,
                        selectedScope = newState.selectedScope,
                        defaultScope = newState.defaultScope
                    )
                }
            }
        }

        viewModelScope.launch {
            queryString.flatMapLatest { queryString ->
                if (TextUtils.isEmpty(queryString)) {
                    configurationDao.getApplicationConfigurations(applicationId)
                } else {
                    configurationDao.getApplicationConfigurations(applicationId, "%$queryString%")
                }
            }.collect { value ->
                _state.value = reduce(_state.value, PartialViewState.Configurations(value))
            }
        }
    }

    private fun reduce(viewState: ViewState, partialViewState: PartialViewState): ViewState {
        return when (partialViewState) {
            is PartialViewState.Configurations -> viewState.copy(
                configurations = partialViewState.configurations
            )

            PartialViewState.Empty -> viewState
            is PartialViewState.DefaultScope -> viewState.copy(defaultScope = partialViewState.scope)
            is PartialViewState.SelectedScope -> viewState.copy(selectedScope = partialViewState.scope)
        }
    }

    fun getQuery(): StateFlow<String> = queryString

    fun setQuery(query: String) {
        queryString.value = query
        savedStateHandle["query"] = query
    }

    internal fun deleteApplication(wrenchApplication: WrenchApplication) {
        viewModelScope.launch {
            applicationDao.delete(wrenchApplication)
        }
    }

    fun restartApplication(wrenchApplication: WrenchApplication) {
        val activityManager =
            context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        activityManager.killBackgroundProcesses(wrenchApplication.packageName)

        val intent =
            context.packageManager.getLaunchIntentForPackage(wrenchApplication.packageName)
        if (intent != null) {
            context.startActivity(Intent.makeRestartActivityTask(intent.component))
        }
    }
}
