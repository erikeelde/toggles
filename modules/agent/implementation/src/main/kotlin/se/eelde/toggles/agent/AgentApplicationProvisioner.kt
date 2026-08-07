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
 * an unknown package is not automatically an error — but a caller-supplied package name is
 * unvalidated input, and inserting an application row for a typo would silently create junk that
 * appears in `/apps` forever. The compromise: only auto-create the application row when
 * [PackageManager] confirms the package is genuinely installed on the device.
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
     * Returns the known or newly created application, or null when [packageName] is neither known
     * to Toggles nor installed on the device.
     */
    // Each early return is a distinct gate (already known, not installed) or the final result —
    // matching the early-return style used throughout AgentCallHandler for the same reason.
    @Suppress("ReturnCount")
    fun resolveOrCreate(packageName: String): TogglesApplication? {
        agentDao.getApplicationByPackageName(packageName)?.let { return it }

        val applicationInfo = try {
            packageManager.getApplicationInfo(packageName, 0)
        } catch (_: PackageManager.NameNotFoundException) {
            return null
        }

        val application = TogglesApplication(
            id = 0,
            packageName = packageName,
            applicationLabel = applicationInfo.loadLabel(packageManager).toString(),
            shortcutId = packageName,
        )
        application.id = agentMutationDao.insertApplication(application)

        // Every application needs both scopes for setConfigurationValue's default-scope fallback
        // to work; see TogglesScope.defaultScope/developmentScope for why this must match
        // TogglesProvider's first-touch creation exactly.
        agentMutationDao.insertScope(TogglesScope.defaultScope(application.id, clock))
        agentMutationDao.insertScope(TogglesScope.developmentScope(application.id, clock))

        return application
    }
}
