package com.example.sweetbakes.ui

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.sweetbakes.R
import com.example.sweetbakes.utils.showTimePicker
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToDoScreen(
    activityContext: Context,
    localizedContext: Context,
    onBackClick: () -> Unit,
    title: String,
    onTitleChange: (String) -> Unit,
    isTitleError: Boolean,
    isTitleDuplicate: Boolean,
    description: String,
    onDescriptionChange: (String) -> Unit,
    time: String,
    onTimeChange: (String) -> Unit,
    onSave: () -> Unit,
    editingItem: ToDoItem?
) {
    val scrollState = rememberScrollState()
    val currentLocale = LocalContext.current.resources.configuration.locales[0] ?: Locale.getDefault()

    val showDuplicateError by derivedStateOf {
        isTitleDuplicate && title.isNotEmpty() && !isTitleError
    }

    val isSaveEnabled by derivedStateOf {
        title.isNotEmpty() && !isTitleError && !isTitleDuplicate
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(if (editingItem != null) stringResource(R.string.edit_todo) else stringResource(R.string.add_todo))
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        CompositionLocalProvider(LocalContext provides localizedContext) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = if (editingItem != null) stringResource(R.string.edit_your_todo) else stringResource(R.string.create_new_todo),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { newTitle ->
                        if (newTitle.length <= 18) {
                            onTitleChange(newTitle)
                        }
                    },
                    label = { Text("Title *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = showDuplicateError,
                    supportingText = {
                        when {
                            showDuplicateError -> {
                                Text(
                                    stringResource(R.string.title_exist),
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            else -> {
                                Text(
                                    "${title.length}/18",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    label = { Text(stringResource(R.string.description)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp),
                    maxLines = 5
                )

                Spacer(modifier = Modifier.height(24.dp))

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.reminder_time),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedButton(
                        onClick = {
                            showTimePicker(activityContext) { hourOfDay, minute ->
                                val calendar = Calendar.getInstance().apply {
                                    set(Calendar.HOUR_OF_DAY, hourOfDay)
                                    set(Calendar.MINUTE, minute)
                                }
                                val timeFormat = SimpleDateFormat("h:mm a", currentLocale)
                                onTimeChange(timeFormat.format(calendar.time))
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (time.isNotEmpty()) time else stringResource(R.string.select_time),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (title.isNotEmpty()) {
                                onSave()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        enabled = isSaveEnabled,
                        colors = if (isSaveEnabled) {
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            )
                        }
                    ) {
                        Text(
                            text = "Save",
                            color = Color.White,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}