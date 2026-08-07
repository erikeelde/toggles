package se.eelde.toggles.provider

import android.content.pm.PackageManager
import android.os.Binder

interface IPackageManagerWrapper {
    /**
     * @throws SecurityException if the calling uid cannot be resolved to a package name.
     */
    val applicationLabel: String

    /**
     * @throws SecurityException if the calling uid cannot be resolved to a package name.
     */
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
     *
     * A caller whose uid resolves to no name at all is a different matter, and still propagates
     * as a [SecurityException] — see [callingApplicationPackageName].
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

    /**
     * The calling application's package name.
     *
     * [PackageManager.getNameForUid] returns null when the uid "is not currently assigned", which
     * leaves the caller unidentifiable. Every row this provider serves is partitioned by calling
     * package name, so there is no data to hand such a caller and no safe identity to invent for
     * it — it is rejected rather than served.
     *
     * [SecurityException] is deliberate: it is the platform's answer for a caller that may not be
     * served, and it is one of the exception types `Parcel.writeException` marshals, so the
     * message survives the binder hop instead of failing the transaction opaquely.
     *
     * @throws SecurityException if the calling uid cannot be resolved to a package name.
     */
    override val callingApplicationPackageName: String
        get() {
            val callingUid = Binder.getCallingUid()
            return packageManager.getNameForUid(callingUid)
                ?: throw SecurityException(
                    "Cannot resolve a package name for calling uid $callingUid - the uid is not " +
                        "currently assigned to any package, so the caller cannot be identified"
                )
        }
}
