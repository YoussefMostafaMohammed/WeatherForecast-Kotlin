package com.example.weatherforecast.ui.alarm

import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.media.RingtoneManager
import com.bumptech.glide.Glide
import com.example.weatherforecast.model.AlarmRepositoryImpl
import com.example.weatherforecast.AlarmService
import com.example.weatherforecast.BuildConfig
import com.example.weatherforecast.R
import com.example.weatherforecast.db.WeatherDatabase
import com.example.weatherforecast.model.WeatherRepositoryImpl
import com.example.weatherforecast.db.AlarmLocalDataSourceImpl
import com.example.weatherforecast.db.WeatherLocalDataSourceImpl
import com.example.weatherforecast.network.RetrofitClient
import com.example.weatherforecast.network.WeatherRemoteDataSourceImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AlarmActivity : AppCompatActivity() {
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var alarmRepo: AlarmRepositoryImpl
    private lateinit var weatherRepo: WeatherRepositoryImpl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var alarmId = intent.getIntExtra("alarm_id", 0)
        var cityId = intent.getIntExtra("city_id", -1)
        Log.d("AlarmActivity", "onCreate: alarmId=$alarmId, cityId=$cityId")

        setContentView(R.layout.activity_alarm)
        window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
        window.setBackgroundDrawableResource(android.R.color.transparent)
        window.setDimAmount(0.6f)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        val db = WeatherDatabase.getDatabase(this)
        alarmRepo = AlarmRepositoryImpl.getInstance(AlarmLocalDataSourceImpl(db.alarmDao()))
        weatherRepo = WeatherRepositoryImpl.getInstance(
            WeatherRemoteDataSourceImpl(RetrofitClient.getInstance().apiService),
            WeatherLocalDataSourceImpl(db),
            BuildConfig.WEATHER_API_KEY
        )

        CoroutineScope(Dispatchers.IO).launch {
            if (cityId == -1 && alarmId != 0) {
                try {
                    val alarm = alarmRepo.getAllAlarms().value?.find { it.id == alarmId }
                    if (alarm != null && alarm.cityId != -1) {
                        cityId = alarm.cityId
                        Log.d("AlarmActivity", "Fetched cityId=$cityId from AlarmEntity for alarmId=$alarmId")
                    } else {
                        Log.w("AlarmActivity", "No valid cityId in AlarmEntity for alarmId=$alarmId")
                    }
                } catch (e: Exception) {
                    Log.e("AlarmActivity", "Error fetching AlarmEntity", e)
                }
            }

            // Show toast if cityId is still invalid
            if (cityId == -1) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AlarmActivity, "Invalid city for alarm", Toast.LENGTH_LONG).show()
                }
            }

            // Fetch & display weather
            try {
                val city = weatherRepo.getCityById(cityId)
                val weather = weatherRepo.getCurrentWeatherForCity(cityId)
                withContext(Dispatchers.Main) {
                    findViewById<TextView>(R.id.tvCity).text = city?.name ?: "Unknown City"
                    if (weather != null) {
                        findViewById<TextView>(R.id.tvTemperature).text = getString(R.string.temperature_format, weather.temp)
                        findViewById<TextView>(R.id.tvWeatherDescription).text = weather.weatherDescription.replaceFirstChar { it.uppercase() }
                        Glide.with(this@AlarmActivity)
                            .load("http://openweathermap.org/img/wn/${weather.weatherIcon}@2x.png")
                            .placeholder(R.drawable.ic_notification)
                            .error(R.drawable.ic_notification)
                            .into(findViewById<ImageView>(R.id.ivWeatherIcon))
                        Log.d("AlarmActivity", "Loaded weather for cityId=$cityId: temp=${weather.temp}, desc=${weather.weatherDescription}, icon=${weather.weatherIcon}")
                    } else {
                        findViewById<TextView>(R.id.tvTemperature).text = "N/A"
                        findViewById<TextView>(R.id.tvWeatherDescription).text = "No data"
                        findViewById<ImageView>(R.id.ivWeatherIcon).setImageResource(R.drawable.ic_notification)
                        Log.w("AlarmActivity", "No weather data for cityId=$cityId")
                    }
                }
            } catch (e: Exception) {
                Log.e("AlarmActivity", "Error loading weather", e)
                withContext(Dispatchers.Main) {
                    findViewById<TextView>(R.id.tvCity).text = "Unknown City"
                    findViewById<TextView>(R.id.tvTemperature).text = "N/A"
                    findViewById<TextView>(R.id.tvWeatherDescription).text = "Error"
                    findViewById<ImageView>(R.id.ivWeatherIcon).setImageResource(R.drawable.ic_notification)
                }
            }
        }

        // Play alarm sound
        mediaPlayer = MediaPlayer.create(this, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)).apply {
            isLooping = true
            try {
                start()
                Log.d("AlarmActivity", "MediaPlayer started for alarmId=$alarmId")
            } catch (e: Exception) {
                Log.e("AlarmActivity", "Failed to start MediaPlayer", e)
            }
        }

        findViewById<Button>(R.id.btnSnooze).setOnClickListener {
            mediaPlayer.stop()
            val svc = Intent(this, AlarmService::class.java).apply {
                action = AlarmService.ACTION_SNOOZE_ALARM
                putExtra("alarm_id", alarmId)
                putExtra("city_id", cityId)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                startForegroundService(svc)
            else
                startService(svc)
            Log.d("AlarmActivity", "Snooze clicked for alarmId=$alarmId, cityId=$cityId")
            finish()
        }
        findViewById<Button>(R.id.btnDismiss).setOnClickListener {
            mediaPlayer.stop()
            val svc = Intent(this, AlarmService::class.java).apply {
                action = AlarmService.ACTION_DELETE_ALARM
                putExtra("alarm_id", alarmId)
                putExtra("city_id", cityId)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                startForegroundService(svc)
            else
                startService(svc)
            Log.d("AlarmActivity", "Dismiss clicked for alarmId=$alarmId, cityId=$cityId")
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
            mediaPlayer.stop()
            mediaPlayer.release()
        }
        Log.d("AlarmActivity", "Destroyed")
    }
}