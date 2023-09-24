package com.example.toggles.prefs

import android.app.Application
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.toggles.Config
import com.example.toggles.MyEnum
import com.example.toggles.R
import com.example.toggles.ViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import se.eelde.toggles.prefs.TogglesPreferences
import javax.inject.Inject

@HiltViewModel
class TogglesPrefsViewModel @Inject internal constructor(
    application: Application,
    private val togglesPreferences: TogglesPreferences
) : ViewModel() {

    private val resources = application.resources

    internal val viewState: MutableState<ViewState> = mutableStateOf(ViewState.loading(resources))

    init {
        updateViewState()
    }

    private fun updateViewState() {
        viewModelScope.launch {
            viewState.value = ViewState(
                stringConfig = getStringConfiguration(),
                intConfig = getIntConfiguration(),
                boolConfig = getBooleanConfiguration(),
                enumConfig = getEnumConfiguration(),
                urlConfig = getUrlConfiguration(),
            )
        }
    }

    private fun getStringConfiguration(): Config<String> =
        Config.Success(
            title = resources.getString(R.string.string_configuration),
            value = togglesPreferences.getString(
                resources.getString(R.string.string_configuration),
                "string1"
            )
        )

    private fun getUrlConfiguration(): Config<String> =
        Config.Success(
            title = resources.getString(R.string.url_configuration),
            value = togglesPreferences.getString(
                resources.getString(R.string.url_configuration),
                "http://www.example.com/path?param=value"
            )
        )

    private fun getBooleanConfiguration(): Config<Boolean> =
        Config.Success(
            title = resources.getString(R.string.boolean_configuration),
            value = togglesPreferences.getBoolean(
                resources.getString(R.string.boolean_configuration),
                true
            )
        )

    private fun getIntConfiguration(): Config<Int> =
        Config.Success(
            title = resources.getString(R.string.int_configuration),
            value = togglesPreferences.getInt(
                resources.getString(R.string.int_configuration),
                1
            )
        )

    private fun getEnumConfiguration(): Config<MyEnum> =
        Config.Success(
            title = resources.getString(R.string.enum_configuration),
            value = MyEnum.FIRST
        )
}
