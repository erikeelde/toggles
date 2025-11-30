package se.eelde.toggles.example.scoped

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ScopedTogglesView(viewModel: ScopedTogglesViewModel, modifier: Modifier = Modifier) {
    val viewState by viewModel.viewState

    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Scoped Toggles Demo",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "This demonstrates per-user feature flags using scope-specific toggle instances",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Admin Scope Card
        ScopeCard(
            scopeName = "Admin Scope",
            featureEnabled = viewState.adminFeatureEnabled,
            apiEndpoint = viewState.adminApiEndpoint
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Guest Scope Card
        ScopeCard(
            scopeName = "Guest Scope",
            featureEnabled = viewState.guestFeatureEnabled,
            apiEndpoint = viewState.guestApiEndpoint
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Default Scope Card
        ScopeCard(
            scopeName = "Default Scope",
            featureEnabled = viewState.defaultFeatureEnabled,
            apiEndpoint = viewState.defaultApiEndpoint
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "How to use:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "1. Open the Toggles app\n" +
                        "2. Create scopes: 'admin' and 'guest'\n" +
                        "3. Create toggles: 'advanced_mode' (boolean) and 'api_endpoint' (string)\n" +
                        "4. Set different values for each scope\n" +
                        "5. Watch the values update in real-time!",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun ScopeCard(
    scopeName: String,
    featureEnabled: Boolean,
    apiEndpoint: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = scopeName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Feature toggle status
            Text(
                text = "Advanced Mode:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (featureEnabled) "✅ Enabled" else "❌ Disabled",
                style = MaterialTheme.typography.bodyLarge,
                color = if (featureEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(8.dp))

            // API endpoint
            Text(
                text = "API Endpoint:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = apiEndpoint,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
