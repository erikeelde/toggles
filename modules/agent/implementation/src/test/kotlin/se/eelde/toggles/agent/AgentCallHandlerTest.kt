package se.eelde.toggles.agent

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
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
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import se.eelde.toggles.database.TogglesApplication
import se.eelde.toggles.database.TogglesConfiguration
import se.eelde.toggles.database.TogglesConfigurationValue
import se.eelde.toggles.database.TogglesDatabase
import se.eelde.toggles.database.TogglesPredefinedConfigurationValue
import se.eelde.toggles.database.TogglesScope
import se.eelde.toggles.database.dao.agent.AgentMutationDao
import java.util.concurrent.Executor
import kotlin.time.Clock
import kotlin.time.Instant

// One exhaustive test class covering every AgentCallHandler endpoint (including the agent control
// notification wiring added alongside the mutation endpoints themselves) reads better as a single
// file organized by endpoint than split across files that would each need their own database and
// handler setup.
@Suppress("LargeClass")
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class AgentCallHandlerTest {

    private val json = Json { ignoreUnknownKeys = false }

    private lateinit var database: TogglesDatabase
    private lateinit var handler: AgentCallHandler
    private lateinit var notifier: RecordingChangeNotifier
    private lateinit var controlNotifier: RecordingControlNotifier

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
        controlNotifier = RecordingControlNotifier()
        val context = ApplicationProvider.getApplicationContext<Application>()
        handler = AgentCallHandler(
            agentDao = database.agentDao(),
            agentMutationDao = database.agentMutationDao(),
            changeNotifier = notifier,
            controlNotifier = controlNotifier,
            clock = fixedClock,
            packageManager = context.packageManager
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

    // --- createConfiguration ---

    @Test
    fun `creating a configuration for a known application creates it and it appears via the read handler`() {
        insertApplication("com.example.app", "Example")

        val response = createConfiguration(
            packageName = "com.example.app",
            key = "feature_x",
            type = "boolean"
        )

        val decoded = decode(response)
        assertEquals("createConfiguration", decoded.method)
        val configurationId = requireNotNull(decoded.configurationId)
        assertEquals(
            "feature_x",
            applicationDetail("com.example.app").configurations.single { it.id == configurationId }.key
        )
    }

    @Test
    fun `creating a configuration for an installed never-seen package creates the application and configuration`() {
        installPackage("com.example.neverseen", "Never Seen")

        val response = createConfiguration(
            packageName = "com.example.neverseen",
            key = "feature_x",
            type = "boolean"
        )

        val decoded = decode(response)
        assertEquals("createConfiguration", decoded.method)
        assertEquals(true, decoded.packageVerified)
        val detail = applicationDetail("com.example.neverseen")
        assertEquals("feature_x", detail.configurations.single().key)
    }

    // Robolectric's ShadowPackageManager does not implement Android 11+ package visibility
    // filtering, so this and the test below cannot reproduce the real-device failure that
    // motivated allowing this: on a real device, getApplicationInfo throws
    // NameNotFoundException for any package that has never interacted with Toggles, which is
    // exactly the set of packages createConfiguration exists to pre-create for. See
    // AgentApplicationProvisioner's kdoc. Verified manually via `adb shell content call` against
    // a genuinely-installed-but-never-seen package — see the task's on-device verification.
    @Test
    fun `creating a configuration for a package PackageManager cannot resolve still creates it, flagged unverified`() {
        val response = createConfiguration(
            packageName = "com.example.notinstalled",
            key = "feature_x",
            type = "boolean"
        )

        val decoded = decode(response)
        assertEquals("createConfiguration", decoded.method)
        assertEquals(false, decoded.packageVerified)
        assertEquals(
            "feature_x",
            applicationDetail("com.example.notinstalled").configurations.single().key
        )
    }

    @Test
    fun `packageVerified distinguishes an unresolvable package from a resolvable one`() {
        installPackage("com.example.neverseen", "Never Seen")

        val resolvable = createConfiguration(
            packageName = "com.example.neverseen",
            key = "feature_x",
            type = "boolean"
        )
        val unresolvable = createConfiguration(
            packageName = "com.example.notinstalled",
            key = "feature_x",
            type = "boolean"
        )

        assertEquals(true, decode(resolvable).packageVerified)
        assertEquals(false, decode(unresolvable).packageVerified)
        assertTrue(
            "expected a note about the unresolved package, got: ${decode(unresolvable).summary}",
            decode(unresolvable).summary.contains("could not be confirmed")
        )
    }

    @Test
    fun `creating a configuration for an already-known application leaves packageVerified null`() {
        insertApplication("com.example.app", "Example")

        val response = createConfiguration(
            packageName = "com.example.app",
            key = "feature_x",
            type = "boolean"
        )

        assertNull(decode(response).packageVerified)
    }

    @Test
    fun `a duplicate configuration key for the same application returns invalid_argument`() {
        val appId = insertApplication("com.example.app", "Example")
        insertConfiguration(appId, "feature_x", "boolean")

        val response = createConfiguration(
            packageName = "com.example.app",
            key = "feature_x",
            type = "boolean"
        )

        assertEquals("invalid_argument", errorCode(response))
    }

    @Test
    fun `creating a configuration with an invalid type returns invalid_argument`() {
        insertApplication("com.example.app", "Example")

        val response = createConfiguration(
            packageName = "com.example.app",
            key = "feature_x",
            type = "not-a-type"
        )

        assertEquals("invalid_argument", errorCode(response))
    }

    @Test
    fun `creating a configuration for a disabled application returns agent_control_disabled`() {
        val appId = insertApplication("com.example.app", "Example")
        database.openHelper.writableDatabase.execSQL(
            "UPDATE application SET agentControlEnabled = 0 WHERE id = $appId"
        )

        val response = createConfiguration(
            packageName = "com.example.app",
            key = "feature_x",
            type = "boolean"
        )

        assertEquals("agent_control_disabled", errorCode(response))
    }

    @Test
    fun `a newly created application gets default and development scopes so setConfigurationValue works`() {
        installPackage("com.example.neverseen", "Never Seen")

        val createResponse = createConfiguration(
            packageName = "com.example.neverseen",
            key = "feature_x",
            type = "boolean"
        )
        val configurationId = requireNotNull(decode(createResponse).configurationId)

        val defaultScopeId = requireNotNull(
            applicationDetail("com.example.neverseen").scopes.firstOrNull { it.default }?.id
        )

        val setResponse = call(configurationId = configurationId, scopeId = defaultScopeId, value = "true")

        assertEquals("setConfigurationValue", decode(setResponse).method)
        assertEquals("true", effectiveValue("com.example.neverseen"))
    }

    // --- deleteConfiguration ---

    @Test
    fun `deleting a configuration removes it and its value rows`() {
        val appId = insertApplication("com.example.app", "Example")
        val scopeId = insertScope(appId, "toggles_default")
        val configId = insertConfiguration(appId, "feature_x", "boolean")
        insertValue(configId, scopeId, "true")

        val response = deleteConfiguration(configId)

        assertEquals("deleteConfiguration", decode(response).method)
        assertNull(database.agentMutationDao().getConfiguration(configId))
        assertTrue(database.agentDao().getConfigurationValues(appId).isEmpty())
    }

    @Test
    fun `deleting a configuration notifies the notifier with the configuration's id`() {
        val appId = insertApplication("com.example.app", "Example")
        val configId = insertConfiguration(appId, "feature_x", "boolean")

        deleteConfiguration(configId)

        assertEquals(listOf(configId), notifier.configurationsNotified)
    }

    @Test
    fun `deleting an unknown configuration id returns unknown_id`() {
        val response = deleteConfiguration(999L)

        assertEquals("unknown_id", errorCode(response))
    }

    @Test
    fun `deleting a configuration for a disabled application returns agent_control_disabled and deletes nothing`() {
        val appId = insertApplication("com.example.app", "Example")
        val configId = insertConfiguration(appId, "feature_x", "boolean")
        database.openHelper.writableDatabase.execSQL(
            "UPDATE application SET agentControlEnabled = 0 WHERE id = $appId"
        )

        val response = deleteConfiguration(configId)

        assertEquals("agent_control_disabled", errorCode(response))
        assertEquals(configId, database.agentMutationDao().getConfiguration(configId)?.id)
        assertTrue(notifier.configurationsNotified.isEmpty())
    }

    // --- deleteConfigurationValue ---

    @Test
    fun `removing an override makes effectiveValue fall back to the default scope`() {
        val appId = insertApplication("com.example.app", "Example")
        val defaultScopeId = insertScope(appId, "toggles_default")
        val devScopeId = insertScope(appId, "Development scope")
        val configId = insertConfiguration(appId, "feature_x", "boolean")
        insertValue(configId, defaultScopeId, "true")
        insertValue(configId, devScopeId, "false")

        val response = deleteConfigurationValue(configId, devScopeId)

        assertEquals("deleteConfigurationValue", decode(response).method)
        assertEquals("true", effectiveValue("com.example.app"))
        assertEquals(
            "true",
            database.agentDao().getConfigurationValues(appId).single { it.scope == defaultScopeId }.value
        )
    }

    @Test
    fun `removing an override notifies the notifier with the configuration's id`() {
        val appId = insertApplication("com.example.app", "Example")
        val defaultScopeId = insertScope(appId, "toggles_default")
        val devScopeId = insertScope(appId, "Development scope")
        val configId = insertConfiguration(appId, "feature_x", "boolean")
        insertValue(configId, defaultScopeId, "true")
        insertValue(configId, devScopeId, "false")

        deleteConfigurationValue(configId, devScopeId)

        assertEquals(listOf(configId), notifier.configurationsNotified)
    }

    @Test
    fun `removing a non-existent override succeeds, changes nothing, and says so`() {
        val appId = insertApplication("com.example.app", "Example")
        val defaultScopeId = insertScope(appId, "toggles_default")
        val devScopeId = insertScope(appId, "Development scope")
        val configId = insertConfiguration(appId, "feature_x", "boolean")
        insertValue(configId, defaultScopeId, "true")

        val response = deleteConfigurationValue(configId, devScopeId)

        val decoded = decode(response)
        assertEquals("deleteConfigurationValue", decoded.method)
        assertTrue(
            "expected the summary to say nothing was removed, got: ${decoded.summary}",
            decoded.summary.contains("nothing") || decoded.summary.contains("no")
        )
        assertEquals("true", effectiveValue("com.example.app"))
        assertTrue(notifier.configurationsNotified.isEmpty())
    }

    @Test
    fun `removing an override for a scope from a different application returns invalid_argument and deletes nothing`() {
        val appA = insertApplication("com.example.a", "A")
        val appB = insertApplication("com.example.b", "B")
        val scopeA = insertScope(appA, "toggles_default")
        val scopeB = insertScope(appB, "toggles_default")
        val configA = insertConfiguration(appA, "feature_x", "boolean")
        insertValue(configA, scopeA, "true")

        val response = deleteConfigurationValue(configA, scopeB)

        assertEquals("invalid_argument", errorCode(response))
        assertEquals("true", effectiveValue("com.example.a"))
    }

    @Test
    fun `removing an override for a disabled application returns agent_control_disabled and deletes nothing`() {
        val appId = insertApplication("com.example.app", "Example")
        val scopeId = insertScope(appId, "toggles_default")
        val configId = insertConfiguration(appId, "feature_x", "boolean")
        insertValue(configId, scopeId, "true")
        database.openHelper.writableDatabase.execSQL(
            "UPDATE application SET agentControlEnabled = 0 WHERE id = $appId"
        )

        val response = deleteConfigurationValue(configId, scopeId)

        assertEquals("agent_control_disabled", errorCode(response))
        assertEquals("true", database.agentDao().getConfigurationValues(appId).single().value)
        assertTrue(notifier.configurationsNotified.isEmpty())
    }

    @Test
    fun `removing an override for an unknown configuration returns unknown_id`() {
        val response = deleteConfigurationValue(999L, 1L)

        assertEquals("unknown_id", errorCode(response))
    }

    @Test
    fun `deleting the default scope's only row leaves effectiveValue null`() {
        val appId = insertApplication("com.example.app", "Example")
        val defaultScopeId = insertScope(appId, "toggles_default")
        val configId = insertConfiguration(appId, "feature_x", "boolean")
        insertValue(configId, defaultScopeId, "true")

        val response = deleteConfigurationValue(configId, defaultScopeId)

        assertEquals("deleteConfigurationValue", decode(response).method)
        assertNull(effectiveValue("com.example.app"))
    }

    // --- deleteScope ---

    @Test
    fun `deleting a non-selected, non-default scope removes it and its value rows, leaving other scopes untouched`() {
        val appId = insertApplication("com.example.app", "Example")
        val defaultScopeId = insertScope(appId, "toggles_default", epochMillis = 0)
        val devScopeId = insertScope(appId, "Development scope", epochMillis = 10_000)
        val extraScopeId = insertScope(appId, "extra scope", epochMillis = 20_000)
        val configId = insertConfiguration(appId, "feature_x", "boolean")
        insertValue(configId, defaultScopeId, "false")
        insertValue(configId, devScopeId, "true")
        insertValue(configId, extraScopeId, "true")

        val response = deleteScope(packageName = "com.example.app", scopeId = extraScopeId)

        assertEquals("deleteScope", decode(response).method)
        assertNull(database.agentMutationDao().getScope(extraScopeId))
        val remainingValues = database.agentDao().getConfigurationValues(appId)
        assertEquals(2, remainingValues.size)
        assertEquals("false", remainingValues.single { it.scope == defaultScopeId }.value)
        assertEquals("true", remainingValues.single { it.scope == devScopeId }.value)
    }

    @Test
    fun `deleting a scope leaves no orphaned configurationValue rows referencing it`() {
        // Protects configurationValue.scope's FK to scope(id) ON DELETE CASCADE (MIGRATION_9_10):
        // AgentScopeDeleter no longer cleans these rows up explicitly, so this now exercises the
        // database-enforced cascade rather than application code.
        val appId = insertApplication("com.example.app", "Example")
        insertScope(appId, "toggles_default", epochMillis = 0)
        val extraScopeId = insertScope(appId, "extra scope", epochMillis = 10_000)
        val configId = insertConfiguration(appId, "feature_x", "boolean")
        insertValue(configId, extraScopeId, "true")

        deleteScope(packageName = "com.example.app", scopeId = extraScopeId)

        assertTrue(database.agentDao().getConfigurationValues(appId).none { it.scope == extraScopeId })
    }

    @Test
    fun `deleting the default scope is rejected and the scope still exists`() {
        val appId = insertApplication("com.example.app", "Example")
        val defaultScopeId = insertScope(appId, "toggles_default", epochMillis = 0)

        val response = deleteScope(packageName = "com.example.app", scopeId = defaultScopeId)

        assertEquals("invalid_argument", errorCode(response))
        assertEquals(defaultScopeId, database.agentMutationDao().getScope(defaultScopeId)?.id)
    }

    @Test
    fun `deleting the selected scope moves selection to the next most recent scope and the summary says which`() {
        val appId = insertApplication("com.example.app", "Example")
        insertScope(appId, "toggles_default", epochMillis = 0)
        val devScopeId = insertScope(appId, "Development scope", epochMillis = 10_000)
        val newestScopeId = insertScope(appId, "newest scope", epochMillis = 20_000)
        assertEquals(newestScopeId, selectedScopeId("com.example.app"))

        val response = deleteScope(packageName = "com.example.app", scopeId = newestScopeId)

        val decoded = decode(response)
        assertEquals("deleteScope", decoded.method)
        assertTrue(
            "expected the summary to name the newly selected scope, got: ${decoded.summary}",
            decoded.summary.contains("Development scope")
        )
        assertEquals(devScopeId, selectedScopeId("com.example.app"))
    }

    @Test
    fun `deleting a scope from a different application returns invalid_argument and deletes nothing`() {
        val appA = insertApplication("com.example.a", "A")
        val appB = insertApplication("com.example.b", "B")
        insertScope(appA, "toggles_default", epochMillis = 0)
        val scopeB = insertScope(appB, "toggles_default", epochMillis = 0)

        val response = deleteScope(packageName = "com.example.a", scopeId = scopeB)

        assertEquals("invalid_argument", errorCode(response))
        assertEquals(scopeB, database.agentMutationDao().getScope(scopeB)?.id)
    }

    @Test
    fun `deleting an unknown scope returns unknown_id`() {
        insertApplication("com.example.app", "Example")

        val response = deleteScope(packageName = "com.example.app", scopeId = 999L)

        assertEquals("unknown_id", errorCode(response))
    }

    @Test
    fun `deleting a scope for an unknown package returns unknown_package`() {
        val response = deleteScope(packageName = "com.example.missing", scopeId = 1L)

        assertEquals("unknown_package", errorCode(response))
    }

    @Test
    fun `deleting a scope for a disabled application returns agent_control_disabled and deletes nothing`() {
        val appId = insertApplication("com.example.app", "Example")
        insertScope(appId, "toggles_default", epochMillis = 0)
        val extraScopeId = insertScope(appId, "extra scope", epochMillis = 10_000)
        database.openHelper.writableDatabase.execSQL(
            "UPDATE application SET agentControlEnabled = 0 WHERE id = $appId"
        )

        val response = deleteScope(packageName = "com.example.app", scopeId = extraScopeId)

        assertEquals("agent_control_disabled", errorCode(response))
        assertEquals(extraScopeId, database.agentMutationDao().getScope(extraScopeId)?.id)
    }

    @Test
    fun `deleting a scope notifies the notifier that scopes changed`() {
        val appId = insertApplication("com.example.app", "Example")
        insertScope(appId, "toggles_default", epochMillis = 0)
        val extraScopeId = insertScope(appId, "extra scope", epochMillis = 10_000)

        deleteScope(packageName = "com.example.app", scopeId = extraScopeId)

        assertEquals(1, notifier.scopesNotified)
    }

    // --- agent control notifications (B10) ---

    @Test
    fun `a successful mutation notifies the control notifier naming the package and its label`() {
        val appId = insertApplication("com.example.app", "Example")
        val scopeId = insertScope(appId, "toggles_default")
        val configId = insertConfiguration(appId, "feature_x", "boolean")

        call(configurationId = configId, scopeId = scopeId, value = "true")

        assertEquals(listOf("com.example.app" to "Example"), controlNotifier.notified)
    }

    @Test
    fun `a rejected mutation does not notify the control notifier`() {
        val appId = insertApplication("com.example.app", "Example")
        val scopeId = insertScope(appId, "toggles_default")
        val configId = insertConfiguration(appId, "feature_x", "boolean")
        insertValue(configId, scopeId, "false")

        call(configurationId = configId, scopeId = scopeId, value = "not-a-boolean")

        assertTrue(controlNotifier.notified.isEmpty())
    }

    @Test
    fun `a mutation for a disabled application does not notify the control notifier`() {
        val appId = insertApplication("com.example.app", "Example")
        val scopeId = insertScope(appId, "toggles_default")
        val configId = insertConfiguration(appId, "feature_x", "boolean")
        database.openHelper.writableDatabase.execSQL(
            "UPDATE application SET agentControlEnabled = 0 WHERE id = $appId"
        )

        call(configurationId = configId, scopeId = scopeId, value = "true")

        assertTrue(controlNotifier.notified.isEmpty())
    }

    @Test
    fun `deleting a configuration notifies the control notifier`() {
        val appId = insertApplication("com.example.app", "Example")
        val configId = insertConfiguration(appId, "feature_x", "boolean")

        deleteConfiguration(configId)

        assertEquals(listOf("com.example.app" to "Example"), controlNotifier.notified)
    }

    @Test
    fun `deleting a scope notifies the control notifier`() {
        val appId = insertApplication("com.example.app", "Example")
        insertScope(appId, "toggles_default", epochMillis = 0)
        val extraScopeId = insertScope(appId, "extra scope", epochMillis = 10_000)

        deleteScope(packageName = "com.example.app", scopeId = extraScopeId)

        assertEquals(listOf("com.example.app" to "Example"), controlNotifier.notified)
    }

    @Test
    fun `removing an override notifies the control notifier`() {
        val appId = insertApplication("com.example.app", "Example")
        val defaultScopeId = insertScope(appId, "toggles_default")
        val devScopeId = insertScope(appId, "Development scope")
        val configId = insertConfiguration(appId, "feature_x", "boolean")
        insertValue(configId, defaultScopeId, "true")
        insertValue(configId, devScopeId, "false")

        deleteConfigurationValue(configId, devScopeId)

        assertEquals(listOf("com.example.app" to "Example"), controlNotifier.notified)
    }

    @Test
    fun `removing a non-existent override does not notify the control notifier`() {
        val appId = insertApplication("com.example.app", "Example")
        val defaultScopeId = insertScope(appId, "toggles_default")
        val devScopeId = insertScope(appId, "Development scope")
        val configId = insertConfiguration(appId, "feature_x", "boolean")
        insertValue(configId, defaultScopeId, "true")

        deleteConfigurationValue(configId, devScopeId)

        assertTrue(controlNotifier.notified.isEmpty())
    }

    @Test
    fun `deleting the default scope is rejected and does not notify the control notifier`() {
        val appId = insertApplication("com.example.app", "Example")
        val defaultScopeId = insertScope(appId, "toggles_default", epochMillis = 0)

        deleteScope(packageName = "com.example.app", scopeId = defaultScopeId)

        assertTrue(controlNotifier.notified.isEmpty())
    }

    @Test
    fun `delivering the disable broadcast disables agent control and a subsequent mutation is rejected`() {
        val appId = insertApplication("com.example.app", "Example")
        val scopeId = insertScope(appId, "toggles_default")
        val configId = insertConfiguration(appId, "feature_x", "boolean")

        val context = ApplicationProvider.getApplicationContext<Application>()
        val receiver = AgentControlDisableReceiver()
        receiver.entryPointBuilder = object : AgentControlDisableReceiver.EntryPointBuilder {
            override fun build(context: Context) =
                object : AgentControlDisableReceiver.AgentControlDisableReceiverEntryPoint {
                    override fun provideAgentMutationDao(): AgentMutationDao = database.agentMutationDao()
                }
        }
        // Runs the "background" work inline so the DAO call has completed by the time onReceive
        // returns, rather than racing a real background thread from the test.
        receiver.executor = Executor { it.run() }

        receiver.onReceive(
            context,
            Intent(AgentControlDisableReceiver.ACTION_DISABLE_AGENT_CONTROL).apply {
                putExtra(AgentControlDisableReceiver.EXTRA_PACKAGE_NAME, "com.example.app")
            }
        )

        val response = call(configurationId = configId, scopeId = scopeId, value = "true")

        assertEquals("agent_control_disabled", errorCode(response))
        assertEquals(false, database.agentDao().getApplicationById(appId)?.agentControlEnabled)
    }

    @Test
    fun `a mutation still succeeds and the value still changes when posting the notification fails`() {
        val appId = insertApplication("com.example.app", "Example")
        val scopeId = insertScope(appId, "toggles_default")
        val configId = insertConfiguration(appId, "feature_x", "boolean")
        insertValue(configId, scopeId, "false")

        // A Context whose NotificationManager access always blows up — simulating, among other
        // things, POST_NOTIFICATIONS having been denied — wired into the REAL notifier so this
        // proves SystemAgentControlNotifier's own defensiveness, not just that a test fake stayed
        // quiet.
        val brokenContext = object : android.content.ContextWrapper(
            ApplicationProvider.getApplicationContext()
        ) {
            override fun getSystemService(name: String): Any? {
                if (name == Context.NOTIFICATION_SERVICE) {
                    throw SecurityException("notifications not permitted")
                }
                return super.getSystemService(name)
            }
        }
        val handlerWithRealNotifier = AgentCallHandler(
            agentDao = database.agentDao(),
            agentMutationDao = database.agentMutationDao(),
            changeNotifier = notifier,
            controlNotifier = SystemAgentControlNotifier(brokenContext),
            clock = fixedClock,
            packageManager = ApplicationProvider.getApplicationContext<Application>().packageManager
        )

        val response = handlerWithRealNotifier.handle(
            "setConfigurationValue",
            Bundle().apply {
                putLong("configurationId", configId)
                putLong("scopeId", scopeId)
                putString("value", "true")
            }
        )

        assertEquals("setConfigurationValue", decode(response).method)
        assertEquals("true", effectiveValue("com.example.app"))
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

    private class RecordingControlNotifier : AgentControlNotifier {
        val notified = mutableListOf<Pair<String, String>>()

        override fun notifyFirstMutation(packageName: String, applicationLabel: String) {
            notified += packageName to applicationLabel
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

    private fun createConfiguration(packageName: String, key: String, type: String): String =
        handler.handle(
            "createConfiguration",
            Bundle().apply {
                putString("package", packageName)
                putString("key", key)
                putString("type", type)
            }
        )

    private fun deleteConfiguration(configurationId: Long): String =
        handler.handle(
            "deleteConfiguration",
            Bundle().apply {
                putLong("configurationId", configurationId)
            }
        )

    private fun deleteConfigurationValue(configurationId: Long, scopeId: Long): String =
        handler.handle(
            "deleteConfigurationValue",
            Bundle().apply {
                putLong("configurationId", configurationId)
                putLong("scopeId", scopeId)
            }
        )

    private fun deleteScope(packageName: String, scopeId: Long): String =
        handler.handle(
            "deleteScope",
            Bundle().apply {
                putString("package", packageName)
                putLong("scopeId", scopeId)
            }
        )

    /** Makes [packageName] "installed" as far as PackageManager.getApplicationInfo is concerned. */
    private fun installPackage(packageName: String, label: String) {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val packageInfo = PackageInfo().apply {
            this.packageName = packageName
            applicationInfo = ApplicationInfo().apply {
                this.packageName = packageName
                nonLocalizedLabel = label
            }
        }
        shadowOf(context.packageManager).installPackage(packageInfo)
    }

    private fun applicationDetail(packageName: String): AgentApplicationDetail {
        val readHandler = AgentReadHandler(
            agentDao = database.agentDao(),
            uriMatcher = AgentUriMatcher(AgentDescription.AGENT_AUTHORITY),
            appVersionName = "1.2.3"
        )
        val uri = android.net.Uri.parse("content://${AgentDescription.AGENT_AUTHORITY}/apps/$packageName")
        return json.decodeFromString(readHandler.handle(uri, apiEnabled = true))
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

    private fun insertValue(configurationId: Long, scopeId: Long, value: String): Long =
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
