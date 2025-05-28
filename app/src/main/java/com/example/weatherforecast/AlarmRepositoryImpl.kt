package com.example.weatherforecast

import androidx.lifecycle.LiveData
import com.example.weatherforecast.locale.AlarmLocalDataSource

class AlarmRepositoryImpl private constructor(
    private val alarmLocalDataSource: AlarmLocalDataSource
) : AlarmRepository {

    companion object {
        @Volatile
        private var instance: AlarmRepositoryImpl? = null

        fun getInstance(alarmLocalDataSource: AlarmLocalDataSource): AlarmRepositoryImpl {
            return instance ?: synchronized(this) {
                instance ?: AlarmRepositoryImpl(alarmLocalDataSource).also { instance = it }
            }
        }
    }

    override suspend fun insertAlarm(alarm: AlarmEntity): Long {
        return alarmLocalDataSource.insertAlarm(alarm)
    }

    override suspend fun deleteAlarm(id: Int) {
        alarmLocalDataSource.deleteAlarm(id)
    }

    override suspend fun updateAlarmTriggerTime(id: Int, triggerTime: Long) {
        alarmLocalDataSource.updateAlarmTriggerTime(id, triggerTime)
    }

    override fun getAllAlarms(): LiveData<List<AlarmEntity>> {
        return alarmLocalDataSource.getAllAlarms()
    }
}