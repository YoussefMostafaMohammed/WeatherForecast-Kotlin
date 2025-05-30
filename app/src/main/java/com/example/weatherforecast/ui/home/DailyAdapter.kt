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
import com.example.weatherforecast.model.DailyItem
import java.text.SimpleDateFormat
import java.util.*

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
        private val prefs = view.context.getSharedPreferences("weather_prefs", android.content.Context.MODE_PRIVATE)

        private val inputFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        private val shortDayFormat = SimpleDateFormat("EEE", if (prefs.getString("locale_code", "") == "ar") Locale("ar") else Locale.getDefault())

        fun bind(item: DailyItem) {
            val isArabic = prefs.getString("locale_code", "") == "ar"
            tvDate.textDirection = if (isArabic) View.TEXT_DIRECTION_RTL else View.TEXT_DIRECTION_LTR
            tvDailyTemp.textDirection = if (isArabic) View.TEXT_DIRECTION_RTL else View.TEXT_DIRECTION_LTR
            tvDate.textAlignment = if (isArabic) View.TEXT_ALIGNMENT_TEXT_END else View.TEXT_ALIGNMENT_TEXT_START
            tvDailyTemp.textAlignment = if (isArabic) View.TEXT_ALIGNMENT_TEXT_END else View.TEXT_ALIGNMENT_TEXT_START

            val parsedDate: Date? = try {
                inputFormat.parse(item.date)
            } catch (e: Exception) {
                null
            }

            val dayAbbrev: String = parsedDate?.let { shortDayFormat.format(it) } ?: ""
            val displayDate = when (dayAbbrev) {
                "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat",
                "الأحد", "الإثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت" -> "$dayAbbrev, ${item.date}"
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