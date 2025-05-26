package com.example.weatherforecast.ui.setting

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener

class SettingViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Settings Fragment"
    }
    val text: LiveData<String> = _text

    // LiveData for settings (example implementation)
    private val _useGpsLocation = MutableLiveData<Boolean>(true)
    val useGpsLocation: LiveData<Boolean> = _useGpsLocation

    private val _temperatureUnit = MutableLiveData<String>("Celsius")
    private val _pressureUnit = MutableLiveData<String>("hPa")
    private val _windSpeedUnit = MutableLiveData<String>("Meters per Second")
    private val _elevationUnit = MutableLiveData<String>("Meters")
    private val _visibilityUnit = MutableLiveData<String>("Meters")
    private val _language = MutableLiveData<String>("English")

    // Unit index mappings (based on array order)
    private val temperatureUnits = arrayOf("Celsius", "Fahrenheit")
    private val pressureUnits = arrayOf("hPa", "mb", "inHg", "mmHg")
    private val windSpeedUnits = arrayOf("Meters per Second", "Kilometers per Hour", "Miles per Hour")
    private val elevationUnits = arrayOf("Meters", "Feet")
    private val visibilityUnits = arrayOf("Meters", "Kilometers", "Miles")
    private val languages = arrayOf("English", "Spanish", "French", "German", "Italian", "Portuguese", "Russian", "Chinese", "Japanese", "Arabic")

    fun updateUseGpsLocation(isChecked: Boolean) {
        _useGpsLocation.value = isChecked
    }

    fun getTemperatureUnitIndex(): Int = temperatureUnits.indexOf(_temperatureUnit.value)
    fun getPressureUnitIndex(): Int = pressureUnits.indexOf(_pressureUnit.value)
    fun getWindSpeedUnitIndex(): Int = windSpeedUnits.indexOf(_windSpeedUnit.value)
    fun getElevationUnitIndex(): Int = elevationUnits.indexOf(_elevationUnit.value)
    fun getVisibilityUnitIndex(): Int = visibilityUnits.indexOf(_visibilityUnit.value)
    fun getLanguageIndex(): Int = languages.indexOf(_language.value)

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
            _language.value = languages[position]
        }
        override fun onNothingSelected(parent: AdapterView<*>?) {}
    }

    fun saveSettings(
        useGps: Boolean,
        tempUnit: String,
        pressureUnit: String,
        windSpeedUnit: String,
        elevationUnit: String,
        visibilityUnit: String,
        language: String
    ) {
        _useGpsLocation.value = useGps
        _temperatureUnit.value = tempUnit
        _pressureUnit.value = pressureUnit
        _windSpeedUnit.value = windSpeedUnit
        _elevationUnit.value = elevationUnit
        _visibilityUnit.value = visibilityUnit
        _language.value = language
        _text.value = "Settings saved: $language, $tempUnit"
    }
}