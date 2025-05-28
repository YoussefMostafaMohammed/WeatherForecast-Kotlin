package com.example.weatherforecast

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.NumberPicker
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import java.text.SimpleDateFormat
import java.util.*

class AddAlertDialogFragment : DialogFragment() {

    private var selectedCityId: Int? = null
    private var selectedCityName: String? = null
    private var selectedDate: Calendar = Calendar.getInstance()

    companion object {
        const val REQUEST_CODE_SELECT_CITY = 1001
        private const val DEFAULT_CITY_ID = 361320
        private const val DEFAULT_CITY_NAME = "Al Fayyum"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_add_alert, container, false)

        val npHours = view.findViewById<NumberPicker>(R.id.npHours)
        val npMinutes = view.findViewById<NumberPicker>(R.id.npMinutes)
        val rgType = view.findViewById<RadioGroup>(R.id.rgType)
        val btnConfirm = view.findViewById<Button>(R.id.btnConfirm)
        val tvSelectedCity = view.findViewById<TextView>(R.id.tvSelectedCity)
        val btnSelectCity = view.findViewById<Button>(R.id.btnSelectCity)
        val tvSelectedDate = view.findViewById<TextView>(R.id.tvSelectedDate)
        val btnSelectDate = view.findViewById<Button>(R.id.btnSelectDate)

        // Load city from SharedPreferences
        val prefs = requireActivity().getSharedPreferences("weather_prefs", Context.MODE_PRIVATE)
        selectedCityId = prefs.getInt("current_city_id", -1)
        selectedCityName = prefs.getString("current_city_name", null)
        if (selectedCityId == -1) {
            selectedCityId = DEFAULT_CITY_ID
            selectedCityName = DEFAULT_CITY_NAME
        }
        tvSelectedCity.text = getString(R.string.selected_city, selectedCityName ?: DEFAULT_CITY_NAME)
        Log.d("AddAlertDialogFragment", "Initial cityId=$selectedCityId, cityName=$selectedCityName")

        // Initialize NumberPickers with current time
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Kiev"))
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        npHours.minValue = 0
        npHours.maxValue = 23
        npHours.value = currentHour
        npHours.wrapSelectorWheel = true

        npMinutes.minValue = 0
        npMinutes.maxValue = 59
        npMinutes.value = currentMinute
        npMinutes.wrapSelectorWheel = true

        // Set initial selected date
        tvSelectedDate.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedDate.time)

        btnSelectDate.setOnClickListener {
            val year = selectedDate.get(Calendar.YEAR)
            val month = selectedDate.get(Calendar.MONTH)
            val day = selectedDate.get(Calendar.DAY_OF_MONTH)
            DatePickerDialog(
                requireContext(),
                { _, selectedYear, selectedMonth, selectedDay ->
                    selectedDate.set(selectedYear, selectedMonth, selectedDay)
                    tvSelectedDate.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedDate.time)
                    Log.d("AddAlertDialogFragment", "Selected date: ${selectedDate.time}")
                },
                year,
                month,
                day
            ).show()
        }

        btnSelectCity.setOnClickListener {
            val intent = Intent(requireContext(), MapPickerActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_SELECT_CITY)
            Log.d("AddAlertDialogFragment", "Started MapPickerActivity")
        }

        btnConfirm.setOnClickListener {
            val hours = npHours.value
            val minutes = npMinutes.value
            Log.d("AddAlertDialogFragment", "Confirm clicked: hours=$hours, minutes=$minutes, date=${selectedDate.time}, cityId=$selectedCityId")

            if (selectedCityId == null || selectedCityId == -1) {
                Toast.makeText(requireContext(), "Please select a valid city", Toast.LENGTH_SHORT).show()
                Log.w("AddAlertDialogFragment", "Invalid cityId=$selectedCityId")
                return@setOnClickListener
            }

            val type = if (rgType.checkedRadioButtonId == R.id.rbNotification) "notification" else "alarm"

            // Calculate trigger time
            val triggerCalendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Kiev"))
            triggerCalendar.set(Calendar.YEAR, selectedDate.get(Calendar.YEAR))
            triggerCalendar.set(Calendar.MONTH, selectedDate.get(Calendar.MONTH))
            triggerCalendar.set(Calendar.DAY_OF_MONTH, selectedDate.get(Calendar.DAY_OF_MONTH))
            triggerCalendar.set(Calendar.HOUR_OF_DAY, hours)
            triggerCalendar.set(Calendar.MINUTE, minutes)
            triggerCalendar.set(Calendar.SECOND, 0)
            triggerCalendar.set(Calendar.MILLISECOND, 0)
            val triggerTime = triggerCalendar.timeInMillis

            if (triggerTime <= System.currentTimeMillis()) {
                Toast.makeText(requireContext(), "Please select a future time", Toast.LENGTH_SHORT).show()
                Log.w("AddAlertDialogFragment", "Trigger time is in the past: $triggerTime")
                return@setOnClickListener
            }

            val dateFormat = SimpleDateFormat("HH:mm 'on' dd/MM/yyyy", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("Europe/Kiev")
            }
            Log.d("AddAlertDialogFragment", "Saving alarm: triggerTime=$triggerTime, formatted=${dateFormat.format(Date(triggerTime))}, type=$type, cityId=$selectedCityId")

            val result = Bundle().apply {
                putLong("trigger_time", triggerTime)
                putString("type", type)
                putInt("city_id", selectedCityId!!)
            }
            parentFragmentManager.setFragmentResult("add_alert", result)
            dismiss()
        }

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("AddAlertDialogFragment", "onActivityResult: requestCode=$requestCode, resultCode=$resultCode, data=$data")
        if (requestCode == REQUEST_CODE_SELECT_CITY && resultCode == Activity.RESULT_OK) {
            selectedCityId = data?.getIntExtra("city_id", -1)
            selectedCityName = data?.getStringExtra("city_name")
            if (selectedCityId == -1) {
                Log.w("AddAlertDialogFragment", "MapPickerActivity returned invalid cityId=-1")
                Toast.makeText(requireContext(), "Invalid city selected", Toast.LENGTH_SHORT).show()
            } else {
                Log.d("AddAlertDialogFragment", "Selected city: id=$selectedCityId, name=$selectedCityName")
                view?.findViewById<TextView>(R.id.tvSelectedCity)?.text = getString(R.string.selected_city, selectedCityName ?: DEFAULT_CITY_NAME)
            }
        }
    }
}