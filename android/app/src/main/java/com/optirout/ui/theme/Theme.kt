package com.optirout

import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColors: ColorScheme = darkColorScheme(
    primary = Color(0xFF7CD4FF),
    onPrimary = Color(0xFF06202F),
    secondary = Color(0xFF8AE6A6),
    background = Color(0xFF0B1117),
    surface = Color(0xFF111922),
    surfaceVariant = Color(0xFF182230),
    onSurfaceVariant = Color(0xFFB6C3D1),
)

private val LightColors: ColorScheme = lightColorScheme(
    primary = Color(0xFF0F5DA8),
    onPrimary = Color.White,
    secondary = Color(0xFF166A44),
    background = Color(0xFFF4F7FB),
    surface = Color.White,
    surfaceVariant = Color(0xFFE6ECF3),
    onSurfaceVariant = Color(0xFF526272),
)

@Composable
fun OptiRoutTheme(
    useDarkTheme: Boolean = true,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (useDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        useDarkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = androidx.compose.material3.Typography(),
        content = content,
    )
}
