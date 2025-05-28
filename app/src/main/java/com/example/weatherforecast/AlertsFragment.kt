package com.example.weatherforecast

import android.Manifest
import android.app.Application
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherforecast.locale.AlarmLocalDataSourceImpl
import com.example.weatherforecast.databinding.FragmentAlertsBinding
import com.example.weatherforecast.databinding.ItemAlarmBinding
import java.text.SimpleDateFormat
import java.util.*

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
        AlarmService.startService(requireContext())

        // Initialize ViewModel with AlarmRepository
        val database = WeatherDatabase.getDatabase(requireActivity().application)
        val alarmLocalDataSource = AlarmLocalDataSourceImpl(database.alarmDao())
        val alarmRepository = AlarmRepositoryImpl.getInstance(alarmLocalDataSource)
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
            Log.d("AlertsFragment", "Received alarm: triggerTime=$triggerTime, formatted=${dateFormat.format(Date(triggerTime))}, type=$type, cityId=$cityId")
            viewModel.addAlarm(triggerTime, type, cityId)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class AlarmAdapter(private val onDelete: (Int) -> Unit) :
    ListAdapter<AlarmEntity, AlarmAdapter.AlarmViewHolder>(AlarmDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val binding = ItemAlarmBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AlarmViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AlarmViewHolder(private val binding: ItemAlarmBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(alarm: AlarmEntity) {
            // Format trigger time in EEST
            val dateFormat = SimpleDateFormat("HH:mm 'on' dd/MM/yyyy", Locale.getDefault())
            dateFormat.timeZone = TimeZone.getTimeZone("Europe/Kiev") // EEST
            val formattedTime = dateFormat.format(Date(alarm.triggerTime))
            Log.d("AlarmAdapter", "Displaying alarm id=${alarm.id}, triggerTime=${alarm.triggerTime}, formatted=$formattedTime")
            binding.tvAlarmTime.text = formattedTime
            binding.btnDeleteAlarm.setOnClickListener {
                onDelete(alarm.id)
            }
        }
    }

    class AlarmDiffCallback : DiffUtil.ItemCallback<AlarmEntity>() {
        override fun areItemsTheSame(oldItem: AlarmEntity, newItem: AlarmEntity) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: AlarmEntity, newItem: AlarmEntity) = oldItem == newItem
    }
}

class AlertsViewModelFactory(
    private val application: Application,
    private val repository: AlarmRepositoryImpl
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AlertsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AlertsViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
