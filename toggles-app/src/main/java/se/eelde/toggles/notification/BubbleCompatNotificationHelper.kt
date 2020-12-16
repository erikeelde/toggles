package se.eelde.toggles.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.WorkerThread
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.app.RemoteInput
import androidx.core.content.LocusIdCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.graphics.drawable.toBitmap
import com.izettle.wrench.MainActivity
import com.izettle.wrench.database.TogglesNotification
import com.izettle.wrench.database.WrenchApplication
import se.eelde.toggles.BubbleActivity
import se.eelde.toggles.R
import se.eelde.toggles.core.TogglesProviderContract

@RequiresApi(Build.VERSION_CODES.R)
class BubbleCompatNotificationHelper(private val context: Context) {
    companion object {
        /**
         * The notification channel for messages. This is used for showing Bubbles.
         */
        private const val CHANNEL_NEW_MESSAGES = "new_messages"

        private const val REQUEST_CONTENT = 1
        private const val REQUEST_BUBBLE = 2
    }

    private val notificationManagerCompat: NotificationManagerCompat =
        NotificationManagerCompat.from(context)

    init {
        setUpNotificationChannels()
    }

    private fun setUpNotificationChannels() {
        if (notificationManagerCompat.getNotificationChannel(CHANNEL_NEW_MESSAGES) == null) {
            notificationManagerCompat.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_NEW_MESSAGES,
                    context.getString(R.string.notification_channel_name),
                    // The importance must be IMPORTANCE_HIGH to show Bubbles.
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = context.getString(R.string.notification_channel_description)
                }
            )
        }
        // updateShortcuts(null)
    }

    @Suppress("LongMethod")
    @WorkerThread
    fun showNotification(
        wrenchApplication: WrenchApplication,
        togglesNotifications: List<TogglesNotification>,
        fromUser: Boolean
    ) {
        val applicationIcon = context.packageManager.getApplicationIcon(wrenchApplication.packageName)

        val icon = IconCompat.createWithAdaptiveBitmap(applicationIcon.toBitmap())
        val user = Person.Builder().setName(context.getString(R.string.sender_you)).build()
        val person = Person.Builder().setName(wrenchApplication.applicationLabel).setIcon(icon).build()
        val contentUri = TogglesProviderContract.applicationUri(wrenchApplication.id)

        val pendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_BUBBLE,
            // Launch BubbleActivity as the expanded bubble.
            Intent(context, BubbleActivity::class.java)
                .setAction(Intent.ACTION_VIEW)
                .setData(contentUri),
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_NEW_MESSAGES)
            // A notification can be shown as a bubble by calling setBubbleMetadata()
            .setBubbleMetadata(
                NotificationCompat.BubbleMetadata.Builder(pendingIntent, icon)
                    // The height of the expanded bubble.
                    .setDesiredHeight(context.resources.getDimensionPixelSize(R.dimen.bubble_height))
                    .apply {
                        // When the bubble is explicitly opened by the user, we can show the bubble
                        // automatically in the expanded state. This works only when the app is in
                        // the foreground.
                        if (fromUser) {
                            setAutoExpandBubble(true)
                            setSuppressNotification(true)
                        }
                    }
                    .build()
            )
            // The user can turn off the bubble in system settings. In that case, this notification
            // is shown as a normal notification instead of a bubble. Make sure that this
            // notification works as a normal notification as well.
            .setContentTitle(context.resources.getString(R.string.notification_content_title))
            .setSmallIcon(R.drawable.ic_notification_24dp)
            .setCategory(Notification.CATEGORY_MESSAGE)
            .setShortcutId(wrenchApplication.shortcutId)
            // This ID helps the intelligence services of the device to correlate this notification
            // with the corresponding dynamic shortcut.
            .setLocusId(LocusIdCompat(wrenchApplication.shortcutId))
            .addPerson(person)
            .setShowWhen(true)
            // The content Intent is used when the user clicks on the "Open Content" icon button on
            // the expanded bubble, as well as when the fall-back notification is clicked.
            .setContentIntent(
                PendingIntent.getActivity(
                    context,
                    REQUEST_CONTENT,
                    Intent(context, MainActivity::class.java)
                        .setAction(Intent.ACTION_VIEW)
                        .setData(contentUri),
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
            // Direct Reply
            .addAction(
                NotificationCompat.Action
                    .Builder(
                        IconCompat.createWithResource(context, android.R.drawable.ic_menu_send),
                        context.getString(R.string.label_reply),
                        PendingIntent.getBroadcast(
                            context,
                            REQUEST_CONTENT,
                            Intent(context, BubbleActivity::class.java).setData(contentUri),
                            PendingIntent.FLAG_UPDATE_CURRENT
                        )
                    )
                    .addRemoteInput(
                        RemoteInput.Builder(ReplyReceiver.KEY_TEXT_REPLY)
                            .setLabel(context.getString(R.string.hint_input))
                            .build()
                    )
                    .setAllowGeneratedReplies(true)
                    .build()
            )
            // Let's add some more content to the notification in case it falls back to a normal
            // notification.
            .setStyle(
                NotificationCompat.MessagingStyle(user)
                    .also {
                        for (togglesNotification in togglesNotifications) {
                            it.addMessage(
                                NotificationCompat.MessagingStyle.Message(
                                    togglesNotification.configurationKey,
                                    togglesNotification.added.time,
                                    person
                                )
                            )
                        }
                    }
                    .setGroupConversation(false)
            )
            .setWhen(togglesNotifications.last().added.time)

        notificationManagerCompat.notify(wrenchApplication.id.toInt(), builder.build())
    }

    fun dismissNotification(id: Long) {
        notificationManagerCompat.cancel(id.toInt())
    }
}
