package com.example.weatherforecast.network

import com.example.weatherforecast.model.CurrentWeatherResponse
import com.example.weatherforecast.model.WeatherResponse
interface WeatherRemoteDataSource {
    suspend fun fetchCurrentWeather(cityName: String, apiKey: String, units: String = "metric"): CurrentWeatherResponse
    suspend fun fetchCurrentWeatherByCoordinates(lat: Double, lon: Double, apiKey: String, units: String = "metric"): CurrentWeatherResponse
    suspend fun fetchForecast(cityId: Int, apiKey: String, units: String = "metric"): WeatherResponse
    suspend fun fetchForecastByCoordinates(lat: Double, lon: Double, apiKey: String, units: String = "metric"): WeatherResponse
}