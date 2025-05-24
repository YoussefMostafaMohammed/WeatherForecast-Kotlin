package com.example.weatherforecast


import com.example.weatherforecast.CurrentWeatherEntity
import com.example.weatherforecast.ForecastEntity

interface WeatherRepository {
    suspend fun getCurrentWeather(cityName: String): CurrentWeatherEntity
    suspend fun getForecast(cityId: Int): List<ForecastEntity>
}
