package com.example.weatherforecast.ui.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.weatherforecast.R
import com.example.weatherforecast.databinding.FragmentSettingBinding
import com.google.android.material.snackbar.Snackbar

class SettingFragment : Fragment() {

    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SettingViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            // Initialize Use GPS Location Switch
            switchUseGps.isChecked = viewModel.useGpsLocation.value ?: true
            switchUseGps.setOnCheckedChangeListener { _, isChecked ->
                viewModel.updateUseGpsLocation(isChecked)
            }

            // Helper function to set up Spinner adapters
            fun setupSpinner(spinner: Spinner, entries: Array<String>, selectedIndex: Int, listener: AdapterView.OnItemSelectedListener) {
                val adapter = ArrayAdapter(requireContext(), R.layout.spinner_item, entries).apply {
                    setDropDownViewResource(R.layout.spinner_dropdown_item)
                }
                spinner.adapter = adapter
                spinner.post {
                    spinner.setSelection(selectedIndex)
                    spinner.onItemSelectedListener = listener
                }
            }

            // Initialize Spinners with ViewModel data
            setupSpinner(
                spinnerTemperatureUnit,
                resources.getStringArray(R.array.temperature_units),
                viewModel.getTemperatureUnitIndex(),
                viewModel.getTemperatureUnitListener()
            )

            setupSpinner(
                spinnerPressureUnit,
                resources.getStringArray(R.array.pressure_units),
                viewModel.getPressureUnitIndex(),
                viewModel.getPressureUnitListener()
            )

            setupSpinner(
                spinnerWindSpeedUnit,
                resources.getStringArray(R.array.wind_speed_units),
                viewModel.getWindSpeedUnitIndex(),
                viewModel.getWindSpeedUnitListener()
            )

            setupSpinner(
                spinnerElevationUnit,
                resources.getStringArray(R.array.elevation_units),
                viewModel.getElevationUnitIndex(),
                viewModel.getElevationUnitListener()
            )

            setupSpinner(
                spinnerVisibilityUnit,
                resources.getStringArray(R.array.visibility_units),
                viewModel.getVisibilityUnitIndex(),
                viewModel.getVisibilityUnitListener()
            )

            setupSpinner(
                spinnerLanguage,
                resources.getStringArray(R.array.languages),
                viewModel.getLanguageIndex(),
                viewModel.getLanguageListener()
            )

            // Choose from Map Button
            btnChooseFromMap.setOnClickListener {
                findNavController().navigate(R.id.action_global_mapPickerFragment)
            }

            // Floating Action Button (Save)
            fab.setOnClickListener {
                saveSettings()
            }
        }

        // Observe ViewModel changes for UI updates
        viewModel.text.observe(viewLifecycleOwner) { text ->
            Snackbar.make(binding.root, text, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun saveSettings() {
        // Example: Collect settings from UI and update ViewModel
        val useGps = binding.switchUseGps.isChecked
        val tempUnit = binding.spinnerTemperatureUnit.selectedItem.toString()
        val pressureUnit = binding.spinnerPressureUnit.selectedItem.toString()
        val windSpeedUnit = binding.spinnerWindSpeedUnit.selectedItem.toString()
        val elevationUnit = binding.spinnerElevationUnit.selectedItem.toString()
        val visibilityUnit = binding.spinnerVisibilityUnit.selectedItem.toString()
        val language = binding.spinnerLanguage.selectedItem.toString()

        viewModel.saveSettings(
            useGps,
            tempUnit,
            pressureUnit,
            windSpeedUnit,
            elevationUnit,
            visibilityUnit,
            language
        )
        setFragmentResult("settingsSaved", bundleOf("success" to true))
        Snackbar.make(binding.root, "Settings saved", Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}