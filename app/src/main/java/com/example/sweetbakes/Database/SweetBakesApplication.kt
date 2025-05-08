package com.example.sweetbakes

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.work.*
import com.example.sweetbakes.data.AppDatabase
import com.example.sweetbakes.model.SettingsEntity
import com.example.sweetbakes.ToDo.NotificationUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

class SweetBakesApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    private val applicationScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        LocaleHelper.onAttach(this)
        scheduleMidnightCleanup()
        initializeDefaultSettings()
        initializeNotifications()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        LocaleHelper.onAttach(this)
    }

    private fun initializeNotifications() {
        NotificationUtils.createNotificationChannel(this)
    }

    private fun initializeDefaultSettings() {
        applicationScope.launch {
            if (database.settingsDao().getDarkMode() == null) {
                database.settingsDao().saveSettings(SettingsEntity(isDarkMode = false))
            }
        }
    }

    private fun scheduleMidnightCleanup() {
        val cleanupWork = PeriodicWorkRequestBuilder<MidnightCleanupWorker>(
            24, TimeUnit.HOURS
        ).setInitialDelay(
            calculateInitialDelayToMidnight(),
            TimeUnit.MILLISECONDS
        ).setConstraints(
            Constraints.Builder()
                .setRequiresCharging(false)
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "midnight_cleanup",
            ExistingPeriodicWorkPolicy.REPLACE,
            cleanupWork
        )
    }

    private fun calculateInitialDelayToMidnight(): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_MONTH, 1)
        }
        return calendar.timeInMillis - System.currentTimeMillis()
    }
}

class MidnightCleanupWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(applicationContext)
        database.checkedOrdersDao().clearAllCheckedOrders()
        database.orderStatesDao().clearAllStates()
        return Result.success()
    }
}