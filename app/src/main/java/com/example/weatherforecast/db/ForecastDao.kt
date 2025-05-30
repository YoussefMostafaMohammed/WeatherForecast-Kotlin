package com.example.weatherforecast.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.weatherforecast.model.ForecastEntity

@Dao
interface ForecastDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(forecasts: List<ForecastEntity>)

    @Query("SELECT * FROM forecast WHERE cityId = :cityId")
    suspend fun getForecastsForCity(cityId: Int): List<ForecastEntity>

    @Query("DELETE FROM forecast WHERE cityId = :cityId")
    suspend fun deleteForecastsForCity(cityId: Int)

    @Query("DELETE FROM forecast")
    suspend fun deleteAllForecasts()

    @Query("SELECT * FROM forecast")
    suspend fun getAllForecasts(): List<ForecastEntity>
}