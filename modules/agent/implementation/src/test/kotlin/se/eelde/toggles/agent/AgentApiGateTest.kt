package se.eelde.toggles.agent

import android.content.Context
import android.os.Build
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
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

/**
 * [AgentApiGate] must resolve the `beta_agent_api` toggle for the Toggles app's own package
 * ([Context.getPackageName]) specifically — never for whatever application happens to have a
 * configuration keyed `beta_agent_api`. See `another application's beta_agent_api toggle does not
 * enable the gate` below for the test that pins that down.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class AgentApiGateTest {

    private lateinit var database: TogglesDatabase
    private lateinit var context: Context
    private lateinit var gate: AgentApiGate

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, TogglesDatabase::class.java)
            .allowMainThreadQueries().build()
        gate = AgentApiGate(agentDao = database.agentDao(), context = context)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `disabled when the toggles app itself is not registered at all`() {
        assertFalse(gate.isEnabled())
    }

    @Test
    fun `disabled when the toggles app has no beta_agent_api configuration`() {
        insertApplication(context.packageName)

        assertFalse(gate.isEnabled())
    }

    @Test
    fun `disabled when beta_agent_api is explicitly set to false`() {
        setBetaAgentApi(context.packageName, "false")

        assertFalse(gate.isEnabled())
    }

    @Test
    fun `enabled when beta_agent_api is set to true`() {
        setBetaAgentApi(context.packageName, "true")

        assertTrue(gate.isEnabled())
    }

    @Test
    fun `disabled when beta_agent_api holds an unrecognized value`() {
        setBetaAgentApi(context.packageName, "yes")

        assertFalse(gate.isEnabled())
    }

    @Test
    fun `another application's beta_agent_api toggle does not enable the gate`() {
        // The Toggles app itself is registered but has never set the toggle.
        insertApplication(context.packageName)
        // Some other app, however, has created and enabled a configuration with the exact same key.
        setBetaAgentApi("com.example.other", "true")

        assertFalse(
            "a third-party application's own beta_agent_api toggle must never flip the global gate",
            gate.isEnabled()
        )
    }

    private fun setBetaAgentApi(packageName: String, value: String) {
        val applicationId = insertApplication(packageName)
        val scopeId = insertScope(applicationId, TogglesScope.SCOPE_DEFAULT)
        val configurationId = insertConfiguration(applicationId, AgentApiGate.BETA_AGENT_API_KEY)
        insertValue(configurationId, scopeId, value)
    }

    private fun insertApplication(packageName: String): Long =
        database.providerApplicationDao().insert(
            TogglesApplication(
                id = 0,
                shortcutId = packageName,
                packageName = packageName,
                applicationLabel = packageName
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

    private fun insertConfiguration(applicationId: Long, key: String): Long =
        database.providerConfigurationDao().insert(
            TogglesConfiguration(
                id = 0,
                applicationId = applicationId,
                key = key,
                type = "boolean",
                lastUse = Clock.System.now()
            )
        )

    private fun insertValue(configurationId: Long, scopeId: Long, value: String): Long =
        database.providerConfigurationValueDao().insertSync(
            TogglesConfigurationValue(id = 0, configurationId = configurationId, value = value, scope = scopeId)
        )
}
