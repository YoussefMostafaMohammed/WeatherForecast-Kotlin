package com.example.weatherforecast

import com.example.weatherforecast.remote.WeatherRemoteDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WeatherRepositoryImpl private constructor(
    private val remote: WeatherRemoteDataSource,
    private val local: WeatherLocalDataSource,
    private val apiKey: String
) : WeatherRepository {

    companion object {
        @Volatile
        private var instance: WeatherRepositoryImpl? = null

        fun getInstance(
            remote: WeatherRemoteDataSource,
            local: WeatherLocalDataSource,
            apiKey: String
        ): WeatherRepositoryImpl =
            instance ?: synchronized(this) {
                instance ?: WeatherRepositoryImpl(remote, local, apiKey).also { instance = it }
            }
    }

    override suspend fun fetchCurrentWeather(cityName: String): Pair<CityEntity, CurrentWeatherEntity> =
        withContext(Dispatchers.IO) {
            try {
                val response = remote.fetchCurrentWeather(cityName, apiKey)
                val cityEntity = mapToCityEntity(response.toCity())
                val currentEntity = mapToCurrentEntity(response)
                cityEntity to currentEntity
            } catch (e: Exception) {
                throw Exception("Error fetching current weather for $cityName: ${e.message}", e)
            }
        }

    override suspend fun fetchCurrentWeatherByCoordinates(lat: Double, lon: Double): Pair<CityEntity, CurrentWeatherEntity> =
        withContext(Dispatchers.IO) {
            try {
                val response = remote.fetchCurrentWeatherByCoordinates(lat, lon, apiKey)
                val cityEntity = mapToCityEntity(response.toCity())
                val currentEntity = mapToCurrentEntity(response)
                cityEntity to currentEntity
            } catch (e: Exception) {
                throw Exception("Error fetching current weather by coordinates: ${e.message}", e)
            }
        }

    override suspend fun fetchForecast(cityId: Int): Pair<CityEntity, List<ForecastEntity>> =
        withContext(Dispatchers.IO) {
            try {
                val response = remote.fetchForecast(cityId, apiKey)
                val cityEntity = mapToCityEntity(response.city)
                val forecastEntities = mapToForecastEntities(response.list, cityId)
                cityEntity to forecastEntities
            } catch (e: Exception) {
                throw Exception("Error fetching forecast for city ID $cityId: ${e.message}", e)
            }
        }

    override suspend fun fetchForecastByCoordinates(lat: Double, lon: Double): Pair<CityEntity, List<ForecastEntity>> =
        withContext(Dispatchers.IO) {
            try {
                val response = remote.fetchForecastByCoordinates(lat, lon, apiKey)
                val cityEntity = mapToCityEntity(response.city)
                val forecastEntities = mapToForecastEntities(response.list, cityEntity.id)
                cityEntity to forecastEntities
            } catch (e: Exception) {
                throw Exception("Error fetching forecast by coordinates: ${e.message}", e)
            }
        }

    override suspend fun saveCurrentWeather(cityEntity: CityEntity, currentEntity: CurrentWeatherEntity) =
        withContext(Dispatchers.IO) {
            val existingCity = local.getCityById(cityEntity.id)
            val updatedCity = cityEntity.copy(isFavorite = existingCity?.isFavorite ?: false)
            local.saveCity(updatedCity)
            local.upsertCurrent(currentEntity)
        }

    override suspend fun saveForecast(cityEntity: CityEntity, forecasts: List<ForecastEntity>) =
        withContext(Dispatchers.IO) {
            val existingCity = local.getCityById(cityEntity.id)
            val updatedCity = cityEntity.copy(isFavorite = existingCity?.isFavorite ?: false)
            local.saveCity(updatedCity)
            local.saveForecasts(forecasts)
        }

    override suspend fun getFavoriteCities(): List<CityEntity> =
        withContext(Dispatchers.IO) {
            local.getFavoriteCities()
        }

    override suspend fun getCurrentWeatherForCity(cityId: Int): CurrentWeatherEntity? =
        withContext(Dispatchers.IO) {
            local.getCurrentForCity(cityId)
        }

    override suspend fun updateCityFavoriteStatus(city: CityEntity) =
        withContext(Dispatchers.IO) {
            local.saveCity(city)
        }

    override suspend fun getCityById(cityId: Int): CityEntity? =
        withContext(Dispatchers.IO) {
            local.getCityById(cityId)
        }

    override suspend fun getCityByName(name: String): CityEntity? =
        withContext(Dispatchers.IO) {
            local.getCityByName(name)
        }

    override suspend fun getCachedCurrentWeather(cityName: String): CurrentWeatherEntity? =
        withContext(Dispatchers.IO) {
            val cachedCity = local.getCityByName(cityName) ?: return@withContext null
            local.getCurrentForCity(cachedCity.id)
        }

    override suspend fun getCachedForecast(cityId: Int): List<ForecastEntity> =
        withContext(Dispatchers.IO) {
            local.getForecastsForCity(cityId)
        }
}