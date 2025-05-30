package com.example.weatherforecast.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.weatherforecast.model.CurrentWeatherEntity

@Dao
interface CurrentWeatherDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(current: CurrentWeatherEntity)

    @Query("SELECT * FROM current_weather WHERE cityId = :cityId LIMIT 1")
    suspend fun getCurrentForCity(cityId: Int): CurrentWeatherEntity?

    @Query("DELETE FROM current_weather WHERE cityId = :cityId")
    suspend fun deleteCurrentForCity(cityId: Int)

    @Query("DELETE FROM current_weather")
    suspend fun deleteAllCurrents()
}