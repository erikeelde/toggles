package se.eelde.toggles.configurations

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
import androidx.compose.runtime.State
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
@Suppress("LongParameterList")
internal fun ConfigurationListView(
    navigateToBooleanConfiguration: (scopeId: Long, configurationId: Long) -> Unit,
    navigateToIntegerConfiguration: (scopeId: Long, configurationId: Long) -> Unit,
    navigateToStringConfiguration: (scopeId: Long, configurationId: Long) -> Unit,
    navigateToEnumConfiguration: (scopeId: Long, configurationId: Long) -> Unit,
    uiState: State<ViewState>,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier) {
        LazyColumn {
            uiState.value.configurations.forEach { configuration ->
                val defaultScope =
                    getItemForScope(uiState.value.defaultScope, configuration.configurationValues!!)
                val selectedScope = getItemForScope(
                    uiState.value.selectedScope,
                    configuration.configurationValues!!
                )
                item {
                    Column(
                        modifier = Modifier
                            .clickable {
                                Log.w("Clicked configuration", "")
                                configurationClicked(
                                    navigateToBooleanConfiguration = navigateToBooleanConfiguration,
                                    navigateToIntegerConfiguration = navigateToIntegerConfiguration,
                                    navigateToStringConfiguration = navigateToStringConfiguration,
                                    navigateToEnumConfiguration = navigateToEnumConfiguration,
                                    configuration = configuration,
                                    selectedScope = uiState.value.selectedScope
                                )
                            }
                            .fillMaxWidth(1.0f)
                            .padding(16.dp)
                    ) {
                        Text(
                            modifier = Modifier,
                            style = MaterialTheme.typography.headlineSmall,
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
                                style = MaterialTheme.typography.titleLarge,
                                text = "${defaultScope?.value}"
                            )
                        }

                        if (selectedScope != null) {
                            Text(
                                style = MaterialTheme.typography.titleLarge,
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

@Suppress("LongMethod", "LongParameterList")
fun configurationClicked(
    navigateToBooleanConfiguration: (scopeId: Long, configurationId: Long) -> Unit,
    navigateToIntegerConfiguration: (scopeId: Long, configurationId: Long) -> Unit,
    navigateToStringConfiguration: (scopeId: Long, configurationId: Long) -> Unit,
    navigateToEnumConfiguration: (scopeId: Long, configurationId: Long) -> Unit,
    configuration: WrenchConfigurationWithValues,
    selectedScope: WrenchScope?
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
        navigateToStringConfiguration(selectedScope!!.id, configuration.id)
    } else if (TextUtils.equals(Int::class.java.name, configuration.type) || TextUtils.equals(
            Toggle.TYPE.INTEGER,
            configuration.type
        )
    ) {
        navigateToIntegerConfiguration(selectedScope!!.id, configuration.id)
    } else if (TextUtils.equals(
            Boolean::class.java.name,
            configuration.type
        ) || TextUtils.equals(Toggle.TYPE.BOOLEAN, configuration.type)
    ) {
        navigateToBooleanConfiguration(selectedScope!!.id, configuration.id)
    } else if (TextUtils.equals(Enum::class.java.name, configuration.type) || TextUtils.equals(
            Toggle.TYPE.ENUM,
            configuration.type
        )
    ) {
        navigateToEnumConfiguration(selectedScope!!.id, configuration.id)
    } else {
//        Snackbar.make(
//            binding.animator,
//            "Not sure what to do with type: " + configuration.type!!,
//            Snackbar.LENGTH_LONG
//        ).show()
    }
}
