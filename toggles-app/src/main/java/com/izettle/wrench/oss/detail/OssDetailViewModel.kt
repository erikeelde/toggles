package com.izettle.wrench.oss.detail

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.izettle.wrench.oss.LicenceMetadata
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OssDetailViewModel @Inject internal constructor(val application: Application) : ViewModel() {

    fun getThirdPartyMetadata(licenceMetadata: LicenceMetadata): LiveData<String> {
        return LicenceMetadataLiveData(application, licenceMetadata)
    }
}
