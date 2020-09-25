package com.izettle.wrench.oss.list

import android.app.Application
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.izettle.wrench.oss.LicenceMetadata

class OssListViewModel @ViewModelInject internal constructor(val application: Application) : ViewModel() {
    fun getThirdPartyMetadata(): LiveData<List<LicenceMetadata>> {
        return ThirdPartyLicenceMetadataLiveData(application)
    }
}
