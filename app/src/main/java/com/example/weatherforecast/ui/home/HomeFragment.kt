package com.example.weatherforecast.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherforecast.R
import com.example.weatherforecast.WeatherViewModel
import com.example.weatherforecast.databinding.FragmentHomeBinding
import com.example.weatherforecast.ui.home.HourlyAdapter.HourlyViewHolder
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: WeatherViewModel
    private lateinit var hourlyAdapter: HourlyAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[WeatherViewModel::class.java]
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        setupRecyclerView()
        setupObservers()
        loadInitialData()

        return binding.root
    }

    private fun setupRecyclerView() {
        hourlyAdapter = HourlyAdapter()
        binding.rvHourly.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = hourlyAdapter
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupObservers() {
        viewModel.current.observe(viewLifecycleOwner) { current ->
            current?.let {
                binding.apply {
                    // Use the city name from the ViewModel if available
                    tvCity.text = viewModel.city.value?.name ?: "Moscow"
                    tvCondition.text = it.weatherDescription.replaceFirstChar { char -> char.uppercase() }
                    tvCurrentTemp.text = "${it.temp.toInt()}°C"
                    tvTempRange.text = "${it.tempMin.toInt()}°C / ${it.tempMax.toInt()}°C"
                    tvFeelsLike.text = "Feels • ${it.feelsLike.toInt()}°C"
                    tvHumidity.text = "Humidity • ${it.humidity}%"
                    tvWindSpeed.text = "Wind • ${it.windSpeed} m/s"
                    tvPressure.text = "Pressure • ${it.pressure} hPa"
                    tvCloudiness.text = "Clouds • ${it.cloudiness}%"
                    // Use the current date (May 24, 2025)
                    val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
                    tvDate.text = dateFormat.format(Date()) // Current date

                    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    tvSunrise.text = timeFormat.format(Date(it.sunrise * 1000))
                    tvSunset.text = timeFormat.format(Date(it.sunset * 1000))

                    // Set the weather icon
                    val iconResId = mapWeatherIcon(it.weatherIcon)
                    ivWeatherIcon.setImageResource(iconResId)
                }
            }
        }

        viewModel.forecasts.observe(viewLifecycleOwner) { forecasts ->
            forecasts?.let {
                val hourlyItems = it.take(8).map { forecast -> // Limit to 4 items to match screenshot
                    HourlyItem(
                        time = SimpleDateFormat("HH:mm", Locale.getDefault())
                            .format(Date(forecast.dt * 1000)),
                        temp = "${forecast.temp.toInt()}°C",
                        weatherIcon = forecast.weatherIcon
                    )
                }
                hourlyAdapter.submitList(hourlyItems)
            }
        }
    }

    private fun loadInitialData() {
        viewModel.fetchCurrent("Moscow", "897f05d7107c1a4583eb10de82e05435")
        viewModel.fetchWeather(524901, "897f05d7107c1a4583eb10de82e05435") // 524901 is Moscow's city ID
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Map weather icon code to drawable resource
    private fun mapWeatherIcon(iconCode: String): Int {
        return when (iconCode) {
            "01d" -> R.drawable.ic_day_sunny            // clear day
            "01n" -> R.drawable.ic_night_clear          // clear night

            "02d" -> R.drawable.ic_day_cloudy            // few clouds day
            "02n" -> R.drawable.ic_night_alt_partly_cloudy  // few clouds night

            "03d", "03n" -> R.drawable.ic_cloud          // scattered clouds (same for day and night)

            "04d", "04n" -> R.drawable.ic_cloudy_gusts  // broken clouds (closest match)

            "09d", "09n" -> R.drawable.ic_showers       // shower rain

            "10d" -> R.drawable.ic_day_rain              // rain day
            "10n" -> R.drawable.ic_night_rain            // rain night

            "11d" -> R.drawable.ic_day_thunderstorm      // thunderstorm day
            "11n" -> R.drawable.ic_night_thunderstorm    // thunderstorm night

            "13d", "13n" -> R.drawable.ic_snow           // snow (same for day and night)

            "50d", "50n" -> R.drawable.ic_fog
            else -> R.drawable.ic_day_sunny
        }
    }
}

class HourlyAdapter : ListAdapter<HourlyItem, HourlyViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourlyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_forecast, parent, false)
        return HourlyViewHolder(view)
    }

    override fun onBindViewHolder(holder: HourlyViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class HourlyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvTime: TextView = view.findViewById(R.id.tvTime)
        private val ivHourlyWeatherIcon: ImageView = view.findViewById(R.id.ivWeatherIcon)
        private val tvTemp: TextView = view.findViewById(R.id.tvTemp)

        fun bind(item: HourlyItem) {
            tvTime.text = item.time

            tvTemp.text = item.temp
            val iconResId = when (item.weatherIcon) {
                "01d" -> R.drawable.ic_day_sunny            // clear day
                "01n" -> R.drawable.ic_night_clear          // clear night

                "02d" -> R.drawable.ic_day_cloudy            // few clouds day
                "02n" -> R.drawable.ic_night_alt_partly_cloudy  // few clouds night

                "03d", "03n" -> R.drawable.ic_cloud          // scattered clouds (same for day and night)

                "04d", "04n" -> R.drawable.ic_cloudy_gusts  // broken clouds (closest match)

                "09d", "09n" -> R.drawable.ic_showers       // shower rain

                "10d" -> R.drawable.ic_day_rain              // rain day
                "10n" -> R.drawable.ic_night_rain            // rain night

                "11d" -> R.drawable.ic_day_thunderstorm      // thunderstorm day
                "11n" -> R.drawable.ic_night_thunderstorm    // thunderstorm night

                "13d", "13n" -> R.drawable.ic_snow           // snow (same for day and night)

                "50d", "50n" -> R.drawable.ic_fog            // mist/fog

                else -> R.drawable.ic_day_sunny
            }

            ivHourlyWeatherIcon.setImageResource(iconResId)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<HourlyItem>() {
        override fun areItemsTheSame(oldItem: HourlyItem, newItem: HourlyItem) =
            oldItem.time == newItem.time
        override fun areContentsTheSame(oldItem: HourlyItem, newItem: HourlyItem) =
            oldItem == newItem
    }
}

data class HourlyItem(val time: String, val temp: String, val weatherIcon: String)