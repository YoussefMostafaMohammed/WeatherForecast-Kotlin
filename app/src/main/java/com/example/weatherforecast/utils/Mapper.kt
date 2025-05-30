package com.example.weatherforecast.utils

import com.example.weatherforecast.model.City
import com.example.weatherforecast.model.CurrentWeatherResponse
import com.example.weatherforecast.model.Forecast
import com.example.weatherforecast.model.CityEntity
import com.example.weatherforecast.model.CurrentWeatherEntity
import com.example.weatherforecast.model.ForecastEntity

fun mapToCityEntity(city: City): CityEntity {
    return CityEntity(
        id = city.id,
        name = city.name,
        coordLat = city.coord.lat,
        coordLon = city.coord.lon,
        country = city.country,
        population = city.population,
        timezone = city.timezone,
        sunrise = city.sunrise,
        sunset = city.sunset,
        isFavorite = false,
        isSearched = true
    )
}

fun mapToForecastEntities(list: List<Forecast>, cityId: Int): List<ForecastEntity> {
    return list.map { forecast ->
        ForecastEntity(
            cityId = cityId,
            dt = forecast.dt,
            dtTxt = forecast.dtTxt,
            temp = forecast.main.temp,
            feelsLike = forecast.main.feelsLike,
            tempMin = forecast.main.tempMin,
            tempMax = forecast.main.tempMax,
            pressure = forecast.main.pressure,
            humidity = forecast.main.humidity,
            weatherId = forecast.weather.firstOrNull()?.id ?: 0,
            weatherMain = forecast.weather.firstOrNull()?.main ?: "",
            weatherDescription = forecast.weather.firstOrNull()?.description ?: "",
            weatherIcon = forecast.weather.firstOrNull()?.icon ?: "",
            cloudsAll = forecast.clouds.all,
            windSpeed = forecast.wind.speed,
            windDeg = forecast.wind.deg,
            windGust = forecast.wind.gust,
            visibility = forecast.visibility,
            pop = forecast.pop,
            rain3h = forecast.rain?.h3,
            sysPod = forecast.sys.pod
        )
    }
}

fun mapToCurrentEntity(resp: CurrentWeatherResponse): CurrentWeatherEntity {
    val w = resp.weather.firstOrNull()
    return CurrentWeatherEntity(
        cityId = resp.id,
        timestamp = resp.dt,
        temp = resp.main.temp,
        feelsLike = resp.main.feelsLike,
        pressure = resp.main.pressure,
        humidity = resp.main.humidity,
        weatherMain = w?.main ?: "",
        weatherDescription = w?.description ?: "",
        weatherIcon = w?.icon ?: "",
        windSpeed = resp.wind.speed,
        windDeg = resp.wind.deg,
        cloudiness = resp.clouds.all,
        visibility = resp.visibility,
        tempMin = resp.main.tempMin,
        tempMax = resp.main.tempMax,
        sunrise = resp.sys.sunrise,
        sunset = resp.sys.sunset,
    )
}
fun CurrentWeatherResponse.toCity() = City(
    id = this.id,
    name = this.name,
    coord = this.coord,
    country = this.sys.country,
    population = 0,
    timezone = this.timezone,
    sunrise = this.sys.sunrise,
    sunset = this.sys.sunset
)
