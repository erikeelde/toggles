package se.eelde.toggles

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


@Suppress("MagicNumber")
// https://material.io/design/color/dark-theme.html#anatomy
private val DarkColorPalette = darkColors(
    primary = Color(0xFF1E1A87),
    primaryVariant = Color(0xFF131061),
    secondary = Color(0xFF07BD46),
)

@Suppress("MagicNumber")
private val LightColorPalette = lightColors(
    primary = Color(0xFF1E1A87),
    primaryVariant = Color(0xFF131061),
    secondary = Color(0xff287F46),
)

@Composable
fun TogglesTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
    ) {
        content()
    }
}
