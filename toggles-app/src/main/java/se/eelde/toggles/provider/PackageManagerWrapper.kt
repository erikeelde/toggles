package se.eelde.toggles.provider

import android.content.pm.PackageManager
import android.os.Binder

interface IPackageManagerWrapper {
    val applicationLabel: String

    val callingApplicationPackageName: String?
}

class PackageManagerWrapper(private val packageManager: PackageManager) : IPackageManagerWrapper {

    override val applicationLabel: String
        @Throws(PackageManager.NameNotFoundException::class)
        get() {
            val applicationInfo = packageManager.getApplicationInfo(
                callingApplicationPackageName!!,
                PackageManager.GET_META_DATA
            )
            return applicationInfo.loadLabel(packageManager).toString()
        }

    override val callingApplicationPackageName: String?
        get() = packageManager.getNameForUid(Binder.getCallingUid())
}
