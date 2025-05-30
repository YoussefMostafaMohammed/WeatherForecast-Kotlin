package com.example.weatherforecast.ui.favorite.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherforecast.R
import com.example.weatherforecast.databinding.FragmentFavoriteBinding
import com.example.weatherforecast.db.WeatherDatabase
import com.example.weatherforecast.db.WeatherLocalDataSourceImpl
import com.example.weatherforecast.model.WeatherRepositoryImpl
import com.example.weatherforecast.network.RetrofitClient
import com.example.weatherforecast.network.WeatherRemoteDataSourceImpl
import com.example.weatherforecast.ui.favorite.viewmodel.FavoriteViewModel
import com.example.weatherforecast.ui.favorite.viewmodel.FavoriteViewModelFactory

class FavoriteFragment : Fragment() {

    private var _binding: FragmentFavoriteBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: FavoriteViewModel
    private lateinit var adapter: FavoriteCityAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val database = WeatherDatabase.Companion.getDatabase(requireContext())
        val localDataSource = WeatherLocalDataSourceImpl(database)
        val remoteDataSource =
            WeatherRemoteDataSourceImpl(RetrofitClient.Companion.getInstance().apiService)
        val repository = WeatherRepositoryImpl.Companion.getInstance(
            remoteDataSource,
            localDataSource,
            "897f05d7107c1a4583eb10de82e05435"
        )

        val factory = FavoriteViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[FavoriteViewModel::class.java]

        adapter = FavoriteCityAdapter(viewModel) { city ->
            val bundle = Bundle().apply {
                putInt("cityId", city.id)  // The argument name should match what HomeFragment expects
            }
            findNavController().navigate(R.id.nav_home, bundle)
        }

        binding.rvFavorites.layoutManager = LinearLayoutManager(context)
        binding.rvFavorites.adapter = adapter

        viewModel.favoriteCities.observe(viewLifecycleOwner) { cities ->
            adapter.submitList(cities)
        }

        viewModel.toastEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadFavoriteCities()
    }
}