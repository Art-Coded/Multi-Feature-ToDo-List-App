package com.example.sweetbakes.utils

import android.app.TimePickerDialog
import android.content.Context
import androidx.activity.ComponentActivity
import java.util.*

fun showTimePicker(context: Context, onTimeSelected: (hour: Int, minute: Int) -> Unit) {
    if (context !is ComponentActivity) return

    val calendar = Calendar.getInstance()
    TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            onTimeSelected(hourOfDay, minute)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        false // 24-hour format
    ).show()
}