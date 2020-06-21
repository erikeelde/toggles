package com.izettle.wrench.configurationlist

import android.text.TextUtils
import androidx.lifecycle.*
import com.izettle.wrench.database.*
import kotlinx.coroutines.launch
import javax.inject.Inject

class ConfigurationViewModel
@Inject internal constructor(private val applicationDao: WrenchApplicationDao, configurationDao: WrenchConfigurationDao, private val scopeDao: WrenchScopeDao) : ViewModel() {
    private val queryLiveData: MutableLiveData<String> = MutableLiveData()

    private val configurationListLiveData: MediatorLiveData<List<WrenchConfigurationWithValues>>

    internal val wrenchApplication: LiveData<WrenchApplication> by lazy {
        Transformations.switchMap(applicationIdLiveData) { applicationId: Long -> applicationDao.getApplication(applicationId) }
    }

    private val applicationIdLiveData: MutableLiveData<Long> = MutableLiveData()
    internal val selectedScopeLiveData: LiveData<WrenchScope> by lazy {
        Transformations.switchMap(applicationIdLiveData) { applicationId: Long -> scopeDao.getSelectedScopeLiveData(applicationId) }
    }

    internal val defaultScopeLiveData: LiveData<WrenchScope> by lazy {
        Transformations.switchMap(applicationIdLiveData) { applicationId: Long -> scopeDao.getDefaultScopeLiveData(applicationId) }
    }

    private val listEmpty: MutableLiveData<Boolean>

    internal val configurations: LiveData<List<WrenchConfigurationWithValues>>
        get() = configurationListLiveData

    internal val isListEmpty: LiveData<Boolean>
        get() = listEmpty

    init {

        setQuery("")

        listEmpty = MutableLiveData()

        val configurationsLiveData = Transformations.switchMap<String, List<WrenchConfigurationWithValues>>(queryLiveData) { query ->
            if (TextUtils.isEmpty(query)) {
                configurationDao.getApplicationConfigurations(applicationIdLiveData.value!!)
            } else {
                configurationDao.getApplicationConfigurations(applicationIdLiveData.value!!, "%$query%")
            }
        }

        configurationListLiveData = MediatorLiveData()
        configurationListLiveData.addSource(configurationsLiveData) { wrenchConfigurationWithValues ->
            listEmpty.value = wrenchConfigurationWithValues == null || wrenchConfigurationWithValues.isEmpty()
            configurationListLiveData.setValue(wrenchConfigurationWithValues)
        }
    }

    internal fun setApplicationId(applicationIdLiveData: Long) {
        this.applicationIdLiveData.value = applicationIdLiveData
    }

    fun setQuery(query: String) {
        queryLiveData.value = query
    }

    internal fun deleteApplication(wrenchApplication: WrenchApplication) {
        viewModelScope.launch {
            applicationDao.delete(wrenchApplication)
        }
    }
}
