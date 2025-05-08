package com.example.sweetbakes.ToDo

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class ReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val title = inputData.getString("title") ?: return Result.failure()
        val description = inputData.getString("description") ?: ""

        NotificationUtils.showNotification(
            applicationContext,
            title,
            description,
            title.hashCode()
        )

        return Result.success()
    }
}