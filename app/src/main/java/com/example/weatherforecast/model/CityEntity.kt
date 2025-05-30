package com.example.weatherforecast.model

import androidx.room.Entity
import androidx.room.PrimaryKey

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
    val sunset: Long,
    val isFavorite: Boolean = false,
    val isSearched: Boolean = false
)