package com.example.weatherforecast.locale

import com.example.weatherforecast.CityEntity
import com.example.weatherforecast.CurrentWeatherEntity
import com.example.weatherforecast.ForecastEntity
import com.example.weatherforecast.WeatherDatabase
import com.example.weatherforecast.locale.WeatherLocalDataSource

class WeatherLocalDataSourceImpl(
    db: WeatherDatabase
) : WeatherLocalDataSource {

    private val cityDao = db.cityDao()
    private val currentDao = db.currentWeatherDao()
    private val forecastDao = db.forecastDao()

    override suspend fun saveCity(city: CityEntity) = cityDao.insert(city)
    override suspend fun getCityById(id: Int) = cityDao.getCityById(id)
    override suspend fun getCityByName(name: String) = cityDao.getCityByName(name)

    override suspend fun upsertCurrent(current: CurrentWeatherEntity) = currentDao.upsert(current)
    override suspend fun getCurrentForCity(id: Int) = currentDao.getCurrentForCity(id)

    override suspend fun saveForecasts(forecasts: List<ForecastEntity>) {
        if (forecasts.isNotEmpty()) {
            forecastDao.deleteForecastsForCity(forecasts.first().cityId)
            forecastDao.insert(forecasts)
        }
    }

    override suspend fun getForecastsForCity(cityId: Int) =
        forecastDao.getForecastsForCity(cityId)

    override suspend fun getFavoriteCities() = cityDao.getFavoriteCities()
}