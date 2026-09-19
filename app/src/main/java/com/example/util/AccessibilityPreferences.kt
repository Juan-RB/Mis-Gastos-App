package com.example.util

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.accessibilityDataStore: DataStore<Preferences> by preferencesDataStore(name = "accessibility_preferences")

enum class FontSizeScale(val label: String, val factor: Float) {
    SMALL("Pequeño", 0.85f),
    NORMAL("Normal", 1.0f),
    LARGE("Grande", 1.15f),
    EXTRA_LARGE("Muy grande", 1.30f);

    companion object {
        fun fromName(name: String?): FontSizeScale {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: NORMAL
        }
    }
}

enum class ContrastLevel(val label: String) {
    NORMAL("Normal"),
    HIGH("Alto contraste");

    companion object {
        fun fromHighContrast(isHigh: Boolean): ContrastLevel {
            return if (isHigh) HIGH else NORMAL
        }
    }
}

/**
 * Arquitectura de Temas Extensible
 */
sealed interface AppTheme {
    val id: String
    val label: String

    data object System : AppTheme {
        override val id: String = "SYSTEM"
        override val label: String = "Sistema"
    }

    data object Light : AppTheme {
        override val id: String = "LIGHT"
        override val label: String = "Claro"
    }

    data object Dark : AppTheme {
        override val id: String = "DARK"
        override val label: String = "Oscuro"
    }

    companion object {
        val allThemes: List<AppTheme> = listOf(System, Light, Dark)

        fun fromId(id: String?): AppTheme {
            return when (id?.uppercase()) {
                "LIGHT" -> Light
                "DARK" -> Dark
                else -> System
            }
        }
    }
}

object PresetAccentColors {
    // 10 colores vibrantes y neobrutalistas seleccionados cuidadosamente
    val colors: List<Long> = listOf(
        0xFF5C4ADEL, // Violeta Neo (original)
        0xFF4DA2FFL, // Azul Eléctrico
        0xFF55DB9CL, // Verde Menta
        0xFFFB4903L, // Naranja Fuego
        0xFFFFD731L, // Amarillo Sol
        0xFFE02424L, // Rojo Carmín
        0xFFEC4899L, // Rosa Magenta
        0xFF06B6D4L, // Cian Turquesa
        0xFF8B5CF6L, // Púrpura Intenso
        0xFF10B981L  // Esmeralda
    )

    const val DEFAULT_ACCENT: Long = 0xFF5C4ADEL
}

data class AccessibilitySettings(
    val fontSizeScale: FontSizeScale = FontSizeScale.NORMAL,
    val isHighContrast: Boolean = false,
    val appTheme: AppTheme = AppTheme.System,
    val accentColor: Long = PresetAccentColors.DEFAULT_ACCENT
)

val LocalAccessibilitySettings = compositionLocalOf { AccessibilitySettings() }

class AccessibilityPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val FONT_SIZE_SCALE = stringPreferencesKey("font_size_scale")
        val HIGH_CONTRAST = booleanPreferencesKey("high_contrast")
        val APP_THEME = stringPreferencesKey("app_theme")
        val ACCENT_COLOR_HEX = stringPreferencesKey("accent_color_hex")
    }

    val accessibilitySettingsFlow: Flow<AccessibilitySettings> = context.accessibilityDataStore.data
        .map { preferences ->
            val scaleName = preferences[PreferencesKeys.FONT_SIZE_SCALE] ?: FontSizeScale.NORMAL.name
            val isHigh = preferences[PreferencesKeys.HIGH_CONTRAST] ?: false
            val themeId = preferences[PreferencesKeys.APP_THEME] ?: AppTheme.System.id
            val accentHexStr = preferences[PreferencesKeys.ACCENT_COLOR_HEX]
            val accentLong = parseHexColorToLong(accentHexStr) ?: PresetAccentColors.DEFAULT_ACCENT

            AccessibilitySettings(
                fontSizeScale = FontSizeScale.fromName(scaleName),
                isHighContrast = isHigh,
                appTheme = AppTheme.fromId(themeId),
                accentColor = accentLong
            )
        }

    suspend fun setFontSizeScale(scale: FontSizeScale) {
        context.accessibilityDataStore.edit { preferences ->
            preferences[PreferencesKeys.FONT_SIZE_SCALE] = scale.name
        }
    }

    suspend fun setHighContrast(isHigh: Boolean) {
        context.accessibilityDataStore.edit { preferences ->
            preferences[PreferencesKeys.HIGH_CONTRAST] = isHigh
        }
    }

    suspend fun setAppTheme(theme: AppTheme) {
        context.accessibilityDataStore.edit { preferences ->
            preferences[PreferencesKeys.APP_THEME] = theme.id
        }
    }

    suspend fun setAccentColor(colorLong: Long) {
        context.accessibilityDataStore.edit { preferences ->
            preferences[PreferencesKeys.ACCENT_COLOR_HEX] = String.format("#%08X", colorLong)
        }
    }

    companion object {
        fun parseHexColorToLong(hex: String?): Long? {
            if (hex.isNullOrBlank()) return null
            val cleanHex = hex.trim().removePrefix("#")
            return try {
                when (cleanHex.length) {
                    6 -> {
                        // RRGGBB -> AARRGGBB with FF alpha
                        0xFF000000L or cleanHex.toLong(16)
                    }
                    8 -> {
                        // AARRGGBB
                        cleanHex.toLong(16)
                    }
                    else -> null
                }
            } catch (e: Exception) {
                null
            }
        }
    }
}
