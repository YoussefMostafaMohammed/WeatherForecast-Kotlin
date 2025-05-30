package com.example.weatherforecast.utils

import com.example.weatherforecast.model.CurrentWeatherEntity
import java.text.SimpleDateFormat
import java.util.*

fun Long.toHourMinuteString(): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    // multiply by 1000 because Java Date wants milliseconds
    return sdf.format(Date(this * 1000L))
}

fun Long.toDateTimeString(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return sdf.format(Date(this * 1000L))
}

fun CurrentWeatherEntity.getFormattedSunrise(): String =
    this.sunrise.toHourMinuteString()

fun CurrentWeatherEntity.getFormattedSunset(): String =
    this.sunset.toHourMinuteString()
