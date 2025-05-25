
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import com.example.weatherforecast.R
import com.example.weatherforecast.databinding.FragmentSlideshowBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng

class SlideshowFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentSlideshowBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSlideshowBinding.inflate(inflater, container, false)

        // Initialize the SupportMapFragment
        val mapFrag = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFrag.getMapAsync(this)

        return binding.root
    }

    override fun onMapReady(googleMap: GoogleMap) {
        // Default camera position
        val defaultLatLng = LatLng(51.5074, -0.1278) // London
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLatLng, 5f))

        // When user taps the map, send result and navigate back
        googleMap.setOnMapClickListener { latLng ->
            // Send the picked coords
            setFragmentResult("locationPicked", bundleOf(
                "lat" to latLng.latitude,
                "lng" to latLng.longitude
            ))
            // Go back to Home
            requireActivity()
                .supportFragmentManager
                .popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
