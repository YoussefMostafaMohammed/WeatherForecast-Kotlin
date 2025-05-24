package com.example.weatherforecast

import com.example.weatherforecast.ApiService

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
        throw Exception("Error fetching current: ${'$'}{response.code()} ${'$'}{response.message()}")
    }

    override suspend fun fetchForecast(
        cityId: Int,
        apiKey: String,
        units: String
    ): WeatherResponse {
        val response = api.getWeather(cityId, apiKey, units)
        if (response.isSuccessful) return response.body()!!
        throw Exception("Error fetching forecast: ${'$'}{response.code()} ${'$'}{response.message()}")
    }
}
