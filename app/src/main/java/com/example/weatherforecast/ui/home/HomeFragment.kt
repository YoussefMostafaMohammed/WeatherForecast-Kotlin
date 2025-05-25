package com.example.weatherforecast.ui.home

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherforecast.R
import com.example.weatherforecast.databinding.FragmentHomeBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.text.SimpleDateFormat
import java.util.*
import android.Manifest
import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.weatherforecast.BuildConfig
import com.example.weatherforecast.RetrofitClient
import com.example.weatherforecast.WeatherRepository
import com.example.weatherforecast.WeatherRepositoryImpl
import com.example.weatherforecast.WeatherLocalDataSourceImpl
import com.example.weatherforecast.WeatherDatabase
import com.example.weatherforecast.WeatherRemoteDataSourceImpl



class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: HomeViewModel
    private lateinit var hourlyAdapter: HourlyAdapter
    private lateinit var dailyAdapter: DailyAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            fetchLocation()
        } else {
            loadWeatherData("London", 2643743)
            Log.w("HomeFragment", "Location permission denied, using London as fallback")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Initialize repository
        val database = WeatherDatabase.getDatabase(requireActivity().application)
        val apiService = RetrofitClient.getInstance().apiService
        val remoteDataSource = WeatherRemoteDataSourceImpl(apiService)
        val localDataSource = WeatherLocalDataSourceImpl(database)
        val apiKey = BuildConfig.WEATHER_API_KEY
        val repository = WeatherRepositoryImpl.getInstance(remoteDataSource, localDataSource, apiKey)
        val factory = HomeViewModelFactory(requireActivity().application, repository)

        viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]

        setupRecyclerViews()
        setupObservers()

        // Handle navigation arguments
        val cityId = arguments?.getInt("cityId")
        val latitude = arguments?.getFloat("latitude")
        val longitude = arguments?.getFloat("longitude")

        when {
            cityId != null && cityId != 0 -> {
                Log.d("HomeFragment", "Received cityId: $cityId")
                viewModel.fetchCityById(cityId)
            }
            latitude != null && longitude != null && latitude != 0f && longitude != 0f -> {
                Log.d("HomeFragment", "Received coordinates: $latitude, $longitude")
                viewModel.fetchCityFromCoordinates(latitude.toDouble(), longitude.toDouble())
                viewModel.fetchCurrentByCoordinates(latitude.toDouble(), longitude.toDouble())
            }
            else -> {
                checkLocationPermission()
            }
        }

        binding.fab.setOnClickListener {
            viewModel.markCurrentCityFavorite()
        }

        return binding.root
    }

    private fun setupRecyclerViews() {
        hourlyAdapter = HourlyAdapter()
        binding.rvHourly.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = hourlyAdapter
        }

        dailyAdapter = DailyAdapter()
        binding.rvDaily.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = dailyAdapter
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupObservers() {
        viewModel.current.observe(viewLifecycleOwner) { current ->
            current?.let {
                Log.d("HomeFragment", "Current weather data received: $it")
                binding.apply {
                    tvCity.text = viewModel.city.value?.name ?: "Unknown City"
                    tvCondition.text = it.weatherDescription.replaceFirstChar { char -> char.uppercase() }
                    tvCurrentTemp.text = "${it.temp.toInt()}°C"
                    tvTempRange.text = "${it.tempMin.toInt()}°C / ${it.tempMax.toInt()}°C"
                    tvFeelsLike.text = "Feels • ${it.feelsLike.toInt()}°C"
                    tvHumidity.text = "Humidity • ${it.humidity}%"
                    tvWindSpeed.text = "Wind • ${it.windSpeed} m/s"
                    tvPressure.text = "Pressure • ${it.pressure} hPa"
                    tvCloudiness.text = "Clouds • ${it.cloudiness}%"
                    val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy 'at' hh:mm a z", Locale.getDefault())
                    val calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Kiev"))
                    tvDate.text = dateFormat.format(calendar.time)
                    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    tvSunrise.text = timeFormat.format(Date(it.sunrise * 1000))
                    tvSunset.text = timeFormat.format(Date(it.sunset * 1000))

                    val iconResId = mapWeatherIcon(it.weatherIcon)
                    ivWeatherIcon.setImageResource(iconResId)
                }
            } ?: Log.w("HomeFragment", "Current weather data is null")
        }

        viewModel.forecasts.observe(viewLifecycleOwner) { forecasts ->
            forecasts?.let {
                Log.d("HomeFragment", "Forecasts received: ${it.size} items")
                val hourlyItems = it.take(8).map { forecast ->
                    HourlyItem(
                        time = SimpleDateFormat("h a", Locale.getDefault()).format(Date(forecast.dt * 1000)),
                        temp = "${forecast.temp.toInt()}°C",
                        weatherIcon = forecast.weatherIcon
                    )
                }
                hourlyAdapter.submitList(hourlyItems)

                val dailyItems = it.groupBy { forecast ->
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(forecast.dt * 1000))
                }.map { (_, dailyForecasts) ->
                    val firstForecast = dailyForecasts.first()
                    DailyItem(
                        date = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(firstForecast.dt * 1000)),
                        temp = "${dailyForecasts.maxOf { it.temp.toInt() }}°C",
                        weatherIcon = firstForecast.weatherIcon
                    )
                }.drop(1).take(5)
                viewModel.city.value?.let { city ->
                    binding.tvCity.text = city.name
                }

                dailyAdapter.submitList(dailyItems)
            } ?: Log.w("HomeFragment", "Forecasts data is null")
        }

        viewModel.favoriteEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { wasSaved ->
                val msg = if (wasSaved) "Saved to favorites" else "Already a favorite"
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.errorEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                fetchLocation()
            }
            else -> {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetchLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                Log.d("HomeFragment", "Fetching weather for location: ${location.latitude}, ${location.longitude}")
                viewModel.fetchCityFromCoordinates(location.latitude, location.longitude)
                viewModel.fetchCurrentByCoordinates(location.latitude, location.longitude)
            } else {
                Log.w("HomeFragment", "Location is null, using London as fallback")
                loadWeatherData("London", 2643743)
            }
        }.addOnFailureListener {
            Log.e("HomeFragment", "Failed to get location", it)
            loadWeatherData("London", 2643743)
        }
    }

    private fun loadWeatherData(cityName: String, cityId: Int) {
        Log.d("HomeFragment", "Loading weather data for $cityName (ID: $cityId)")
        viewModel.fetchCurrent(cityName)
        viewModel.fetchWeather(cityId.toString())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun mapWeatherIcon(iconCode: String): Int {
        return when (iconCode) {
            "01d" -> R.drawable.ic_day_sunny
            "01n" -> R.drawable.ic_night_clear
            "02d" -> R.drawable.ic_day_cloudy
            "02n" -> R.drawable.ic_night_alt_partly_cloudy
            "03d", "03n" -> R.drawable.ic_cloud
            "04d", "04n" -> R.drawable.ic_cloudy_gusts
            "09d", "09n" -> R.drawable.ic_showers
            "10d" -> R.drawable.ic_day_rain
            "10n" -> R.drawable.ic_night_rain
            "11d" -> R.drawable.ic_day_thunderstorm
            "11n" -> R.drawable.ic_night_thunderstorm
            "13d", "13n" -> R.drawable.ic_snow
            "50d", "50n" -> R.drawable.ic_fog
            else -> R.drawable.ic_day_sunny
        }
    }
}

class HourlyAdapter : ListAdapter<HourlyItem, HourlyAdapter.HourlyViewHolder>(HourlyDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourlyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_forecast, parent, false)
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
                "01d" -> R.drawable.ic_day_sunny
                "01n" -> R.drawable.ic_night_clear
                "02d" -> R.drawable.ic_day_cloudy
                "02n" -> R.drawable.ic_night_alt_partly_cloudy
                "03d", "03n" -> R.drawable.ic_cloud
                "04d", "04n" -> R.drawable.ic_cloudy_gusts
                "09d", "09n" -> R.drawable.ic_showers
                "10d" -> R.drawable.ic_day_rain
                "10n" -> R.drawable.ic_night_rain
                "11d" -> R.drawable.ic_day_thunderstorm
                "11n" -> R.drawable.ic_night_thunderstorm
                "13d", "13n" -> R.drawable.ic_snow
                "50d", "50n" -> R.drawable.ic_fog
                else -> R.drawable.ic_day_sunny
            }
            ivHourlyWeatherIcon.setImageResource(iconResId)
        }
    }

    class HourlyDiffCallback : DiffUtil.ItemCallback<HourlyItem>() {
        override fun areItemsTheSame(oldItem: HourlyItem, newItem: HourlyItem) = oldItem.time == newItem.time
        override fun areContentsTheSame(oldItem: HourlyItem, newItem: HourlyItem) = oldItem == newItem
    }
}

class DailyAdapter : ListAdapter<DailyItem, DailyAdapter.DailyViewHolder>(DailyDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_daily_forecast, parent, false)
        return DailyViewHolder(view)
    }

    override fun onBindViewHolder(holder: DailyViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DailyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvDate: TextView = view.findViewById(R.id.tvDailyDate)
        private val ivDailyWeatherIcon: ImageView = view.findViewById(R.id.ivDailyWeatherIcon)
        private val tvDailyTemp: TextView = view.findViewById(R.id.tvDailyTemp)

        private val inputFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        private val shortDayFormat = SimpleDateFormat("EEE", Locale.getDefault())

        fun bind(item: DailyItem) {
            val parsedDate: Date? = try {
                inputFormat.parse(item.date)
            } catch (e: Exception) {
                null
            }

            val dayAbbrev: String = parsedDate?.let { shortDayFormat.format(it) } ?: ""
            val displayDate = when (dayAbbrev) {
                "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" -> "$dayAbbrev, ${item.date}"
                else -> item.date
            }

            tvDate.text = displayDate
            tvDailyTemp.text = item.temp

            val iconResId = when (item.weatherIcon) {
                "01d" -> R.drawable.ic_day_sunny
                "01n" -> R.drawable.ic_night_clear
                "02d" -> R.drawable.ic_day_cloudy
                "02n" -> R.drawable.ic_night_alt_partly_cloudy
                "03d", "03n" -> R.drawable.ic_cloud
                "04d", "04n" -> R.drawable.ic_cloudy_gusts
                "09d", "09n" -> R.drawable.ic_showers
                "10d" -> R.drawable.ic_day_rain
                "10n" -> R.drawable.ic_night_rain
                "11d" -> R.drawable.ic_day_thunderstorm
                "11n" -> R.drawable.ic_night_thunderstorm
                "13d", "13n" -> R.drawable.ic_snow
                "50d", "50n" -> R.drawable.ic_fog
                else -> R.drawable.ic_day_sunny
            }
            ivDailyWeatherIcon.setImageResource(iconResId)
        }
    }

    class DailyDiffCallback : DiffUtil.ItemCallback<DailyItem>() {
        override fun areItemsTheSame(oldItem: DailyItem, newItem: DailyItem) =
            oldItem.date == newItem.date
        override fun areContentsTheSame(oldItem: DailyItem, newItem: DailyItem) = oldItem == newItem
    }
}

data class HourlyItem(val time: String, val temp: String, val weatherIcon: String)
data class DailyItem(val date: String, val temp: String, val weatherIcon: String)

class HomeViewModelFactory(
    private val application: Application,
    private val repository: WeatherRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}