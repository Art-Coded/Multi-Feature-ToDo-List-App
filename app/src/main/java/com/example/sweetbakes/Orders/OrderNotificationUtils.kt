package com.example.sweetbakes.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.sweetbakes.R

object OrderNotificationUtils {
    private const val CHANNEL_ID = "order_reminder_channel"
    private const val CHANNEL_NAME = "Order Reminders"

    fun createNotificationChannel(context: Context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for Order reminders"
            }

            val notificationManager = context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showOrderNotification(
        context: Context,
        title: String,
        description: String,
        notificationId: Int
    ) {
        val appContext = context.applicationContext

        val builder = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.applogo)
            .setContentTitle("Reminder! Don't forget about the pending order: '$title'!")
            .setContentText(description)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(appContext)) {
            notify(notificationId, builder.build())
        }
    }
}