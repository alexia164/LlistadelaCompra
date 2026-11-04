package cat.insbaixcamp.llistadelacompra.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = GreenPrimaryDark,
    onPrimary = GreenOnPrimaryDark,
    primaryContainer = GreenContainerDark,
    onPrimaryContainer = GreenOnContainerDark,
    secondary = GreenSecondaryDark,
    onSecondary = GreenOnSecondaryDark,
    background = GreenBackgroundDark,
    surface = GreenSurfaceDark,
    onSurface = GreenOnSurfaceDark
)

private val LightColorScheme = lightColorScheme(
    primary = GreenPrimaryLight,
    onPrimary = GreenOnPrimaryLight,
    primaryContainer = GreenContainerLight,
    onPrimaryContainer = GreenOnContainerLight,
    secondary = GreenSecondaryLight,
    onSecondary = GreenOnSecondaryLight,
    background = GreenBackgroundLight,
    surface = GreenSurfaceLight,
    onSurface = GreenOnSurfaceLight
)

@Composable
fun LlistaDeLaCompraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set false to ensure green theme brand identity
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
