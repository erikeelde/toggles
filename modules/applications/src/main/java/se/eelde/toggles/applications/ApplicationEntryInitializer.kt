package se.eelde.toggles.applications

import android.content.Context
import androidx.startup.Initializer
import se.eelde.toggles.navigation.featureDestinations

class ApplicationEntryInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        ApplicationEntry().also { featureDestinations[ApplicationEntry::class.java] = it }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        // No dependencies on other libraries.
        return emptyList()
    }
}