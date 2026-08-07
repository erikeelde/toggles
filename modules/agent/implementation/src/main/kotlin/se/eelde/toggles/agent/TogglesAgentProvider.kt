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
import java.io.FileOutputStream

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
        fun provideAgentUriMatcher(): AgentUriMatcher
        fun provideCallerAuthorization(): CallerAuthorization
    }

    private val readHandler: AgentReadHandler by lazy {
        AgentReadHandler(
            agentDao = entryPoint.provideAgentDao(),
            uriMatcher = entryPoint.provideAgentUriMatcher(),
            appVersionName = appVersionName()
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
        val json = if (entryPoint.provideCallerAuthorization().isAuthorizedCaller()) {
            readHandler.handle(uri)
        } else {
            AgentError.json(
                AgentErrorCode.NOT_AUTHORIZED,
                "the toggles agent API is only callable from adb (uid 2000) or root (uid 0)"
            )
        }

        // openPipeHelper writes on a background thread, so a large payload blocks that thread
        // rather than the binder call.
        return openPipeHelper(uri, MIME_TYPE, null, json) { output, _, _, _, payload ->
            FileOutputStream(output.fileDescriptor).use { stream ->
                stream.write(payload.orEmpty().toByteArray())
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
            // Mutations arrive here in a later plan. Until then every method is unknown.
            putString(
                RESULT_KEY,
                AgentError.json(
                    AgentErrorCode.UNKNOWN_ENDPOINT,
                    "no such method: $method. Read /describe for the available endpoints."
                )
            )
        }

    companion object {
        const val MIME_TYPE = "application/json"
        const val RESULT_KEY = "result"
    }
}
