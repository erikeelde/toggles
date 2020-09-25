package com.izettle.wrench.oss.detail

import android.app.Application
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.izettle.wrench.oss.LicenceMetadata

class OssDetailViewModel @ViewModelInject internal constructor(val application: Application) : ViewModel() {

    fun getThirdPartyMetadata(licenceMetadata: LicenceMetadata): LiveData<String> {
        return LicenceMetadataLiveData(application, licenceMetadata)
    }

}
