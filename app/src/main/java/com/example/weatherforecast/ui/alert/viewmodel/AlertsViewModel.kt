package com.example.weatherforecast.ui.alert.viewmodel

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.weatherforecast.AlarmReceiver
import com.example.weatherforecast.model.AlarmEntity
import com.example.weatherforecast.model.AlarmRepositoryImpl
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class AlertsViewModel(
    application: Application,
    private val repository: AlarmRepositoryImpl
) : AndroidViewModel(application) {

    val alarms: LiveData<List<AlarmEntity>> = repository.getAllAlarms()

    fun addAlarm(triggerTime: Long, type: String, cityId: Int) {
        viewModelScope.launch {
            try {
                if (cityId == -1) {
                    Log.e("AlertsViewModel", "Invalid cityId=$cityId for alarm")
                    return@launch
                }
                val alarmId = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
                val alarm = AlarmEntity(
                    id = alarmId,
                    triggerTime = triggerTime,
                    type = type,
                    cityId = cityId
                )
                repository.insertAlarm(alarm)
                Log.d("AlertsViewModel", "Inserted alarm id=$alarmId, triggerTime=$triggerTime, type=$type, cityId=$cityId")
                scheduleAlarm(alarmId, triggerTime, type, cityId)
            } catch (e: Exception) {
                Log.e("AlertsViewModel", "Error inserting alarm", e)
            }
        }
    }

    fun deleteAlarm(id: Int) {
        viewModelScope.launch {
            try {
                repository.deleteAlarm(id)
                cancelAlarm(id)
                Log.d("AlertsViewModel", "Deleted alarm id=$id")
            } catch (e: Exception) {
                Log.e("AlertsViewModel", "Error deleting alarm", e)
            }
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleAlarm(alarmId: Int, triggerTime: Long, type: String, cityId: Int) {
        val context = getApplication<Application>().applicationContext
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("alarm_id", alarmId)
            putExtra("type", type)
            putExtra("city_id", cityId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                Log.d("AlertsViewModel", "Scheduled inexact alarm id=$alarmId, cityId=$cityId at ${formatTime(triggerTime)}")
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                Log.d("AlertsViewModel", "Scheduled exact alarm id=$alarmId, cityId=$cityId at ${formatTime(triggerTime)}")
            }
        } catch (e: SecurityException) {
            Log.e("AlertsViewModel", "Exact alarm permission denied, falling back to setExact", e)
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }

    private fun cancelAlarm(alarmId: Int) {
        val context = getApplication<Application>().applicationContext
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId,
            intent,
            PendingIntent.FLAG_NO_CREATE or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )
        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
            Log.d("AlertsViewModel", "Cancelled alarm id=$alarmId")
        }
    }

    private fun formatTime(timeInMillis: Long): String {
        val dateFormat = SimpleDateFormat("HH:mm 'on' dd/MM/yyyy", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("Europe/Kiev")
        }
        return dateFormat.format(Date(timeInMillis))
    }
}