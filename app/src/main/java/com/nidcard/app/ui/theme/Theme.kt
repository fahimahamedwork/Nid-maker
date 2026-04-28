package com.nidcard.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

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
)

@Composable
fun NIDCardTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme()
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
