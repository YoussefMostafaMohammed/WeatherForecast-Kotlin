package com.example.weatherforecast.model

data class CityWithWeather(
    val city: CityEntity,
    val weather: CurrentWeatherEntity?
)