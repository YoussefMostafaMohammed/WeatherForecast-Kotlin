package com.example.weatherforecast

interface WeatherRepository {
    // Fetch operations
    suspend fun fetchCurrentWeather(cityName: String): Pair<CityEntity, CurrentWeatherEntity>
    suspend fun fetchCurrentWeatherByCoordinates(lat: Double, lon: Double): Pair<CityEntity, CurrentWeatherEntity>
    suspend fun fetchForecast(cityId: Int): Pair<CityEntity, List<ForecastEntity>>
    suspend fun fetchForecastByCoordinates(lat: Double, lon: Double): Pair<CityEntity, List<ForecastEntity>>

    // Save operations
    suspend fun saveCurrentWeather(cityEntity: CityEntity, currentEntity: CurrentWeatherEntity)
    suspend fun saveForecast(cityEntity: CityEntity, forecasts: List<ForecastEntity>)

    // Other operations
    suspend fun getFavoriteCities(): List<CityEntity>
    suspend fun getCurrentWeatherForCity(cityId: Int): CurrentWeatherEntity?
    suspend fun updateCityFavoriteStatus(city: CityEntity)
    suspend fun getCityById(cityId: Int): CityEntity?
    suspend fun getCityByName(name: String): CityEntity?
    suspend fun getCachedCurrentWeather(cityName: String): CurrentWeatherEntity?
    suspend fun getCachedForecast(cityId: Int): List<ForecastEntity>
}