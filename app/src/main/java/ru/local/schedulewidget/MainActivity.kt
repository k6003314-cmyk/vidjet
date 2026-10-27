package ru.local.schedulewidget

import android.os.Bundle
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFFB995E8),
                    background = Color(0xFF20152B),
                    surface = Color(0xFF2B2038),
                ),
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val lessons = remember { ScheduleRepository.load(this) }
                    val dayNames = listOf(
                        DayOfWeek.MONDAY to "Понедельник",
                        DayOfWeek.TUESDAY to "Вторник",
                        DayOfWeek.WEDNESDAY to "Среда",
                        DayOfWeek.THURSDAY to "Четверг",
                        DayOfWeek.FRIDAY to "Пятница",
                        DayOfWeek.SATURDAY to "Суббота",
                    )
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        item {
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Расписание ИСП-25-1", style = MaterialTheme.typography.headlineSmall)
                                    Text("1 семестр · до 27 декабря 2026", style = MaterialTheme.typography.bodyMedium)
                                }
                                TextButton(onClick = { startActivity(Intent(this@MainActivity, SettingsActivity::class.java)) }) {
                                    Text("Настройки")
                                }
                            }
                        }
                        dayNames.forEach { (day, label) ->
                            val dayLessons = lessons.filter { it.day == day.value }.sortedBy { it.start }
                            if (dayLessons.isNotEmpty()) {
                                item { Text(label, style = MaterialTheme.typography.titleLarge) }
                                items(dayLessons) { lesson ->
                                    Card {
                                        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Text("${lesson.start}\n${lesson.end}", style = MaterialTheme.typography.labelLarge)
                                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                                Text(lesson.title, style = MaterialTheme.typography.bodyLarge)
                                                val weekLabel = when (lesson.week) {
                                                    "odd" -> "Нечётная неделя"
                                                    "even" -> "Чётная неделя"
                                                    else -> "Каждую неделю"
                                                }
                                                Text("$weekLabel · ауд. ${lesson.room}", style = MaterialTheme.typography.bodySmall)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
