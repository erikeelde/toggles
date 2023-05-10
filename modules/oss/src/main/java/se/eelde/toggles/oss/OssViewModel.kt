package se.eelde.toggles.oss

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

sealed class State<T> {
    class Loading<T> : State<T>()
    data class Success<T>(val data: T) : State<T>()
    data class Failed<T>(val failed: String) : State<T>()
}

data class OssViewState(
    val loadingState: State<List<ViewData>> = State.Loading(),
)

data class ViewData(
    val title: String,
    val licenses: List<License>,
)

data class License(
    val title: String,
    val url: String,
)

sealed class OssViewEffect {
    data class Loaded(val viewdData: List<ViewData>) : OssViewEffect()
}

@HiltViewModel
class OssProjectViewModel @Inject constructor(
    licenseParser: LicenceProvider,
) : ViewModel() {

    private val _state =
        MutableStateFlow(OssViewState())

    val uiState: StateFlow<OssViewState>
        get() = _state

    init {
        _state.value = reduce(
            _state.value,
            OssViewEffect.Loaded(
                licenseParser.licences().map {
                    val licenses = it.spdxLicenses.orEmpty()
                        .map { spdx -> License(spdx.name, spdx.url) } +
                        it.unknownLicenses.orEmpty()
                            .map { unknown -> License(unknown.name, unknown.url) }

                    val nameOrPackage = it.name ?: (it.groupId + ":" + it.groupId)
                    ViewData(
                        nameOrPackage,
                        licenses
                    )
                }.sortedBy { it.title }
            )
        )
    }

    fun reduce(
        oldViewState: OssViewState,
        viewEffect: OssViewEffect,
    ) = when (viewEffect) {
        is OssViewEffect.Loaded -> oldViewState.copy(
            loadingState = State.Success(viewEffect.viewdData),
        )
    }
}
