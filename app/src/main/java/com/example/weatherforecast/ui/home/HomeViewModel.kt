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

    private val _errorEvent = MutableLiveData<Event<String>>()
    val errorEvent: LiveData<Event<String>> = _errorEvent

    companion object {
        private const val DEFAULT_CITY = "London"
        private const val DEFAULT_CITY_ID = 2643743
        private const val TAG = "HomeViewModel"
    }

    fun markCurrentCityFavorite() {
        viewModelScope.launch {
            city.value?.let { cityEntity ->
                val wasSaved = saveCityToFavorites(cityEntity)
                _favoriteEvent.value = Event(wasSaved)
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
                _errorEvent.postValue(Event("Failed to fetch location-based weather. Using cached data or default city."))
                loadCachedOrDefaultData()
            }
        }
    }

    fun fetchWeather(cityName: String) {
        viewModelScope.launch {
            try {
                val city = repository.getCityByName(cityName) ?: run {
                    val (cityEntity, _) = repository.fetchCurrentWeather(cityName)
                    cityEntity
                }
                val (cityEntity, forecasts) = repository.fetchForecast(city.id)
                repository.saveForecast(cityEntity, forecasts)
                _forecasts.postValue(forecasts)
                _city.postValue(cityEntity)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch forecast for $cityName", e)
                loadCachedWeather(cityName)
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
                _errorEvent.postValue(Event("Failed to fetch forecast. Using cached data or default city."))
                loadCachedOrDefaultData()
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
                loadCachedWeather(cityName)
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
                _errorEvent.postValue(Event("Failed to fetch current weather. Using cached data or default city."))
                loadCachedOrDefaultData()
            }
        }
    }

    fun fetchCityById(cityId: Int) {
        viewModelScope.launch {
            try {
                val city = repository.getCityById(cityId)

                if (city != null) {
                    _city.postValue(city)
                    loadCachedWeather(city.name)
                } else {
                    Log.w(TAG, "City with ID $cityId not found in database")
                    _errorEvent.postValue(Event("City not found. Using cached data or default city."))
                    loadCachedOrDefaultData()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching city data for ID $cityId", e)
                _errorEvent.postValue(Event("Error loading city data. Using cached data or default city."))
                loadCachedOrDefaultData()
            }
        }
    }

    private suspend fun loadCachedWeather(cityName: String) {
        val cachedCity = repository.getCityByName(cityName)
        if (cachedCity != null) {
            Log.w(TAG, "cashed data for $cityName")

            val cachedWeather = repository.getCachedCurrentWeather(cityName)
            val cachedForecasts = repository.getCachedForecast(cachedCity.id)
            if (cachedWeather != null) {
                _current.postValue(cachedWeather)
                _city.postValue(cachedCity)
            }
            if (cachedForecasts.isNotEmpty()) {
                _forecasts.postValue(cachedForecasts)
            }
            if (cachedWeather == null && cachedForecasts.isEmpty()) {
                Log.w(TAG, "No cached weather data for $cityName")
                _errorEvent.postValue(Event("No cached data for $cityName. Falling back to default city."))
                loadCachedOrDefaultData()
            }
        } else {
            Log.w(TAG, "No cached city data for $cityName")
            _errorEvent.postValue(Event("City $cityName not found in cache. Falling back to default city."))
            loadCachedOrDefaultData()
        }
    }

    private suspend fun loadCachedOrDefaultData() {
        val cachedCity = repository.getCityByName(DEFAULT_CITY)
        if (cachedCity != null) {
            val cachedWeather = repository.getCachedCurrentWeather(DEFAULT_CITY)
            val cachedForecasts = repository.getCachedForecast(DEFAULT_CITY_ID)
            if (cachedWeather != null) {
                _current.postValue(cachedWeather)
                _city.postValue(cachedCity)
            }
            if (cachedForecasts.isNotEmpty()) {
                _forecasts.postValue(cachedForecasts)
            }
            if (cachedWeather == null && cachedForecasts.isEmpty()) {
                Log.w(TAG, "No cached data for default city $DEFAULT_CITY")
                _errorEvent.postValue(Event("No cached data available. Please connect to the internet."))
            }
        } else {
            Log.w(TAG, "Default city $DEFAULT_CITY not found in cache")
            _errorEvent.postValue(Event("No cached data available. Please connect to the internet."))
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