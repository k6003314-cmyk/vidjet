package ru.local.schedulewidget

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.clickable
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.height
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.cornerRadius
import androidx.glance.unit.ColorProvider
import androidx.glance.unit.sp
import java.time.ZonedDateTime

class ScheduleWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Responsive(
        setOf(DpSize(280.dp, 240.dp), DpSize(280.dp, 150.dp)),
    )

    override suspend fun provideGlance(context: android.content.Context, id: GlanceId) {
        val lessons = ScheduleRepository.nextTwo(context, ZonedDateTime.now())
        val palette = WidgetTheme.palette(WidgetTheme.selected(context))
        provideContent {
            val compact = LocalSize.current.height < 190.dp
            Column(
                modifier = GlanceModifier.fillMaxSize()
                    .background(ColorProvider(palette.background))
                    .padding(if (compact) 8.dp else 12.dp)
                    .clickable(actionStartActivity<MainActivity>()),
            ) {
                Text("РАСПИСАНИЕ · ИСП-25-1", style = TextStyle(color = ColorProvider(palette.accent), fontSize = if (compact) 11.sp else 13.sp, fontWeight = FontWeight.Bold))
                androidx.glance.layout.Spacer(modifier = GlanceModifier.height(if (compact) 4.dp else 8.dp))
                if (lessons.isEmpty()) {
                    Text("В этом семестре занятий больше нет", style = TextStyle(color = ColorProvider(Color.White), fontSize = 16.sp))
                } else {
                    val today = ZonedDateTime.now().toLocalDate()
                    lessons.forEachIndexed { index, scheduled ->
                        val lesson = scheduled.lesson
                        Column(
                            modifier = GlanceModifier.fillMaxWidth()
                                .background(ColorProvider(if (index == 0) palette.currentCard else palette.nextCard))
                                .cornerRadius(if (compact) 10.dp else 14.dp)
                                .padding(if (compact) 7.dp else 10.dp),
                        ) {
                            Row {
                                Text(
                                    if (index == 0 && scheduled.isCurrent) "СЕЙЧАС" else if (index == 0) "БЛИЖАЙШАЯ" else "ДАЛЬШЕ",
                                    style = TextStyle(color = ColorProvider(palette.accent), fontSize = if (compact) 10.sp else 11.sp, fontWeight = FontWeight.Bold),
                                )
                                Text("  ${lesson.start} — ${lesson.end}", style = TextStyle(color = ColorProvider(palette.accent), fontSize = if (compact) 11.sp else 13.sp, fontWeight = FontWeight.Bold))
                            }
                            Text(lesson.title, style = TextStyle(color = ColorProvider(Color.White), fontSize = if (compact) 12.sp else 15.sp, fontWeight = FontWeight.Bold))
                            Text(
                                buildString {
                                    if (scheduled.date != today) append("${ScheduleRepository.dayLabel(scheduled.date, today)} · ")
                                    append("Ауд. ${lesson.room}")
                                    if (!compact && lesson.teacher.isNotBlank()) append(" · ${lesson.teacher}")
                                },
                                style = TextStyle(color = ColorProvider(palette.secondaryText), fontSize = if (compact) 10.sp else 11.sp),
                            )
                        }
                        if (index == 0 && lessons.size > 1) {
                            androidx.glance.layout.Spacer(modifier = GlanceModifier.height(if (compact) 4.dp else 8.dp))
                        }
                    }
                }
            }
        }
    }
}


class ScheduleWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ScheduleWidget()
}
