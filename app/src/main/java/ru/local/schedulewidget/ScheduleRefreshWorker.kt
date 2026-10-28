package ru.local.schedulewidget

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.glance.appwidget.updateAll
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

object ScheduleRefreshScheduler {
    private const val PERIODIC_WORK = "schedule-refresh-every-30-minutes"
    private const val IMMEDIATE_WORK = "schedule-refresh-immediate"
    private const val BOUNDARY_WORK = "schedule-refresh-at-class-boundary"

    fun ensure(context: Context) {
        val periodic = PeriodicWorkRequestBuilder<ScheduleRefreshWorker>(30, TimeUnit.MINUTES).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            periodic,
        )
        scheduleNextBoundary(context)
    }

    fun requestImmediate(context: Context) {
        val request = OneTimeWorkRequestBuilder<ScheduleRefreshWorker>().build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            IMMEDIATE_WORK,
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }

    fun scheduleNextBoundary(context: Context) {
        val now = ZonedDateTime.now()
        val boundary = ScheduleRepository.nextTransition(context, now) ?: return
        val delay = Duration.between(now, boundary).toMillis().coerceAtLeast(1_000L)
        val request = OneTimeWorkRequestBuilder<ScheduleBoundaryWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            BOUNDARY_WORK,
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }
}

class ScheduleRefreshWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = runCatching {
        ScheduleWidget().updateAll(applicationContext)
        Result.success()
    }.getOrElse { Result.retry() }
}

class ScheduleBoundaryWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = runCatching {
        ScheduleWidget().updateAll(applicationContext)
        ScheduleRefreshScheduler.scheduleNextBoundary(applicationContext)
        Result.success()
    }.getOrElse { Result.retry() }
}
