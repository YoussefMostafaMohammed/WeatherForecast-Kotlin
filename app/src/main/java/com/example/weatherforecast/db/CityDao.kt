package com.example.weatherforecast.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.weatherforecast.model.CityEntity

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