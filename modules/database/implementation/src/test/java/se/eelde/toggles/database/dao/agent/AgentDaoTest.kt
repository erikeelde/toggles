package se.eelde.toggles.database.dao.agent

import android.os.Build
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
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
import kotlin.time.Instant

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class AgentDaoTest {

    private lateinit var database: TogglesDatabase
    private lateinit var agentDao: AgentDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            TogglesDatabase::class.java
        ).allowMainThreadQueries().build()
        agentDao = database.agentDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

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

    private fun insertConfiguration(applicationId: Long, key: String): Long =
        database.providerConfigurationDao().insert(
            TogglesConfiguration(
                id = 0,
                applicationId = applicationId,
                key = key,
                type = "boolean",
                lastUse = Instant.fromEpochMilliseconds(0)
            )
        )

    private fun insertConfigurationValue(configurationId: Long, scopeId: Long, value: String): Long =
        database.providerConfigurationValueDao().insertSync(
            TogglesConfigurationValue(
                id = 0,
                configurationId = configurationId,
                value = value,
                scope = scopeId
            )
        )

    private fun insertPredefinedConfigurationValue(configurationId: Long, value: String): Long =
        database.providerPredefinedConfigurationValueDao().insert(
            TogglesPredefinedConfigurationValue(
                id = 0,
                configurationId = configurationId,
                value = value
            )
        )

    @Test
    fun `getApplications returns inserted applications`() {
        insertApplication("com.example.app", "Example")

        val applications = agentDao.getApplications()

        assertEquals(1, applications.size)
        assertEquals("com.example.app", applications[0].packageName)
        assertTrue(applications[0].agentControlEnabled)
    }

    @Test
    fun `getApplications is empty when nothing is registered`() {
        assertTrue(agentDao.getApplications().isEmpty())
    }

    @Test
    fun `getApplicationByPackageName returns null for an unknown package`() {
        assertNull(agentDao.getApplicationByPackageName("com.example.missing"))
    }

    @Test
    fun `getApplicationByPackageName returns the matching application`() {
        insertApplication("com.example.app", "Example")

        val application = agentDao.getApplicationByPackageName("com.example.app")

        assertEquals("Example", application?.applicationLabel)
    }

    @Test
    fun `getApplicationByPackageName selects the right one when several exist`() {
        insertApplication("com.example.a", "A")
        insertApplication("com.example.b", "B")

        assertEquals("B", agentDao.getApplicationByPackageName("com.example.b")?.applicationLabel)
        assertEquals(2, agentDao.getApplications().size)
    }

    @Test
    fun `getApplicationById returns the matching application and null for an unknown id`() {
        val appA = insertApplication("com.example.a", "A")
        insertApplication("com.example.b", "B")

        assertEquals("A", agentDao.getApplicationById(appA)?.applicationLabel)
        assertNull(agentDao.getApplicationById(999L))
    }

    @Test
    fun `getConfigurations returns only the requested application's configurations, ordered by key`() {
        val appA = insertApplication("com.example.a", "A")
        val appB = insertApplication("com.example.b", "B")
        insertConfiguration(appA, "zebra")
        insertConfiguration(appA, "apple")
        insertConfiguration(appB, "middle")

        val configurations = agentDao.getConfigurations(appA)

        assertEquals(listOf("apple", "zebra"), configurations.map { it.key })
    }

    @Test
    fun `getConfigurations is empty for an application with no configurations`() {
        val appA = insertApplication("com.example.a", "A")
        insertApplication("com.example.b", "B").also { insertConfiguration(it, "middle") }

        assertTrue(agentDao.getConfigurations(appA).isEmpty())
    }

    @Test
    fun `getConfigurations is empty for an applicationId that does not exist`() {
        insertApplication("com.example.a", "A").also { insertConfiguration(it, "featureA") }

        assertTrue(agentDao.getConfigurations(999L).isEmpty())
    }

    @Test
    fun `getScopes returns only the requested application's scopes, ordered by id`() {
        val appA = insertApplication("com.example.a", "A")
        val appB = insertApplication("com.example.b", "B")
        insertScope(appA, "beta")
        insertScope(appA, "alpha")
        insertScope(appB, "gamma")

        val scopes = agentDao.getScopes(appA)

        assertEquals(listOf("beta", "alpha"), scopes.map { it.name })
        assertEquals(2, scopes.size)
    }

    @Test
    fun `getConfigurationValues returns only the requested application's values`() {
        // Filler applications and a filler configuration desynchronize the application id
        // sequence from the configuration id sequence, so a query that accidentally filters on
        // configuration.id instead of configuration.applicationId cannot coincidentally pass.
        insertApplication("com.example.filler1", "Filler1")
        insertApplication("com.example.filler2", "Filler2")
        insertApplication("com.example.filler3", "Filler3")
        val appA = insertApplication("com.example.a", "A")
        val appB = insertApplication("com.example.b", "B")
        val configA = insertConfiguration(appA, "featureA")
        val configB = insertConfiguration(appB, "featureB")
        val scopeA = insertScope(appA, "default")
        val scopeB = insertScope(appB, "default")
        insertConfigurationValue(configA, scopeA, "valueA")
        insertConfigurationValue(configB, scopeB, "valueB")

        check(appA != configA) { "test setup must desynchronize the id sequences" }

        val values = agentDao.getConfigurationValues(appA)

        assertEquals(1, values.size)
        assertEquals("valueA", values[0].value)
        assertTrue(values.none { it.value == "valueB" })
    }

    @Test
    fun `getConfigurationValues is empty when configurations exist but have no value rows`() {
        val appA = insertApplication("com.example.a", "A")
        insertConfiguration(appA, "featureA")

        assertTrue(agentDao.getConfigurationValues(appA).isEmpty())
    }

    @Test
    fun `getConfigurationValues is empty for an applicationId that does not exist`() {
        val appA = insertApplication("com.example.a", "A")
        val configA = insertConfiguration(appA, "featureA")
        val scopeA = insertScope(appA, "default")
        insertConfigurationValue(configA, scopeA, "valueA")

        assertTrue(agentDao.getConfigurationValues(999L).isEmpty())
    }

    @Test
    fun `getConfigurationValues returns all scoped values for a configuration`() {
        val appA = insertApplication("com.example.a", "A")
        val configA = insertConfiguration(appA, "featureA")
        val defaultScope = insertScope(appA, "default")
        val devScope = insertScope(appA, "development")
        insertConfigurationValue(configA, defaultScope, "defaultValue")
        insertConfigurationValue(configA, devScope, "devValue")

        val values = agentDao.getConfigurationValues(appA)

        assertEquals(setOf("defaultValue", "devValue"), values.map { it.value }.toSet())
        assertEquals(2, values.size)
    }

    @Test
    fun `getPredefinedConfigurationValues returns only the requested application's values`() {
        // Filler applications and a filler configuration desynchronize the application id
        // sequence from the configuration id sequence, so a query that accidentally filters on
        // configuration.id instead of configuration.applicationId cannot coincidentally pass.
        insertApplication("com.example.filler1", "Filler1")
        insertApplication("com.example.filler2", "Filler2")
        insertApplication("com.example.filler3", "Filler3")
        val appA = insertApplication("com.example.a", "A")
        val appB = insertApplication("com.example.b", "B")
        val configA = insertConfiguration(appA, "featureA")
        val configB = insertConfiguration(appB, "featureB")
        insertPredefinedConfigurationValue(configA, "predefinedA")
        insertPredefinedConfigurationValue(configB, "predefinedB")

        check(appA != configA) { "test setup must desynchronize the id sequences" }

        val values = agentDao.getPredefinedConfigurationValues(appA)

        assertEquals(1, values.size)
        assertEquals("predefinedA", values[0].value)
        assertTrue(values.none { it.value == "predefinedB" })
    }

    @Test
    fun `getPredefinedConfigurationValues returns all predefined values for a configuration`() {
        val appA = insertApplication("com.example.a", "A")
        val configA = insertConfiguration(appA, "featureA")
        insertPredefinedConfigurationValue(configA, "true")
        insertPredefinedConfigurationValue(configA, "false")

        val values = agentDao.getPredefinedConfigurationValues(appA)

        assertEquals(setOf("true", "false"), values.map { it.value }.toSet())
        assertEquals(2, values.size)
    }
}
