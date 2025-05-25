package com.example.weatherforecast.remote

import com.example.weatherforecast.CurrentWeatherResponse
import com.example.weatherforecast.WeatherResponse
interface WeatherRemoteDataSource {
    suspend fun fetchCurrentWeather(cityName: String, apiKey: String, units: String = "metric"): CurrentWeatherResponse
    suspend fun fetchCurrentWeatherByCoordinates(lat: Double, lon: Double, apiKey: String, units: String = "metric"): CurrentWeatherResponse
    suspend fun fetchForecast(cityId: Int, apiKey: String, units: String = "metric"): WeatherResponse
    suspend fun fetchForecastByCoordinates(lat: Double, lon: Double, apiKey: String, units: String = "metric"): WeatherResponse
}