package com.example.weatherforecast.ui.favorite

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.example.weatherforecast.model.CityEntity
import com.example.weatherforecast.model.CityWithWeather
import com.example.weatherforecast.model.CurrentWeatherEntity
import com.example.weatherforecast.model.WeatherRepository
import com.example.weatherforecast.ui.favorite.viewmodel.FavoriteViewModel
import com.example.weatherforecast.utils.Event
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.*

@ExperimentalCoroutinesApi
class FavoriteViewModelTest {

 @get:Rule
 val instantTaskExecutorRule = InstantTaskExecutorRule()

 private lateinit var viewModel: FavoriteViewModel
 private val repository: WeatherRepository = mockk(relaxed = true)

 private val testDispatcher = StandardTestDispatcher()

 @Before
 fun setup() {
  Dispatchers.setMain(testDispatcher)
  viewModel = FavoriteViewModel(repository)
 }

 @After
 fun tearDown() {
  Dispatchers.resetMain()
 }

 @Test
 fun `loadFavoriteCities - success`() = runTest {
  // Given
  val city = CityEntity(id = 1, name = "Cairo", coordLat = 30.0, coordLon = 31.0, country = "EG",
   population = 0, timezone = 0, sunrise = 0L, sunset = 0L, isFavorite = true, isSearched = false)
  val weather = CurrentWeatherEntity(cityId = 1, timestamp = 0L, temp = 25.0, feelsLike = 25.0,
   tempMin = 24.0, tempMax = 26.0, pressure = 1010, humidity = 60, weatherMain = "Clear",
   weatherDescription = "clear", weatherIcon = "01d", windSpeed = 5.0, windDeg = 90,
   cloudiness = 0, visibility = 10000, sunrise = 0L, sunset = 0L)

  coEvery { repository.getFavoriteCities() } returns listOf(city)
  coEvery { repository.getCurrentWeatherForCity(city.id) } returns weather

  // When
  viewModel.loadFavoriteCities()
  advanceUntilIdle()

  // Then
  val result = viewModel.favoriteCities.value
  Assert.assertEquals(1, result?.size)
  Assert.assertEquals(city, result?.get(0)?.city)
  Assert.assertEquals(weather, result?.get(0)?.weather)
 }

 @Test
 fun `loadFavoriteCities - failure sets toastEvent`() = runTest {
  // Given
  coEvery { repository.getFavoriteCities() } throws RuntimeException("Error")

  // Observer for toast event
  val toastObserver = mockk<Observer<Event<String>>>(relaxed = true)
  viewModel.toastEvent.observeForever(toastObserver)

  // When
  viewModel.loadFavoriteCities()
  advanceUntilIdle()

  // Then
  val value = viewModel.favoriteCities.value
  Assert.assertTrue(value?.isEmpty() == true)
  verify { toastObserver.onChanged(match { it.peekContent() == "Failed to load favorite cities" }) }
 }

 @Test
 fun `toggleFavorite calls update and reloads`() = runTest {
  // Given
  val city = CityEntity(id = 1, name = "Cairo", coordLat = 30.0, coordLon = 31.0, country = "EG",
   population = 0, timezone = 0, sunrise = 0L, sunset = 0L, isFavorite = true, isSearched = false)
  coEvery { repository.updateCityFavoriteStatus(any()) } just Runs
  coEvery { repository.getFavoriteCities() } returns emptyList()
  coEvery { repository.getCurrentWeatherForCity(any()) } returns mockk()

  val toastObserver = mockk<Observer<Event<String>>>(relaxed = true)
  viewModel.toastEvent.observeForever(toastObserver)

  // When
  viewModel.toggleFavorite(city)
  advanceUntilIdle()

  // Then
  coVerify { repository.updateCityFavoriteStatus(city.copy(isFavorite = false)) }
  verify { toastObserver.onChanged(match { it.peekContent() == "Removed from favorites" }) }
 }
}
