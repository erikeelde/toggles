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
    fun `valueFormats has an entry for every configuration type`() {
        val document = describe()

        assertEquals(
            document.model.configurationTypes.toSet(),
            document.model.valueFormats.keys
        )
    }

    @Test
    fun `the boolean value format warns about the silent-false behaviour`() {
        assertTrue(describe().model.valueFormats.getValue("boolean").contains("false"))
    }

    @Test
    fun `the enum value format points at predefinedValues`() {
        assertTrue(describe().model.valueFormats.getValue("enum").contains("predefinedValues"))
    }

    @Test
    fun `describe lists every error code the api can return`() {
        assertEquals(AgentErrorCode.entries.map { it.wireValue }, describe().errors)
    }

    @Test
    fun `describe lists every mutation method`() {
        assertEquals(
            listOf(
                "setConfigurationValue",
                "createConfiguration",
                "deleteConfiguration",
                "deleteConfigurationValue",
                "createScope",
                "selectScope",
                "deleteScope"
            ),
            describe().methods.map { it.method }
        )
    }

    @Test
    fun `every method carries a runnable call example naming its method`() {
        describe().methods.forEach { method ->
            assertTrue(
                "method ${method.method} is missing a content call example command",
                method.example.startsWith("adb shell content call --uri content://")
            )
            assertTrue(
                "method ${method.method}'s example does not name its method, got: ${method.example}",
                method.example.contains("--method")
            )
        }
    }

    @Test
    fun `every method's example uses correctly typed --extra bindings for its arguments`() {
        describe().methods.forEach { method ->
            method.arguments.forEach { argument ->
                val binding = when (argument.type) {
                    "long" -> "${argument.name}:l:"
                    "string" -> "${argument.name}:s:"
                    else -> error("unhandled argument type \"${argument.type}\" for ${argument.name}")
                }
                assertTrue(
                    "expected ${method.method}'s example to bind ${argument.name} as " +
                        "\"$binding\", got: ${method.example}",
                    method.example.contains(binding)
                )
            }
        }
    }

    @Test
    fun `the model documents the Result Bundle wrapper content call output needs stripped`() {
        val notes = describe().model.callResultWrapping

        assertTrue(notes.contains("Result: Bundle"))
        assertTrue(
            "the note must say the wrapper needs to be stripped, got: $notes",
            notes.contains("strip")
        )
    }

    @Test
    fun `describe is valid json that round trips`() {
        val encoded = AgentDescription.json(appVersionName = "9.9.9")
        val decoded = json.decodeFromString<AgentDescriptionDocument>(encoded)

        assertEquals("9.9.9", decoded.togglesAppVersion)
    }
}
