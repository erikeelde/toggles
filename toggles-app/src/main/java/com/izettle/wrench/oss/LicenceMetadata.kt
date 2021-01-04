package com.izettle.wrench.oss

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LicenceMetadata(
    val dependency: String,
    val skipBytes: Long,
    val length: Int
) : Parcelable
