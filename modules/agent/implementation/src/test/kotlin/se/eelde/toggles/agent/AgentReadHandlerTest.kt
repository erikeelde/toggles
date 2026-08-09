package se.eelde.toggles.agent

import android.net.Uri
import android.os.Build
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import se.eelde.toggles.database.TogglesApplication
import se.eelde.toggles.database.TogglesConfiguration
import se.eelde.toggles.database.TogglesConfigurationValue
import se.eelde.toggles.database.TogglesDatabase
import se.eelde.toggles.database.TogglesScope
import kotlin.time.Clock
import kotlin.time.Instant

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class AgentReadHandlerTest {

    private val json = Json { ignoreUnknownKeys = false }

    private lateinit var database: TogglesDatabase
    private lateinit var handler: AgentReadHandler

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            TogglesDatabase::class.java
        ).allowMainThreadQueries().build()
        handler = AgentReadHandler(
            agentDao = database.agentDao(),
            uriMatcher = AgentUriMatcher(AgentDescription.AGENT_AUTHORITY),
            appVersionName = "1.2.3"
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `describe is served`() {
        val document =
            json.decodeFromString<AgentDescriptionDocument>(handler.handle(uri("/describe"), apiEnabled = true))

        assertEquals(1, document.agentApiVersion)
    }

    @Test
    fun `unknown endpoint returns unknown_endpoint`() {
        assertEquals("unknown_endpoint", errorCode(handler.handle(uri("/nope"), apiEnabled = true)))
    }

    @Test
    fun `describe is served and reports enabled true when the api is enabled`() {
        val document =
            json.decodeFromString<AgentDescriptionDocument>(handler.handle(uri("/describe"), apiEnabled = true))

        assertTrue(document.enabled)
    }

    @Test
    fun `describe is still served and reports enabled false when the api is disabled`() {
        val document =
            json.decodeFromString<AgentDescriptionDocument>(handler.handle(uri("/describe"), apiEnabled = false))

        assertEquals(1, document.agentApiVersion)
        assertFalse(document.enabled)
    }

    @Test
    fun `apps returns agent_api_disabled when the api is disabled`() {
        insertApplication("com.example.app", "Example")

        assertEquals("agent_api_disabled", errorCode(handler.handle(uri("/apps"), apiEnabled = false)))
    }

    @Test
    fun `an application detail returns agent_api_disabled when the api is disabled`() {
        insertApplication("com.example.app", "Example")

        assertEquals(
            "agent_api_disabled",
            errorCode(handler.handle(uri("/apps/com.example.app"), apiEnabled = false))
        )
    }

    @Test
    fun `apps lists known applications`() {
        insertApplication("com.example.app", "Example")

        val list = json.decodeFromString<AgentApplicationList>(handler.handle(uri("/apps"), apiEnabled = true))

        assertEquals(1, list.applications.size)
        assertEquals("com.example.app", list.applications[0].packageName)
        assertTrue(list.applications[0].agentControlEnabled)
    }

    @Test
    fun `apps is empty when nothing is registered`() {
        val list = json.decodeFromString<AgentApplicationList>(handler.handle(uri("/apps"), apiEnabled = true))

        assertTrue(list.applications.isEmpty())
    }

    @Test
    fun `unknown package returns unknown_package`() {
        assertEquals("unknown_package", errorCode(handler.handle(uri("/apps/com.example.missing"), apiEnabled = true)))
    }

    @Test
    fun `disabled application returns agent_control_disabled`() {
        val applicationId = insertApplication("com.example.app", "Example")
        database.openHelper.writableDatabase.execSQL(
            "UPDATE application SET agentControlEnabled = 0 WHERE id = $applicationId"
        )

        assertEquals(
            "agent_control_disabled",
            errorCode(handler.handle(uri("/apps/com.example.app"), apiEnabled = true))
        )
    }

    @Test
    fun `application detail reports the effective value from the selected scope`() {
        val applicationId = insertApplication("com.example.app", "Example")
        val defaultScopeId =
            insertScope(applicationId, "toggles_default", Instant.fromEpochMilliseconds(1))
        val devScopeId =
            insertScope(applicationId, "Development scope", Instant.fromEpochMilliseconds(2))
        val configurationId = insertConfiguration(applicationId, "feature_x", "boolean")
        insertValue(configurationId, defaultScopeId, "false")
        insertValue(configurationId, devScopeId, "true")

        val configuration = detail("com.example.app").configurations.single()

        assertEquals("feature_x", configuration.key)
        assertEquals("boolean", configuration.type)
        assertEquals("true", configuration.effectiveValue)
        assertEquals(2, configuration.values.size)
    }

    @Test
    fun `application detail falls back to the default scope`() {
        val applicationId = insertApplication("com.example.app", "Example")
        val defaultScopeId =
            insertScope(applicationId, "toggles_default", Instant.fromEpochMilliseconds(1))
        insertScope(applicationId, "Development scope", Instant.fromEpochMilliseconds(2))
        val configurationId = insertConfiguration(applicationId, "feature_x", "boolean")
        insertValue(configurationId, defaultScopeId, "false")

        assertEquals("false", detail("com.example.app").configurations.single().effectiveValue)
    }

    // A prior version of this test proved that a null-valued row in the selected scope still won
    // over the default scope (key presence, not value nullability, decides resolution). That
    // state is no longer representable: `configurationValue.value` is NOT NULL at the database
    // level (see MIGRATION_11_12), so `insertValue` can no longer construct such a row - inserting
    // one would throw a SQLiteConstraintException instead of exercising the code path this test
    // meant to cover.

    @Test
    fun `a value in an unresolved scope is reported but does not affect the effective value`() {
        val applicationId = insertApplication("com.example.app", "Example")
        val defaultScopeId =
            insertScope(applicationId, "toggles_default", Instant.fromEpochMilliseconds(1))
        insertScope(applicationId, "Development scope", Instant.fromEpochMilliseconds(3))
        val qaScopeId = insertScope(applicationId, "QA", Instant.fromEpochMilliseconds(2))
        val configurationId = insertConfiguration(applicationId, "feature_x", "boolean")
        insertValue(configurationId, defaultScopeId, "false")
        insertValue(configurationId, qaScopeId, "true")

        val configuration = detail("com.example.app").configurations.single()

        // Selected scope is "Development scope" (latest timestamp) and holds no value, so
        // resolution falls through to the default scope. The QA value is reported but ignored.
        assertEquals("false", configuration.effectiveValue)
        assertEquals(2, configuration.values.size)
        assertTrue(configuration.values.any { it.scopeName == "QA" && it.value == "true" })
    }

    @Test
    fun `application detail marks the selected scope`() {
        val applicationId = insertApplication("com.example.app", "Example")
        insertScope(applicationId, "toggles_default", Instant.fromEpochMilliseconds(1))
        insertScope(applicationId, "Development scope", Instant.fromEpochMilliseconds(2))

        val selected = detail("com.example.app").scopes.filter { it.selected }

        assertEquals(1, selected.size)
        assertEquals("Development scope", selected.single().name)
    }

    @Test
    fun `application detail marks the default scope`() {
        val applicationId = insertApplication("com.example.app", "Example")
        insertScope(applicationId, "toggles_default", Instant.fromEpochMilliseconds(1))
        insertScope(applicationId, "Development scope", Instant.fromEpochMilliseconds(2))

        val defaults = detail("com.example.app").scopes.filter { it.default }

        assertEquals(1, defaults.size)
        assertEquals("toggles_default", defaults.single().name)
    }

    @Test
    fun `application detail reports every scope including unresolved ones`() {
        val applicationId = insertApplication("com.example.app", "Example")
        insertScope(applicationId, "toggles_default", Instant.fromEpochMilliseconds(1))
        insertScope(applicationId, "Development scope", Instant.fromEpochMilliseconds(2))
        insertScope(applicationId, "QA", Instant.fromEpochMilliseconds(0))

        assertEquals(3, detail("com.example.app").scopes.size)
    }

    @Test
    fun `an application with no configurations still reports its scopes`() {
        val applicationId = insertApplication("com.example.app", "Example")
        insertScope(applicationId, "toggles_default", Instant.fromEpochMilliseconds(1))

        val document = detail("com.example.app")

        assertTrue(document.configurations.isEmpty())
        assertEquals(1, document.scopes.size)
    }

    @Test
    fun `another application's data does not leak into a detail response`() {
        val appA = insertApplication("com.example.a", "A")
        val appB = insertApplication("com.example.b", "B")
        insertScope(appA, "toggles_default", Instant.fromEpochMilliseconds(1))
        insertScope(appB, "toggles_default", Instant.fromEpochMilliseconds(1))
        insertConfiguration(appA, "feature_a", "boolean")
        insertConfiguration(appB, "feature_b", "boolean")

        val document = detail("com.example.a")

        assertEquals(1, document.configurations.size)
        assertEquals("feature_a", document.configurations.single().key)
    }

    private fun detail(packageName: String): AgentApplicationDetail =
        json.decodeFromString(handler.handle(uri("/apps/$packageName"), apiEnabled = true))

    private fun errorCode(payload: String): String =
        json.decodeFromString<AgentErrorEnvelope>(payload).error.code

    private fun uri(path: String) = Uri.parse("content://${AgentDescription.AGENT_AUTHORITY}$path")

    private fun insertApplication(packageName: String, label: String): Long =
        database.providerApplicationDao().insert(
            TogglesApplication(
                id = 0,
                shortcutId = packageName,
                packageName = packageName,
                applicationLabel = label
            )
        )

    private fun insertScope(applicationId: Long, name: String, timeStamp: Instant): Long =
        database.providerScopeDao().insert(
            TogglesScope(id = 0, applicationId = applicationId, name = name, timeStamp = timeStamp)
        )

    private fun insertConfiguration(applicationId: Long, key: String, type: String): Long =
        database.providerConfigurationDao().insert(
            TogglesConfiguration(
                id = 0,
                applicationId = applicationId,
                key = key,
                type = type,
                lastUse = Clock.System.now()
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
}
