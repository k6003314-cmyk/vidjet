package ru.local.schedulewidget

import android.content.Context
import org.json.JSONObject
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields

data class Lesson(
    val day: Int,
    val start: LocalTime,
    val end: LocalTime,
    val week: String,
    val title: String,
    val room: String,
    val teacher: String,
    val clubGroup: Int? = null,
)

data class ScheduledLesson(val lesson: Lesson, val date: LocalDate, val isCurrent: Boolean)

object ScheduleRepository {
    private val timeFormat = DateTimeFormatter.ofPattern("HH:mm")

    fun load(context: Context): List<Lesson> {
        val json = context.assets.open("schedule.json").bufferedReader().use { it.readText() }
        val root = JSONObject(json)
        val array = root.getJSONArray("entries")
        return (0 until array.length()).map { index ->
            val item = array.getJSONObject(index)
            Lesson(
                day = item.getInt("day"),
                start = LocalTime.parse(item.getString("start"), timeFormat),
                end = LocalTime.parse(item.getString("end"), timeFormat),
                week = item.getString("week"),
                title = item.getString("title"),
                room = item.getString("room"),
                teacher = item.getString("teacher"),
                clubGroup = if (item.has("clubGroup")) item.getInt("clubGroup") else null,
            )
        }
    }

    fun nextTwo(context: Context, now: java.time.ZonedDateTime = java.time.ZonedDateTime.now()): List<ScheduledLesson> {
        val root = context.assets.open("schedule.json").bufferedReader().use { JSONObject(it.readText()) }
        val from = LocalDate.parse(root.getString("semesterStart"))
        val through = LocalDate.parse(root.getString("semesterEnd"))
        val today = now.toLocalDate()
        if (today.isAfter(through)) return emptyList()
        val lessons = load(context)
        var date = if (today.isBefore(from)) from else today
        val result = mutableListOf<ScheduledLesson>()
        while (!date.isAfter(through)) {
            val isoWeek = date.get(WeekFields.ISO.weekOfWeekBasedYear())
            val parity = if (isoWeek % 2 == 0) "even" else "odd"
            val dayLessons = lessons
                .filter { it.day == date.dayOfWeek.value && (it.week == "both" || it.week == parity) }
                .sortedBy { it.start }
            for (lesson in dayLessons) {
                val start = date.atTime(lesson.start).atZone(now.zone)
                val end = date.atTime(lesson.end).atZone(now.zone)
                if (now.isBefore(end)) {
                    result += ScheduledLesson(lesson, date, isCurrent = !now.isBefore(start))
                    if (result.size == 2) return result
                }
            }
            date = date.plusDays(1)
        }
        return result
    }

    fun dayLabel(date: LocalDate, today: LocalDate): String = when (date) {
        today -> "Сегодня"
        today.plusDays(1) -> "Завтра"
        else -> {
            val dayName = when (date.dayOfWeek) {
                DayOfWeek.MONDAY -> "Понедельник"
                DayOfWeek.TUESDAY -> "Вторник"
                DayOfWeek.WEDNESDAY -> "Среда"
                DayOfWeek.THURSDAY -> "Четверг"
                DayOfWeek.FRIDAY -> "Пятница"
                DayOfWeek.SATURDAY -> "Суббота"
                DayOfWeek.SUNDAY -> "Воскресенье"
            }
            "$dayName, ${date.dayOfMonth} ${monthName(date.monthValue)}"
        }
    }

    fun monthName(month: Int): String = listOf(
        "января", "февраля", "марта", "апреля", "мая", "июня",
        "июля", "августа", "сентября", "октября", "ноября", "декабря",
    )[month - 1]
}
