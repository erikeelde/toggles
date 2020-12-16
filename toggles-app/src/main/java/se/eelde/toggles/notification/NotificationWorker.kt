package se.eelde.toggles.notification

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.WorkerThread
import androidx.core.app.Person
import androidx.core.content.LocusIdCompat
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.Assisted
import androidx.hilt.work.WorkerInject
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.izettle.wrench.MainActivity
import com.izettle.wrench.database.WrenchApplication
import com.izettle.wrench.database.WrenchDatabase
import se.eelde.toggles.core.TogglesProviderContract
import java.util.concurrent.TimeUnit

class NotificationWorker @WorkerInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    val wrenchDatabase: WrenchDatabase,
) :
    CoroutineWorker(appContext, workerParams) {

    companion object {
        fun scheduleNotification(context: Context) {
            val notificationWorker: OneTimeWorkRequest =
                OneTimeWorkRequestBuilder<NotificationWorker>()
                    .setInitialDelay(2, TimeUnit.SECONDS)
                    .build()

            WorkManager
                .getInstance(context)
                .enqueueUniqueWork(
                    "notificationWorker",
                    ExistingWorkPolicy.REPLACE,
                    notificationWorker
                )
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override suspend fun doWork(): Result {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val applicationsWithPendingNotifications =
                wrenchDatabase.togglesNotificationsDao().getApplicationsWithPendingNotifications()

            applicationsWithPendingNotifications.forEach { wrenchApplication ->
                val togglesNotifications = wrenchDatabase.togglesNotificationsDao()
                    .getPendingNotificationsForApplication(wrenchApplication.packageName)

                updateShortcuts(applicationsWithPendingNotifications, wrenchApplication)

                BubbleCompatNotificationHelper(context = applicationContext).showNotification(
                    wrenchApplication = wrenchApplication,
                    togglesNotifications = togglesNotifications,
                    fromUser = false
                )
            }
        }

        return Result.success()
    }

    @WorkerThread
    fun updateShortcuts(
        applications: List<WrenchApplication>,
        importantApplication: WrenchApplication?,
    ) {
        var shortcuts = applications.map { application ->

            val applicationIcon =
                applicationContext.packageManager.getApplicationIcon(application.packageName)
            val icon = IconCompat.createWithAdaptiveBitmap(applicationIcon.toBitmap())

            // Create a dynamic shortcut for each of the contacts.
            // The same shortcut ID will be used when we show a bubble notification.
            ShortcutInfoCompat.Builder(applicationContext, application.shortcutId)
                .setLocusId(LocusIdCompat(application.shortcutId))
                .setActivity(ComponentName(applicationContext, MainActivity::class.java))
                .setShortLabel(application.applicationLabel)
                .setIcon(icon)
                .setLongLived(true)
                .setCategories(setOf("com.example.android.bubbles.category.TEXT_SHARE_TARGET"))
                .setIntent(
                    Intent(
                        ACTION_VIEW,
                        TogglesProviderContract.applicationUri(application.id)
                    )
                )
                .setPerson(
                    Person.Builder()
                        .setName(application.applicationLabel)
                        .setIcon(icon)
                        .build()
                )
                .build()
        }

        // Move the important contact to the front of the shortcut list.
        if (importantApplication != null) {
            shortcuts = shortcuts.sortedByDescending { it.id == importantApplication.shortcutId }
        }
        // Truncate the list if we can't show all of our contacts.
        val maxCount = ShortcutManagerCompat.getMaxShortcutCountPerActivity(applicationContext)
        if (shortcuts.size > maxCount) {
            shortcuts = shortcuts.take(maxCount)
        }
        ShortcutManagerCompat.addDynamicShortcuts(applicationContext, shortcuts)
    }
}
