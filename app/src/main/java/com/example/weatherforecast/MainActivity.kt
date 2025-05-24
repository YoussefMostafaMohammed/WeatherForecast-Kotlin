package com.example.weatherforecast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

import com.example.weatherforecast.databinding.ItemForecastBinding

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.weatherforecast.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: WeatherViewModel
    private val forecastAdapter = ForecastAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewModel = ViewModelProvider(this)[WeatherViewModel::class.java]

        binding.forecastRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = forecastAdapter
        }

        binding.refreshFab.setOnClickListener {
            viewModel.fetchCurrent("Faiyum", "897f05d7107c1a4583eb10de82e05435")
            viewModel.fetchWeather(361320, "897f05d7107c1a4583eb10de82e05435")
        }

        viewModel.city.observe(this) { city ->
            binding.cityNameTextView.text = city?.name ?: "—"
            binding.coordsTextView.text = city?.let { "Lat: ${it.coordLat} / Lon: ${it.coordLon }" } ?: "Lat: -- / Lon: --"
        }

        viewModel.forecasts.observe(this) { forecasts ->
            forecastAdapter.submitList(forecasts)
        }

        viewModel.current.observe(this) { forecast ->
            binding.currentTempTextView.text = "${forecast.temp}°C"
            binding.currentDescTextView.text = forecast.weatherDescription
            binding.feelsLikeChip.text = "Feels like: ${forecast.feelsLike}°C"
            binding.humidityChip.text = "Humidity: ${forecast.humidity}%"
            binding.windChip.text = "Wind: ${forecast.windSpeed} m/s"

        }
    }
}


class ForecastAdapter : ListAdapter<ForecastEntity, ForecastAdapter.ViewHolder>(ForecastDiffCallback) {

    inner class ViewHolder(private val binding: ItemForecastBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(forecast: ForecastEntity) {}
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemForecastBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val ForecastDiffCallback = object : DiffUtil.ItemCallback<ForecastEntity>() {
            override fun areItemsTheSame(oldItem: ForecastEntity, newItem: ForecastEntity): Boolean {
                return oldItem.dtTxt == newItem.dtTxt
            }

            override fun areContentsTheSame(oldItem: ForecastEntity, newItem: ForecastEntity): Boolean {
                return oldItem == newItem
            }
        }
    }
}