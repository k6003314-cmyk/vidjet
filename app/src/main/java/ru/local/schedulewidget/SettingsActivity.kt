package ru.local.schedulewidget

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val background = Color(0xFF160F20)
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFFB995E8),
                    background = background,
                    surface = Color(0xFF251B30),
                ),
            ) {
                Surface(modifier = Modifier.fillMaxSize(), color = background) {
                    var selected by remember { mutableStateOf(WidgetTheme.selected(this)) }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        item {
                            Column(modifier = Modifier.padding(top = 24.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Настройки", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                                Text("Настрой внешний вид виджета", color = Color(0xFFB8A9C4), fontSize = 15.sp)
                            }
                        }
                        item {
                            Text("ЦВЕТ ТЕМЫ", color = Color(0xFFD9C4F4), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    ThemeOption("Фиолетовый", Color(0xFF8F5BE8), selected == WidgetTheme.PURPLE, Modifier.weight(1f)) {
                                        select(WidgetTheme.PURPLE) { selected = it }
                                    }
                                    ThemeOption("Синий", Color(0xFF438CE3), selected == WidgetTheme.BLUE, Modifier.weight(1f)) {
                                        select(WidgetTheme.BLUE) { selected = it }
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    ThemeOption("Зелёный", Color(0xFF38A879), selected == WidgetTheme.GREEN, Modifier.weight(1f)) {
                                        select(WidgetTheme.GREEN) { selected = it }
                                    }
                                    ThemeOption("Красный", Color(0xFFD65368), selected == WidgetTheme.RED, Modifier.weight(1f)) {
                                        select(WidgetTheme.RED) { selected = it }
                                    }
                                }
                            }
                        }
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF251B30)),
                                shape = RoundedCornerShape(20.dp),
                            ) {
                                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text("Размер виджета", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
                                    Text("Большой показывает аудиторию и преподавателя, но занимает меньше высоты.", color = Color(0xFFB8A9C4), fontSize = 14.sp)
                                    Text("Компактный показывает время и короткие названия двух ближайших пар.", color = Color(0xFFB8A9C4), fontSize = 14.sp)
                                    Text("Размер можно изменить на главном экране: удерживай виджет и потяни за рамку.", color = Color(0xFF9F91AE), fontSize = 13.sp)
                                }
                            }
                        }
                        item {
                            Surface(color = Color(0xFF21182B), shape = RoundedCornerShape(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    Surface(color = Color(0xFF3A2A4B), shape = CircleShape, modifier = Modifier.size(38.dp)) {
                                        Spacer(Modifier.size(38.dp))
                                    }
                                    Column {
                                        Text("Обновление расписания", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                        Text("Проверка в фоне примерно каждые 30 минут", color = Color(0xFFB8A9C4), fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                        item {
                            TextButton(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    startActivity(Intent(this@SettingsActivity, MainActivity::class.java))
                                    finish()
                                },
                            ) {
                                Text("←  К расписанию", color = Color(0xFFD9C4F4), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        item { Spacer(Modifier.height(12.dp)) }
                    }
                }
            }
        }
    }

    private fun select(theme: String, updateSelection: (String) -> Unit) {
        updateSelection(theme)
        WidgetTheme.save(this, theme)
        ScheduleRefreshScheduler.requestImmediate(this)
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
            onClick = onClick,
            colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFF392A49) else Color(0xFF251B30)),
            shape = RoundedCornerShape(18.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 17.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(11.dp),
            ) {
                Surface(color = swatch, shape = CircleShape, modifier = Modifier.size(28.dp)) {
                    Spacer(Modifier.size(28.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(label, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    if (isSelected) Text("Выбрана", color = Color(0xFFD9C4F4), fontSize = 11.sp)
                }
                if (isSelected) Text("✓", color = Color(0xFFD9C4F4), fontSize = 19.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
