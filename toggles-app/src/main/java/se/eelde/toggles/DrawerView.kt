package se.eelde.toggles

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview
@Composable
fun PreviewDrawerView() {
    DrawerView(openApplications = {}, openOss = {}, openHelp = {})
}

@Composable
fun DrawerView(
    openApplications: () -> Unit,
    openOss: () -> Unit,
    openHelp: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            modifier = Modifier.background(colorResource(id = R.color.toggles_blue)),
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "Application icon"
        )
        Text(
            text = stringResource(id = R.string.app_name),
            style = MaterialTheme.typography.displaySmall
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
        }

        IconAndText(
            modifier = Modifier
                .align(Alignment.Start)
                .clickable { openApplications() },

            iconResouceId = R.drawable.ic_settings_white_24dp,
            stringRes = R.string.applications
        )

        Divider()

        IconAndText(
            modifier = Modifier
                .align(Alignment.Start)
                .clickable { openOss() },

            iconResouceId = R.drawable.ic_oss,
            stringRes = R.string.oss
        )

        IconAndText(
            modifier = Modifier
                .align(Alignment.Start)
                .clickable { openHelp() },

            iconResouceId = R.drawable.ic_report_black_24dp,
            stringRes = R.string.help
        )
    }
}

@Preview
@Composable
fun IconAndTextPreview() {
    IconAndText(iconResouceId = R.drawable.ic_oss, stringRes = R.string.oss)
}

@Composable
fun IconAndText(
    modifier: Modifier = Modifier,
    @DrawableRes iconResouceId: Int,
    @StringRes stringRes: Int
) {
    Row(
        modifier = modifier
            .fillMaxWidth(1.0f)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(painter = painterResource(id = iconResouceId), contentDescription = null)
        Text(
            stringResource(id = stringRes), style = MaterialTheme.typography.titleMedium,
        )
    }
}
