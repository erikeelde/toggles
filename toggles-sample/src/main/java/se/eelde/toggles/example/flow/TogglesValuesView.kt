package se.eelde.toggles.example.flow

import android.content.res.Resources
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import se.eelde.toggles.example.MyEnum
import se.eelde.toggles.example.R

sealed class Config<out T : Any> {
    abstract val title: String

    data class Loading<out T : Any>(override val title: String) : Config<T>()
    data class Success<out T : Any>(override val title: String, val value: T) : Config<T>()
}

data class ViewState(
    val stringConfig: Config<String>,
    val urlConfig: Config<String>,
    val boolConfig: Config<Boolean>,
    val intConfig: Config<Int>,
    val enumConfig: Config<MyEnum>,
) {
    companion object {
        fun loading(resources: Resources): ViewState =
            ViewState(
                stringConfig = Config.Loading(resources.getString(R.string.string_configuration)),
                intConfig = Config.Loading(resources.getString(R.string.int_configuration)),
                boolConfig = Config.Loading(resources.getString(R.string.boolean_configuration)),
                urlConfig = Config.Loading(resources.getString(R.string.url_configuration)),
                enumConfig = Config.Loading(resources.getString(R.string.enum_configuration)),
            )
    }
}

@Composable
fun TogglesValuesView(viewState: ViewState, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(16.dp)) {
        ToggleValue(viewState.stringConfig)
        ToggleValue(viewState.urlConfig)
        ToggleValue(viewState.boolConfig)
        ToggleValue(viewState.intConfig)
        ToggleValue(viewState.enumConfig)
    }
}

@Composable
fun ToggleValue(config: Config<Any>) {
    when (config) {
        is Config.Loading -> ToggleValueLoadingView(config = config)
        is Config.Success -> TogglesValuesSuccessView(config = config)
    }
}

@Composable
fun ToggleValueLoadingView(config: Config<Any>, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            style = MaterialTheme.typography.titleLarge,
            text = config.title
        )
        CircularProgressIndicator()
    }
}

@Composable
fun TogglesValuesSuccessView(config: Config.Success<Any>, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            style = MaterialTheme.typography.titleLarge,
            text = config.title
        )
        Text(text = config.value.toString())
        Spacer(modifier = Modifier.height(16.dp))
    }
}
