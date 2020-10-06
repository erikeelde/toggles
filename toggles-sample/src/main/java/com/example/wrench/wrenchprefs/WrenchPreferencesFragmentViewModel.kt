package com.example.wrench.wrenchprefs

import android.app.Application
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.wrench.MyEnum
import com.example.wrench.R
import com.izettle.wrench.preferences.ITogglesPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class WrenchPreferencesFragmentViewModel @ViewModelInject internal constructor(private val application: Application, private val togglesPreferences: ITogglesPreferences) : ViewModel() {

    private val job = Job()
    private val scope = CoroutineScope(job + Dispatchers.Main)

    private val stringConfig: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    fun getStringConfiguration(): LiveData<String> {
        scope.launch {
            stringConfig.postValue(togglesPreferences.getString(application.getString(R.string.string_configuration), "string1"))
        }
        return stringConfig
    }

    private val urlConfig: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    fun getUrlConfiguration(): LiveData<String> {
        scope.launch {
            urlConfig.postValue(togglesPreferences.getString(application.getString(R.string.url_configuration), "http://www.example.com/path?param=value"))
        }
        return urlConfig
    }

    private val booleanConfig: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    fun getBooleanConfiguration(): LiveData<Boolean> {
        scope.launch {
            booleanConfig.postValue(togglesPreferences.getBoolean(application.getString(R.string.boolean_configuration), true))
        }
        return booleanConfig
    }

    private val intConfig: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    fun getIntConfiguration(): LiveData<Int> {
        scope.launch {
            intConfig.postValue(togglesPreferences.getInt(application.getString(R.string.int_configuration), 1))
        }
        return intConfig
    }

    private val enumConfig: MutableLiveData<MyEnum> by lazy {
        MutableLiveData<MyEnum>()
    }

    fun getEnumConfiguration(): LiveData<MyEnum> {
        scope.launch {
            enumConfig.postValue(togglesPreferences.getEnum(application.getString(R.string.enum_configuration), MyEnum::class.java, MyEnum.SECOND))
        }
        return enumConfig
    }
}
