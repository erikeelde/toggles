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
import se.eelde.toggles.database.TogglesDatabase

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
}
