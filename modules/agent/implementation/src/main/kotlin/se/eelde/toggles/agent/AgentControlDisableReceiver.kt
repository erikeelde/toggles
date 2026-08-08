package se.eelde.toggles.agent

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import se.eelde.toggles.database.dao.agent.AgentMutationDao
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * Handles the "Disable agent control" action on the notification [AgentControlNotifier] posts.
 *
 * Deliberately reachable only from that in-process action — see the `exported="false"` on this
 * receiver's manifest entry, and [AgentControlNotifier]'s kdoc for why revocation is explicit-tap
 * only rather than tied to dismissing the notification.
 *
 * [AgentMutationDao] is a plain synchronous DAO (see its kdoc: it mirrors [AgentCallHandler]
 * running synchronously on the binder thread), and the production database does not allow
 * main-thread queries, while `onReceive` always runs on the main thread. The write is therefore
 * dispatched onto [executor] rather than run inline.
 */
class AgentControlDisableReceiver : BroadcastReceiver() {

    internal interface EntryPointBuilder {
        fun build(context: Context): AgentControlDisableReceiverEntryPoint
    }

    internal var entryPointBuilder: EntryPointBuilder = object : EntryPointBuilder {
        override fun build(context: Context): AgentControlDisableReceiverEntryPoint =
            EntryPointAccessors.fromApplication(context, AgentControlDisableReceiverEntryPoint::class.java)
    }

    internal var executor: Executor = Executors.newSingleThreadExecutor()

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface AgentControlDisableReceiverEntryPoint {
        fun provideAgentMutationDao(): AgentMutationDao
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_DISABLE_AGENT_CONTROL) return
        val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME) ?: return

        val agentMutationDao = entryPointBuilder.build(context.applicationContext)
            .provideAgentMutationDao()

        executor.execute {
            agentMutationDao.setAgentControlEnabled(packageName, false)
        }
    }

    companion object {
        const val ACTION_DISABLE_AGENT_CONTROL = "se.eelde.toggles.agent.action.DISABLE_AGENT_CONTROL"
        const val EXTRA_PACKAGE_NAME = "package"
    }
}
