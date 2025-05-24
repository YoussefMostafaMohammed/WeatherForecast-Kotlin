package com.example.weatherforecast

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class WeatherViewModel(application: Application) : AndroidViewModel(application) {
    private val database = WeatherDatabase.getDatabase(application)
    private val cityDao = database.cityDao()
    private val forecastDao = database.forecastDao()
    private val currentDao = database.currentWeatherDao()

    private val _forecasts = MutableLiveData<List<ForecastEntity>>()
    val forecasts: LiveData<List<ForecastEntity>> = _forecasts

    private val _city = MutableLiveData<CityEntity>()
    val city: LiveData<CityEntity> = _city


    private val _current = MutableLiveData<CurrentWeatherEntity>()
    val current: LiveData<CurrentWeatherEntity> = _current

    fun fetchWeather(cityId: Int, apiKey: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.getInstance().apiService.getWeather(cityId, apiKey)
                if (response.isSuccessful) {
                    response.body()?.let { weatherResponse ->
                        val cityEntity = mapToCityEntity(weatherResponse.city)
                        cityDao.insert(cityEntity)
                        val forecastEntities = mapToForecastEntities(weatherResponse.list, cityEntity.id)
                        forecastDao.insert(forecastEntities)
                        loadData(cityId)
                    }
                }
            } catch (e: Exception) {
                Log.d("tag", e.message.toString())
            }
        }
    }


    fun fetchCurrent(cityName: String, apiKey: String) {
        viewModelScope.launch {
            try {
                val resp = RetrofitClient.getInstance()
                    .apiService.getCurrentWeather(cityName, apiKey)
                if (resp.isSuccessful) {
                    resp.body()?.let { data ->
                        val ent = mapToCurrentEntity(data)
                        currentDao.upsert(ent)
                        _current.value = ent
                    }
                }
            } catch (e: Exception) {
                Log.e("WeatherVM", "current error", e)
            }
        }
    }


    private fun loadData(cityId: Int) {
        viewModelScope.launch {
            _city.value = cityDao.getCityById(cityId)
            _forecasts.value = forecastDao.getForecastsForCity(cityId)
        }
    }
}