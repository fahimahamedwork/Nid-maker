package com.nidcard.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = GovGreen,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = GovGreenLight,
    onPrimaryContainer = androidx.compose.ui.graphics.Color.White,
    secondary = GovGold,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    background = GovBg,
    surface = GovCard,
    onBackground = GovText,
    onSurface = GovText,
    error = GovRed,
    outline = GovBorder,
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFFF0F2F5),
)

private val DarkColorScheme = darkColorScheme(
    primary = GovGreenLight,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = GovGreen,
    onPrimaryContainer = androidx.compose.ui.graphics.Color.White,
    secondary = GovGold,
    background = androidx.compose.ui.graphics.Color(0xFF121212),
    surface = androidx.compose.ui.graphics.Color(0xFF1E1E1E),
    onBackground = androidx.compose.ui.graphics.Color.White,
    onSurface = androidx.compose.ui.graphics.Color.White,
    error = androidx.compose.ui.graphics.Color(0xFFCF6679),
)

@Composable
fun NIDCardTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    // Always use our custom Bangladesh government color scheme
    // dynamicColor = false ensures the green theme is preserved on all devices
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
