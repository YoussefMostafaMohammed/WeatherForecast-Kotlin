package com.example.weatherforecast.ui.favorite.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherforecast.R
import com.example.weatherforecast.model.CityEntity
import com.example.weatherforecast.model.CityWithWeather
import com.example.weatherforecast.ui.favorite.viewmodel.FavoriteViewModel

class FavoriteCityAdapter(
    private val viewModel: FavoriteViewModel,
    private val onCityClicked: (CityEntity) -> Unit
) : ListAdapter<CityWithWeather, FavoriteCityAdapter.CityViewHolder>(CityDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorite_city, parent, false)
        return CityViewHolder(view, viewModel, onCityClicked)
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