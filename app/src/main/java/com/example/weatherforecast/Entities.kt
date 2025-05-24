package com.example.weatherforecast

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "current_weather")
data class CurrentWeatherEntity(
    @PrimaryKey val cityId: Int,
    val timestamp: Long,
    val temp: Double,
    val feelsLike: Double,
    val tempMin: Double,
    val tempMax: Double,
    val pressure: Int,
    val humidity: Int,
    val weatherMain: String,
    val weatherDescription: String,
    val weatherIcon: String,
    val windSpeed: Double,
    val windDeg: Int,
    val cloudiness: Int,
    val visibility: Int,
    val sunrise: Long,
    val sunset: Long
)

@Entity(tableName = "city")
data class CityEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val coordLat: Double,
    val coordLon: Double,
    val country: String,
    val population: Int,
    val timezone: Int,
    val sunrise: Long,
    val sunset: Long
)

@Entity(
    tableName = "forecast",
    foreignKeys = [ForeignKey(
        entity = CityEntity::class,
        parentColumns = ["id"],
        childColumns = ["cityId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class ForecastEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val cityId: Int,
    val dt: Long,
    val dtTxt: String,
    val temp: Double,
    val feelsLike: Double,
    val tempMin: Double,
    val tempMax: Double,
    val pressure: Int,
    val humidity: Int,
    val weatherId: Int,
    val weatherMain: String,
    val weatherDescription: String,
    val weatherIcon: String,
    val cloudsAll: Int,
    val windSpeed: Double,
    val windDeg: Int,
    val windGust: Double,
    val visibility: Int,
    val pop: Double,
    val rain3h: Double?,
    val sysPod: String
)