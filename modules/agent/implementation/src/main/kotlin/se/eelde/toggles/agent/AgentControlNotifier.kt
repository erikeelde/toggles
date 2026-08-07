package se.eelde.toggles.agent

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import se.eelde.toggles.agent.implementation.R
import java.util.concurrent.ConcurrentHashMap

/**
 * Discloses, via a system notification, the first time this process mutates a given application's
 * toggles through the agent API — with a "Disable agent control" action so the disclosure comes
 * bundled with a way to act on it.
 *
 * `agentControlEnabled` defaults to true and is deliberately frictionless (see its kdoc on
 * [se.eelde.toggles.database.TogglesApplication]): anyone with adb can already do strictly more.
 * This notifier is the visibility half of that trade — the user finds out an agent touched their
 * toggles, and can turn control off, without every mutation demanding attention up front.
 *
 * Revocation is intentionally NOT wired to the notification being dismissed (swiped away, or
 * cleared by "clear all"): people dismiss notifications reflexively, and a silent mid-session
 * revoke would reproduce exactly the confusing failure mode `adb shell content call` already hides
 * behind "No result found" — an agent that suddenly, silently stops working. Only the explicit
 * "Disable agent control" action (handled by [AgentControlDisableReceiver]) turns it off.
 */
interface AgentControlNotifier {
    fun notifyFirstMutation(packageName: String, applicationLabel: String)
}

class SystemAgentControlNotifier(
    private val context: Context,
) : AgentControlNotifier {

    // Per-process, in-memory, deliberately not persisted: a fresh notification after every
    // process restart is fine (arguably desirable — it re-surfaces the disclosure rather than
    // letting "already told them once, years ago" go stale), and this doesn't need the migration
    // and cleanup story a persisted "already notified" flag would.
    private val alreadyNotified = ConcurrentHashMap.newKeySet<String>()

    // Posting a notification is disclosure, not part of the mutation's contract: whatever goes
    // wrong here (POST_NOTIFICATIONS denied, no NotificationManager, anything else) must never be
    // allowed to make an otherwise-successful mutation look like it failed. AgentCallHandler calls
    // this inline after the write already happened, wrapped in its own top-level catch that turns
    // *any* escaping exception into an internal_error response — so this method must not let one
    // escape, or a permission the user never granted would flip a successful write into a
    // reported failure.
    @SuppressLint("MissingPermission")
    @Suppress("TooGenericExceptionCaught")
    override fun notifyFirstMutation(packageName: String, applicationLabel: String) {
        if (!alreadyNotified.add(packageName)) return

        try {
            ensureChannel()

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_agent_control)
                .setContentTitle(
                    context.getString(R.string.agent_control_notification_title, applicationLabel)
                )
                .setContentText(
                    context.getString(R.string.agent_control_notification_text, packageName)
                )
                .setAutoCancel(true)
                .addAction(
                    NotificationCompat.Action.Builder(
                        0,
                        context.getString(R.string.agent_control_disable_action),
                        disablePendingIntent(packageName)
                    ).build()
                )
                .build()

            NotificationManagerCompat.from(context).notify(packageName.hashCode(), notification)
        } catch (_: Exception) {
            // See kdoc above: swallow, never propagate.
        }
    }

    private fun disablePendingIntent(packageName: String): PendingIntent {
        val intent = Intent(context, AgentControlDisableReceiver::class.java).apply {
            action = AgentControlDisableReceiver.ACTION_DISABLE_AGENT_CONTROL
            putExtra(AgentControlDisableReceiver.EXTRA_PACKAGE_NAME, packageName)
        }
        return PendingIntent.getBroadcast(
            context,
            packageName.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        if (manager != null && manager.getNotificationChannel(CHANNEL_ID) == null) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.agent_control_notification_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
        }
    }

    private companion object {
        const val CHANNEL_ID = "agent_control"
    }
}
