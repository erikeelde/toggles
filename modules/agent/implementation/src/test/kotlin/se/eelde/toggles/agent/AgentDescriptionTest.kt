package se.eelde.toggles.agent

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AgentDescriptionTest {

    private val json = Json { ignoreUnknownKeys = false }

    private fun describe(): AgentDescriptionDocument =
        json.decodeFromString(AgentDescription.json(appVersionName = "1.2.3"))

    @Test
    fun `describe reports the agent api version and the app version`() {
        val document = describe()

        assertEquals(1, document.agentApiVersion)
        assertEquals("1.2.3", document.togglesAppVersion)
        assertEquals("se.eelde.toggles.agentprovider", document.authority)
    }

    @Test
    fun `describe lists the read endpoints`() {
        assertEquals(
            listOf("/describe", "/apps", "/apps/{package}"),
            describe().endpoints.map { it.path }
        )
    }

    @Test
    fun `every endpoint carries a runnable example command`() {
        describe().endpoints.forEach { endpoint ->
            assertTrue(
                "endpoint ${endpoint.path} is missing an example command",
                endpoint.example.startsWith("adb shell content read --uri content://")
            )
        }
    }

    @Test
    fun `describe explains that resolution consults only two scopes`() {
        val notes = describe().model.scopeResolution

        assertTrue(notes.contains("selected"))
        assertTrue(notes.contains("default"))
        assertTrue(
            "scope resolution notes must warn that other scopes are ignored, got: $notes",
            notes.contains("ignored")
        )
    }

    @Test
    fun `describe lists the configuration types`() {
        assertEquals(
            listOf("boolean", "integer", "string", "enum"),
            describe().model.configurationTypes
        )
    }

    @Test
    fun `describe lists every error code the api can return`() {
        assertEquals(AgentErrorCode.entries.map { it.wireValue }, describe().errors)
    }

    @Test
    fun `describe is valid json that round trips`() {
        val encoded = AgentDescription.json(appVersionName = "9.9.9")
        val decoded = json.decodeFromString<AgentDescriptionDocument>(encoded)

        assertEquals("9.9.9", decoded.togglesAppVersion)
    }
}
