package com.example.nkust_app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * 把 Material 的顏色角色對應到 iOS 系統色：
 * background / surface        → systemBackground（淺色白、深色純黑）
 * surfaceContainerLow         → systemGray6（卡片底色）
 * surfaceVariant              → systemGray5（未選中的膠囊）
 * onSurfaceVariant            → secondaryLabel（灰色副標）
 */
private val LightColorScheme = lightColorScheme(
    primary = IosTealLight,
    onPrimary = Color.White,
    secondary = IosOrange,
    onSecondary = Color.White,
    background = Color.White,
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    surfaceVariant = IosGray5Light,
    onSurfaceVariant = IosSecondaryLabelLight,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = IosGray6Light,
    surfaceContainer = IosGray6Light,
    surfaceContainerHigh = IosGray5Light,
    surfaceContainerHighest = IosGray4Light,
    outlineVariant = IosSeparatorLight,
    error = IosRedLight,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = IosTealDark,
    onPrimary = Color.White,
    secondary = IosOrange,
    onSecondary = Color.White,
    background = Color.Black,
    onBackground = Color.White,
    surface = Color.Black,
    onSurface = Color.White,
    surfaceVariant = IosGray5Dark,
    onSurfaceVariant = IosSecondaryLabelDark,
    surfaceContainerLowest = Color.Black,
    surfaceContainerLow = IosGray6Dark,
    surfaceContainer = IosGray6Dark,
    surfaceContainerHigh = IosGray5Dark,
    surfaceContainerHighest = IosGray4Dark,
    outlineVariant = IosSeparatorDark,
    error = IosRedDark,
    onError = Color.White
)

@Composable
fun Nkust_AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}