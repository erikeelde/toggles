package se.eelde.toggles.notification

import android.content.Context
import com.izettle.wrench.core.Bolt
import com.izettle.wrench.database.TogglesNotification
import com.izettle.wrench.database.TogglesNotificationDao
import com.izettle.wrench.database.WrenchApplication
import com.izettle.wrench.database.WrenchConfigurationDao
import java.util.Date

fun configurationRequested(
    context: Context,
    configurationDao: WrenchConfigurationDao,
    togglesNotificationDao: TogglesNotificationDao,
    application: WrenchApplication,
    bolt: Bolt,
) {
    configurationDao.getWrenchConfigurationById(application.id, bolt.id)
        ?.let { configuration ->
            togglesNotificationDao.insert(
                TogglesNotification(
                    applicationId = application.id,
                    applicationPackageName = application.packageName,
                    configurationId = configuration.id,
                    configurationKey = bolt.key,
                    configurationValue = bolt.value!!,
                    added = Date(),
                )
            )
        }

    NotificationWorker.scheduleNotification(context)
}
