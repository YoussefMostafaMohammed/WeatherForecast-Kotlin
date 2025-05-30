package com.example.weatherforecast.model
import com.example.weatherforecast.db.WeatherLocalDataSource

import com.example.weatherforecast.model.CityEntity
import com.example.weatherforecast.model.CurrentWeatherEntity
import com.example.weatherforecast.model.WeatherRepositoryImpl
import com.example.weatherforecast.network.WeatherRemoteDataSource
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class WeatherRepositoryImplTest {

 private val remoteDataSource: WeatherRemoteDataSource = mockk(relaxUnitFun = true)
 private val localDataSource: WeatherLocalDataSource = mockk(relaxUnitFun = true)
 private val apiKey = "test_api_key"
 private val repository: WeatherRepositoryImpl = WeatherRepositoryImpl.getInstance(remoteDataSource, localDataSource, apiKey)

 @Test
 fun `saveCurrentWeather preserves existing favorite status when saving`() = runBlocking {
  // Given
  val cityEntity = CityEntity(
   id = 2643743,
   name = "London",
   coordLat = 51.5074,
   coordLon = -0.1278,
   country = "UK",
   population = 0,
   timezone = 3600,
   sunrise = 1629950000L,
   sunset = 1630000000L,
   isFavorite = false,
   isSearched = true
  )
  val existingCity = cityEntity.copy(isFavorite = true)
  val weatherEntity = CurrentWeatherEntity(
   cityId = 2643743,
   timestamp = 1630000000L,
   temp = 15.0,
   feelsLike = 14.5,
   tempMin = 14.0,
   tempMax = 16.0,
   pressure = 1012,
   humidity = 70,
   weatherMain = "Clear",
   weatherDescription = "clear sky",
   weatherIcon = "01d",
   windSpeed = 5.0,
   windDeg = 180,
   cloudiness = 0,
   visibility = 10000,
   sunrise = 1629950000L,
   sunset = 1630000000L
  )
  coEvery { localDataSource.getCityById(2643743) } returns existingCity

  // When
  repository.saveCurrentWeather(cityEntity, weatherEntity)

  // Then
  val expectedSavedCity = cityEntity.copy(isFavorite = true)
  coVerify { localDataSource.saveCity(expectedSavedCity) }
  coVerify { localDataSource.upsertCurrent(weatherEntity) }
  coVerify { localDataSource.getCityById(2643743) }
 }

 @Test
 fun `getFavoriteCities returns favorite cities from local data source`() = runBlocking {
  // Given
  val favoriteCities = listOf(
   CityEntity(
    id = 2643743,
    name = "London",
    coordLat = 51.5074,
    coordLon = -0.1278,
    country = "UK",
    population = 8961989,
    timezone = 3600,
    sunrise = 1629950000L,
    sunset = 1630000000L,
    isFavorite = true,
    isSearched = false
   ),
   CityEntity(
    id = 5128581,
    name = "New York",
    coordLat = 40.7128,
    coordLon = -74.0060,
    country = "US",
    population = 8419600,
    timezone = -14400,
    sunrise = 1629960000L,
    sunset = 1630010000L,
    isFavorite = true,
    isSearched = false
   )
  )
  coEvery { localDataSource.getFavoriteCities() } returns favoriteCities

  // When
  val result = repository.getFavoriteCities()

  // Then
  assertEquals(favoriteCities, result)
  coVerify { localDataSource.getFavoriteCities() }
 }
}