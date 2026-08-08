package se.eelde.toggles.agent

import android.content.ContentResolver
import se.eelde.toggles.core.TogglesProviderContract
import se.eelde.toggles.provider.notifyUpdate

/**
 * Fires the ContentResolver notifications a running app depends on.
 *
 * `toggles-flow` observes `configurationUri()` and `toggleUri()` with notifyForDescendants = true.
 * Writing to the database without notifying leaves a running app on a stale value until it
 * restarts, which presents as an intermittent bug rather than a missing notification.
 */
interface AgentChangeNotifier {
    fun notifyConfigurationChanged(configurationId: Long)
    fun notifyScopesChanged()
}

class ContentResolverAgentChangeNotifier(
    private val contentResolver: ContentResolver,
) : AgentChangeNotifier {

    override fun notifyConfigurationChanged(configurationId: Long) {
        contentResolver.notifyUpdate(TogglesProviderContract.toggleUri(configurationId))
        contentResolver.notifyUpdate(TogglesProviderContract.configurationUri(configurationId))
    }

    override fun notifyScopesChanged() {
        contentResolver.notifyUpdate(TogglesProviderContract.scopeUri())
    }
}
