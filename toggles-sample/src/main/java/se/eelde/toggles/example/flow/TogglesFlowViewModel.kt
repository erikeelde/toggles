package se.eelde.toggles.example.flow

import android.app.Application
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import se.eelde.toggles.example.MyEnum
import se.eelde.toggles.example.R
import se.eelde.toggles.flow.Toggles
import javax.inject.Inject

@HiltViewModel
class TogglesFlowViewModel @Inject constructor(
    application: Application,
    private val toggles: Toggles
) : ViewModel() {
    private val resources = application.resources

    internal val viewState: MutableState<ViewState> = mutableStateOf(ViewState.loading(resources))

    init {
        viewModelScope.launch {
            toggles.toggle(
                resources.getString(R.string.string_configuration),
                "string1"
            ).collect {
                viewState.value = viewState.value.copy(
                    stringConfig = Config.Success(
                        viewState.value.stringConfig.title,
                        it
                    )
                )
            }
        }

        viewModelScope.launch {
            toggles.toggle(
                resources.getString(R.string.int_configuration),
                1
            ).collect {
                viewState.value = viewState.value.copy(
                    intConfig = Config.Success(
                        viewState.value.intConfig.title,
                        it
                    )
                )
            }
        }

        viewModelScope.launch {
            toggles.toggle(
                resources.getString(R.string.boolean_configuration),
                true
            ).collect {
                viewState.value = viewState.value.copy(
                    boolConfig = Config.Success(
                        viewState.value.boolConfig.title,
                        it
                    )
                )
            }
        }

        viewModelScope.launch {
            toggles.toggle(
                resources.getString(R.string.url_configuration),
                "http://www.example.com/path?param=value"
            ).collect {
                viewState.value = viewState.value.copy(
                    urlConfig = Config.Success(
                        viewState.value.urlConfig.title,
                        it
                    )
                )
            }
        }

        viewModelScope.launch {
            toggles.toggle(
                resources.getString(R.string.enum_configuration),
                MyEnum::class.java,
                MyEnum.FIRST
            ).collect {
                viewState.value = viewState.value.copy(
                    enumConfig = Config.Success(
                        viewState.value.enumConfig.title,
                        it
                    )
                )
            }
        }
    }
}
