// AlarmReceiver.kt
package com.example.weatherforecast

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import android.media.RingtoneManager

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra("alarm_id", 0)
        val cityId  = intent.getIntExtra("city_id", -1)
        val type    = intent.getStringExtra("type") ?: "notification"
        Log.d("AlarmReceiver", "Received alarm: id=$alarmId, type=$type, cityId=$cityId")

        val notificationManager = NotificationManagerCompat.from(context)
        val builder = NotificationCompat.Builder(context, "weather_alerts")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Weather Alert")
            .setContentText("Tap to view your weather details.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        if (type == "alarm") {
            // 1) Build the full-screen Intent with both alarm_id & city_id
            val fullIntent = Intent(context, AlarmActivity::class.java).apply {
                putExtra("alarm_id", alarmId)
                putExtra("city_id", cityId)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            val fullPending = PendingIntent.getActivity(
                context, alarmId, fullIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.setFullScreenIntent(fullPending, true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))

            // 2) Snooze & Dismiss actions also carry cityId
            val snoozeIntent = Intent(context, SnoozeReceiver::class.java).apply {
                putExtra("alarm_id", alarmId)
                putExtra("city_id", cityId)
            }
            val dismissIntent = Intent(context, DismissReceiver::class.java).apply {
                putExtra("alarm_id", alarmId)
                putExtra("city_id", cityId)
            }
            builder.addAction(
                0, "Snooze",
                PendingIntent.getBroadcast(context, alarmId, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            ).addAction(
                0, "Dismiss",
                PendingIntent.getBroadcast(context, alarmId, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            )

            // 3) Fire it off
            try {
                context.startActivity(fullIntent)
                Log.d("AlarmReceiver", "Launched AlarmActivity for alarmId=$alarmId")
            } catch (e: Exception) {
                Log.e("AlarmReceiver", "Error launching AlarmActivity", e)
            }
        } else {
            // fallback tap action
            val tapIntent = Intent(context, Navigation::class.java)
            val tapPending = PendingIntent.getActivity(
                context, 0, tapIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.setContentIntent(tapPending)
        }

        // finally post
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                notificationManager.notify(alarmId, builder.build())
            } else {
                Log.w("AlarmReceiver", "Missing POST_NOTIFICATIONS permission")
            }
        } else {
            notificationManager.notify(alarmId, builder.build())
        }
    }
}
