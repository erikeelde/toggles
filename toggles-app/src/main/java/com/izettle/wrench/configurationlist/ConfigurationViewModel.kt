package com.izettle.wrench.configurationlist

import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.izettle.wrench.database.WrenchApplication
import com.izettle.wrench.database.WrenchApplicationDao
import com.izettle.wrench.database.WrenchConfigurationDao
import com.izettle.wrench.database.WrenchConfigurationWithValues
import com.izettle.wrench.database.WrenchScope
import com.izettle.wrench.database.WrenchScopeDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

internal data class ViewState(
    val configurations: List<WrenchConfigurationWithValues> = listOf(),
)

internal sealed class PartialViewState {
    object Empty : PartialViewState()
    class Configurations(val configurations: List<WrenchConfigurationWithValues>) :
        PartialViewState()
}


@HiltViewModel
class ConfigurationViewModel @Inject internal constructor(
    private val applicationDao: WrenchApplicationDao,
    configurationDao: WrenchConfigurationDao,
    private val scopeDao: WrenchScopeDao,
    val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val queryLiveData: MutableLiveData<String> = MutableLiveData()

    private val configurationListLiveData: MediatorLiveData<List<WrenchConfigurationWithValues>>

    private val applicationId: Long = savedStateHandle.get<Long>("applicationId")!!

    private val _state = MutableStateFlow(reduce(ViewState(), PartialViewState.Empty))

    internal val state: StateFlow<ViewState>
        get() = _state

    internal val wrenchApplication: LiveData<WrenchApplication> =
        applicationDao.getApplicationLiveData(applicationId)

    internal val selectedScopeLiveData: LiveData<WrenchScope> =
        scopeDao.getSelectedScopeLiveData(applicationId)

    internal val defaultScopeLiveData: LiveData<WrenchScope> =
        scopeDao.getDefaultScopeLiveData(applicationId)

    private val listEmpty: MutableLiveData<Boolean>

    internal val configurations: LiveData<List<WrenchConfigurationWithValues>>
        get() = configurationListLiveData

    internal val isListEmpty: LiveData<Boolean>
        get() = listEmpty

    init {
        setQuery(savedStateHandle.get<String>("query") ?: "")

        listEmpty = MutableLiveData()

        val configurationsLiveData = Transformations.switchMap(queryLiveData) { query ->
            if (TextUtils.isEmpty(query)) {
                configurationDao.getApplicationConfigurations(applicationId)
            } else {
                configurationDao.getApplicationConfigurations(applicationId, "%$query%")
            }
        }

        configurationListLiveData = MediatorLiveData()
        configurationListLiveData.addSource(configurationsLiveData) { wrenchConfigurationWithValues ->
            listEmpty.value =
                wrenchConfigurationWithValues == null || wrenchConfigurationWithValues.isEmpty()
            configurationListLiveData.setValue(wrenchConfigurationWithValues)
        }
    }

    private fun reduce(viewState: ViewState, partialViewState: PartialViewState): ViewState {
        return when (partialViewState) {
            is PartialViewState.Configurations -> viewState.copy(
                configurations = partialViewState.configurations
            )
            PartialViewState.Empty -> viewState
        }
    }

    fun setQuery(query: String) {
        queryLiveData.value = query
        savedStateHandle["query"] = query
    }

    internal fun deleteApplication(wrenchApplication: WrenchApplication) {
        viewModelScope.launch {
            applicationDao.delete(wrenchApplication)
        }
    }
}
