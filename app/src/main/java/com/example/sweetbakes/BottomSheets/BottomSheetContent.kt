package com.example.sweetbakes.content

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.TextFieldValue
import com.example.sweetbakes.model.OrdersItem
import com.example.sweetbakes.ui.BottomSheetUI
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BottomSheetContent(
    context: Context,
    localizedContext: Context,
    existingOrders: List<OrdersItem>,
    onOrderAdded: (OrdersItem) -> Unit
) {
    var title by remember { mutableStateOf(TextFieldValue("")) }
    var description by remember { mutableStateOf(TextFieldValue("")) }
    var selectedPriority by remember { mutableStateOf("Medium") }
    var dueDate by remember { mutableStateOf<String?>(null) }
    var reminderDate by remember { mutableStateOf<String?>(null) }
    var reminderTime by remember { mutableStateOf<String?>(null) }

    val isTitleDuplicate = existingOrders.any { it.title == title.text }

    BottomSheetUI(
        context = localizedContext,
        title = title,
        description = description,
        selectedPriority = selectedPriority,
        dueDate = dueDate?.let { "Due on: $it" },
        reminderDate = reminderDate,
        reminderTime = reminderTime,
        isTitleDuplicate = isTitleDuplicate,
        onTitleChange = { title = it },
        onDescriptionChange = { description = it },
        onPriorityChange = { selectedPriority = it },
        onDueDateClick = {
            if (context.isValidContext()) {
                showDatePicker(context) { selectedDate -> dueDate = selectedDate }
            }
        },
        onReminderClick = {
            if (context.isValidContext()) {
                showDatePicker(context) { date ->
                    if (context.isValidContext()) {
                        showTimePicker(context) { time ->
                            reminderDate = date
                            reminderTime = time
                        }
                    }
                }
            }
        },
        onDueDateClear = { dueDate = null },
        onReminderClear = {
            reminderDate = null
            reminderTime = null
        },
        onAddOrderClick = {
            val newOrder = OrdersItem(
                title = title.text,
                description = description.text,
                priority = selectedPriority,
                dueDate = dueDate ?: "No due date",
                reminders = if (reminderDate != null && reminderTime != null) {
                    "$reminderDate $reminderTime"
                } else ""
            )
            onOrderAdded(newOrder)
        }
    )
}

fun Context.isValidContext(): Boolean {
    return when (this) {
        is ComponentActivity -> !isFinishing && !isDestroyed
        else -> true
    }
}

fun showDatePicker(context: Context, onDateSelected: (String) -> Unit) {
    if (!context.isValidContext()) return

    val calendar = Calendar.getInstance()
    DatePickerDialog(
        context,
        { _, year, month, day ->
            val formattedDate = SimpleDateFormat("MMMM d, yyyy", context.resources.configuration.locale)
                .format(Calendar.getInstance().apply {
                    set(year, month, day)
                }.time)
            onDateSelected(formattedDate)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}

fun showTimePicker(context: Context, onTimeSelected: (String) -> Unit) {
    if (!context.isValidContext()) return

    val calendar = Calendar.getInstance()
    TimePickerDialog(
        context,
        { _, hour, minute ->
            val formattedTime = SimpleDateFormat("h:mm a", context.resources.configuration.locale)
                .format(Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                }.time)
            onTimeSelected(formattedTime)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        false
    ).show()
}