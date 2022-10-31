package se.eelde.toggles.configurationlist

import android.text.TextUtils
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import se.eelde.toggles.core.Toggle
import se.eelde.toggles.database.WrenchConfigurationValue
import se.eelde.toggles.database.WrenchConfigurationWithValues
import se.eelde.toggles.database.WrenchScope

@Composable
internal fun ConfigurationListView(
    navigateToStringConfiguration: ConfigurationNavigation,
    navigateToIntegerConfiguration: ConfigurationNavigation,
    navigateToBooleanConfiguration: ConfigurationNavigation,
    navigateToEnumConfiguration: ConfigurationNavigation,
    uiState: ViewState
) {
    Surface(modifier = Modifier.padding(16.dp)) {
        LazyColumn {
            uiState.configurations.forEach { configuration ->
                val defaultScope =
                    getItemForScope(uiState.defaultScope, configuration.configurationValues!!)
                val selectedScope = getItemForScope(
                    uiState.selectedScope,
                    configuration.configurationValues!!
                )

                item {
                    Column(
                        modifier = Modifier
                            .clickable {
                                Log.w("Clicked configuration", "")
                                configurationClicked(
                                    configuration = configuration,
                                    selectedScope = uiState.selectedScope!!,
                                    navigateToStringConfiguration = navigateToStringConfiguration,
                                    navigateToIntegerConfiguration = navigateToIntegerConfiguration,
                                    navigateToBooleanConfiguration = navigateToBooleanConfiguration,
                                    navigateToEnumConfiguration = navigateToEnumConfiguration,
                                )
                            }
                            .fillMaxWidth(1.0f)
                            .padding(16.dp)
                    ) {
                        Text(
                            modifier = Modifier,
                            style = MaterialTheme.typography.titleMedium,
                            text = configuration.key!!
                        )

                        if (selectedScope != null) {
                            Text(
                                style = MaterialTheme.typography.bodyMedium,
                                text = buildAnnotatedString {
                                    withStyle(style = SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                                        append("${defaultScope?.value}")
                                    }
                                }
                            )
                        } else {
                            Text(
                                style = MaterialTheme.typography.titleMedium,
                                text = "${defaultScope?.value}"
                            )
                        }

                        if (selectedScope != null) {
                            Text(
                                style = MaterialTheme.typography.titleMedium,
                                text = selectedScope.value ?: ""
                            )
                        }
                    }
                }
            }
        }
    }
}

@Suppress("ReturnCount")
private fun getItemForScope(
    scope: WrenchScope?,
    wrenchConfigurationValues: Set<WrenchConfigurationValue>
): WrenchConfigurationValue? {
    if (scope == null) {
        return null
    }

    for (wrenchConfigurationValue in wrenchConfigurationValues) {
        if (wrenchConfigurationValue.scope == scope.id) {
            return wrenchConfigurationValue
        }
    }

    return null
}

typealias ConfigurationNavigation = (configurationId: Long, scopeId: Long) -> Unit

@Suppress("LongMethod")
fun configurationClicked(
    configuration: WrenchConfigurationWithValues,
    selectedScope: WrenchScope,
    navigateToStringConfiguration: ConfigurationNavigation,
    navigateToIntegerConfiguration: ConfigurationNavigation,
    navigateToBooleanConfiguration: ConfigurationNavigation,
    navigateToEnumConfiguration: ConfigurationNavigation,
) {
//    if (viewModel.selectedScopeLiveData.value == null) {
//        Snackbar.make(binding.animator, "No selected scope found", Snackbar.LENGTH_LONG).show()
//        return
//    }

    if (TextUtils.equals(
            String::class.java.name,
            configuration.type
        ) || TextUtils.equals(Toggle.TYPE.STRING, configuration.type)
    ) {
        navigateToStringConfiguration(configuration.id, selectedScope.id)
    } else if (TextUtils.equals(Int::class.java.name, configuration.type) || TextUtils.equals(
            Toggle.TYPE.INTEGER,
            configuration.type
        )
    ) {
        navigateToIntegerConfiguration(configuration.id, selectedScope.id)
    } else if (TextUtils.equals(
            Boolean::class.java.name,
            configuration.type
        ) || TextUtils.equals(Toggle.TYPE.BOOLEAN, configuration.type)
    ) {
        navigateToBooleanConfiguration(configuration.id, selectedScope.id)
    } else if (TextUtils.equals(Enum::class.java.name, configuration.type) || TextUtils.equals(
            Toggle.TYPE.ENUM,
            configuration.type
        )
    ) {
        navigateToEnumConfiguration(configuration.id, selectedScope.id)
    } else {
//        Snackbar.make(
//            binding.animator,
//            "Not sure what to do with type: " + configuration.type!!,
//            Snackbar.LENGTH_LONG
//        ).show()
    }
}
