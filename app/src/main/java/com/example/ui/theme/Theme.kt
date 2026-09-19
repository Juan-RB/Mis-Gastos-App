package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import com.example.util.AccessibilitySettings
import com.example.util.AppTheme
import com.example.util.FontSizeScale
import com.example.util.LocalAccessibilitySettings

private val DarkColorScheme =
  darkColorScheme(
    primary = ButtonPrimaryBgDark,
    onPrimary = ButtonPrimaryTextDark,
    primaryContainer = NeoSecondaryBgDark,
    onPrimaryContainer = NeoTextAndBorderDark,
    secondary = ButtonSecondaryBgDark,
    onSecondary = ButtonSecondaryTextDark,
    secondaryContainer = NeoSecondaryBgDark,
    onSecondaryContainer = NeoTextAndBorderDark,
    tertiary = AccentViolet,
    onTertiary = Color.White,
    background = NeoBackgroundDark,
    onBackground = NeoTextAndBorderDark,
    surface = NeoSurfaceDark,
    onSurface = NeoTextAndBorderDark,
    surfaceVariant = NeoSecondaryBgDark,
    onSurfaceVariant = NeoTextAndBorderDark,
    outline = NeoTextAndBorderDark,
    outlineVariant = NeoTextAndBorderDark,
    error = AccentOrange,
    onError = Color.White
  )

private val HighContrastDarkColorScheme =
  darkColorScheme(
    primary = Color(0xFFFFFFFF),
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFF222831),
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = Color(0xFF000000),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFF222831),
    onSecondaryContainer = Color(0xFFFFFFFF),
    tertiary = Color(0xFF7A6BFF),
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFF000000), // Pure black background
    onBackground = Color(0xFFFFFFFF), // Pure white text
    surface = Color(0xFF0D0D0D), // Stark contrast surface
    onSurface = Color(0xFFFFFFFF), // Crisp white text
    surfaceVariant = Color(0xFF1F242C),
    onSurfaceVariant = Color(0xFFFFFFFF),
    outline = Color(0xFFFFFFFF), // Crisp sharp white border
    outlineVariant = Color(0xFFFFFFFF),
    error = Color(0xFFFF3333),
    onError = Color(0xFFFFFFFF)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = ButtonPrimaryBgLight,
    onPrimary = ButtonPrimaryTextLight,
    primaryContainer = NeoBackgroundLight,
    onPrimaryContainer = NeoTextAndBorderLight,
    secondary = ButtonSecondaryBgLight,
    onSecondary = ButtonSecondaryTextLight,
    secondaryContainer = NeoSecondaryBgLight,
    onSecondaryContainer = NeoTextAndBorderLight,
    tertiary = AccentViolet,
    onTertiary = NeoSurfaceLight,
    background = NeoBackgroundLight,
    onBackground = NeoTextAndBorderLight,
    surface = NeoSurfaceLight,
    onSurface = NeoTextAndBorderLight,
    surfaceVariant = NeoSecondaryBgLight,
    onSurfaceVariant = NeoTextAndBorderLight,
    outline = NeoTextAndBorderLight,
    outlineVariant = NeoTextAndBorderLight,
    error = AccentOrange,
    onError = Color.White
  )

private val HighContrastLightColorScheme =
  lightColorScheme(
    primary = Color(0xFF000000),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFC4E2FF),
    onPrimaryContainer = Color(0xFF000000),
    secondary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFF000000),
    secondaryContainer = Color(0xFFE2E8F0),
    onSecondaryContainer = Color(0xFF000000),
    tertiary = Color(0xFF4332D6),
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFFCCE4FF), // Slightly darker contrast background
    onBackground = Color(0xFF000000), // Pure black text
    surface = Color(0xFFFFFFFF), // Pure white surface
    onSurface = Color(0xFF000000), // Pure black text
    surfaceVariant = Color(0xFFD6DFE8),
    onSurfaceVariant = Color(0xFF000000), // Pure black text
    outline = Color(0xFF000000), // Pure black borders
    outlineVariant = Color(0xFF000000),
    error = Color(0xFFD31A00),
    onError = Color(0xFFFFFFFF)
  )

@Composable
fun MyApplicationTheme(
  accessibilitySettings: AccessibilitySettings = AccessibilitySettings(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val systemDark = isSystemInDarkTheme()
  val darkTheme = when (accessibilitySettings.appTheme) {
    AppTheme.System -> systemDark
    AppTheme.Light -> false
    AppTheme.Dark -> true
  }

  val dynamicAccent = Color(accessibilitySettings.accentColor)

  val baseColorScheme = when {
    darkTheme && accessibilitySettings.isHighContrast -> HighContrastDarkColorScheme
    darkTheme -> DarkColorScheme
    accessibilitySettings.isHighContrast -> HighContrastLightColorScheme
    else -> LightColorScheme
  }

  val colorScheme = if (accessibilitySettings.isHighContrast) {
    baseColorScheme
  } else {
    // Incorporamos el color de acento respetando el estilo neobrutalista
    baseColorScheme.copy(
      tertiary = dynamicAccent,
      onTertiary = if (darkTheme) Color.Black else Color.White
    )
  }

  val scaledTypography = getScaledTypography(accessibilitySettings.fontSizeScale.factor)

  CompositionLocalProvider(LocalAccessibilitySettings provides accessibilitySettings) {
    MaterialTheme(colorScheme = colorScheme, typography = scaledTypography, content = content)
  }
}


