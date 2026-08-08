package se.eelde.toggles.agent

import android.os.Binder
import android.os.Process

/**
 * The agent provider is exported so that adb can reach it, which means every installed application
 * can reach it too. This restricts it to callers that already control the device.
 *
 * Without this check any application could impersonate any other and rewrite its toggles, which
 * would be a genuine privilege escalation. With it, a caller can already do strictly more through
 * `pm install`, `am start` and `run-as`.
 */
class CallerAuthorization {

    fun isAuthorizedCaller(): Boolean = when (Binder.getCallingUid()) {
        Process.SHELL_UID, Process.ROOT_UID -> true
        else -> false
    }
}
