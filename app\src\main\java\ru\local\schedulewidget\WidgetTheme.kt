package ru.local.schedulewidget

import android.content.Context
import androidx.compose.ui.graphics.Color

data class WidgetPalette(
    val background: Color,
    val currentCard: Color,
    val nextCard: Color,
    val accent: Color,
    val secondaryText: Color,
)

object WidgetTheme {
    private const val PREFS = "widget_settings"
    private const val THEME_KEY = "theme"

    const val PURPLE = "purple"
    const val BLUE = "blue"
    const val GREEN = "green"
    const val RED = "red"

    fun selected(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(THEME_KEY, PURPLE) ?: PURPLE

    fun save(context: Context, theme: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(THEME_KEY, theme).apply()
    }

    fun palette(theme: String): WidgetPalette = when (theme) {
        BLUE -> WidgetPalette(Color(0xFF101B2B), Color(0xFF1B3D5E), Color(0xFF172A40), Color(0xFF9CCFFF), Color(0xFFB4C4D5))
        GREEN -> WidgetPalette(Color(0xFF101F1A), Color(0xFF214838), Color(0xFF193629), Color(0xFF9BE0B8), Color(0xFFB6CFC0))
        RED -> WidgetPalette(Color(0xFF26151B), Color(0xFF512A36), Color(0xFF3A202A), Color(0xFFFFB5C0), Color(0xFFD1B5BC))
        else -> WidgetPalette(Color(0xFF20152B), Color(0xFF3A2A4B), Color(0xFF2B2038), Color(0xFFD8C4F3), Color(0xFFB8A9C4))
    }
}
