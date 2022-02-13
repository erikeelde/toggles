package se.eelde.toggles.applicationlist

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.izettle.wrench.applicationlist.ApplicationViewModel
import se.eelde.toggles.R

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun ApplicationListView(
    navController: NavController,
    viewModel: ApplicationViewModel = hiltViewModel()
) {
    val uiState = viewModel.state.collectAsState()

    val context = LocalContext.current

    uiState.value.let {
        Surface(modifier = Modifier.padding(16.dp)) {
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
                        } catch (e: PackageManager.NameNotFoundException) {
                            secondaryText = stringResource(id = R.string.not_installed)
                        }

                        ListItem(
                            icon = {
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
                            modifier = Modifier.clickable {
                                navController.navigate("configurations/${application.id}")
                            }
                        ) {
                            Text(application.applicationLabel)
                        }
                    }
                }
            }
        }
    }
}
