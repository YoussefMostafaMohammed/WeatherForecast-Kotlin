package com.example.weatherforecast

import com.example.weatherforecast.CurrentWeatherResponse
import com.example.weatherforecast.WeatherResponse

interface WeatherRemoteDataSource {
    suspend fun fetchCurrentWeather(cityName: String, apiKey: String, units: String = "metric"): CurrentWeatherResponse
    suspend fun fetchForecast(cityId: Int, apiKey: String, units: String = "metric"): WeatherResponse
}
