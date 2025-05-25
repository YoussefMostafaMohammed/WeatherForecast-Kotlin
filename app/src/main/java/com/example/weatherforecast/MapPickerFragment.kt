package com.example.weatherforecast

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
import com.example.weatherforecast.databinding.FragmentMapPickerBinding

class MapPickerFragment : Fragment() {

    private var _binding: FragmentMapPickerBinding? = null
    private val binding get() = _binding!!

    private val LOCATION_PERM = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    private val REQUEST_LOCATION = 1234

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
                val bundle = Bundle().apply {
                    putFloat("latitude", latitude.toFloat())
                    putFloat("longitude", longitude.toFloat())
                }
                findNavController().navigate(R.id.action_global_homeFragment, bundle)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
