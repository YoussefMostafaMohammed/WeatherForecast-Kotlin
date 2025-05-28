package com.example.weatherforecast
import androidx.lifecycle.LiveData

interface   AlarmRepository {
    suspend fun insertAlarm(alarm: AlarmEntity): Long
    suspend fun deleteAlarm(id: Int)
    suspend fun updateAlarmTriggerTime(id: Int, triggerTime: Long)
    fun getAllAlarms(): LiveData<List<AlarmEntity>>
}