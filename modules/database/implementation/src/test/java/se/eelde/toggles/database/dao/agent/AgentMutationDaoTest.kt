package se.eelde.toggles.database.dao.agent

import android.database.sqlite.SQLiteConstraintException
import android.os.Build
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
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
import kotlin.time.Instant

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class AgentMutationDaoTest {

    private lateinit var database: TogglesDatabase
    private lateinit var agentDao: AgentDao
    private lateinit var agentMutationDao: AgentMutationDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            TogglesDatabase::class.java
        ).allowMainThreadQueries().build()
        agentDao = database.agentDao()
        agentMutationDao = database.agentMutationDao()
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

    @Test
    fun `insertConfigurationValue then updateConfigurationValue changes the value and leaves one row`() {
        val appA = insertApplication("com.example.a", "A")
        val configA = insertConfiguration(appA, "featureA")
        val scopeA = insertScope(appA, "default")

        agentMutationDao.insertConfigurationValue(
            TogglesConfigurationValue(id = 0, configurationId = configA, value = "false", scope = scopeA)
        )
        val updated = agentMutationDao.updateConfigurationValue(configA, scopeA, "true")

        assertEquals(1, updated)
        val values = agentDao.getConfigurationValues(appA)
        assertEquals(1, values.size)
        assertEquals("true", values[0].value)
    }

    @Test
    fun `findConfigurationValueId returns the id when present and null otherwise`() {
        val appA = insertApplication("com.example.a", "A")
        val configA = insertConfiguration(appA, "featureA")
        val scopeA = insertScope(appA, "default")

        assertNull(agentMutationDao.findConfigurationValueId(configA, scopeA))

        val id = agentMutationDao.insertConfigurationValue(
            TogglesConfigurationValue(id = 0, configurationId = configA, value = "true", scope = scopeA)
        )

        assertEquals(id, agentMutationDao.findConfigurationValueId(configA, scopeA))
    }

    @Test
    fun `insertScope creates a scope and returns its id`() {
        val appA = insertApplication("com.example.a", "A")

        val scopeId = agentMutationDao.insertScope(
            TogglesScope(id = 0, applicationId = appA, name = "staging", timeStamp = Instant.fromEpochMilliseconds(0))
        )

        assertNotEquals(0L, scopeId)
        val scope = agentMutationDao.getScope(scopeId)
        assertEquals("staging", scope?.name)
    }

    @Test
    fun `inserting a scope with a duplicate applicationId and name throws SQLiteConstraintException`() {
        val appA = insertApplication("com.example.a", "A")
        insertScope(appA, "default")

        // The (applicationId, name) unique index rejects the duplicate insert outright, rather
        // than silently ignoring it or upserting — verified by running it, not assumed.
        assertThrows(SQLiteConstraintException::class.java) {
            agentMutationDao.insertScope(
                TogglesScope(
                    id = 0,
                    applicationId = appA,
                    name = "default",
                    timeStamp = Instant.fromEpochMilliseconds(0)
                )
            )
        }
    }

    @Test
    fun `touchScope updates selectedTimestamp so getScopes reports it as the latest`() {
        val appA = insertApplication("com.example.a", "A")
        val scopeOld = insertScope(appA, "old")
        val scopeNew = insertScope(appA, "new")

        agentMutationDao.touchScope(scopeOld, Instant.fromEpochMilliseconds(5_000))

        val scopes = agentDao.getScopes(appA)
        val latest = scopes.maxByOrNull { it.timeStamp }
        assertEquals(scopeOld, latest?.id)
        assertNotEquals(scopeNew, latest?.id)
    }

    @Test
    fun `insertConfiguration returns an id and deleteConfiguration removes it and returns 1`() {
        val appA = insertApplication("com.example.a", "A")

        val configId = agentMutationDao.insertConfiguration(
            TogglesConfiguration(
                id = 0,
                applicationId = appA,
                key = "featureA",
                type = "boolean",
                lastUse = Instant.fromEpochMilliseconds(0)
            )
        )

        assertNotEquals(0L, configId)
        assertEquals("featureA", agentMutationDao.getConfiguration(configId)?.key)

        val deleted = agentMutationDao.deleteConfiguration(configId)

        assertEquals(1, deleted)
        assertNull(agentMutationDao.getConfiguration(configId))
    }

    @Test
    fun `deleteConfiguration cascades to its configurationValue rows`() {
        val appA = insertApplication("com.example.a", "A")
        val configA = insertConfiguration(appA, "featureA")
        val scopeA = insertScope(appA, "default")
        agentMutationDao.insertConfigurationValue(
            TogglesConfigurationValue(id = 0, configurationId = configA, value = "true", scope = scopeA)
        )

        // Room enables PRAGMA foreign_keys=ON by default, but the cascade is asserted for real
        // here rather than assumed.
        agentMutationDao.deleteConfiguration(configA)

        assertTrue(agentDao.getConfigurationValues(appA).isEmpty())
    }

    @Test
    fun `insertApplication creates an application row defaulting agentControlEnabled to true`() {
        val id = agentMutationDao.insertApplication(
            TogglesApplication(
                id = 0,
                shortcutId = "com.example.new",
                packageName = "com.example.new",
                applicationLabel = "New"
            )
        )

        assertNotEquals(0L, id)
        val application = agentDao.getApplicationByPackageName("com.example.new")
        assertTrue(application?.agentControlEnabled == true)
    }

    @Test
    fun `setAgentControlEnabled flips the flag and getApplicationByPackageName reflects it`() {
        insertApplication("com.example.a", "A")

        val updated = agentMutationDao.setAgentControlEnabled("com.example.a", false)

        assertEquals(1, updated)
        val application = agentDao.getApplicationByPackageName("com.example.a")
        assertFalse(application?.agentControlEnabled == true)
    }

    @Test
    fun `touch updates lastUse and leaves other configurations untouched`() {
        val appA = insertApplication("com.example.a", "A")
        val configA = insertConfiguration(appA, "featureA")
        val configB = insertConfiguration(appA, "featureB")

        val updated = agentMutationDao.touch(configA, Instant.fromEpochMilliseconds(5_000))

        assertEquals(1, updated)
        assertEquals(Instant.fromEpochMilliseconds(5_000), agentMutationDao.getConfiguration(configA)?.lastUse)
        assertEquals(Instant.fromEpochMilliseconds(0), agentMutationDao.getConfiguration(configB)?.lastUse)
    }

    @Test
    fun `getConfiguration and getScope return the row by id, and null for an unknown id`() {
        val appA = insertApplication("com.example.a", "A")
        val configA = insertConfiguration(appA, "featureA")
        val scopeA = insertScope(appA, "default")

        assertEquals("featureA", agentMutationDao.getConfiguration(configA)?.key)
        assertEquals("default", agentMutationDao.getScope(scopeA)?.name)

        assertNull(agentMutationDao.getConfiguration(999L))
        assertNull(agentMutationDao.getScope(999L))
    }

    @Test
    fun `deleteConfigurationValue removes the matching row and returns 1, leaving other scopes' rows`() {
        val appA = insertApplication("com.example.a", "A")
        val configA = insertConfiguration(appA, "featureA")
        val scopeA = insertScope(appA, "default")
        val scopeB = insertScope(appA, "other")
        agentMutationDao.insertConfigurationValue(
            TogglesConfigurationValue(id = 0, configurationId = configA, value = "true", scope = scopeA)
        )
        agentMutationDao.insertConfigurationValue(
            TogglesConfigurationValue(id = 0, configurationId = configA, value = "false", scope = scopeB)
        )

        val deleted = agentMutationDao.deleteConfigurationValue(configA, scopeA)

        assertEquals(1, deleted)
        val remaining = agentDao.getConfigurationValues(appA)
        assertEquals(1, remaining.size)
        assertEquals(scopeB, remaining[0].scope)
    }

    @Test
    fun `deleteConfigurationValue returns 0 when there is no matching row`() {
        val appA = insertApplication("com.example.a", "A")
        val configA = insertConfiguration(appA, "featureA")
        val scopeA = insertScope(appA, "default")

        val deleted = agentMutationDao.deleteConfigurationValue(configA, scopeA)

        assertEquals(0, deleted)
    }

}
