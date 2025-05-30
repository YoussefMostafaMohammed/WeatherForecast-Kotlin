package com.example.weatherforecast.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.weatherforecast.model.AlarmEntity

@Dao
interface AlarmDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(alarm: AlarmEntity): Long

    @Delete
    suspend fun delete(alarm: AlarmEntity)

    @Query("DELETE FROM alarms WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("UPDATE alarms SET triggerTime = :triggerTime WHERE id = :id")
    suspend fun updateTriggerTime(id: Int, triggerTime: Long)

    @Query("SELECT * FROM alarms ORDER BY triggerTime ASC")
    fun getAllAlarms(): LiveData<List<AlarmEntity>>
}