package com.example.weatherforecast.ui.home

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherforecast.R
import com.example.weatherforecast.databinding.FragmentHomeBinding
import com.example.weatherforecast.utils.WeatherFormatter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.Manifest
import android.util.Log
import com.example.weatherforecast.BuildConfig
import com.example.weatherforecast.network.RetrofitClient
import com.example.weatherforecast.model.WeatherRepository
import com.example.weatherforecast.model.WeatherRepositoryImpl
import com.example.weatherforecast.db.WeatherLocalDataSourceImpl
import com.example.weatherforecast.db.WeatherDatabase
import com.example.weatherforecast.model.DailyItem
import com.example.weatherforecast.model.HourlyItem
import com.example.weatherforecast.network.WeatherRemoteDataSourceImpl
import com.example.weatherforecast.ui.home.HomeViewModel
import com.example.weatherforecast.ui.home.HomeViewModelFactory
import com.example.weatherforecast.utils.WeatherConstants
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: HomeViewModel
    private lateinit var hourlyAdapter: HourlyAdapter
    private lateinit var dailyAdapter: DailyAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val prefs by lazy { requireContext().getSharedPreferences("weather_prefs", Context.MODE_PRIVATE) }
    private val formatter by lazy { WeatherFormatter(prefs) }

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            fetchLocation()
        } else {
            loadWeatherData(WeatherConstants.DEFAULT_CITY, WeatherConstants.DEFAULT_CITY_ID)
            Log.w("HomeFragment", "Location permission denied, using ${WeatherConstants.DEFAULT_CITY} as fallback")
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

        // Apply RTL support based on language
        val language = prefs.getString("locale_code", "") ?: ""
        if (language == "ar") {
            binding.root.layoutDirection = View.LAYOUT_DIRECTION_RTL
            binding.root.textDirection = View.TEXT_DIRECTION_RTL
        } else {
            binding.root.layoutDirection = View.LAYOUT_DIRECTION_LTR
            binding.root.textDirection = View.TEXT_DIRECTION_LTR
        }

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
            }
            else -> {
                val locationMode = prefs.getString("location_mode", "gps") ?: "gps"
                if (locationMode == "gps") {
                    checkLocationPermission()
                } else {
                    val savedLat = prefs.getFloat("map_latitude", 0f)
                    val savedLon = prefs.getFloat("map_longitude", 0f)
                    if (savedLat != 0f && savedLon != 0f) {
                        viewModel.fetchCityFromCoordinates(savedLat.toDouble(), savedLon.toDouble())
                    } else {
                        loadWeatherData(WeatherConstants.DEFAULT_CITY, WeatherConstants.DEFAULT_CITY_ID)
                    }
                }
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
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                prefs.getString("locale_code", "") == "ar"
            )
            adapter = hourlyAdapter
        }

        dailyAdapter = DailyAdapter()
        binding.rvDaily.apply {
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.VERTICAL,
                false
            )
            adapter = dailyAdapter
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupObservers() {
        viewModel.current.observe(viewLifecycleOwner) { current ->
            current?.let {
                Log.d("HomeFragment", "Current weather data received: $it")
                binding.apply {
                    // Apply RTL text alignment for Arabic
                    val isArabic = prefs.getString("locale_code", "") == "ar"
                    listOf(
                        tvCity, tvCondition, tvCurrentTemp, tvTempRange, tvFeelsLike,
                        tvHumidity, tvWindSpeed, tvPressure, tvCloudiness, tvVisibility,
                        tvDate, tvSunrise, tvSunset
                    ).forEach { textView ->
                        textView.textDirection = if (isArabic) View.TEXT_DIRECTION_RTL else View.TEXT_DIRECTION_LTR
                        textView.textAlignment = if (isArabic) View.TEXT_ALIGNMENT_TEXT_END else View.TEXT_ALIGNMENT_TEXT_START
                    }

                    tvCity.text = viewModel.city.value?.name ?: "Unknown City"
                    tvCondition.text = formatter.translateWeatherDescription(it.weatherDescription)
                    tvCurrentTemp.text = formatter.formatTemperature(it.temp)
                    tvTempRange.text = "${formatter.formatTemperature(it.tempMin)} / ${formatter.formatTemperature(it.tempMax)}"
                    tvFeelsLike.text = "${getString(R.string.Feels)} • ${formatter.formatTemperature(it.feelsLike)}"
                    tvHumidity.text = "${getString(R.string.humidity)} • ${it.humidity}%"
                    tvWindSpeed.text = "${getString(R.string.wind)} • ${formatter.formatWindSpeed(it.windSpeed)}"
                    tvPressure.text = "${getString(R.string.pressure)} • ${formatter.formatPressure(it.pressure)}"
                    tvCloudiness.text = "${getString(R.string.clouds)} • ${it.cloudiness}%"
                    tvVisibility.text = "${getString(R.string.visibility)} • ${formatter.formatVisibility(it.visibility)}"

                    // Use locale-specific date and time formatting
                    val locale = if (isArabic) Locale("ar") else Locale.getDefault()
                    val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy 'at' hh:mm a z", locale)
                    val calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Kiev"))
                    tvDate.text = dateFormat.format(calendar.time)
                    val timeFormat = SimpleDateFormat("HH:mm", locale)
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
                val isArabic = prefs.getString("locale_code", "") == "ar"
                val locale = if (isArabic) Locale("ar") else Locale.getDefault()
                val hourlyItems = it.take(8).map { forecast ->
                    HourlyItem(
                        time = SimpleDateFormat("h a", locale).format(Date(forecast.dt * 1000)),
                        temp = formatter.formatTemperature(forecast.temp),
                        weatherIcon = forecast.weatherIcon
                    )
                }
                hourlyAdapter.submitList(hourlyItems)

                val dailyItems = it.groupBy { forecast ->
                    SimpleDateFormat("yyyy-MM-dd", locale).format(Date(forecast.dt * 1000))
                }.map { (_, dailyForecasts) ->
                    val firstForecast = dailyForecasts.first()
                    DailyItem(
                        date = SimpleDateFormat("MMM d, yyyy", locale).format(Date(firstForecast.dt * 1000)),
                        temp = formatter.formatTemperature(dailyForecasts.maxOf { it.temp }),
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
                val msg = if (wasSaved) {
                    if (prefs.getString("locale_code", "") == "ar") "تم الحفظ في المفضلة" else "Saved to favorites"
                } else {
                    if (prefs.getString("locale_code", "") == "ar") "موجود بالفعل في المفضلة" else "Already a favorite"
                }
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.errorEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { message ->
                val translatedMessage = if (prefs.getString("locale_code", "") == "ar") {
                    when (message) {
                        "Failed to fetch location-based weather. Using cached data or default city." ->
                            "فشل جلب الطقس بناءً على الموقع. استخدام البيانات المخزنة أو المدينة الافتراضية."
                        "Failed to fetch forecast. Using cached data or default city." ->
                            "فشل جلب التوقعات. استخدام البيانات المخزنة أو المدينة الافتراضية."
                        "Failed to fetch current weather. Using cached data or default city." ->
                            "فشل جلب الطقس الحالي. استخدام البيانات المخزنة أو المدينة الافتراضية."
                        "City not found. Using cached data or default city." ->
                            "المدينة غير موجودة. استخدام البيانات المخزنة أو المدينة الافتراضية."
                        "Error loading city data. Using cached data or default city." ->
                            "خطأ في تحميل بيانات المدينة. استخدام البيانات المخزنة أو المدينة الافتراضية."
                        "No cached data for Giza Governorate. Falling back to default city." ->
                            "لا توجد بيانات مخزنة للندن. العودة إلى المدينة الافتراضية."
                        "No cached data available. Please connect to the internet." ->
                            "لا توجد بيانات مخزنة متاحة. يرجى الاتصال بالإنترنت."
                        else -> message
                    }
                } else {
                    message
                }
                Toast.makeText(requireContext(), translatedMessage, Toast.LENGTH_LONG).show()
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
            } else {
                Log.w("HomeFragment", "Location is null, using ${WeatherConstants.DEFAULT_CITY} as fallback")
                loadWeatherData(WeatherConstants.DEFAULT_CITY, WeatherConstants.DEFAULT_CITY_ID)
            }
        }.addOnFailureListener {
            Log.e("HomeFragment", "Failed to get location", it)
            loadWeatherData(WeatherConstants.DEFAULT_CITY, WeatherConstants.DEFAULT_CITY_ID)
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
