package com.example.weatherforecast.ui.setting

import android.app.Application
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.weatherforecast.R
import com.example.weatherforecast.databinding.FragmentSettingBinding
import com.google.android.material.snackbar.Snackbar

class SettingFragment : Fragment() {

    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SettingViewModel by viewModels { SettingViewModelFactory(requireActivity().application) }
    private var isRecreating = false

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
            switchUseGps.isChecked = viewModel.useGpsLocation.value ?: true
            switchUseGps.setOnCheckedChangeListener { _, isChecked ->
                viewModel.updateUseGpsLocation(isChecked)
            }

            fun setupSpinner(spinner: Spinner, entries: Array<String>, selectedIndex: Int, listener: AdapterView.OnItemSelectedListener) {
                val adapter = ArrayAdapter(requireContext(), R.layout.spinner_item, entries).apply {
                    setDropDownViewResource(R.layout.spinner_dropdown_item)
                }
                spinner.adapter = adapter
                spinner.setSelection(selectedIndex, false)
                spinner.onItemSelectedListener = listener
            }

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

            btnChooseFromMap.setOnClickListener {
                findNavController().navigate(R.id.action_global_mapPickerFragment)
            }

            fab.setOnClickListener {
                saveSettings()
            }
        }

        viewModel.text.observe(viewLifecycleOwner) { text ->
            Snackbar.make(binding.root, text, Snackbar.LENGTH_SHORT).show()
        }

        // Restore spinner states if available
        savedInstanceState?.let { bundle ->
            binding.spinnerTemperatureUnit.setSelection(bundle.getInt("tempUnitIndex", viewModel.getTemperatureUnitIndex()))
            binding.spinnerPressureUnit.setSelection(bundle.getInt("pressureUnitIndex", viewModel.getPressureUnitIndex()))
            binding.spinnerWindSpeedUnit.setSelection(bundle.getInt("windSpeedUnitIndex", viewModel.getWindSpeedUnitIndex()))
            binding.spinnerElevationUnit.setSelection(bundle.getInt("elevationUnitIndex", viewModel.getElevationUnitIndex()))
            binding.spinnerVisibilityUnit.setSelection(bundle.getInt("visibilityUnitIndex", viewModel.getVisibilityUnitIndex()))
            binding.spinnerLanguage.setSelection(bundle.getInt("languageIndex", viewModel.getLanguageIndex()))
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save spinner states
        outState.putInt("tempUnitIndex", binding.spinnerTemperatureUnit.selectedItemPosition)
        outState.putInt("pressureUnitIndex", binding.spinnerPressureUnit.selectedItemPosition)
        outState.putInt("windSpeedUnitIndex", binding.spinnerWindSpeedUnit.selectedItemPosition)
        outState.putInt("elevationUnitIndex", binding.spinnerElevationUnit.selectedItemPosition)
        outState.putInt("visibilityUnitIndex", binding.spinnerVisibilityUnit.selectedItemPosition)
        outState.putInt("languageIndex", binding.spinnerLanguage.selectedItemPosition)
    }

    private fun saveSettings() {
        if (isRecreating) return

        viewModel.saveSettings()
        setFragmentResult("settingsSaved", bundleOf("success" to true))
        val localeCode = viewModel.localeCode.value ?: ""
        val message = if (localeCode.isEmpty()) {
            "Settings saved, using system language"
        } else {
            "Settings saved: $localeCode"
        }
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
        isRecreating = true
        requireActivity().recreate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
class SettingViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}