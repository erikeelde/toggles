package se.eelde.toggles.agent

import android.net.Uri
import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class AgentUriMatcherTest {

    private val matcher = AgentUriMatcher(AGENT_AUTHORITY)

    @Test
    fun `matches describe`() {
        assertEquals(
            AgentUriMatch.DESCRIBE,
            matcher.match(Uri.parse("content://$AGENT_AUTHORITY/describe"))
        )
    }

    @Test
    fun `matches apps`() {
        assertEquals(
            AgentUriMatch.APPLICATIONS,
            matcher.match(Uri.parse("content://$AGENT_AUTHORITY/apps"))
        )
    }

    @Test
    fun `matches a single app`() {
        assertEquals(
            AgentUriMatch.APPLICATION,
            matcher.match(Uri.parse("content://$AGENT_AUTHORITY/apps/com.example.app"))
        )
    }

    @Test
    fun `unknown path is UNKNOWN`() {
        assertEquals(
            AgentUriMatch.UNKNOWN,
            matcher.match(Uri.parse("content://$AGENT_AUTHORITY/nope"))
        )
    }

    @Test
    fun `a different authority is UNKNOWN`() {
        assertEquals(
            AgentUriMatch.UNKNOWN,
            matcher.match(Uri.parse("content://se.eelde.toggles.configprovider/apps"))
        )
    }

    private companion object {
        const val AGENT_AUTHORITY = "se.eelde.toggles.agentprovider"
    }
}
