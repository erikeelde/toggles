package com.example.wrench.livedataprefs

import android.app.Application
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.wrench.MyEnum
import com.example.wrench.R
import kotlinx.coroutines.ExperimentalCoroutinesApi
import se.eelde.toggles.coroutines.booleanBoltFlow
import se.eelde.toggles.coroutines.enumBoltFlow
import se.eelde.toggles.coroutines.integerBoltFlow
import se.eelde.toggles.coroutines.stringBoltFlow

class LiveDataPreferencesFragmentViewModel @ViewModelInject constructor(private val application: Application) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val stringConfig by lazy {
        stringBoltFlow(application, application.resources.getString(R.string.string_configuration), "string1").asLiveData(viewModelScope.coroutineContext)
    }

    fun getStringConfiguration(): LiveData<String> {
        return stringConfig
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val intConfig by lazy {
        integerBoltFlow(application, application.resources.getString(R.string.int_configuration), 1).asLiveData(viewModelScope.coroutineContext)
    }

    fun getIntConfiguration(): LiveData<Int> {
        return intConfig
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val booleanConfig by lazy {
        booleanBoltFlow(application, application.resources.getString(R.string.boolean_configuration), true).asLiveData(viewModelScope.coroutineContext)
    }

    fun getBooleanConfiguration(): LiveData<Boolean> {
        return booleanConfig
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val urlConfig by lazy {
        stringBoltFlow(application, application.resources.getString(R.string.url_configuration), "http://www.example.com/path?param=value").asLiveData(viewModelScope.coroutineContext)
    }

    fun getUrlConfiguration(): LiveData<String> {
        return urlConfig
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val enumConfig by lazy {
        enumBoltFlow(application, application.resources.getString(R.string.enum_configuration), MyEnum::class.java, MyEnum.FIRST).asLiveData(viewModelScope.coroutineContext)
    }

    fun getEnumConfiguration(): LiveData<MyEnum> {
        return enumConfig
    }
}
