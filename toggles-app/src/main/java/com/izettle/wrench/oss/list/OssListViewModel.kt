package com.izettle.wrench.oss.list

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.izettle.wrench.oss.LicenceMetadata
import javax.inject.Inject

class OssListViewModel @Inject internal constructor(val application: Application) : ViewModel() {
    fun getThirdPartyMetadata(): LiveData<List<LicenceMetadata>> {
        return ThirdPartyLicenceMetadataLiveData(application)
    }
}
