package com.example.weatherforecast

import com.example.weatherforecast.CurrentWeatherEntity
import com.example.weatherforecast.ForecastEntity
import com.example.weatherforecast.CityEntity

interface WeatherLocalDataSource {
    // City
    suspend fun saveCity(city: CityEntity)
    suspend fun getCityById(id: Int): CityEntity?
    suspend fun getCityByName(name: String): CityEntity?

    // Current
    suspend fun upsertCurrent(current: CurrentWeatherEntity)
    suspend fun getCurrentForCity(id: Int): CurrentWeatherEntity?

    // Forecast
    suspend fun saveForecasts(forecasts: List<ForecastEntity>)
    suspend fun getForecastsForCity(cityId: Int): List<ForecastEntity>
}


