package se.eelde.toggles.provider

import android.content.pm.PackageManager
import android.os.Binder

interface IPackageManagerWrapper {
    val applicationLabel: String

    val callingApplicationPackageName: String
}

class PackageManagerWrapper(private val packageManager: PackageManager) : IPackageManagerWrapper {

    /**
     * The calling application's user-visible label, or its package name when the label cannot be
     * resolved.
     *
     * Resolution fails for callers that are not installed packages — adb/shell arrives as the
     * shared user "android.uid.shell:2000" — and for packages hidden by Android 11+ package
     * visibility filtering. The label is cosmetic, so it must never fail a provider call.
     */
    override val applicationLabel: String
        get() {
            val packageName = callingApplicationPackageName
            return try {
                packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
                    .loadLabel(packageManager)
                    .toString()
            } catch (_: PackageManager.NameNotFoundException) {
                packageName
            }
        }

    override val callingApplicationPackageName: String
        get() = requireNotNull(packageManager.getNameForUid(Binder.getCallingUid())) {
            "Cannot resolve package name for calling UID ${Binder.getCallingUid()}"
        }
}
