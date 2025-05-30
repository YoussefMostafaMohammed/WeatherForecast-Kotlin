package com.example.weatherforecast.network

import com.example.weatherforecast.model.CurrentWeatherResponse
import com.example.weatherforecast.model.WeatherResponse

class WeatherRemoteDataSourceImpl(
    private val api: ApiService
) : WeatherRemoteDataSource {

    override suspend fun fetchCurrentWeather(
        cityName: String,
        apiKey: String,
        units: String
    ): CurrentWeatherResponse {
        val response = api.getCurrentWeather(cityName, apiKey, units)
        if (response.isSuccessful) return response.body()!!
        throw Exception("Error fetching current: ${response.code()} ${response.message()}")
    }

    override suspend fun fetchCurrentWeatherByCoordinates(
        lat: Double,
        lon: Double,
        apiKey: String,
        units: String
    ): CurrentWeatherResponse {
        val response = api.getCurrentWeatherByCoordinates(lat, lon, apiKey, units)
        if (response.isSuccessful) return response.body()!!
        throw Exception("Error fetching current by coordinates: ${response.code()} ${response.message()}")
    }

    override suspend fun fetchForecast(
        cityId: Int,
        apiKey: String,
        units: String
    ): WeatherResponse {
        val response = api.getWeatherById(cityId, apiKey, units)
        if (response.isSuccessful) return response.body()!!
        throw Exception("Error fetching forecast: ${response.code()} ${response.message()}")
    }

    override suspend fun fetchForecastByCoordinates(
        lat: Double,
        lon: Double,
        apiKey: String,
        units: String
    ): WeatherResponse {
        val response = api.getWeatherByCoordinates(lat, lon, apiKey, units)
        if (response.isSuccessful) return response.body()!!
        throw Exception("Error fetching forecast by coordinates: ${response.code()} ${response.message()}")
    }
}