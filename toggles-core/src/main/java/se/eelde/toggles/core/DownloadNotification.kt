package se.eelde.toggles.core

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

private const val CHANNEL_ID = "Download toggles channel"

private fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = context.getString(R.string.download_toggles_notification_channel)
        val descriptionText =
            context.getString(R.string.download_toggles_notification_channel_description)
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

@RequiresApi(Build.VERSION_CODES.M)
fun getStartActivityPendingIntentPostM(context: Context, intent: Intent): PendingIntent =
    PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

@SuppressLint("UnspecifiedImmutableFlag")
fun getStartActivityPendingIntentPreM(context: Context, intent: Intent): PendingIntent =
    PendingIntent.getActivity(context, 0, intent, 0)

fun getStartActivityPendingIntent(context: Context, intent: Intent): PendingIntent =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        getStartActivityPendingIntentPostM(context, intent)
    } else {
        getStartActivityPendingIntentPreM(context, intent)
    }

fun showDownloadNotification(context: Context) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse(
            "https://play.google.com/store/apps/details?id=se.eelde.toggles"
        )
        setPackage("com.android.vending")
    }

    createNotificationChannel(context)

    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_download_notification)
        .setContentTitle(context.getString(R.string.notification_content_title))
        .setContentText(context.getString(R.string.notification_content_text))
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setContentIntent(getStartActivityPendingIntent(context, intent))
        .setAutoCancel(true)

    with(NotificationManagerCompat.from(context)) {
        // notificationId is a unique int for each notification that you must define
        notify(R.id.downloadNotification, builder.build())
    }
}
