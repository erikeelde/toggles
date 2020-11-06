package se.eelde.toggles.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.navigation.NavDeepLinkBuilder
import com.izettle.wrench.configurationlist.ConfigurationsFragmentArgs
import com.izettle.wrench.database.WrenchApplication
import se.eelde.toggles.R

private const val CHANNEL_ID = "Toggles channel"
private const val GROUP_ID = "se.eelde.toggles.TOGGLES_GROUP"

private fun createNotificationChannel(context: Context) {
    // Create the NotificationChannel, but only on API 26+ because
    // the NotificationChannel class is new and not in the support library
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = context.getString(R.string.channel_name)
        val descriptionText = context.getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        // Register the channel with the system
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}

fun showNotification(context: Context, application: WrenchApplication) {

    val pendingIntent = NavDeepLinkBuilder(context)
        .setGraph(R.navigation.navigation_graph)
        .setDestination(R.id.configurationsFragment)
        .setArguments(ConfigurationsFragmentArgs(application.id).toBundle())
        .createPendingIntent()

    val groupIntent = NavDeepLinkBuilder(context)
        .setGraph(R.navigation.navigation_graph)
        .setDestination(R.id.action_applications)
        .createPendingIntent()

    val packageManager = context.packageManager
    val applicationIcon = packageManager.getApplicationIcon(application.packageName)

    createNotificationChannel(context)

    val summaryNotification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setContentTitle("Summary title")
        // set content text to support devices running API level < 24
        .setContentText("Summary text")
        .setSmallIcon(R.drawable.ic_notification_24dp)
        .setColor(ContextCompat.getColor(context, R.color.notification_icon_color))
        // build summary info into InboxStyle template
        .setStyle(
            NotificationCompat.InboxStyle()
                .addLine("Line 1")
                .addLine("Line 2")
                .setBigContentTitle("Big title")
        )
        // .setSummaryText("Summary text"))
        // specify which group this notification belongs to
        .setContentIntent(groupIntent)
        .setGroup(GROUP_ID)
        // set this notification as the summary for the group
        .setGroupSummary(true)
        .setAutoCancel(true)
        .build()

    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_notification_24dp)
        .setLargeIcon(applicationIcon.toBitmap())
        .setColor(ContextCompat.getColor(context, R.color.notification_icon_color))
        .setContentTitle(context.getString(R.string.notification_content_title, application.applicationLabel))
        .setContentText(context.getString(R.string.notification_content_text))
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setContentIntent(pendingIntent)
        .setGroup(GROUP_ID)
        .setAutoCancel(true)

    with(NotificationManagerCompat.from(context)) {
        // notificationId is a unique int for each notification that you must define
        notify(R.id.notification, summaryNotification)
        notify(application.id.toInt(), builder.build())
    }
}
