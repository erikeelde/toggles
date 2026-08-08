package se.eelde.toggles.agent

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import se.eelde.toggles.database.dao.agent.AgentDao
import se.eelde.toggles.database.dao.agent.AgentMutationDao
import java.io.FileOutputStream
import java.io.IOException
import kotlin.time.Clock

/**
 * The adb-facing surface. Exported so that shell can reach it; restricted to shell and root by
 * [CallerAuthorization].
 *
 * Reads are served through [openFile] so that `adb shell content read` receives raw JSON on stdout
 * with no wrapper — `content query` would mangle values containing commas or `=`, and `content
 * call` wraps its output in `Result: Bundle[{...}]`.
 */
class TogglesAgentProvider : ContentProvider() {

    private val requireContext: Context
        get() = requireNotNull(context) { "ContentProvider context not yet initialized" }

    private val entryPoint: TogglesAgentProviderEntryPoint by lazy {
        entryPointBuilder.build(requireContext)
    }

    internal interface EntryPointBuilder {
        fun build(context: Context): TogglesAgentProviderEntryPoint
    }

    internal var entryPointBuilder: EntryPointBuilder = object : EntryPointBuilder {
        override fun build(context: Context): TogglesAgentProviderEntryPoint =
            EntryPointAccessors.fromApplication(context, TogglesAgentProviderEntryPoint::class.java)
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface TogglesAgentProviderEntryPoint {
        fun provideAgentDao(): AgentDao
        fun provideAgentMutationDao(): AgentMutationDao
        fun provideAgentUriMatcher(): AgentUriMatcher
        fun provideCallerAuthorization(): CallerAuthorization
        fun provideAgentChangeNotifier(): AgentChangeNotifier
        fun provideAgentControlNotifier(): AgentControlNotifier
        fun provideClock(): Clock
        fun providePackageManager(): PackageManager
    }

    private val readHandler: AgentReadHandler by lazy {
        AgentReadHandler(
            agentDao = entryPoint.provideAgentDao(),
            uriMatcher = entryPoint.provideAgentUriMatcher(),
            appVersionName = appVersionName()
        )
    }

    // Reads context.packageName rather than a hardcoded "se.eelde.toggles" so this keeps working
    // under a debug/applicationIdSuffix build variant too.
    private val agentApiGate: AgentApiGate by lazy {
        AgentApiGate(agentDao = entryPoint.provideAgentDao(), context = requireContext)
    }

    private val callHandler: AgentCallHandler by lazy {
        AgentCallHandler(
            agentDao = entryPoint.provideAgentDao(),
            agentMutationDao = entryPoint.provideAgentMutationDao(),
            changeNotifier = entryPoint.provideAgentChangeNotifier(),
            controlNotifier = entryPoint.provideAgentControlNotifier(),
            clock = entryPoint.provideClock(),
            packageManager = entryPoint.providePackageManager()
        )
    }

    private fun appVersionName(): String = try {
        requireContext.packageManager
            .getPackageInfo(requireContext.packageName, 0)
            .versionName
            .orEmpty()
    } catch (_: PackageManager.NameNotFoundException) {
        ""
    }

    override fun onCreate() = true

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor {
        // CallerAuthorization first: an unauthorized caller must get not_authorized regardless of
        // whether the API is enabled — the enabled state must never leak to a caller who is not
        // even allowed to ask. Only once authorized does readHandler decide, per endpoint, whether
        // apiEnabled gates it (every endpoint but /describe does).
        val json = if (entryPoint.provideCallerAuthorization().isAuthorizedCaller()) {
            readHandler.handle(uri, apiEnabled = agentApiGate.isEnabled())
        } else {
            AgentError.json(
                AgentErrorCode.NOT_AUTHORIZED,
                "the toggles agent API is only callable from adb (uid 2000) or root (uid 0)"
            )
        }

        // openPipeHelper writes on a background thread, so a large payload blocks that thread
        // rather than the binder call.
        return openPipeHelper(uri, MIME_TYPE, null, json) { output, _, _, _, payload ->
            try {
                FileOutputStream(output.fileDescriptor).use { stream ->
                    stream.write(payload.orEmpty().toByteArray())
                }
            } catch (_: IOException) {
                // The reader closed the pipe before we finished writing — e.g. piping through
                // `head` or a parser that stops early. There is nothing useful to do but stop.
                // Letting this escape would surface as a fatal RuntimeException on the AsyncTask
                // worker thread and take down the Toggles process.
            }
        }
    }

    override fun getType(uri: Uri): String = MIME_TYPE

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int = 0

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int = 0

    override fun call(method: String, arg: String?, extras: Bundle?): Bundle =
        Bundle().apply {
            putString(RESULT_KEY, callResult(method, extras))
        }

    // Two early returns — caller authorization, then the enabled gate — each a distinct rejection
    // reason a caller needs to tell apart, same rationale AgentCallHandler's endpoints use.
    @Suppress("ReturnCount")
    private fun callResult(method: String, extras: Bundle?): String {
        // Same ordering as openFile: caller authorization first, the enabled gate second. Unlike
        // reads, call() has no /describe-equivalent exemption — every mutation method is gated.
        if (!entryPoint.provideCallerAuthorization().isAuthorizedCaller()) {
            return AgentError.json(
                AgentErrorCode.NOT_AUTHORIZED,
                "the toggles agent API is only callable from adb (uid 2000) or root (uid 0)"
            )
        }
        if (!agentApiGate.isEnabled()) {
            return agentApiDisabledError()
        }
        return callHandler.handle(method, extras)
    }

    companion object {
        const val MIME_TYPE = "application/json"
        const val RESULT_KEY = "result"
    }
}
