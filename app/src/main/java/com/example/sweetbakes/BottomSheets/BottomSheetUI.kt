package com.example.sweetbakes.ui

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sweetbakes.R

@Composable
fun BottomSheetUI(
    context: Context,
    title: TextFieldValue,
    description: TextFieldValue,
    selectedPriority: String,
    dueDate: String?,
    reminderDate: String?,
    reminderTime: String?,
    isTitleDuplicate: Boolean,
    onTitleChange: (TextFieldValue) -> Unit,
    onDescriptionChange: (TextFieldValue) -> Unit,
    onPriorityChange: (String) -> Unit,
    onDueDateClick: () -> Unit,
    onReminderClick: () -> Unit,
    onDueDateClear: () -> Unit,
    onReminderClear: () -> Unit,
    onAddOrderClick: () -> Unit
) {
    var showTitleError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = context.getString(R.string.add_new_order),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )

        OutlinedTextField(
            value = title,
            onValueChange = { newTitle ->
                showTitleError = false
                if (newTitle.text.length <= 18) {
                    onTitleChange(newTitle)
                }
            },
            label = { Text ("Title") },
            modifier = Modifier.fillMaxWidth(),
            isError = isTitleDuplicate || title.text.length > 18 || showTitleError,
            supportingText = {
                when {
                    isTitleDuplicate -> {
                        Text(
                            text = context.getString(R.string.title_exist),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    title.text.length > 18 -> {
                        Text(
                            text = context.getString(R.string.title_characters),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    showTitleError -> {
                        Text(
                            text = context.getString(R.string.title_empty),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    else -> {
                        Text(
                            text = "${title.text.length}/18",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        )

        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text(context.getString(R.string.description)) },
            modifier = Modifier.fillMaxWidth()
        )

        HorizontalDivider(thickness = 1.dp, color = Color.Gray)

        Text(
            text = context.getString(R.string.select_priority),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Column {
            PriorityCheckbox("Low", selectedPriority, onPriorityChange)
            PriorityCheckbox("Medium", selectedPriority, onPriorityChange)
            PriorityCheckbox("High", selectedPriority, onPriorityChange)
        }

        HorizontalDivider(thickness = 1.dp, color = Color.Gray)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onDueDateClick() }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Calendar Icon",
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = dueDate ?: context.getString(R.string.add_due_date),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                )
                if (dueDate != null) {
                    IconButton(onClick = onDueDateClear) {
                        Icon(Icons.Default.Close, contentDescription = "Clear Due Date")
                    }
                }
            }
        }

        HorizontalDivider(thickness = 1.dp, color = Color.Gray)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onReminderClick() }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Bell Icon",
                    modifier = Modifier.padding(end = 8.dp)
                )
                val reminderText = if (reminderDate != null && reminderTime != null) {
                    "Remind me at $reminderTime, $reminderDate"
                } else {
                    context.getString(R.string.order_remind_me)
                }

                Text(
                    text = reminderText,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                )
                if (reminderDate != null && reminderTime != null) {
                    IconButton(onClick = onReminderClear) {
                        Icon(Icons.Default.Close, contentDescription = "Clear Reminder")
                    }
                }
            }
        }

        HorizontalDivider(thickness = 1.dp, color = Color.Gray)

        Button(
            onClick = {
                if (title.text.isBlank()) {
                    showTitleError = true
                } else {
                    onAddOrderClick()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isTitleDuplicate && title.text.length <= 18
        ) {
            Text(text = "Add Order", color = Color.White)
        }
    }
}

@Composable
fun PriorityCheckbox(priority: String, selectedPriority: String, onClick: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = priority == selectedPriority,
            onCheckedChange = { if (it) onClick(priority) }
        )
        Text(text = priority, style = MaterialTheme.typography.bodyLarge)
    }
}