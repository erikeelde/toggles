package se.eelde.toggles.agent

import android.app.Application
import android.os.Build
import android.os.Bundle
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import se.eelde.toggles.database.TogglesDatabase
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Proves /describe's documented arguments match what AgentCallHandler actually reads — not by
 * comparing two lists of strings (which would pass even if both copies drifted from the real
 * `extras.xxxExtra(...)` call sites), but by driving the real handler: every documented argument
 * must be both necessary (omitting it fails with a missing-argument error naming it) and
 * sufficient (supplying exactly the documented set never fails with a missing-argument error).
 *
 * If a handler method starts reading a key that is not documented, or stops reading a key that
 * still is, one of the two assertions below fails.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class AgentDescriptionArgumentsTest {

    private val json = Json { ignoreUnknownKeys = false }

    private lateinit var database: TogglesDatabase
    private lateinit var handler: AgentCallHandler

    private val fixedClock = object : Clock {
        override fun now(): Instant = Instant.fromEpochMilliseconds(0)
    }

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            TogglesDatabase::class.java
        ).allowMainThreadQueries().build()
        val context = ApplicationProvider.getApplicationContext<Application>()
        handler = AgentCallHandler(
            agentDao = database.agentDao(),
            agentMutationDao = database.agentMutationDao(),
            changeNotifier = object : AgentChangeNotifier {
                override fun notifyConfigurationChanged(configurationId: Long) = Unit
                override fun notifyScopesChanged() = Unit
            },
            clock = fixedClock,
            packageManager = context.packageManager
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `every documented argument is both necessary and sufficient for its method`() {
        AgentDescription.document(appVersionName = "1.0").methods.forEach { method ->
            val fullResponse = handler.handle(method.method, bundleFor(method.arguments))
            assertFalse(
                "supplying every documented argument for ${method.method} still reports a " +
                    "missing argument; response: $fullResponse",
                isAnyMissingArgumentError(fullResponse)
            )

            method.arguments.forEach { argument ->
                val reducedResponse = handler.handle(
                    method.method,
                    bundleFor(method.arguments, omit = argument.name)
                )
                assertTrue(
                    "omitting documented argument \"${argument.name}\" from ${method.method} " +
                        "did not produce a missing-argument error naming it; " +
                        "response: $reducedResponse",
                    isMissingArgumentError(reducedResponse, argument.name)
                )
            }
        }
    }

    private fun bundleFor(arguments: List<AgentMethodArgument>, omit: String? = null): Bundle =
        Bundle().apply {
            arguments.filter { it.name != omit }.forEach { argument ->
                when (argument.type) {
                    "long" -> putLong(argument.name, 1L)
                    "string" -> putString(argument.name, "dummy")
                    else -> error("unhandled documented argument type \"${argument.type}\"")
                }
            }
        }

    private fun isAnyMissingArgumentError(response: String): Boolean {
        val error = errorOrNull(response) ?: return false
        return error.code == "invalid_argument" &&
            error.message.contains("missing or wrong-typed required extra")
    }

    private fun isMissingArgumentError(response: String, key: String): Boolean {
        val error = errorOrNull(response) ?: return false
        return error.code == "invalid_argument" &&
            error.message.contains("missing or wrong-typed required extra \"$key\"")
    }

    private fun errorOrNull(response: String): AgentErrorBody? = try {
        json.decodeFromString<AgentErrorEnvelope>(response).error
    } catch (_: Exception) {
        null
    }
}
