package com.example.weatherforecast.utils

import android.content.SharedPreferences
import java.util.*

class WeatherFormatter(private val prefs: SharedPreferences) {

    fun formatTemperature(temp: Double): String {
        val unit = prefs.getString("temp_unit", "Celsius") ?: "Celsius"
        val value: Int
        val unitSymbol: String
        when (unit) {
            "Fahrenheit" -> {
                value = ((temp * 9 / 5) + 32).toInt()
                unitSymbol = "°F"
            }
            "Kelvin" -> {
                value = (temp + 273.15).toInt()
                unitSymbol = "K"
            }
            else -> {
                value = temp.toInt()
                unitSymbol = "°C"
            }
        }
        val translatedUnit = getTranslatedUnit(unitSymbol)
        return "$value$translatedUnit"
    }

    fun formatWindSpeed(speed: Double): String {
        val unit = prefs.getString("wind_speed_unit", "m/s") ?: "m/s"
        val value: Double = when (unit) {
            "km/h" -> speed * 3.6
            "mph" -> speed * 2.237
            else -> speed
        }
        val formattedValue = String.format("%.1f", value)
        val translatedUnit = getTranslatedUnit(unit)
        return "$formattedValue $translatedUnit"
    }

    fun formatPressure(pressure: Int): String {
        val unit = prefs.getString("pressure_unit", "hPa") ?: "hPa"
        val value: Double = when (unit) {
            "mb" -> pressure.toDouble()
            "in Hg" -> pressure * 0.02953
            "mm Hg" -> pressure * 0.75006
            else -> pressure.toDouble()
        }
        val translatedUnit = getTranslatedUnit(unit)
        return when (unit) {
            "mb" -> "$value $translatedUnit"
            "in Hg" -> String.format("%.2f %s", value, translatedUnit)
            "mm Hg" -> String.format("%.1f %s", value, translatedUnit)
            else -> "$value $translatedUnit"
        }
    }

    fun formatVisibility(visibility: Int): String {
        val unit = prefs.getString("visibility_unit", "Meters") ?: "Meters"
        val value: Double = when (unit) {
            "Kilometers" -> visibility / 1000.0
            "Miles" -> visibility / 1609.34
            else -> visibility.toDouble()
        }
        val translatedUnit = getTranslatedUnit(
            when (unit) {
                "Kilometers" -> "km"
                "Miles" -> "mi"
                else -> "m"
            }
        )
        return when (unit) {
            "Kilometers" -> String.format("%.1f %s", value, translatedUnit)
            "Miles" -> String.format("%.1f %s", value, translatedUnit)
            else -> "$value $translatedUnit"
        }
    }

    fun formatElevation(elevation: Int): String {
        val unit = prefs.getString("elevation_unit", "Meters") ?: "Meters"
        val value: Double = if (unit == "Feet") elevation * 3.28084 else elevation.toDouble()
        val translatedUnit = getTranslatedUnit(if (unit == "Feet") "ft" else "m")
        return "${value.toInt()} $translatedUnit"
    }

    fun translateWeatherDescription(description: String): String {
        val language = prefs.getString("locale_code", "") ?: ""
        return if (language == "ar") {
            WeatherConstants.weatherTranslations[description.lowercase()] ?: description.replaceFirstChar { it.uppercase() }
        } else {
            description.replaceFirstChar { it.uppercase() }
        }
    }

    private fun getTranslatedUnit(unit: String): String {
        val language = prefs.getString("locale_code", "") ?: ""
        if (language != "ar") return unit
        return when (unit) {
            "°C" -> "°س"
            "°F" -> "°ف"
            "K" -> "ك"
            "m/s" -> "م/ث"
            "km/h" -> "كم/س"
            "mph" -> "ميل/س"
            "hPa" -> "هـ ب"
            "mb" -> "مليبار"
            "in Hg" -> "بوصة زئبق"
            "mm Hg" -> "مم زئبق"
            "m" -> "م"
            "km" -> "كم"
            "mi" -> "ميل"
            "ft" -> "قدم"
            else -> unit
        }
    }
}