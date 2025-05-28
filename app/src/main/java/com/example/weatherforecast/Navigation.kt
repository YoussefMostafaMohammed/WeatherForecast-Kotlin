package com.example.weatherforecast

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.weatherforecast.databinding.ActivityNavigationBinding
import com.google.android.material.button.MaterialButton
import java.util.*

class Navigation : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityNavigationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        applySavedLocale()
        super.onCreate(savedInstanceState)
        binding = ActivityNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.appBarNavigation.toolbar)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        // Create notification channel for weather alerts
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "weather_alerts",
                "Weather Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for weather alert notifications"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        val navHost = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_content_navigation)
                as NavHostFragment
        val navController = navHost.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.nav_home, R.id.nav_favorite, R.id.nav_setting),
            binding.drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)
    }

    private fun applySavedLocale() {
        val prefs = getSharedPreferences("weather_prefs", Context.MODE_PRIVATE)
        val localeCode = prefs.getString("locale_code", "") ?: ""
        val locale = if (localeCode.isNotEmpty()) {
            Locale(localeCode)
        } else {
            val systemLocales = resources.configuration.locales
            if (!systemLocales.isEmpty) systemLocales[0] else Locale.getDefault()
        }

        Locale.setDefault(locale)
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        applicationContext.createConfigurationContext(config)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        applySavedLocale()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        val mapMenuItem = menu.findItem(R.id.action_map)
        val mapButton = mapMenuItem.actionView
            ?.findViewById<MaterialButton>(R.id.btn_map)
        mapButton?.setOnClickListener {
            val bundle = Bundle().apply {
                putBoolean("is_city_selection_mode", false)
            }
            (supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment_content_navigation)
                    as NavHostFragment)
                .navController
                .navigate(R.id.action_global_mapPickerFragment, bundle)
        }
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHost = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_content_navigation)
                as NavHostFragment
        val navController = navHost.navController
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}