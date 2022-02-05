package se.eelde.toggles.configurationlist

import android.text.TextUtils
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.izettle.wrench.configurationlist.ConfigurationViewModel
import se.eelde.toggles.database.WrenchConfigurationValue
import se.eelde.toggles.database.WrenchConfigurationWithValues
import se.eelde.toggles.database.WrenchScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import se.eelde.toggles.core.Toggle

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun ConfigurationListView(
    navController: NavController,
    applicationId: Long,
    viewModel: ConfigurationViewModel = hiltViewModel()
) {
    val uiState = viewModel.state.collectAsState()

    Surface(modifier = Modifier.padding(16.dp)) {
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
                                    navController = navController,
                                    configuration = configuration,
                                    selectedScope = uiState.value.selectedScope
                                )
                            }
                            .fillMaxWidth(1.0f)
                            .padding(16.dp)
                    ) {
                        Text(
                            modifier = Modifier,
                            style = MaterialTheme.typography.h5,
                            text = configuration.key!!
                        )

                        if (selectedScope != null) {
                            Text(style = MaterialTheme.typography.body2,
                                text = buildAnnotatedString {
                                    withStyle(style = SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                                        append("${defaultScope?.value}")
                                    }
                                })
                        } else {
                            Text(
                                style = MaterialTheme.typography.h6,
                                text = "${defaultScope?.value}"
                            )
                        }

                        if (selectedScope != null) {
                            Text(
                                style = MaterialTheme.typography.h6,
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

@Suppress("LongMethod")
@OptIn(ExperimentalCoroutinesApi::class)
fun configurationClicked(
    navController: NavController,
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
        navController.navigate("configuration/${configuration.id}/${selectedScope!!.id}/string")
    } else if (TextUtils.equals(Int::class.java.name, configuration.type) || TextUtils.equals(
            Toggle.TYPE.INTEGER,
            configuration.type
        )
    ) {
        navController.navigate("configuration/${configuration.id}/${selectedScope!!.id}/integer")
    } else if (TextUtils.equals(
            Boolean::class.java.name,
            configuration.type
        ) || TextUtils.equals(Toggle.TYPE.BOOLEAN, configuration.type)
    ) {
        navController.navigate("configuration/${configuration.id}/${selectedScope!!.id}/boolean")
    } else if (TextUtils.equals(Enum::class.java.name, configuration.type) || TextUtils.equals(
            Toggle.TYPE.ENUM,
            configuration.type
        )
    ) {
        navController.navigate("configuration/${configuration.id}/${selectedScope!!.id}/enum")
    } else {
//        Snackbar.make(
//            binding.animator,
//            "Not sure what to do with type: " + configuration.type!!,
//            Snackbar.LENGTH_LONG
//        ).show()
    }
}
