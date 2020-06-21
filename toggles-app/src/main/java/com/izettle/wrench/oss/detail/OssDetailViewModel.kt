package com.izettle.wrench.oss.detail

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.izettle.wrench.oss.LicenceMetadata
import javax.inject.Inject

class OssDetailViewModel @Inject internal constructor(val application: Application) : ViewModel() {

    fun getThirdPartyMetadata(licenceMetadata: LicenceMetadata): LiveData<String> {
        return LicenceMetadataLiveData(application, licenceMetadata)
    }

}
