package ca.amandeep.bcbpscanner.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Card
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat

private val DarkColorScheme = darkColorScheme()

private val LightColorScheme = lightColorScheme()

@Composable
fun BCBPScannerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode && view.context is Activity) {
        SideEffect {
            (view.context as Activity).window.statusBarColor = colorScheme.surface.toArgb()
            @Suppress("DEPRECATION")
            ViewCompat.getWindowInsetsController(view)?.isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
    ) {
        androidx.compose.material.MaterialTheme(
            colors = androidx.compose.material.MaterialTheme.colors.copy(
                primary = colorScheme.primary,
                primaryVariant = colorScheme.primary,
                secondary = colorScheme.secondary,
                secondaryVariant = colorScheme.secondary,
                background = colorScheme.background,
                surface = colorScheme.surface,
                error = colorScheme.error,
                onPrimary = colorScheme.onPrimary,
                onSecondary = colorScheme.onSecondary,
                onBackground = colorScheme.onBackground,
                onSurface = colorScheme.onSurface,
                onError = colorScheme.onError,
                isLight = !darkTheme,
            ),
        ) {
            content()
        }
    }
}

@Composable
fun Card3(
    modifier: Modifier = Modifier,
    elevation: Dp = 1.dp,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier,
        elevation = elevation,
        backgroundColor =
            if (isSystemInDarkTheme()) {
                MaterialTheme.colorScheme.surface
            } else {
                lerp(
                    MaterialTheme.colorScheme.surface,
                    Color(0xFF7E7E7E),
                    0.07f,
                )
            },
    ) {
        ProvideTextStyle(TextStyle(color = MaterialTheme.colorScheme.onSurface)) {
            CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
                content()
            }
        }
    }
}
