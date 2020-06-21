package com.izettle.wrench.provider

interface IPackageManagerWrapper {
    val applicationLabel: String

    val callingApplicationPackageName: String?
}
