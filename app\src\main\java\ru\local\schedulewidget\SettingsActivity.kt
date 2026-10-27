package ru.local.schedulewidget

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.appwidget.GlanceAppWidgetManager
import kotlinx.coroutines.launch

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary = Color(0xFFB995E8), background = Color(0xFF130D1D), surface = Color(0xFF21182B))) {
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF130D1D)) {
                    var selected by remember { mutableStateOf(WidgetTheme.selected(this)) }
                    val scope = rememberCoroutineScope()
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        item {
                            Text("Настройки виджета", style = MaterialTheme.typography.headlineSmall, color = Color.White)
                            Text("Выберите цвет. Размер можно изменить на главном экране.", color = Color(0xFFB8A9C4))
                        }
                        item { Text("ЦВЕТОВАЯ ТЕМА", color = Color(0xFFC8B0EF), style = MaterialTheme.typography.labelLarge) }
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    ThemeOption("Фиолетовый", Color(0xFF8F5BE8), selected == WidgetTheme.PURPLE, Modifier.weight(1f)) {
                                        chooseTheme(WidgetTheme.PURPLE, scope) { selected = it }
                                    }
                                    ThemeOption("Синий", Color(0xFF438CE3), selected == WidgetTheme.BLUE, Modifier.weight(1f)) {
                                        chooseTheme(WidgetTheme.BLUE, scope) { selected = it }
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    ThemeOption("Зелёный", Color(0xFF38A879), selected == WidgetTheme.GREEN, Modifier.weight(1f)) {
                                        chooseTheme(WidgetTheme.GREEN, scope) { selected = it }
                                    }
                                    ThemeOption("Красный", Color(0xFFD65368), selected == WidgetTheme.RED, Modifier.weight(1f)) {
                                        chooseTheme(WidgetTheme.RED, scope) { selected = it }
                                    }
                                }
                            }
                        }
                        item { Text("РАЗМЕРЫ", color = Color(0xFFC8B0EF), style = MaterialTheme.typography.labelLarge) }
                        item {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Обычный", color = Color.White, style = MaterialTheme.typography.titleMedium)
                                    Text("Показывает две пары с аудиторией и преподавателем.", color = Color(0xFFB8A9C4))
                                    Text("Компактный", color = Color.White, style = MaterialTheme.typography.titleMedium)
                                    Text("Показывает те же пары в более плотном виде.", color = Color(0xFFB8A9C4))
                                    Text("Чтобы выбрать размер, добавьте виджет на экран или удерживайте его и потяните за границу.", color = Color(0xFFB8A9C4))
                                }
                            }
                        }
                        item {
                            Button(onClick = { startActivity(Intent(this@SettingsActivity, MainActivity::class.java)); finish() }) {
                                Text("К расписанию")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun chooseTheme(theme: String, scope: kotlinx.coroutines.CoroutineScope, updateSelection: (String) -> Unit) {
        updateSelection(theme)
        WidgetTheme.save(this, theme)
        scope.launch {
            val manager = GlanceAppWidgetManager(this@SettingsActivity)
            manager.getGlanceIds(ScheduleWidget::class.java).forEach { id ->
                ScheduleWidget().update(this@SettingsActivity, id)
            }
        }
    }

    @Composable
    private fun ThemeOption(
        label: String,
        swatch: Color,
        isSelected: Boolean,
        modifier: Modifier,
        onClick: () -> Unit,
    ) {
        Card(
            modifier = modifier,
            border = if (isSelected) BorderStroke(2.dp, Color(0xFFB995E8)) else null,
            onClick = onClick,
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("●", color = swatch, fontSize = 22.sp)
                    if (isSelected) Text("✓", color = Color(0xFFD8C4F3))
                }
                Text(label, color = Color.White, fontSize = 14.sp)
            }
        }
    }
}
