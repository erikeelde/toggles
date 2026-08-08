package se.eelde.toggles.agent

import android.app.Application
import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/**
 * [SystemAgentControlNotifier] is the real, on-device implementation of [AgentControlNotifier];
 * [AgentCallHandlerTest] exercises the interface's wiring into [AgentCallHandler] with a recording
 * fake, but the "only once per package" and "never throws" behaviour lives here, in the real
 * implementation, so it is tested directly against it.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class AgentControlNotifierTest {

    private val context: Application = ApplicationProvider.getApplicationContext()
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    @Test
    fun `a successful mutation posts exactly one notification`() {
        val notifier = SystemAgentControlNotifier(context)

        notifier.notifyFirstMutation("com.example.app", "Example")

        assertEquals(1, shadowOf(notificationManager).size())
    }

    @Test
    fun `a second call for the same package posts no further notification`() {
        val notifier = SystemAgentControlNotifier(context)

        notifier.notifyFirstMutation("com.example.app", "Example")
        notifier.notifyFirstMutation("com.example.app", "Example")

        assertEquals(1, shadowOf(notificationManager).size())
    }

    @Test
    fun `a different package gets its own notification`() {
        val notifier = SystemAgentControlNotifier(context)

        notifier.notifyFirstMutation("com.example.app", "Example")
        notifier.notifyFirstMutation("com.example.other", "Other")

        assertEquals(2, shadowOf(notificationManager).size())
    }

    @Test
    fun `the notification text contains the package name`() {
        val notifier = SystemAgentControlNotifier(context)

        notifier.notifyFirstMutation("com.example.app", "Example")

        val notification = shadowOf(notificationManager).allNotifications.single()
        val title = notification.extras.getCharSequence(Notification.EXTRA_TITLE).toString()
        val text = notification.extras.getCharSequence(Notification.EXTRA_TEXT).toString()
        assertTrue(
            "expected the package name in the notification, got title=\"$title\" text=\"$text\"",
            title.contains("com.example.app") || text.contains("com.example.app")
        )
    }

    @Test
    fun `the notification carries a disable agent control action`() {
        val notifier = SystemAgentControlNotifier(context)

        notifier.notifyFirstMutation("com.example.app", "Example")

        val notification = shadowOf(notificationManager).allNotifications.single()
        assertEquals(1, notification.actions?.size)
    }

    @Test
    fun `posting never throws even when the NotificationManager is unreachable`() {
        val brokenContext = object : ContextWrapper(context) {
            override fun getSystemService(name: String): Any? {
                if (name == Context.NOTIFICATION_SERVICE) {
                    throw SecurityException("notifications not permitted")
                }
                return super.getSystemService(name)
            }
        }
        val notifier = SystemAgentControlNotifier(brokenContext)

        // The absence of a thrown exception IS the assertion here.
        notifier.notifyFirstMutation("com.example.app", "Example")
    }
}
