package se.eelde.toggles.notification

import android.content.Context
import com.izettle.wrench.core.Bolt
import com.izettle.wrench.database.TogglesNotification
import com.izettle.wrench.database.TogglesNotificationDao
import com.izettle.wrench.database.WrenchApplication
import com.izettle.wrench.database.WrenchConfigurationDao
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import se.eelde.toggles.coroutines.booleanBoltFlow
import java.util.Date
import javax.inject.Inject

class ChangedHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val configurationDao: WrenchConfigurationDao,
    private val togglesNotificationDao: TogglesNotificationDao,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun configurationRequested(
        application: WrenchApplication,
        bolt: Bolt,
        scope: CoroutineScope
    ) {
        scope.launch(Dispatchers.IO) {
            val notificationsEnabled =
                booleanBoltFlow(context, "Enable notifications", false).first()

            if (notificationsEnabled) {
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
        }
    }
}
