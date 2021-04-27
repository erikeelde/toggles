package com.izettle.wrench.oss.detail

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.izettle.wrench.oss.LicenceMetadata
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

internal data class ViewState(
    val licenceMetadata: LicenceMetadata
)

@HiltViewModel
class OssDetailViewModel @Inject internal constructor(
    val savedStateHandle: SavedStateHandle,
    val application: Application
) : ViewModel() {
    private val dependency = savedStateHandle.get<String>("dependency")!!
    private val skip = savedStateHandle.get<Int>("skip")!!
    private val length = savedStateHandle.get<Int>("length")!!

    private val _state = MutableStateFlow(
        ViewState(
            licenceMetadata = LicenceMetadata(
                dependency,
                skip.toLong(),
                length
            )
        )
    )

    internal val state: StateFlow<ViewState>
        get() = _state

    fun getThirdPartyMetadata(): LiveData<String> {
        return LicenceMetadataLiveData(application, _state.value.licenceMetadata)
    }
}
