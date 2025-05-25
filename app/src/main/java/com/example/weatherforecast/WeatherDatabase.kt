    package com.example.weatherforecast

    import androidx.room.Database

    import androidx.room.Room
    import androidx.room.RoomDatabase
    import android.content.Context
    import androidx.room.Dao
    import androidx.room.Insert
    import androidx.room.OnConflictStrategy
    import androidx.room.Query

    @Database(entities = [CityEntity::class, ForecastEntity::class, CurrentWeatherEntity::class], version = 1)
    abstract class WeatherDatabase : RoomDatabase() {
        abstract fun cityDao(): CityDao
        abstract fun forecastDao(): ForecastDao
        abstract fun currentWeatherDao(): CurrentWeatherDao
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

    @Dao
    interface CityDao {
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insert(city: CityEntity)

        @Query("SELECT * FROM city WHERE id = :cityId")
        suspend fun getCityById(cityId: Int): CityEntity?

        @Query("SELECT * FROM city WHERE name = :cityName")
        suspend fun getCityByName(cityName: String): CityEntity?

        @Query("SELECT * FROM city")
        suspend fun getAllCities(): List<CityEntity>

        @Query("SELECT * FROM city WHERE isFavorite = 1")
        suspend fun getFavoriteCities(): List<CityEntity>

        @Query("SELECT * FROM city WHERE isSearched = 1")
        suspend fun getSearchedCities(): List<CityEntity>

        @Query("DELETE FROM city WHERE id = :cityId")
        suspend fun deleteCityById(cityId: Int)

        @Query("DELETE FROM city")
        suspend fun deleteAllCities()
    }

    @Dao
    interface ForecastDao {
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insert(forecasts: List<ForecastEntity>)

        @Query("SELECT * FROM forecast WHERE cityId = :cityId")
        suspend fun getForecastsForCity(cityId: Int): List<ForecastEntity>

        @Query("DELETE FROM forecast WHERE cityId = :cityId")
        suspend fun deleteForecastsForCity(cityId: Int)

        @Query("DELETE FROM forecast")
        suspend fun deleteAllForecasts()

        @Query("SELECT * FROM forecast")
        suspend fun getAllForecasts(): List<ForecastEntity>
    }

    @Dao
    interface CurrentWeatherDao {
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun upsert(current: CurrentWeatherEntity)
        @Query("SELECT * FROM current_weather WHERE cityId = :cityId LIMIT 1")
        suspend fun getCurrentForCity(cityId: Int): CurrentWeatherEntity?
        @Query("DELETE FROM current_weather WHERE cityId = :cityId")
        suspend fun deleteCurrentForCity(cityId: Int)
        @Query("DELETE FROM current_weather")
        suspend fun deleteAllCurrents()
    }