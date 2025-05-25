package com.example.weatherforecast.ui.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.weatherforecast.CityEntity
import com.example.weatherforecast.CurrentWeatherEntity
import com.example.weatherforecast.ForecastEntity
import com.example.weatherforecast.WeatherRepository
import kotlinx.coroutines.launch

open class Event<out T>(private val content: T) {
    private var hasBeenHandled = false

    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }
    fun peekContent(): T = content
}


class HomeViewModel(
    application: Application,
    private val repository: WeatherRepository
) : AndroidViewModel(application) {

    private val _forecasts = MutableLiveData<List<ForecastEntity>>()
    val forecasts: LiveData<List<ForecastEntity>> = _forecasts

    private val _city = MutableLiveData<CityEntity>()
    val city: LiveData<CityEntity> = _city

    private val _current = MutableLiveData<CurrentWeatherEntity>()
    val current: LiveData<CurrentWeatherEntity> = _current

    private val _favoriteEvent = MutableLiveData<Event<Boolean>>()
    val favoriteEvent: LiveData<Event<Boolean>> = _favoriteEvent

    companion object {
        private const val DEFAULT_CITY = "London"
        private const val DEFAULT_CITY_ID = 2643743
        private const val TAG = "HomeViewModel"
    }

    fun markCurrentCityFavorite() {
        viewModelScope.launch {
            city.value?.let { cityEntity ->
                val wasSaved = saveCityToFavorites(cityEntity)
                _favoriteEvent.value=Event(wasSaved)
            }
        }
    }

    fun fetchCityFromCoordinates(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            try {
                fetchCurrentByCoordinates(latitude, longitude)
                fetchWeatherByCoordinates(latitude, longitude)
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching data by coordinates", e)
                fetchCurrent(DEFAULT_CITY)
                fetchWeather(DEFAULT_CITY)
            }
        }
    }

    fun fetchWeather(cityName: String) {
        viewModelScope.launch {
            try {
                val city = repository.getCityByName(cityName) ?: run {
                    // Fetch current weather to get city data if not cached
                    val (cityEntity, _) = repository.fetchCurrentWeather(cityName)
                    cityEntity
                }
                val (cityEntity, forecasts) = repository.fetchForecast(city.id)
                repository.saveForecast(cityEntity, forecasts)
                _forecasts.postValue(forecasts)
                _city.postValue(cityEntity)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch forecast for $cityName", e)
                val cachedCity = repository.getCityByName(cityName)
                if (cachedCity != null) {
                    val cachedForecasts = repository.getCachedForecast(cachedCity.id)
                    if (cachedForecasts.isNotEmpty()) {
                        _forecasts.postValue(cachedForecasts)
                        _city.postValue(cachedCity)
                    }
                }
            }
        }
    }

    private fun fetchWeatherByCoordinates(lat: Double, lon: Double) {
        viewModelScope.launch {
            try {
                val (cityEntity, forecasts) = repository.fetchForecastByCoordinates(lat, lon)
                repository.saveForecast(cityEntity, forecasts)
                _forecasts.postValue(forecasts)
                _city.postValue(cityEntity)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch forecast by coordinates ($lat, $lon)", e)
            }
        }
    }

    fun fetchCurrent(cityName: String) {
        viewModelScope.launch {
            try {
                val (cityEntity, currentWeather) = repository.fetchCurrentWeather(cityName)
                repository.saveCurrentWeather(cityEntity, currentWeather)
                _current.postValue(currentWeather)
                _city.postValue(cityEntity)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch current weather for $cityName", e)
                val cachedWeather = repository.getCachedCurrentWeather(cityName)
                if (cachedWeather != null) {
                    _current.postValue(cachedWeather)
                    repository.getCityByName(cityName)?.let { cachedCity ->
                        _city.postValue(cachedCity)
                    }
                }
            }
        }
    }

    fun fetchCurrentByCoordinates(lat: Double, lon: Double) {
        viewModelScope.launch {
            try {
                val (cityEntity, currentWeather) = repository.fetchCurrentWeatherByCoordinates(lat, lon)
                repository.saveCurrentWeather(cityEntity, currentWeather)
                _current.postValue(currentWeather)
                _city.postValue(cityEntity)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch current weather by coordinates ($lat, $lon)", e)
            }
        }
    }

    private fun loadCityData(cityId: Int) {
        viewModelScope.launch {
            try {
                val city = repository.getCityById(cityId)
                if (city != null) {
                    _city.postValue(city)
                } else {
                    _city.postValue(
                        CityEntity(
                            id = cityId,
                            name = "Unknown City",
                            coordLat = 0.0,
                            coordLon = 0.0,
                            country = "",
                            population = 0,
                            timezone = 0,
                            sunrise = 0,
                            sunset = 0,
                            isFavorite = false,
                            isSearched = true
                        )
                    )
                    Log.w(TAG, "City with ID $cityId not found, using fallback")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading city data for ID $cityId", e)
            }
        }
    }

    suspend fun saveCityToFavorites(city: CityEntity): Boolean {
        return try {
            val existingCity = repository.getCityByName(city.name)
            if (existingCity?.isFavorite == true) {
                Log.d(TAG, "City ${city.name} already favorited")
                false
            } else {
                val updatedCity = city.copy(isFavorite = true)
                repository.updateCityFavoriteStatus(updatedCity)
                _city.postValue(updatedCity)
                Log.d(TAG, "Saved city ${updatedCity.name} to favorites")
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving city ${city.name} to favorites", e)
            false
        }
    }
}