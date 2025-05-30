package com.example.weatherforecast.ui.mappicker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.GeolocationPermissions
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.weatherforecast.BuildConfig
import com.example.weatherforecast.R
import com.example.weatherforecast.db.WeatherDatabase
import com.example.weatherforecast.model.WeatherRepositoryImpl
import com.example.weatherforecast.databinding.FragmentMapPickerBinding
import com.example.weatherforecast.db.WeatherLocalDataSourceImpl
import com.example.weatherforecast.network.RetrofitClient
import com.example.weatherforecast.network.WeatherRemoteDataSourceImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MapPickerFragment : Fragment() {

    private var _binding: FragmentMapPickerBinding? = null
    private val binding get() = _binding!!

    private val LOCATION_PERM = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    private val REQUEST_LOCATION = 1234

    // Flag to determine the context (city selection or navigation)
    private var isCitySelectionMode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Retrieve the flag from arguments
        isCitySelectionMode = arguments?.getBoolean("is_city_selection_mode", false) ?: false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (hasLocationPermission()) {
            setupWebView()
        } else {
            requestPermissions(LOCATION_PERM, REQUEST_LOCATION)
        }
    }

    private fun hasLocationPermission(): Boolean =
        LOCATION_PERM.all {
            ContextCompat.checkSelfPermission(
                requireContext(), it
            ) == PackageManager.PERMISSION_GRANTED
        }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION && hasLocationPermission()) {
            setupWebView()
        } else {
            Toast.makeText(
                requireContext(),
                "Location permission is required to center map",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun setupWebView() {
        binding.mapWebview.apply {
            settings.javaScriptEnabled = true
            settings.setGeolocationEnabled(true)
            webViewClient = WebViewClient()
            webChromeClient = object : WebChromeClient() {
                override fun onGeolocationPermissionsShowPrompt(
                    origin: String, callback: GeolocationPermissions.Callback
                ) {
                    callback.invoke(origin, true, false)
                }
            }
            addJavascriptInterface(WebAppInterface(), "AndroidInterface")
            loadUrl("file:///android_asset/map.html")
        }
    }

    inner class WebAppInterface {
        @android.webkit.JavascriptInterface
        fun onLocationSelected(latitude: Double, longitude: Double) {
            requireActivity().runOnUiThread {
                if (isCitySelectionMode) {
                    // Initialize repository
                    val database = WeatherDatabase.getDatabase(requireActivity().application)
                    val apiService = RetrofitClient.getInstance().apiService
                    val remoteDataSource = WeatherRemoteDataSourceImpl(apiService)
                    val localDataSource = WeatherLocalDataSourceImpl(database)
                    val repository = WeatherRepositoryImpl.getInstance(
                        remoteDataSource,
                        localDataSource,
                        BuildConfig.WEATHER_API_KEY
                    )

                    // Fetch city by coordinates
                    CoroutineScope(Dispatchers.Main).launch {
                        try {
                            val (cityEntity, _) = withContext(Dispatchers.IO) {
                                repository.fetchCurrentWeatherByCoordinates(latitude, longitude)
                            }
                            (requireActivity() as? MapPickerActivity)?.onCitySelected(cityEntity.id, cityEntity.name)
                        } catch (e: Exception) {
                            Toast.makeText(
                                requireContext(),
                                "Failed to fetch city: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    // For navigation graph: Navigate to HomeFragment
                    val bundle = Bundle().apply {
                        putFloat("latitude", latitude.toFloat())
                        putFloat("longitude", longitude.toFloat())
                    }
                    try {
                        findNavController().navigate(R.id.action_global_homeFragment, bundle)
                    } catch (e: IllegalStateException) {
                        Toast.makeText(
                            requireContext(),
                            "Navigation error: Unable to navigate to Home",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}