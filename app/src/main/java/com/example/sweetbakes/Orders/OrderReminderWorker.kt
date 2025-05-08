package com.example.sweetbakes.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.sweetbakes.utils.OrderNotificationUtils

class OrderReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val title = inputData.getString("title") ?: return Result.failure()
        val description = inputData.getString("description") ?: ""
        val orderId = inputData.getString("orderId") ?: return Result.failure()

        OrderNotificationUtils.showOrderNotification(
            applicationContext,
            title,
            description,
            orderId.hashCode()
        )

        return Result.success()
    }
}