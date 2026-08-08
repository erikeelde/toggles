package se.eelde.toggles.agent

import android.content.pm.PackageManager
import se.eelde.toggles.database.TogglesApplication
import se.eelde.toggles.database.TogglesScope
import se.eelde.toggles.database.dao.agent.AgentDao
import se.eelde.toggles.database.dao.agent.AgentMutationDao
import kotlin.time.Clock

/**
 * Resolves the application a mutation should act against, creating it on demand when Toggles has
 * never seen the package before.
 *
 * `createConfiguration` supports pre-creating a toggle for code that has not run on-device yet, so
 * an unknown package is not an error: the application row is created for whatever package string
 * is supplied, no [PackageManager] check required.
 *
 * An earlier version of this class gated the auto-create on `PackageManager.getApplicationInfo`
 * actually resolving the package, on the theory that this would stop a typo from permanently
 * adding junk to `/apps`. On-device verification proved that check unusable: Android 11+ package
 * visibility filtering makes `getApplicationInfo` throw `NameNotFoundException` for any package
 * that has never interacted with Toggles — declaring no `<queries>` (which would require naming
 * packages up front, defeating the point) and no `QUERY_ALL_PACKAGES` (Play-policy-restricted, not
 * grantable here) — which is exactly the set of packages this feature exists to pre-create for.
 * The guard defeated the feature it was guarding. Robolectric's `ShadowPackageManager` does not
 * implement visibility filtering, so the unit tests that exercised the old gate could not have
 * caught this; see the comment on the equivalent case in AgentCallHandlerTest.
 *
 * The caller is already restricted to uid 2000/0 (see [CallerAuthorization]) — they control the
 * device and can do far more than add a database row, so a typo here is a recoverable annoyance
 * (a junk `/apps` entry, deletable from the Toggles app's own UI), not a security concern. The
 * [PackageManager] lookup is kept, but only for two non-blocking purposes: resolving the real
 * application label when possible (see [ApplicationResolution.packageVerified]'s kdoc), and
 * telling the caller whether the package could actually be confirmed.
 *
 * Split out of [AgentCallHandler] so this provisioning logic — application row, default scope,
 * development scope — has a single seam of its own rather than growing that class's function
 * count past what one class should own.
 */
internal class AgentApplicationProvisioner(
    private val agentDao: AgentDao,
    private val agentMutationDao: AgentMutationDao,
    private val packageManager: PackageManager,
    private val clock: Clock,
) {

    /**
     * The application [AgentCallHandler] should act against, together with whether this call had
     * to provision it.
     */
    data class ApplicationResolution(
        val application: TogglesApplication,
        /**
         * Null when [application] already existed in Toggles before this call — nothing was
         * provisioned, so there is nothing to report. True/false when this call just created the
         * application row, reflecting whether [PackageManager] could confirm the package is
         * installed: false is the signal that a typo in the package name would surface as, since
         * Toggles otherwise has no way to distinguish a genuine not-yet-run package (Android 11+
         * visibility filtering hides it too) from a misspelled one.
         */
        val packageVerified: Boolean?,
    )

    /**
     * Returns the known or newly created application for [packageName]. Always succeeds: an
     * unresolvable package still gets an application row, just with `packageVerified = false` in
     * the result (see [ApplicationResolution]).
     */
    fun resolveOrCreate(packageName: String): ApplicationResolution {
        agentDao.getApplicationByPackageName(packageName)?.let {
            return ApplicationResolution(it, packageVerified = null)
        }

        // Mirrors the try/getApplicationInfo/fallback-to-packageName shape of
        // PackageManagerWrapper.applicationLabel (modules/provider/implementation) rather than
        // reusing it directly: this module depends only on modules/provider/api — it is a peer of
        // TogglesProvider, not a consumer of its implementation module (see the dependency
        // comment in AgentCallHandler.implementation's build.gradle.kts) — so that class is not on
        // this module's classpath.
        val applicationInfo = try {
            packageManager.getApplicationInfo(packageName, 0)
        } catch (_: PackageManager.NameNotFoundException) {
            null
        }
        val applicationLabel = applicationInfo?.loadLabel(packageManager)?.toString() ?: packageName

        val application = TogglesApplication(
            id = 0,
            packageName = packageName,
            applicationLabel = applicationLabel,
            shortcutId = packageName,
        )
        application.id = agentMutationDao.insertApplication(application)

        // Every application needs both scopes for setConfigurationValue's default-scope fallback
        // to work; see TogglesScope.defaultScope/developmentScope for why this must match
        // TogglesProvider's first-touch creation exactly.
        agentMutationDao.insertScope(TogglesScope.defaultScope(application.id, clock))
        agentMutationDao.insertScope(TogglesScope.developmentScope(application.id, clock))

        return ApplicationResolution(application, packageVerified = applicationInfo != null)
    }
}
