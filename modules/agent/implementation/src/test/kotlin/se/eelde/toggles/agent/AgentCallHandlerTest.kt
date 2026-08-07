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

    // --- createScope ---

    @Test
    fun `creating a scope creates it for the application and returns its id`() {
        insertApplication("com.example.app", "Example")

        val response = createScope(packageName = "com.example.app", name = "new scope")

        val decoded = decode(response)
        assertEquals("createScope", decoded.method)
        val scopeId = requireNotNull(decoded.scopeId)
        assertEquals("new scope", database.agentMutationDao().getScope(scopeId)?.name)
    }

    @Test
    fun `a duplicate scope name for the same application returns invalid_argument`() {
        val appId = insertApplication("com.example.app", "Example")
        insertScope(appId, "Development scope")

        val response = createScope(packageName = "com.example.app", name = "Development scope")

        assertEquals("invalid_argument", errorCode(response))
    }

    @Test
    fun `creating a scope for an unknown package returns unknown_package`() {
        val response = createScope(packageName = "com.example.missing", name = "new scope")

        assertEquals("unknown_package", errorCode(response))
    }

    @Test
    fun `creating a scope for a disabled application returns agent_control_disabled`() {
        val appId = insertApplication("com.example.app", "Example")
        database.openHelper.writableDatabase.execSQL(
            "UPDATE application SET agentControlEnabled = 0 WHERE id = $appId"
        )

        val response = createScope(packageName = "com.example.app", name = "new scope")

        assertEquals("agent_control_disabled", errorCode(response))
    }

    @Test
    fun `creating a scope does not select it`() {
        val appId = insertApplication("com.example.app", "Example")
        insertScope(appId, "toggles_default", epochMillis = 0)
        // Clearly newer than the default scope but clearly older than the fixed clock (99_000) used
        // for creation timestamps below, so there is no ambiguity about which scope is selected
        // before or after createScope runs.
        val devScopeId = insertScope(appId, "Development scope", epochMillis = 10_000)

        val response = createScope(packageName = "com.example.app", name = "new scope")

        assertEquals("createScope", decode(response).method)
        assertEquals(devScopeId, selectedScopeId("com.example.app"))
    }

    @Test
    fun `createScope's response summary names the application and the scope`() {
        insertApplication("com.example.app", "Example")

        val response = createScope(packageName = "com.example.app", name = "new scope")

        val summary = decode(response).summary
        assertTrue(
            "expected the package in the summary, got: $summary",
            summary.contains("com.example.app")
        )
        assertTrue(
            "expected the scope name in the summary, got: $summary",
            summary.contains("new scope")
        )
    }

    // --- selectScope ---

    @Test
    fun `selecting a scope makes it the selected one`() {
        val appId = insertApplication("com.example.app", "Example")
        val defaultScopeId = insertScope(appId, "toggles_default", epochMillis = 0)
        // Older than the fixed clock (99_000) used by touchScope, so selecting defaultScopeId is
        // unambiguously a change.
        insertScope(appId, "Development scope", epochMillis = 10_000)

        val response = selectScope(packageName = "com.example.app", scopeId = defaultScopeId)

        assertEquals("selectScope", decode(response).method)
        assertEquals(defaultScopeId, selectedScopeId("com.example.app"))
    }

    @Test
    fun `selecting a scope changes effectiveValue for a configuration with different values per scope`() {
        val appId = insertApplication("com.example.app", "Example")
        val defaultScopeId = insertScope(appId, "toggles_default", epochMillis = 0)
        // Seeded strictly between the default scope's timestamp and the fixed clock's 99_000, so
        // it is unambiguously selected before the call and unambiguously superseded after it — a
        // single touchScope call (using the one fixed clock value) is enough to prove the switch
        // without needing two touches that could tie.
        val devScopeId = insertScope(appId, "Development scope", epochMillis = 10_000)
        val configId = insertConfiguration(appId, "feature_x", "boolean")
        insertValue(configId, defaultScopeId, "false")
        insertValue(configId, devScopeId, "true")

        assertEquals("true", effectiveValue("com.example.app"))

        selectScope(packageName = "com.example.app", scopeId = defaultScopeId)

        assertEquals("false", effectiveValue("com.example.app"))
    }

    @Test
    fun `selecting a scope from a different application returns invalid_argument and changes nothing`() {
        val appA = insertApplication("com.example.a", "A")
        val appB = insertApplication("com.example.b", "B")
        insertScope(appA, "toggles_default", epochMillis = 0)
        val devA = insertScope(appA, "Development scope", epochMillis = 10_000)
        val scopeB = insertScope(appB, "toggles_default", epochMillis = 0)

        val response = selectScope(packageName = "com.example.a", scopeId = scopeB)

        assertEquals("invalid_argument", errorCode(response))
        assertEquals(devA, selectedScopeId("com.example.a"))
    }

    @Test
    fun `selecting a scope notifies the notifier that scopes changed`() {
        val appId = insertApplication("com.example.app", "Example")
        val defaultScopeId = insertScope(appId, "toggles_default", epochMillis = 0)

        selectScope(packageName = "com.example.app", scopeId = defaultScopeId)

        assertEquals(1, notifier.scopesNotified)
    }

    @Test
    fun `selecting an unknown scopeId returns unknown_id`() {
        insertApplication("com.example.app", "Example")

        val response = selectScope(packageName = "com.example.app", scopeId = 999L)

        assertEquals("unknown_id", errorCode(response))
    }

    @Test
    fun `selecting a scope for an unknown package returns unknown_package`() {
        val response = selectScope(packageName = "com.example.missing", scopeId = 1L)

        assertEquals("unknown_package", errorCode(response))
    }

    @Test
    fun `selecting a scope for a disabled application returns agent_control_disabled`() {
        val appId = insertApplication("com.example.app", "Example")
        val scopeId = insertScope(appId, "toggles_default", epochMillis = 0)
        database.openHelper.writableDatabase.execSQL(
            "UPDATE application SET agentControlEnabled = 0 WHERE id = $appId"
        )

        val response = selectScope(packageName = "com.example.app", scopeId = scopeId)

        assertEquals("agent_control_disabled", errorCode(response))
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

    private fun createScope(packageName: String, name: String): String =
        handler.handle(
            "createScope",
            Bundle().apply {
                putString("package", packageName)
                putString("name", name)
            }
        )

    private fun selectScope(packageName: String, scopeId: Long): String =
        handler.handle(
            "selectScope",
            Bundle().apply {
                putString("package", packageName)
                putLong("scopeId", scopeId)
            }
        )

    private fun applicationDetail(packageName: String): AgentApplicationDetail {
        val readHandler = AgentReadHandler(
            agentDao = database.agentDao(),
            uriMatcher = AgentUriMatcher(AgentDescription.AGENT_AUTHORITY),
            appVersionName = "1.2.3"
        )
        val uri = android.net.Uri.parse("content://${AgentDescription.AGENT_AUTHORITY}/apps/$packageName")
        return json.decodeFromString(readHandler.handle(uri))
    }

    private fun effectiveValue(packageName: String): String? =
        applicationDetail(packageName).configurations.single().effectiveValue

    private fun selectedScopeId(packageName: String): Long? =
        applicationDetail(packageName).scopes.firstOrNull { it.selected }?.id

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

    private fun insertScope(applicationId: Long, name: String, epochMillis: Long = 0): Long =
        database.providerScopeDao().insert(
            TogglesScope(
                id = 0,
                applicationId = applicationId,
                name = name,
                timeStamp = Instant.fromEpochMilliseconds(epochMillis)
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
