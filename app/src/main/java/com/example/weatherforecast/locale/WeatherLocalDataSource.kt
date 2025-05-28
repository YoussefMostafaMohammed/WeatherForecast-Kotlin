package com.example.weatherforecast.locale

import com.example.weatherforecast.CityEntity
import com.example.weatherforecast.CurrentWeatherEntity
import com.example.weatherforecast.ForecastEntity

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