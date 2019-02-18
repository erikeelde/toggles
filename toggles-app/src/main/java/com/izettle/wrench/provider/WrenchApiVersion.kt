package com.izettle.wrench.provider


import androidx.annotation.IntDef

const val API_INVALID = 0
const val API_1 = 1

@Retention(AnnotationRetention.SOURCE)
@IntDef(API_INVALID, API_1)
internal annotation class WrenchApiVersion
