package com.example.projectpbd.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = MintPrimary,
    onPrimary = Color.Black,
    primaryContainer = MintPrimary.copy(alpha = 0.2f),
    onPrimaryContainer = MintPrimary,
    secondary = NavySecondary,
    onSecondary = Color.White,
    background = NavyDark,
    onBackground = OffWhite,
    surface = NavySecondary,
    onSurface = OffWhite,
    surfaceVariant = NavyDark,
    onSurfaceVariant = SoftGrey,
    error = ErrorCoral,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = MintPrimary,
    onPrimary = Color.White,
    primaryContainer = MintLight,
    onPrimaryContainer = MintPrimary,
    secondary = NavySecondary,
    onSecondary = Color.White,
    background = OffWhite,
    onBackground = NavyDark,
    surface = Color.White,
    onSurface = NavyDark,
    surfaceVariant = SoftGrey,
    onSurfaceVariant = DarkGrey,
    error = ErrorCoral,
    onError = Color.White
)

@Composable
fun ProjectpbdTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disabled for specific fintech branding
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
