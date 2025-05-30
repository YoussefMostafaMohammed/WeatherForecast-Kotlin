package com.example.weatherforecast.ui.alert.view

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherforecast.databinding.ItemAlarmBinding
import com.example.weatherforecast.model.AlarmEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

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