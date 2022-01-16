package se.eelde.toggles

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview


@Preview
@Composable
fun PreviewDrawerView() {
    DrawerView()
}

@Composable
fun DrawerView() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Image(
            modifier = Modifier.background(Color(0, 0, 80)),
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "Application icon"
        )
        Text(text = stringResource(id = R.string.app_name))

        Text(text = stringResource(id = R.string.applications))
        Text(text = stringResource(id = R.string.oss))
        Text(text = stringResource(id = R.string.help))
    }
}