package se.eelde.toggles.notification

import android.content.Context
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
import se.eelde.toggles.flow.TogglesImpl
import se.eelde.toggles.core.Toggle
import java.util.Date
import javax.inject.Inject

class ChangedHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val configurationDao: WrenchConfigurationDao,
    private val togglesNotificationDao: TogglesNotificationDao,
) {
    fun configurationRequested(
        application: WrenchApplication,
        toggle: Toggle,
        scope: CoroutineScope
    ) = configurationRequested(
        application,
        toggle.id,
        toggle.key,
        toggle.value,
        scope,
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    fun configurationRequested(
        application: WrenchApplication,
        toggleId: Long,
        toggleKey: String,
        toggleValue: String?,
        scope: CoroutineScope
    ) {
        scope.launch(Dispatchers.IO) {
            val notificationsEnabled =
                TogglesImpl(context).toggle("Enable notifications", false).first()

            if (notificationsEnabled) {
                configurationDao.getWrenchConfigurationById(application.id, toggleId)
                    ?.let { configuration ->
                        togglesNotificationDao.insert(
                            TogglesNotification(
                                applicationId = application.id,
                                applicationPackageName = application.packageName,
                                configurationId = configuration.id,
                                configurationKey = toggleKey,
                                configurationValue = toggleValue!!,
                                added = Date(),
                            )
                        )
                    }

                NotificationWorker.scheduleNotification(context)
            }
        }
    }
}
