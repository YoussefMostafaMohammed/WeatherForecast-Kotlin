package com.example.weatherforecast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class DismissReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra("alarm_id", 0)
        val cityId = intent.getIntExtra("city_id", -1)
        Log.d("DismissReceiver", "Received dismiss: alarmId=$alarmId, cityId=$cityId")

        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            action = AlarmService.ACTION_DELETE_ALARM
            putExtra("alarm_id", alarmId)
            putExtra("city_id", cityId)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}