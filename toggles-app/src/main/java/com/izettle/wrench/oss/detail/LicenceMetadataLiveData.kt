package com.izettle.wrench.oss.detail

import android.content.Context
import androidx.lifecycle.LiveData
import com.izettle.wrench.oss.LicenceMetadata
import com.izettle.wrench.oss.list.OssLoading

class LicenceMetadataLiveData(val context: Context, private val licenceMetadata: LicenceMetadata) : LiveData<String>() {

    init {
        run {
            postValue(OssLoading.getThirdPartyLicence(context, licenceMetadata))
        }
    }
}
