package com.izettle.wrench.configurationlist

import android.text.TextUtils
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.izettle.wrench.database.WrenchApplication
import com.izettle.wrench.database.WrenchApplicationDao
import com.izettle.wrench.database.WrenchConfigurationDao
import com.izettle.wrench.database.WrenchConfigurationWithValues
import com.izettle.wrench.database.WrenchScope
import com.izettle.wrench.database.WrenchScopeDao
import kotlinx.coroutines.launch

class ConfigurationViewModel
@ViewModelInject internal constructor(private val applicationDao: WrenchApplicationDao, configurationDao: WrenchConfigurationDao, private val scopeDao: WrenchScopeDao) : ViewModel() {
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

        val configurationsLiveData = Transformations.switchMap(queryLiveData) { query ->
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
