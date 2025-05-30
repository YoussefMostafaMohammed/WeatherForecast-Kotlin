package com.example.weatherforecast.db

import com.example.weatherforecast.model.CityEntity
import com.example.weatherforecast.model.CurrentWeatherEntity
import com.example.weatherforecast.model.ForecastEntity

interface WeatherLocalDataSource {
    suspend fun saveCity(city: CityEntity)
    suspend fun getCityById(id: Int): CityEntity?
    suspend fun getCityByName(name: String): CityEntity?
    suspend fun upsertCurrent(current: CurrentWeatherEntity)
    suspend fun getCurrentForCity(id: Int): CurrentWeatherEntity?
    suspend fun saveForecasts(forecasts: List<ForecastEntity>)
    suspend fun getForecastsForCity(cityId: Int): List<ForecastEntity>
    suspend fun getFavoriteCities(): List<CityEntity>
}