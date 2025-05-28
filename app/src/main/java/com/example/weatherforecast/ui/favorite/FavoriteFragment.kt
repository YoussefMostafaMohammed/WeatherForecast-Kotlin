package com.example.weatherforecast.ui.favorite
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherforecast.R
import android.widget.ImageButton
import android.widget.ImageView
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherforecast.CityEntity
import com.example.weatherforecast.RetrofitClient
import com.example.weatherforecast.WeatherDatabase
import com.example.weatherforecast.locale.WeatherLocalDataSourceImpl
import com.example.weatherforecast.remote.WeatherRemoteDataSourceImpl
import com.example.weatherforecast.WeatherRepository
import com.example.weatherforecast.WeatherRepositoryImpl
import com.example.weatherforecast.databinding.FragmentFavoriteBinding

class FavoriteFragment : Fragment() {

    private var _binding: FragmentFavoriteBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: FavoriteViewModel
    private lateinit var adapter: FavoriteCityAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val database = WeatherDatabase.getDatabase(requireContext())
        val localDataSource = WeatherLocalDataSourceImpl(database)
        val remoteDataSource = WeatherRemoteDataSourceImpl(RetrofitClient.getInstance().apiService)
        val repository = WeatherRepositoryImpl.getInstance(
            remoteDataSource,
            localDataSource,
            "897f05d7107c1a4583eb10de82e05435"
        )

        val factory = FavoriteViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[FavoriteViewModel::class.java]

        adapter = FavoriteCityAdapter(viewModel) { city ->
            val bundle = Bundle().apply {
                putInt("cityId", city.id)  // The argument name should match what HomeFragment expects
            }
            findNavController().navigate(R.id.nav_home, bundle)
        }


        binding.rvFavorites.layoutManager = LinearLayoutManager(context)
        binding.rvFavorites.adapter = adapter

        viewModel.favoriteCities.observe(viewLifecycleOwner) { cities ->
            adapter.submitList(cities)
        }

        viewModel.toastEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    override fun onResume() {
        super.onResume()
        viewModel.loadFavoriteCities()
    }
}

class FavoriteCityAdapter(
    private val viewModel: FavoriteViewModel,
    private val onCityClicked: (CityEntity) -> Unit
) : ListAdapter<CityWithWeather, FavoriteCityAdapter.CityViewHolder>(CityDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorite_city, parent, false)
        return CityViewHolder(view, viewModel,onCityClicked )
    }

    override fun onBindViewHolder(holder: CityViewHolder, position: Int) {
        val cityWithWeather = getItem(position)
        holder.bind(cityWithWeather)
    }

    class CityViewHolder(
        itemView: View,
        private val viewModel: FavoriteViewModel,
        private val onCityClicked: (CityEntity) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val cityNameTextView: TextView = itemView.findViewById(R.id.tvCityName)
        private val countryTextView: TextView = itemView.findViewById(R.id.tvCountry)
        private val temperatureTextView: TextView = itemView.findViewById(R.id.tvTemperature)
        private val weatherIconImageView: ImageView = itemView.findViewById(R.id.ivWeatherIcon)
        private val favoriteButton: ImageButton = itemView.findViewById(R.id.btnFavorite)

        fun bind(cityWithWeather: CityWithWeather) {
            val city = cityWithWeather.city
            val weather = cityWithWeather.weather

            cityNameTextView.text = city.name
            countryTextView.text = city.country
            temperatureTextView.text = weather?.let { "${it.temp.toInt()}°C" } ?: "N/A"

            weather?.weatherIcon?.let { iconCode ->
                val iconResId = mapWeatherIcon(iconCode)
                weatherIconImageView.setImageResource(iconResId)
            } ?: weatherIconImageView.setImageResource(R.drawable.ic_day_sunny)

            favoriteButton.setImageResource(R.drawable.ic_heart_filled)

            favoriteButton.setOnClickListener {
                viewModel.toggleFavorite(city)
            }

            itemView.setOnClickListener {
                onCityClicked(city)
            }
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

    class CityDiffCallback : DiffUtil.ItemCallback<CityWithWeather>() {
        override fun areItemsTheSame(oldItem: CityWithWeather, newItem: CityWithWeather): Boolean {
            return oldItem.city.id == newItem.city.id
        }

        override fun areContentsTheSame(oldItem: CityWithWeather, newItem: CityWithWeather): Boolean {
            return oldItem == newItem
        }
    }
}

class FavoriteViewModelFactory(
    private val repository: WeatherRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavoriteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FavoriteViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}