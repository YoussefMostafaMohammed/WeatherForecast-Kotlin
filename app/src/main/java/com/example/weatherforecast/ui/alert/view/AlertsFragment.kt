package com.example.weatherforecast.ui.alert.view

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherforecast.AlarmService
import com.example.weatherforecast.R
import com.example.weatherforecast.databinding.FragmentAlertsBinding
import com.example.weatherforecast.db.AlarmLocalDataSourceImpl
import com.example.weatherforecast.db.WeatherDatabase
import com.example.weatherforecast.model.AlarmRepositoryImpl
import com.example.weatherforecast.ui.alert.view.AlarmAdapter
import com.example.weatherforecast.ui.alert.viewmodel.AlertsViewModelFactory
import com.example.weatherforecast.ui.alert.viewmodel.AlertsViewModel
import com.example.weatherforecast.ui.dialog.AddAlertDialogFragment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class AlertsFragment : Fragment() {

    private var _binding: FragmentAlertsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: AlertsViewModel
    private lateinit var adapter: AlarmAdapter

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(requireContext(), "Notification permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlertsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        // Start AlarmService
        AlarmService.Companion.startService(requireContext())

        // Initialize ViewModel with AlarmRepository
        val database = WeatherDatabase.Companion.getDatabase(requireActivity().application)
        val alarmLocalDataSource = AlarmLocalDataSourceImpl(database.alarmDao())
        val alarmRepository = AlarmRepositoryImpl.Companion.getInstance(alarmLocalDataSource)
        val factory = AlertsViewModelFactory(requireActivity().application, alarmRepository)
        viewModel = ViewModelProvider(this, factory)[AlertsViewModel::class.java]

        // Set up RecyclerView
        adapter = AlarmAdapter { id -> viewModel.deleteAlarm(id) }
        binding.rvAlarms.adapter = adapter
        binding.rvAlarms.layoutManager = LinearLayoutManager(requireContext())

        // Observe alarms
        viewModel.alarms.observe(viewLifecycleOwner) { alarms ->
            adapter.submitList(alarms)
        }

        // Set up FAB to show dialog
        binding.fabAddAlert.setOnClickListener {
            val dialog = AddAlertDialogFragment()
            dialog.show(childFragmentManager, "add_alert")
        }

        // Listen for new alert results
        childFragmentManager.setFragmentResultListener(
            "add_alert",
            viewLifecycleOwner
        ) { _, result ->
            val triggerTime = result.getLong("trigger_time")
            val type = result.getString("type") ?: "notification"
            val cityId = result.getInt("city_id", -1)
            if (cityId == -1) {
                Toast.makeText(requireContext(), R.string.no_city_selected, Toast.LENGTH_SHORT).show()
                return@setFragmentResultListener
            }
            val dateFormat = SimpleDateFormat("HH:mm 'on' dd/MM/yyyy", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("Europe/Kiev")
            }
            val formattedTime = dateFormat.format(Date(triggerTime))
            viewModel.addAlarm(triggerTime, type, cityId)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}