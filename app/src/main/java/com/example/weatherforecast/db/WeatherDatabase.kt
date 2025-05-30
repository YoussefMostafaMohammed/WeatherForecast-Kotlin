    package com.example.weatherforecast.db

    import androidx.room.Database

    import androidx.room.Room
    import androidx.room.RoomDatabase
    import android.content.Context
    import androidx.lifecycle.LiveData
    import androidx.room.Dao
    import androidx.room.Delete
    import androidx.room.Insert
    import androidx.room.OnConflictStrategy
    import androidx.room.Query
    import com.example.weatherforecast.model.AlarmEntity
    import com.example.weatherforecast.model.CityEntity
    import com.example.weatherforecast.model.CurrentWeatherEntity
    import com.example.weatherforecast.model.ForecastEntity

    @Database(entities = [CityEntity::class, ForecastEntity::class, CurrentWeatherEntity::class, AlarmEntity::class], version = 1)
    abstract class WeatherDatabase : RoomDatabase() {
        abstract fun cityDao(): CityDao
        abstract fun forecastDao(): ForecastDao
        abstract fun currentWeatherDao(): CurrentWeatherDao
        abstract fun alarmDao(): AlarmDao

        companion object {
            @Volatile
            private var INSTANCE: WeatherDatabase? = null
            fun getDatabase(context: Context): WeatherDatabase {
                return INSTANCE ?: synchronized(this) {
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        WeatherDatabase::class.java,
                        "weather_database"
                    ).build()
                    INSTANCE = instance
                    instance
                }
            }
        }
    }
