package com.example.weatherforecast

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.weatherforecast.locale.AlarmLocalDataSourceImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import android.util.Log

class AlarmService : Service() {

    private lateinit var repository: AlarmRepositoryImpl
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var isMonitoring = AtomicBoolean(false)

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "alarm_service_channel"
        const val ACTION_SNOOZE_ALARM = "com.example.weatherforecast.SNOOZE_ALARM"
        const val ACTION_DELETE_ALARM = "com.example.weatherforecast.DELETE_ALARM"
        const val ACTION_START_SERVICE = "com.example.weatherforecast.START_ALARM_SERVICE"

        fun startService(context: Context) {
            val intent = Intent(context, AlarmService::class.java).apply {
                action = ACTION_START_SERVICE
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        val database = WeatherDatabase.getDatabase(applicationContext)
        val localDataSource = AlarmLocalDataSourceImpl(database.alarmDao())
        repository = AlarmRepositoryImpl.getInstance(localDataSource)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundServiceWithNotification()
        when (intent?.action) {
            ACTION_SNOOZE_ALARM -> {
                val alarmId = intent.getIntExtra("alarm_id", 0)
                snoozeAlarm(alarmId)
            }
            ACTION_DELETE_ALARM -> {
                val alarmId = intent.getIntExtra("alarm_id", 0)
                deleteAlarm(alarmId)
            }
            ACTION_START_SERVICE -> {
                if (!isMonitoring.get()) {
                    startAlarmMonitoring()
                }
            }
            else -> {
                if (!isMonitoring.get()) startAlarmMonitoring()
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        isMonitoring.set(false)
        serviceScope.coroutineContext[Job]?.cancel()
    }

    private fun startForegroundServiceWithNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Alarm Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Channel for alarm service notifications"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        val notificationIntent = Intent(this, Navigation::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Weather Alarm Service")
            .setContentText("Managing your weather alerts")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun startAlarmMonitoring() {
        if (isMonitoring.getAndSet(true)) return

        serviceScope.launch {
            while (isActive && isMonitoring.get()) {
                try {
                    val alarms = repository.getAllAlarms().value ?: emptyList()
                    if (alarms.isEmpty()) {
                        stopSelf()
                        isMonitoring.set(false)
                        Log.d("AlarmService", "No alarms found, stopping service")
                        break
                    }
                    val currentTime = System.currentTimeMillis()
                    alarms.forEach { alarm ->
                        if (alarm.triggerTime < currentTime) {
                            cancelAlarm(alarm.id)
                            repository.deleteAlarm(alarm.id)
                            Log.d("AlarmService", "Deleted expired alarm id=${alarm.id}")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("AlarmService", "Error monitoring alarms", e)
                }
                delay(15 * 60 * 1000L)
            }
        }
    }

    private fun snoozeAlarm(alarmId: Int) {
        serviceScope.launch {
            try {
                repository.getAllAlarms().value?.find { it.id == alarmId }?.let { alarm ->
                    val newTriggerTime = System.currentTimeMillis() + 5 * 60_000L
                    repository.updateAlarmTriggerTime(alarmId, newTriggerTime)
                    scheduleAlarm(alarmId, newTriggerTime, alarm.type, alarm.cityId)
                    repository.insertAlarm(alarm.copy(triggerTime = newTriggerTime))
                    NotificationManagerCompat.from(this@AlarmService).cancel(alarmId)
                    Log.d("AlarmService","Snoozed alarm id=$alarmId, newTriggerTime=$newTriggerTime, cityId=${alarm.cityId}")
                }
            } catch (e: Exception) {
                Log.e("AlarmService", "Error snoozing alarm", e)
            }
        }
    }

    private fun deleteAlarm(alarmId: Int) {
        serviceScope.launch {
            try {
                cancelAlarm(alarmId)
                repository.deleteAlarm(alarmId)
                NotificationManagerCompat.from(this@AlarmService).cancel(alarmId)
                Log.d("AlarmService", "Deleted alarm id=$alarmId")
            } catch (e: Exception) {
                Log.e("AlarmService", "Error deleting alarm", e)
            }
        }
    }

    private fun scheduleAlarm(alarmId: Int, triggerTime: Long, type: String, cityId: Int) {
        try {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(this, AlarmReceiver::class.java).apply {
                putExtra("alarm_id", alarmId)
                putExtra("type", type)
                putExtra("city_id", cityId)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                this,
                alarmId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                    Log.d("AlarmService", "Scheduled inexact alarm id=$alarmId, triggerTime=$triggerTime, cityId=$cityId")
                } else {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                    Log.d("AlarmService", "Scheduled exact alarm id=$alarmId, triggerTime=$triggerTime, cityId=$cityId")
                }
            } catch (e: SecurityException) {
                Log.e("AlarmService", "Exact alarm permission denied, falling back to setExact", e)
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        } catch (e: Exception) {
            Log.e("AlarmService", "Error scheduling alarm", e)
        }
    }

    private fun cancelAlarm(alarmId: Int) {
        try {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(this, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                this,
                alarmId,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            pendingIntent?.let {
                alarmManager.cancel(it)
                it.cancel()
                Log.d("AlarmService", "Cancelled alarm id=$alarmId")
            }
        } catch (e: Exception) {
            Log.e("AlarmService", "Error canceling alarm", e)
        }
    }
}