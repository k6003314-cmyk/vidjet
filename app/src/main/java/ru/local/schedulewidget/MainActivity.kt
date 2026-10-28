package ru.local.schedulewidget

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ScheduleRefreshScheduler.ensure(this)
        ScheduleRefreshScheduler.requestImmediate(this)
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
                    val lessons = remember { ScheduleRepository.load(this) }
                    val now = remember { ZonedDateTime.now() }
                    val upcoming = remember { ScheduleRepository.nextTwo(this, now) }
                    val weekStart = remember {
                        LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    }
                    val dates = remember(weekStart) { (0L..5L).map { weekStart.plusDays(it) } }
                    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
                    val parity = if (selectedDate.get(WeekFields.ISO.weekOfWeekBasedYear()) % 2 == 0) "even" else "odd"
                    val selectedLessons = lessons
                        .filter { it.day == selectedDate.dayOfWeek.value && (it.week == "both" || it.week == parity) }
                        .sortedBy { it.start }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.verticalGradient(listOf(background, Color(0xFF21152D))))
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Column {
                                    Text("Расписание", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                                    Text("ИСП-25-1 · 1 семестр", color = Color(0xFFB8A9C4), fontSize = 14.sp)
                                }
                                TextButton(onClick = {
                                    startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                                }) {
                                    Text("Настройки", color = Color(0xFFD8C4F3), fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        item {
                            Surface(color = Color(0xFF30243E), shape = RoundedCornerShape(18.dp)) {
                                Text(
                                    text = "${weekdayName(LocalDate.now().dayOfWeek).uppercase()} · ${LocalDate.now().dayOfMonth} ${ScheduleRepository.monthName(LocalDate.now().monthValue).uppercase()}",
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 13.dp),
                                    color = Color(0xFFD9C4F4),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }

                        item {
                            val first = upcoming.firstOrNull()
                            Surface(
                                color = Color.Transparent,
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier.fillMaxWidth().background(
                                    Brush.linearGradient(listOf(Color(0xFF714CB0), Color(0xFF3E2A57))),
                                    RoundedCornerShape(24.dp),
                                ),
                            ) {
                                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                    Text(
                                        if (first?.isCurrent == true) "ИДЁТ СЕЙЧАС" else "СЛЕДУЮЩАЯ ПАРА",
                                        color = Color(0xFFE5D9F4),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    if (first == null) {
                                        Text("На семестр пар больше нет", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                                    } else {
                                        Text(
                                            "${first.lesson.start} — ${first.lesson.end}",
                                            color = Color.White,
                                            fontSize = 29.sp,
                                            fontWeight = FontWeight.Bold,
                                        )
                                        Text(
                                            first.lesson.title,
                                            color = Color.White,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        Text("Ауд. ${first.lesson.room}", color = Color(0xFFE1D5ED), fontSize = 14.sp)
                                    }
                                }
                            }
                        }

                        item {
                            Text("Расписание на день", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                        }

                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                dates.forEach { date ->
                                    val selected = date == selectedDate
                                    val today = date == LocalDate.now()
                                    Surface(
                                        modifier = Modifier.weight(1f).clickable { selectedDate = date },
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (selected) Color(0xFF744CB0) else Color(0xFF2A2035),
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.padding(vertical = 10.dp),
                                        ) {
                                            Text(dayShort(date.dayOfWeek), color = if (selected) Color.White else Color(0xFFB8A9C4), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Spacer(Modifier.height(3.dp))
                                            Text(date.dayOfMonth.toString(), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                            if (today) Box(Modifier.padding(top = 3.dp).size(4.dp).background(Color(0xFFC5F2D3), CircleShape))
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Text(
                                "${weekdayName(selectedDate.dayOfWeek)} · ${selectedDate.dayOfMonth} ${ScheduleRepository.monthName(selectedDate.monthValue)}",
                                color = Color(0xFFD9C4F4),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }

                        if (selectedLessons.isEmpty()) {
                            item {
                                Surface(color = Color(0xFF251B30), shape = RoundedCornerShape(18.dp)) {
                                    Text("В этот день пар нет", modifier = Modifier.padding(18.dp), color = Color(0xFFB8A9C4), fontSize = 16.sp)
                                }
                            }
                        } else {
                            items(selectedLessons) { lesson ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2035)),
                                    shape = RoundedCornerShape(18.dp),
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                                    ) {
                                        Column(modifier = Modifier.width(58.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text(lesson.start.toString(), color = Color(0xFFD9C4F4), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                            Text(lesson.end.toString(), color = Color(0xFF9F91AE), fontSize = 13.sp)
                                        }
                                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text(lesson.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                                            Text(
                                                "${weekLabel(lesson.week)} · ауд. ${lesson.room}",
                                                color = Color(0xFFB8A9C4),
                                                fontSize = 12.sp,
                                            )
                                            if (lesson.teacher.isNotBlank()) {
                                                Text(lesson.teacher, color = Color(0xFFB8A9C4), fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        item { Spacer(Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }

    private fun weekdayName(day: DayOfWeek): String = when (day) {
        DayOfWeek.MONDAY -> "Понедельник"
        DayOfWeek.TUESDAY -> "Вторник"
        DayOfWeek.WEDNESDAY -> "Среда"
        DayOfWeek.THURSDAY -> "Четверг"
        DayOfWeek.FRIDAY -> "Пятница"
        DayOfWeek.SATURDAY -> "Суббота"
        DayOfWeek.SUNDAY -> "Воскресенье"
    }

    private fun dayShort(day: DayOfWeek): String = when (day) {
        DayOfWeek.MONDAY -> "ПН"
        DayOfWeek.TUESDAY -> "ВТ"
        DayOfWeek.WEDNESDAY -> "СР"
        DayOfWeek.THURSDAY -> "ЧТ"
        DayOfWeek.FRIDAY -> "ПТ"
        DayOfWeek.SATURDAY -> "СБ"
        DayOfWeek.SUNDAY -> "ВС"
    }

    private fun weekLabel(week: String): String = when (week) {
        "odd" -> "Нечётная неделя"
        "even" -> "Чётная неделя"
        else -> "Каждую неделю"
    }
}
