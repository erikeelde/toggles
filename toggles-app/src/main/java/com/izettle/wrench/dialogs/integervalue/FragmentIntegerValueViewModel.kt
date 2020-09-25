package com.izettle.wrench.dialogs.integervalue


import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.izettle.wrench.Event
import com.izettle.wrench.database.WrenchConfigurationDao
import com.izettle.wrench.database.WrenchConfigurationValue
import com.izettle.wrench.database.WrenchConfigurationValueDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.*

class FragmentIntegerValueViewModel
@ViewModelInject internal constructor(private val configurationDao: WrenchConfigurationDao, private val configurationValueDao: WrenchConfigurationValueDao) : ViewModel() {

    private val inputsLiveData = MediatorLiveData<Inputs>().apply {
        value = Inputs()
    }
    private val configurationIdLiveData = MutableLiveData<Long>()
    private val scopeIdLiveData = MutableLiveData<Long>()

    private val configuration = Transformations.switchMap(inputsLiveData) { inputs ->
        configurationDao.getConfiguration(inputs.configurationId!!)
    }

    private val selectedConfigurationValueLiveData = Transformations.switchMap(inputsLiveData) { inputs ->
        configurationValueDao.getConfigurationValue(inputs.configurationId!!, inputs.scopeId!!)
    }

    private var selectedConfigurationValue: WrenchConfigurationValue? = null

    internal val viewState = MediatorLiveData<ViewState>().apply {
        value = reduce(ViewState(), PartialViewState.Empty)
    }

    internal val viewEffects = MutableLiveData<Event<ViewEffect>>()

    private val channel = ConflatedBroadcastChannel<ViewAction>().apply {
        viewModelScope.launch {
            for (viewAction in openSubscription()) {
                when (viewAction) {
                    is ViewAction.SaveAction -> {
                        viewState.value = reduce(viewState.value!!, PartialViewState.Saving)
                        updateConfigurationValue(viewAction.value).join()
                        viewEffects.value = Event(ViewEffect.Dismiss)
                    }
                    ViewAction.RevertAction -> {
                        viewState.value = reduce(viewState.value!!, PartialViewState.Reverting)
                        deleteConfigurationValue()
                        viewEffects.value = Event(ViewEffect.Dismiss)
                    }
                }
            }
        }
    }

    init {
        viewState.addSource(configuration) { wrenchConfig -> viewState.value = reduce(viewState.value!!, PartialViewState.NewConfiguration(wrenchConfig.key)) }

        viewState.addSource(selectedConfigurationValueLiveData) { wrenchConfigurationValue ->
            if (wrenchConfigurationValue != null) {
                selectedConfigurationValue = wrenchConfigurationValue
                viewEffects.value = Event(ViewEffect.ValueChanged(wrenchConfigurationValue.value!!.toInt()))
            }
        }

        inputsLiveData.addSource(Transformations.distinctUntilChanged(configurationIdLiveData)) { configurationId ->
            inputsLiveData.value = inputsLiveData.value!!.copy(configurationId = configurationId)
        }

        inputsLiveData.addSource(Transformations.distinctUntilChanged(scopeIdLiveData)) { scopeId ->
            inputsLiveData.value = inputsLiveData.value!!.copy(scopeId = scopeId)
        }
    }

    internal fun init(configurationId: Long, scopeId: Long) {
        configurationIdLiveData.value = configurationId
        scopeIdLiveData.value = scopeId
    }


    private fun reduce(previousState: ViewState, partialViewState: PartialViewState): ViewState {
        return when (partialViewState) {
            is PartialViewState.NewConfiguration -> {
                previousState.copy(title = partialViewState.title)
            }
            is PartialViewState.Empty -> {
                previousState
            }
            is PartialViewState.Saving -> {
                previousState.copy(saving = true)
            }
            is PartialViewState.Reverting -> {
                previousState.copy(reverting = true)
            }
        }
    }

    internal fun saveClick(value: String) {
        viewModelScope.launch {
            channel.send(ViewAction.SaveAction(value))
        }
    }

    internal fun revertClick() {
        viewModelScope.launch {
            channel.send(ViewAction.RevertAction)
        }
    }

    private suspend fun updateConfigurationValue(value: String): Job = coroutineScope {
        viewModelScope.launch(Dispatchers.IO) {
            if (selectedConfigurationValue != null) {
                configurationValueDao.updateConfigurationValue(inputsLiveData.value!!.configurationId!!, inputsLiveData.value!!.scopeId!!, value)

            } else {
                val wrenchConfigurationValue = WrenchConfigurationValue(0, inputsLiveData.value!!.configurationId!!, value, inputsLiveData.value!!.scopeId!!)
                wrenchConfigurationValue.id = configurationValueDao.insert(wrenchConfigurationValue)
            }

            configurationDao.touch(inputsLiveData.value!!.configurationId!!, Date())
        }
    }

    private suspend fun deleteConfigurationValue() = coroutineScope {
        configurationValueDao.delete(selectedConfigurationValue!!)
    }
}

internal sealed class ViewAction {
    data class SaveAction(val value: String) : ViewAction()
    object RevertAction : ViewAction()
}

internal sealed class ViewEffect {
    object Dismiss : ViewEffect()
    data class ValueChanged(val value: Int) : ViewEffect()
}

internal data class ViewState(val title: String? = null,
                              val saving: Boolean = false,
                              val reverting: Boolean = false)

private sealed class PartialViewState {
    object Empty : PartialViewState()
    data class NewConfiguration(val title: String?) : PartialViewState()

    object Saving : PartialViewState()
    object Reverting : PartialViewState()
}

private data class Inputs(val configurationId: Long? = null, val scopeId: Long? = null)
