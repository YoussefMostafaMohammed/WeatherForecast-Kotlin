package com.example.weatherforecast.locale

import androidx.lifecycle.LiveData
import com.example.weatherforecast.AlarmEntity

interface AlarmLocalDataSource   {
    suspend fun insertAlarm(alarm: AlarmEntity): Long
    suspend fun deleteAlarm(id: Int)
    suspend fun updateAlarmTriggerTime(id: Int, triggerTime: Long)
    fun getAllAlarms(): LiveData<List<AlarmEntity>>
}