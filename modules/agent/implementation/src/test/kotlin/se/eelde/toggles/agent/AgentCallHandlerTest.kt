package se.eelde.toggles.agent

import android.os.Build
import android.os.Bundle
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import se.eelde.toggles.database.TogglesApplication
import se.eelde.toggles.database.TogglesConfiguration
import se.eelde.toggles.database.TogglesConfigurationValue
import se.eelde.toggles.database.TogglesDatabase
import se.eelde.toggles.database.TogglesPredefinedConfigurationValue
import se.eelde.toggles.database.TogglesScope
import kotlin.time.Clock
import kotlin.time.Instant

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class AgentCallHandlerTest {

    private val json = Json { ignoreUnknownKeys = false }

    private lateinit var database: TogglesDatabase
    private lateinit var handler: AgentCallHandler
    private lateinit var notifier: RecordingChangeNotifier

    private val fixedClock = object : Clock {
        override fun now(): Instant = Instant.fromEpochMilliseconds(99_000)
    }

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            TogglesDatabase::class.java
        ).allowMainThreadQueries().build()
        notifier = RecordingChangeNotifier()
        handler = AgentCallHandler(
            agentDao = database.agentDao(),
            agentMutationDao = database.agentMutationDao(),
            changeNotifier = notifier,
            clock = fixedClock
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `setting a value on an existing row updates it`() {
        val appId = insertApplication("com.example.app", "Example")
        val scopeId = insertScope(appId, "toggles_default")
        val configId = insertConfiguration(appId, "feature_x", "boolean")
        insertValue(configId, scopeId, "false")

        val response = call(configurationId = configId, scopeId = scopeId, value = "true")

        assertEquals("setConfigurationValue", decode(response).method)
        assertEquals(
            "true",
            effectiveValue("com.example.app")
        )
    }

    @Test
    fun `setting a value with no existing row for that scope inserts one and leaves the other scope alone`() {
        val appId = insertApplication("com.example.app", "Example")
        val defaultScopeId = insertScope(appId, "toggles_default")
        val devScopeId = insertScope(appId, "Development scope")
        val configId = insertConfiguration(appId, "feature_x", "boolean")
        insertValue(configId, defaultScopeId, "false")

        call(configurationId = configId, scopeId = devScopeId, value = "true")

        val values = database.agentDao().getConfigurationValues(appId)
        assertEquals(2, values.size)
        assertEquals("false", values.single { it.scope == defaultScopeId }.value)
        assertEquals("true", values.single { it.scope == devScopeId }.value)
    }

    @Test
    fun `the notifier is called with the configuration's id`() {
        val appId = insertApplication("com.example.app", "Example")
        val scopeId = insertScope(appId, "toggles_default")
        val configId = insertConfiguration(appId, "feature_x", "boolean")

        call(configurationId = configId, scopeId = scopeId, value = "true")

        assertEquals(listOf(configId), notifier.configurationsNotified)
    }

    @Test
    fun `an invalid value is rejected, nothing is written and the notifier is not called`() {
        val appId = insertApplication("com.example.app", "Example")
        val scopeId = insertScope(appId, "toggles_default")
        val configId = insertConfiguration(appId, "feature_x", "boolean")
        insertValue(configId, scopeId, "false")

        val response = call(configurationId = configId, scopeId = scopeId, value = "not-a-boolean")

        assertEquals("invalid_argument", errorCode(response))
        assertEquals("false", effectiveValue("com.example.app"))
        assertTrue(notifier.configurationsNotified.isEmpty())
    }

    @Test
    fun `an unknown configurationId returns unknown_id`() {
        val appId = insertApplication("com.example.app", "Example")
        val scopeId = insertScope(appId, "toggles_default")

        val response = call(configurationId = 999L, scopeId = scopeId, value = "true")

        assertEquals("unknown_id", errorCode(response))
    }

    @Test
    fun `a disabled application's configuration returns agent_control_disabled and writes nothing`() {
        val appId = insertApplication("com.example.app", "Example")
        val scopeId = insertScope(appId, "toggles_default")
        val configId = insertConfiguration(appId, "feature_x", "boolean")
        insertValue(configId, scopeId, "false")
        database.openHelper.writableDatabase.execSQL(
            "UPDATE application SET agentControlEnabled = 0 WHERE id = $appId"
        )

        val response = call(configurationId = configId, scopeId = scopeId, value = "true")

        assertEquals("agent_control_disabled", errorCode(response))
        assertEquals(
            "false",
            database.agentDao().getConfigurationValues(appId).single().value
        )
        assertTrue(notifier.configurationsNotified.isEmpty())
    }

    @Test
    fun `the response summary contains the configuration key and the scope name`() {
        val appId = insertApplication("com.example.app", "Example")
        val scopeId = insertScope(appId, "Development scope")
        val configId = insertConfiguration(appId, "feature_x", "boolean")

        val response = call(configurationId = configId, scopeId = scopeId, value = "true")

        val summary = decode(response).summary
        assertTrue("expected the key in the summary, got: $summary", summary.contains("feature_x"))
        assertTrue(
            "expected the scope name in the summary, got: $summary",
            summary.contains("Development scope")
        )
    }

    @Test
    fun `a missing required extra returns invalid_argument naming the missing key`() {
        val appId = insertApplication("com.example.app", "Example")
        val scopeId = insertScope(appId, "toggles_default")
        val configId = insertConfiguration(appId, "feature_x", "boolean")

        val missingConfigurationId = handler.handle(
            "setConfigurationValue",
            Bundle().apply {
                putLong("scopeId", scopeId)
                putString("value", "true")
            }
        )
        val missingScopeId = handler.handle(
            "setConfigurationValue",
            Bundle().apply {
                putLong("configurationId", configId)
                putString("value", "true")
            }
        )
        val missingValue = handler.handle(
            "setConfigurationValue",
            Bundle().apply {
                putLong("configurationId", configId)
                putLong("scopeId", scopeId)
            }
        )

        assertEquals("invalid_argument", errorCode(missingConfigurationId))
        assertTrue(errorMessage(missingConfigurationId).contains("configurationId"))
        assertEquals("invalid_argument", errorCode(missingScopeId))
        assertTrue(errorMessage(missingScopeId).contains("scopeId"))
        assertEquals("invalid_argument", errorCode(missingValue))
        assertTrue(errorMessage(missingValue).contains("value"))
    }

    @Test
    fun `a scopeId belonging to a different application returns invalid_argument and writes nothing`() {
        val appA = insertApplication("com.example.a", "A")
        val appB = insertApplication("com.example.b", "B")
        val scopeB = insertScope(appB, "toggles_default")
        val configA = insertConfiguration(appA, "feature_x", "boolean")
        val scopeA = insertScope(appA, "toggles_default")
        insertValue(configA, scopeA, "false")

        val response = call(configurationId = configA, scopeId = scopeB, value = "true")

        assertEquals("invalid_argument", errorCode(response))
        assertEquals("false", effectiveValue("com.example.a"))
        assertTrue(notifier.configurationsNotified.isEmpty())
    }

    @Test
    fun `an unknown method name returns unknown_endpoint`() {
        val response = handler.handle("noSuchMethod", Bundle())

        assertEquals("unknown_endpoint", errorCode(response))
    }

    @Test
    fun `handle never throws even when a Bundle extra is bound with the wrong type`() {
        val appId = insertApplication("com.example.app", "Example")
        val scopeId = insertScope(appId, "toggles_default")
        val configId = insertConfiguration(appId, "feature_x", "boolean")

        val response = handler.handle(
            "setConfigurationValue",
            Bundle().apply {
                // configurationId bound as a String, not a Long — a realistic mistake given
                // `adb shell content call`'s l:/s: typed-extra prefixes.
                putString("configurationId", configId.toString())
                putLong("scopeId", scopeId)
                putString("value", "true")
            }
        )

        assertEquals("invalid_argument", errorCode(response))
    }

    @Test
    fun `setting an enum value rejects a non predefined value and accepts a predefined one`() {
        val appId = insertApplication("com.example.app", "Example")
        val scopeId = insertScope(appId, "toggles_default")
        val configId = insertConfiguration(appId, "feature_x", "enum")
        insertPredefinedValue(configId, "red")
        insertPredefinedValue(configId, "blue")

        val rejected = call(configurationId = configId, scopeId = scopeId, value = "green")
        assertEquals("invalid_argument", errorCode(rejected))
        assertNull(
            database.agentDao().getConfigurationValues(appId).firstOrNull { it.configurationId == configId }
        )

        val accepted = call(configurationId = configId, scopeId = scopeId, value = "blue")
        assertEquals("setConfigurationValue", decode(accepted).method)
        assertEquals("blue", effectiveValue("com.example.app"))
    }

    private class RecordingChangeNotifier : AgentChangeNotifier {
        val configurationsNotified = mutableListOf<Long>()
        var scopesNotified = 0

        override fun notifyConfigurationChanged(configurationId: Long) {
            configurationsNotified += configurationId
        }

        override fun notifyScopesChanged() {
            scopesNotified++
        }
    }

    private fun call(configurationId: Long, scopeId: Long, value: String): String =
        handler.handle(
            "setConfigurationValue",
            Bundle().apply {
                putLong("configurationId", configurationId)
                putLong("scopeId", scopeId)
                putString("value", value)
            }
        )

    private fun effectiveValue(packageName: String): String? {
        val readHandler = AgentReadHandler(
            agentDao = database.agentDao(),
            uriMatcher = AgentUriMatcher(AgentDescription.AGENT_AUTHORITY),
            appVersionName = "1.2.3"
        )
        val uri = android.net.Uri.parse("content://${AgentDescription.AGENT_AUTHORITY}/apps/$packageName")
        val detail: AgentApplicationDetail = json.decodeFromString(readHandler.handle(uri))
        return detail.configurations.single().effectiveValue
    }

    private fun decode(payload: String): AgentMutationResponse = json.decodeFromString(payload)

    private fun errorCode(payload: String): String =
        json.decodeFromString<AgentErrorEnvelope>(payload).error.code

    private fun errorMessage(payload: String): String =
        json.decodeFromString<AgentErrorEnvelope>(payload).error.message

    private fun insertApplication(packageName: String, label: String): Long =
        database.providerApplicationDao().insert(
            TogglesApplication(
                id = 0,
                shortcutId = packageName,
                packageName = packageName,
                applicationLabel = label
            )
        )

    private fun insertScope(applicationId: Long, name: String): Long =
        database.providerScopeDao().insert(
            TogglesScope(
                id = 0,
                applicationId = applicationId,
                name = name,
                timeStamp = Instant.fromEpochMilliseconds(0)
            )
        )

    private fun insertConfiguration(applicationId: Long, key: String, type: String): Long =
        database.providerConfigurationDao().insert(
            TogglesConfiguration(
                id = 0,
                applicationId = applicationId,
                key = key,
                type = type,
                lastUse = Instant.fromEpochMilliseconds(0)
            )
        )

    private fun insertValue(configurationId: Long, scopeId: Long, value: String?): Long =
        database.providerConfigurationValueDao().insertSync(
            TogglesConfigurationValue(
                id = 0,
                configurationId = configurationId,
                value = value,
                scope = scopeId
            )
        )

    private fun insertPredefinedValue(configurationId: Long, value: String): Long =
        database.providerPredefinedConfigurationValueDao().insert(
            TogglesPredefinedConfigurationValue(
                id = 0,
                configurationId = configurationId,
                value = value
            )
        )
}
