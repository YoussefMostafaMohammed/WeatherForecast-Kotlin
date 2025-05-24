package com.example.weatherforecast
import com.example.weatherforecast.mapToCityEntity
import com.example.weatherforecast.mapToCurrentEntity
import com.example.weatherforecast.mapToForecastEntities
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

    override suspend fun getCurrentWeather(cityName: String) = withContext(Dispatchers.IO) {
        try {
            val resp = remote.fetchCurrentWeather(cityName, apiKey)
            val cityEntity = mapToCityEntity(resp.toCity())
            val currentEntity = mapToCurrentEntity(resp)
            local.saveCity(cityEntity)
            local.upsertCurrent(currentEntity)
            local.getCurrentForCity(currentEntity.cityId)!!
        } catch (e: Exception) {
            val cached = local.getCityByName(cityName)
                ?: throw e
            local.getCurrentForCity(cached.id)
                ?: throw Exception("No cached data")
        }
    }

    override suspend fun getForecast(cityId: Int) = withContext(Dispatchers.IO) {
        try {
            val resp = remote.fetchForecast(cityId, apiKey)
            val cityEntity = mapToCityEntity(resp.city)
            val list = mapToForecastEntities(resp.list, cityId)
            local.saveCity(cityEntity)
            local.saveForecasts(list)
            local.getForecastsForCity(cityId)
        } catch (e: Exception) {
            local.getForecastsForCity(cityId)
        }
    }
}
