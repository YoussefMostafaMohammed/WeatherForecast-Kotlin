package com.example.weatherforecast.ui.setting

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener

class SettingViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs: SharedPreferences = application.getSharedPreferences("weather_prefs", Context.MODE_PRIVATE)

    private val _text = MutableLiveData<String>().apply {
        value = "This is Settings Fragment"
    }
    val text: LiveData<String> = _text

    private val _useGpsLocation = MutableLiveData<Boolean>()
    val useGpsLocation: LiveData<Boolean> = _useGpsLocation

    private val _temperatureUnit = MutableLiveData<String>()
    private val _pressureUnit = MutableLiveData<String>()
    private val _windSpeedUnit = MutableLiveData<String>()
    private val _elevationUnit = MutableLiveData<String>()
    private val _visibilityUnit = MutableLiveData<String>()
    private val _localeCode = MutableLiveData<String>()
    val localeCode: LiveData<String> = _localeCode

    private val temperatureUnits = arrayOf("Celsius", "Kelvin", "Fahrenheit")
    private val pressureUnits = arrayOf("hPa", "mb", "in Hg", "mm Hg")
    private val windSpeedUnits = arrayOf("m/s", "km/h", "mph")
    private val elevationUnits = arrayOf("Meters", "Feet")
    private val visibilityUnits = arrayOf("Meters", "Kilometers", "Miles")
    private val localeCodes = listOf("", "en", "ar") // System, English, Arabic

    init {
        _useGpsLocation.value = prefs.getBoolean("use_gps", true)
        _temperatureUnit.value = prefs.getString("temp_unit", "Celsius")
        _pressureUnit.value = prefs.getString("pressure_unit", "hPa")
        _windSpeedUnit.value = prefs.getString("wind_speed_unit", "m/s")
        _elevationUnit.value = prefs.getString("elevation_unit", "Meters")
        _visibilityUnit.value = prefs.getString("visibility_unit", "Meters")
        _localeCode.value = prefs.getString("locale_code", "")
    }

    fun updateUseGpsLocation(isChecked: Boolean) {
        _useGpsLocation.value = isChecked
    }

    fun getTemperatureUnitIndex(): Int = temperatureUnits.indexOf(_temperatureUnit.value)
    fun getPressureUnitIndex(): Int = pressureUnits.indexOf(_pressureUnit.value)
    fun getWindSpeedUnitIndex(): Int = windSpeedUnits.indexOf(_windSpeedUnit.value)
    fun getElevationUnitIndex(): Int = elevationUnits.indexOf(_elevationUnit.value)
    fun getVisibilityUnitIndex(): Int = visibilityUnits.indexOf(_visibilityUnit.value)
    fun getLanguageIndex(): Int = localeCodes.indexOf(_localeCode.value ?: "").takeIf { it >= 0 } ?: 0

    fun getTemperatureUnitListener(): OnItemSelectedListener = object : OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            _temperatureUnit.value = temperatureUnits[position]
        }
        override fun onNothingSelected(parent: AdapterView<*>?) {}
    }

    fun getPressureUnitListener(): OnItemSelectedListener = object : OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            _pressureUnit.value = pressureUnits[position]
        }
        override fun onNothingSelected(parent: AdapterView<*>?) {}
    }

    fun getWindSpeedUnitListener(): OnItemSelectedListener = object : OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            _windSpeedUnit.value = windSpeedUnits[position]
        }
        override fun onNothingSelected(parent: AdapterView<*>?) {}
    }

    fun getElevationUnitListener(): OnItemSelectedListener = object : OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            _elevationUnit.value = elevationUnits[position]
        }
        override fun onNothingSelected(parent: AdapterView<*>?) {}
    }

    fun getVisibilityUnitListener(): OnItemSelectedListener = object : OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            _visibilityUnit.value = visibilityUnits[position]
        }
        override fun onNothingSelected(parent: AdapterView<*>?) {}
    }

    fun getLanguageListener(): OnItemSelectedListener = object : OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            if (position < localeCodes.size) {
                _localeCode.value = localeCodes[position]
            }
        }
        override fun onNothingSelected(parent: AdapterView<*>?) {}
    }

    fun saveSettings() {
        val useGps = _useGpsLocation.value ?: true
        val tempUnit = _temperatureUnit.value ?: "Celsius"
        val pressureUnit = _pressureUnit.value ?: "hPa"
        val windSpeedUnit = _windSpeedUnit.value ?: "m/s"
        val elevationUnit = _elevationUnit.value ?: "Meters"
        val visibilityUnit = _visibilityUnit.value ?: "Meters"
        val localeCode = _localeCode.value ?: ""

        prefs.edit().apply {
            putBoolean("use_gps", useGps)
            putString("temp_unit", tempUnit)
            putString("pressure_unit", pressureUnit)
            putString("wind_speed_unit", windSpeedUnit)
            putString("elevation_unit", elevationUnit)
            putString("visibility_unit", visibilityUnit)
            putString("locale_code", localeCode)
            apply()
        }

        _text.value = if (localeCode.isEmpty()) {
            "Settings saved, using system language"
        } else {
            "Settings saved: $localeCode"
        }
    }
}