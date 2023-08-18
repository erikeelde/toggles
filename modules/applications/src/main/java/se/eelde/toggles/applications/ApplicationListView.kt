package se.eelde.toggles.applications

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap

@Composable
internal fun ApplicationsView(
    viewState: ViewState,
    modifier: Modifier = Modifier,
    navigateToConfigurations: (Long) -> Unit,
) {
    if (viewState.applications.isEmpty()) {
        ApplicationEmptyView(modifier = modifier)
    } else {
        ApplicationListView(
            viewState = viewState,
            modifier = modifier,
            navigateToConfigurations = navigateToConfigurations
        )
    }
}

@Preview
@Composable
internal fun ApplicationEmptyViewPreview() {
    ApplicationEmptyView()
}

@Composable
internal fun ApplicationEmptyView(
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.padding(16.dp)) {
        Column {
            Text(
                style = MaterialTheme.typography.headlineMedium,
                text = stringResource(id = R.string.no_applications_found)
            )
            Text(
                style = MaterialTheme.typography.bodyLarge,
                text = stringResource(id = R.string.no_applications_found_description)
            )
        }
    }
}

@Composable
internal fun ApplicationListView(
    viewState: ViewState,
    modifier: Modifier = Modifier,
    navigateToConfigurations: (Long) -> Unit
) {
    val context = LocalContext.current

    viewState.let {
        Surface(modifier = modifier.padding(16.dp)) {
            LazyColumn {
                it.applications.forEach { application ->
                    item {
                        var lol: ImageBitmap? = null
                        var secondaryText = ""
                        try {
                            val packageManager = context.packageManager
                            val icon: Drawable =
                                packageManager.getApplicationIcon(application.packageName)
                            lol = icon.toBitmap(width = 128, height = 128).asImageBitmap()
                        } catch (ignored: PackageManager.NameNotFoundException) {
                            secondaryText = stringResource(id = R.string.not_installed)
                        }

                        ListItem(
                            leadingContent = {
                                if (lol != null) {
                                    Image(
                                        painter = BitmapPainter(image = lol),
                                        contentDescription = "Application icon"
                                    )
                                } else {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_report_black_24dp),
                                        contentDescription = "Application not installed"
                                    )
                                }
                            },
                            headlineContent = { Text(application.applicationLabel) },
                            supportingContent = { Text(secondaryText) },
                            modifier = Modifier.clickable {
                                navigateToConfigurations(application.id)
                            }
                        )
                    }
                }
            }
        }
    }
}
