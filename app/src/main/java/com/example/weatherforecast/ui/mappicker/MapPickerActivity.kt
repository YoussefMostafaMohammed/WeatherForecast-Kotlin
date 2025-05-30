package com.example.weatherforecast.ui.mappicker

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.weatherforecast.R

class MapPickerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_picker)

        val mapFragment = MapPickerFragment().apply {
            arguments = Bundle().apply {
                putBoolean("is_city_selection_mode", true)
            }
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.map_container, mapFragment)
            .commit()
    }

    fun onCitySelected(cityId: Int, cityName: String) {
        val resultIntent = Intent().apply {
            putExtra("city_id", cityId)
            putExtra("city_name", cityName)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }
}