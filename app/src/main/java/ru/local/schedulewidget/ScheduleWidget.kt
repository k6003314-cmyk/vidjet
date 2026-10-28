package ru.local.schedulewidget

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import java.time.ZonedDateTime

class ScheduleWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Responsive(
        setOf(DpSize(280.dp, 180.dp), DpSize(280.dp, 145.dp)),
    )

    override suspend fun provideGlance(context: android.content.Context, id: GlanceId) {
        val lessons = ScheduleRepository.nextTwo(context, ZonedDateTime.now())
        val palette = WidgetTheme.palette(WidgetTheme.selected(context))
        provideContent {
            val compact = LocalSize.current.height < 165.dp
            Column(
                modifier = GlanceModifier.fillMaxSize()
                    .background(ColorProvider(palette.background))
                    .padding(if (compact) 7.dp else 10.dp)
                    .clickable(actionStartActivity<MainActivity>()),
            ) {
                Text(
                    "РАСПИСАНИЕ · ИСП-25-1",
                    style = TextStyle(
                        color = ColorProvider(palette.accent),
                        fontSize = if (compact) 9.sp else 11.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    maxLines = 1,
                )
                Spacer(modifier = GlanceModifier.height(if (compact) 3.dp else 6.dp))

                if (lessons.isEmpty()) {
                    Text(
                        "Пар больше нет",
                        style = TextStyle(color = ColorProvider(Color.White), fontSize = 13.sp),
                        maxLines = 1,
                    )
                } else {
                    lessons.forEachIndexed { index, scheduled ->
                        val lesson = scheduled.lesson
                        val label = when {
                            index == 0 && scheduled.isCurrent -> "СЕЙЧАС"
                            index == 0 -> "БЛИЖАЙШАЯ"
                            else -> "СЛЕДУЮЩАЯ"
                        }
                        Column(
                            modifier = GlanceModifier.fillMaxWidth()
                                .background(ColorProvider(if (index == 0) palette.currentCard else palette.nextCard))
                                .cornerRadius(if (compact) 9.dp else 12.dp)
                                .padding(horizontal = if (compact) 7.dp else 9.dp, vertical = if (compact) 5.dp else 7.dp),
                        ) {
                            Row {
                                Text(
                                    label,
                                    style = TextStyle(
                                        color = ColorProvider(palette.accent),
                                        fontSize = if (compact) 8.sp else 9.sp,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    maxLines = 1,
                                )
                                Text(
                                    "  ${lesson.start}–${lesson.end}",
                                    style = TextStyle(
                                        color = ColorProvider(palette.accent),
                                        fontSize = if (compact) 9.sp else 11.sp,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    maxLines = 1,
                                )
                            }
                            Text(
                                if (compact) shortTitle(lesson.title) else lesson.title,
                                style = TextStyle(
                                    color = ColorProvider(Color.White),
                                    fontSize = if (compact) 10.sp else 13.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                                maxLines = if (compact) 1 else 2,
                            )
                            if (!compact) {
                                Text(
                                    buildString {
                                        append("Ауд. ${lesson.room}")
                                        if (lesson.teacher.isNotBlank()) append(" · ${lesson.teacher}")
                                    },
                                    style = TextStyle(color = ColorProvider(palette.secondaryText), fontSize = 9.sp),
                                    maxLines = 1,
                                )
                            }
                        }
                        if (index == 0 && lessons.size > 1) {
                            Spacer(modifier = GlanceModifier.height(if (compact) 3.dp else 6.dp))
                        }
                    }
                }
            }
        }
    }

    private fun shortTitle(title: String): String =
        if (title.length > 39) title.take(36).trimEnd() + "…" else title
}

class ScheduleWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ScheduleWidget()

    override fun onEnabled(context: android.content.Context) {
        super.onEnabled(context)
        ScheduleRefreshScheduler.ensure(context)
        ScheduleRefreshScheduler.requestImmediate(context)
    }
}
