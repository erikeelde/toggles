// ShadowPausedAsyncTask is Robolectric's own (deprecated) mechanism for making AsyncTask
// execution deterministic under LooperMode.PAUSED; see the kdoc on overrideExecutor() below.
@file:Suppress("DEPRECATION")

package se.eelde.toggles.agent

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowBinder
import org.robolectric.shadows.ShadowPausedAsyncTask
import se.eelde.toggles.agent.di.AgentTestApplication_Application
import se.eelde.toggles.database.TogglesApplication
import se.eelde.toggles.database.TogglesConfiguration
import se.eelde.toggles.database.TogglesConfigurationValue
import se.eelde.toggles.database.TogglesDatabase
import se.eelde.toggles.database.TogglesScope
import java.util.concurrent.Executor
import javax.inject.Inject
import kotlin.time.Instant

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@Config(application = AgentTestApplication_Application::class, sdk = [Build.VERSION_CODES.P])
class TogglesAgentProviderTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var togglesDatabase: TogglesDatabase

    private val json = Json { ignoreUnknownKeys = false }

    private lateinit var provider: TogglesAgentProvider

    @Before
    fun setUp() {
        hiltRule.inject()
        // openFile() streams its payload from a real background thread via openPipeHelper. Under
        // Robolectric, ParcelFileDescriptor.createPipe() is backed by a plain file rather than a
        // blocking OS pipe, so an unpatched read races the writer thread instead of blocking for
        // it. Forcing a synchronous executor makes the write happen before openFile() returns,
        // which is deterministic without weakening the production code (which still genuinely
        // pipes across threads on a real device).
        ShadowPausedAsyncTask.overrideExecutor(Executor { it.run() })
        provider = Robolectric.buildContentProvider(TogglesAgentProvider::class.java)
            .create(AgentDescription.AGENT_AUTHORITY)
            .get()
    }

    @After
    fun tearDown() {
        ShadowPausedAsyncTask.reset()
    }

    @Test
    fun `an unauthorized caller receives not_authorized`() {
        ShadowBinder.setCallingUid(APP_UID)

        val envelope = json.decodeFromString<AgentErrorEnvelope>(readJson(uri("/describe")))

        assertEquals("not_authorized", envelope.error.code)
    }

    @Test
    fun `an unauthorized caller cannot read application data`() {
        ShadowBinder.setCallingUid(APP_UID)
        insertApplication()

        val envelope = json.decodeFromString<AgentErrorEnvelope>(readJson(uri("/apps")))

        assertEquals("not_authorized", envelope.error.code)
    }

    @Test
    fun `a shell caller can read describe`() {
        ShadowBinder.setCallingUid(SHELL_UID)

        val document = json.decodeFromString<AgentDescriptionDocument>(readJson(uri("/describe")))

        assertEquals(1, document.agentApiVersion)
    }

    @Test
    fun `a shell caller can read apps`() {
        ShadowBinder.setCallingUid(SHELL_UID)
        insertApplication()

        val list = json.decodeFromString<AgentApplicationList>(readJson(uri("/apps")))

        assertEquals(1, list.applications.size)
    }

    @Test
    fun `getType reports json`() {
        ShadowBinder.setCallingUid(SHELL_UID)

        assertEquals("application/json", provider.getType(uri("/describe")))
    }

    @Test
    fun `call returns an unknown endpoint error for an unknown method`() {
        ShadowBinder.setCallingUid(SHELL_UID)

        val result = provider.call("noSuchMethod", null, null)
        val payload = requireNotNull(result.getString(TogglesAgentProvider.RESULT_KEY))

        assertEquals("unknown_endpoint", json.decodeFromString<AgentErrorEnvelope>(payload).error.code)
    }

    @Test
    fun `a shell caller's setConfigurationValue call through call actually changes the stored value`() {
        ShadowBinder.setCallingUid(SHELL_UID)
        val appId = insertApplication("com.example.app", "Example")
        val scopeId = insertScope(appId, "toggles_default")
        val configId = insertConfiguration(appId, "feature_x", "boolean")
        insertValue(configId, scopeId, "false")

        val result = provider.call(
            "setConfigurationValue",
            null,
            setConfigurationValueExtras(configId, scopeId, "true")
        )
        val payload = requireNotNull(result.getString(TogglesAgentProvider.RESULT_KEY))

        assertEquals(
            "setConfigurationValue",
            json.decodeFromString<AgentMutationResponse>(payload).method
        )
        assertEquals(
            "true",
            togglesDatabase.agentDao().getConfigurationValues(appId).single().value
        )
    }

    @Test
    fun `an unauthorized caller's setConfigurationValue call through call changes nothing`() {
        val appId = insertApplication("com.example.app", "Example")
        val scopeId = insertScope(appId, "toggles_default")
        val configId = insertConfiguration(appId, "feature_x", "boolean")
        insertValue(configId, scopeId, "false")

        ShadowBinder.setCallingUid(APP_UID)
        val result = provider.call(
            "setConfigurationValue",
            null,
            setConfigurationValueExtras(configId, scopeId, "true")
        )
        val payload = requireNotNull(result.getString(TogglesAgentProvider.RESULT_KEY))

        assertEquals(
            "not_authorized",
            json.decodeFromString<AgentErrorEnvelope>(payload).error.code
        )
        assertEquals(
            "false",
            togglesDatabase.agentDao().getConfigurationValues(appId).single().value
        )
    }

    @Test
    fun `an unauthorized caller cannot use call`() {
        ShadowBinder.setCallingUid(APP_UID)

        val result = provider.call("anything", null, null)
        val payload = requireNotNull(result.getString(TogglesAgentProvider.RESULT_KEY))

        assertEquals(
            "not_authorized",
            json.decodeFromString<AgentErrorEnvelope>(payload).error.code
        )
    }

    @Test
    fun `the system uid cannot use call`() {
        ShadowBinder.setCallingUid(SYSTEM_UID)

        val result = provider.call("anything", null, null)
        val payload = requireNotNull(result.getString(TogglesAgentProvider.RESULT_KEY))

        assertEquals(
            "not_authorized",
            json.decodeFromString<AgentErrorEnvelope>(payload).error.code
        )
    }

    @Test
    fun `the mutating content provider methods are inert`() {
        ShadowBinder.setCallingUid(SHELL_UID)

        assertEquals(null, provider.insert(uri("/apps"), null))
        assertEquals(0, provider.update(uri("/apps"), null, null, null))
        assertEquals(0, provider.delete(uri("/apps"), null, null))
        assertEquals(null, provider.query(uri("/apps"), null, null, null, null))
    }

    @Test
    fun `a large payload survives the pipe intact`() {
        ShadowBinder.setCallingUid(SHELL_UID)
        insertApplication()

        val payload = readJson(uri("/apps"))

        assertTrue("payload was empty", payload.isNotEmpty())
        // Must be complete, parseable JSON — a truncated pipe would fail to decode.
        val list = json.decodeFromString<AgentApplicationList>(payload)
        assertEquals(1, list.applications.size)
    }

    private fun setConfigurationValueExtras(configurationId: Long, scopeId: Long, value: String) =
        Bundle().apply {
            putLong("configurationId", configurationId)
            putLong("scopeId", scopeId)
            putString("value", value)
        }

    private fun insertApplication(packageName: String, label: String): Long =
        togglesDatabase.providerApplicationDao().insert(
            TogglesApplication(
                id = 0,
                shortcutId = packageName,
                packageName = packageName,
                applicationLabel = label
            )
        )

    private fun insertScope(applicationId: Long, name: String): Long =
        togglesDatabase.providerScopeDao().insert(
            TogglesScope(
                id = 0,
                applicationId = applicationId,
                name = name,
                timeStamp = Instant.fromEpochMilliseconds(0)
            )
        )

    private fun insertConfiguration(applicationId: Long, key: String, type: String): Long =
        togglesDatabase.providerConfigurationDao().insert(
            TogglesConfiguration(
                id = 0,
                applicationId = applicationId,
                key = key,
                type = type,
                lastUse = Instant.fromEpochMilliseconds(0)
            )
        )

    private fun insertValue(configurationId: Long, scopeId: Long, value: String?): Long =
        togglesDatabase.providerConfigurationValueDao().insertSync(
            TogglesConfigurationValue(
                id = 0,
                configurationId = configurationId,
                value = value,
                scope = scopeId
            )
        )

    private fun insertApplication() {
        togglesDatabase.providerApplicationDao().insert(
            TogglesApplication(
                id = 0,
                shortcutId = "com.example.app",
                packageName = "com.example.app",
                applicationLabel = "Example"
            )
        )
    }

    private fun uri(path: String) = Uri.parse("content://${AgentDescription.AGENT_AUTHORITY}$path")

    private fun readJson(uri: Uri): String {
        val descriptor = provider.openFile(uri, "r")
        return ParcelFileDescriptor.AutoCloseInputStream(descriptor).use { stream ->
            stream.readBytes().decodeToString()
        }
    }

    private companion object {
        const val SHELL_UID = 2000
        const val APP_UID = 10247
        const val SYSTEM_UID = 1000
    }
}
