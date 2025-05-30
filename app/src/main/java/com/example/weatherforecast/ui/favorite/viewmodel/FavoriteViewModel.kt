package com.example.weatherforecast.ui.favorite.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherforecast.model.CityEntity
import com.example.weatherforecast.model.CityWithWeather
import com.example.weatherforecast.model.WeatherRepository
import com.example.weatherforecast.utils.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FavoriteViewModel(
    private val repository: WeatherRepository
) : ViewModel() {

    private val _favoriteCities = MutableLiveData<List<CityWithWeather>>()
    val favoriteCities: LiveData<List<CityWithWeather>> = _favoriteCities

    private val _toastEvent = MutableLiveData<Event<String>>()
    val toastEvent: LiveData<Event<String>> = _toastEvent

    init {
        loadFavoriteCities()
    }

    fun loadFavoriteCities() {
        viewModelScope.launch {
            try {
                val cities = repository.getFavoriteCities()
                val citiesWithWeather = withContext(Dispatchers.IO) {
                    cities.map { city ->
                        val weather = repository.getCurrentWeatherForCity(city.id)
                        CityWithWeather(city, weather)
                    }
                }
                _favoriteCities.postValue(citiesWithWeather)
            } catch (e: Exception) {
                _favoriteCities.postValue(emptyList())
                _toastEvent.postValue(Event("Failed to load favorite cities"))
            }
        }
    }

    fun toggleFavorite(city: CityEntity) {
        viewModelScope.launch {
            try {
                val updatedCity = city.copy(isFavorite = false)
                repository.updateCityFavoriteStatus(updatedCity)
                _toastEvent.postValue(Event("Removed from favorites"))
                loadFavoriteCities() // Refresh the list
            } catch (e: Exception) {
                _toastEvent.postValue(Event("Failed to update favorite status"))
            }
        }
    }
}