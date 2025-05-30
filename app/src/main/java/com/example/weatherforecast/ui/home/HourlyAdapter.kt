package com.example.weatherforecast.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherforecast.R
import com.example.weatherforecast.model.HourlyItem

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
        private val prefs = view.context.getSharedPreferences("weather_prefs", android.content.Context.MODE_PRIVATE)

        fun bind(item: HourlyItem) {
            val isArabic = prefs.getString("locale_code", "") == "ar"
            tvTime.textDirection = if (isArabic) View.TEXT_DIRECTION_RTL else View.TEXT_DIRECTION_LTR
            tvTemp.textDirection = if (isArabic) View.TEXT_DIRECTION_RTL else View.TEXT_DIRECTION_LTR
            tvTime.textAlignment = if (isArabic) View.TEXT_ALIGNMENT_TEXT_END else View.TEXT_ALIGNMENT_TEXT_START
            tvTemp.textAlignment = if (isArabic) View.TEXT_ALIGNMENT_TEXT_END else View.TEXT_ALIGNMENT_TEXT_START

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