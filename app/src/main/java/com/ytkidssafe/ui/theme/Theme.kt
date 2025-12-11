package com.ytkidssafe.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val CatppuccinColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = Background,
    primaryContainer = Primary.copy(alpha = 0.2f),
    onPrimaryContainer = TextPrimary,
    secondary = Secondary,
    onSecondary = Background,
    secondaryContainer = Secondary.copy(alpha = 0.2f),
    onSecondaryContainer = TextPrimary,
    tertiary = Accent1,
    onTertiary = Background,
    tertiaryContainer = Accent1.copy(alpha = 0.2f),
    onTertiaryContainer = TextPrimary,
    background = Background,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = CardBackground,
    onSurfaceVariant = TextLight,
    error = Error,
    onError = Background,
    outline = DividerColor
)

@Composable
fun YtKidsTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = CatppuccinColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Background.toArgb()
            window.navigationBarColor = Background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
