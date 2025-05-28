package com.example.weatherforecast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.weatherforecast.locale.AlarmLocalDataSourceImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SnoozeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra("alarm_id", 0)
        var cityId = intent.getIntExtra("city_id", -1)
        Log.d("SnoozeReceiver", "Received snooze: alarmId=$alarmId, cityId=$cityId")

        // Initialize repository
        val db = WeatherDatabase.getDatabase(context)
        val alarmRepo = AlarmRepositoryImpl.getInstance(AlarmLocalDataSourceImpl(db.alarmDao()))

        // Fetch AlarmEntity
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val alarm = alarmRepo.getAllAlarms().value?.find { it.id == alarmId }
                if (alarm != null) {
                    cityId = alarm.cityId
                    val type = alarm.type
                    Log.d("SnoozeReceiver", "Fetched AlarmEntity: id=$alarmId, type=$type, cityId=$cityId")

                    // Trigger AlarmService to snooze (reschedule 5 minutes later)
                    val serviceIntent = Intent(context, AlarmService::class.java).apply {
                        action = AlarmService.ACTION_SNOOZE_ALARM
                        putExtra("alarm_id", alarmId)
                        putExtra("city_id", cityId)
                        putExtra("type", type)
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }
                    Log.d("SnoozeReceiver", "Started AlarmService to snooze alarmId=$alarmId, cityId=$cityId, type=$type")
                } else {
                    Log.w("SnoozeReceiver", "No AlarmEntity found for alarmId=$alarmId")
                }
            } catch (e: Exception) {
                Log.e("SnoozeReceiver", "Error fetching AlarmEntity for alarmId=$alarmId", e)
            }
        }
    }
}