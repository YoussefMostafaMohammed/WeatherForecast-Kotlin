package com.example.weatherforecast.locale

import androidx.lifecycle.LiveData
import com.example.weatherforecast.AlarmDao
import com.example.weatherforecast.AlarmEntity

class AlarmLocalDataSourceImpl(private val alarmDao: AlarmDao) : AlarmLocalDataSource {

    override suspend fun insertAlarm(alarm: AlarmEntity): Long {
        return alarmDao.insert(alarm)
    }

    override suspend fun deleteAlarm(id: Int) {
        alarmDao.deleteById(id)
    }

    override suspend fun updateAlarmTriggerTime(id: Int, triggerTime: Long) {
        alarmDao.updateTriggerTime(id, triggerTime)
    }

    override fun getAllAlarms(): LiveData<List<AlarmEntity>> {
        return alarmDao.getAllAlarms()
    }
}