package com.example.sweetbakes.content

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.sweetbakes.data.AppDatabase
import com.example.sweetbakes.model.ToDoItemEntity
import com.example.sweetbakes.ui.ToDoItem
import com.example.sweetbakes.ToDo.NotificationUtils
import com.example.sweetbakes.ToDo.ReminderWorker
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class ToDoViewModel : ViewModel() {
    var todoList by mutableStateOf(emptyList<ToDoItem>())
    var checkedItems by mutableStateOf(mapOf<ToDoItem, Boolean>())

    // Store date in neutral format (yyyy-MM-dd) for consistency
    private val neutralDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    var selectedDate by mutableStateOf(neutralDateFormat.format(Date()))

    var title by mutableStateOf("")
    var description by mutableStateOf("")
    var time by mutableStateOf("")

    var editingItem by mutableStateOf<ToDoItem?>(null)
    var isTitleError by mutableStateOf(false)
    var shouldCheckDuplicate by mutableStateOf(true)

    private lateinit var database: AppDatabase

    fun initDatabase(context: Context) {
        database = AppDatabase.getDatabase(context)
        NotificationUtils.createNotificationChannel(context)
        loadToDosForDate(selectedDate)
    }

    fun isTitleDuplicate(): Boolean {
        if (!shouldCheckDuplicate) return false

        return if (editingItem != null) {
            todoList.any { it.title == title && it != editingItem }
        } else {
            todoList.any { it.title == title }
        }
    }

    private fun loadToDosForDate(date: String) {
        viewModelScope.launch {
            val items = database.toDoDao().getToDoItemsByDate(date)
            todoList = items.map { entity ->
                ToDoItem(
                    title = entity.title,
                    description = entity.description,
                    time = entity.time,
                    date = entity.date,
                    isChecked = entity.isChecked
                )
            }
            checkedItems = items.associate { entity ->
                val todoItem = ToDoItem(
                    entity.title,
                    entity.description,
                    entity.time,
                    entity.date
                )
                todoItem to entity.isChecked
            }
        }
    }

    fun onDateChanged(newDate: String) {
        // Parse the input date and store in neutral format
        try {
            val inputFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
            val date = inputFormat.parse(newDate)
            selectedDate = date?.let { neutralDateFormat.format(it) } ?: newDate
            loadToDosForDate(selectedDate)
        } catch (e: Exception) {
            selectedDate = newDate
            loadToDosForDate(selectedDate)
        }
    }

    fun onItemCheckedChange(item: ToDoItem, isChecked: Boolean) {
        viewModelScope.launch {
            database.toDoDao().updateCheckedState(
                title = item.title,
                description = item.description,
                date = selectedDate,
                isChecked = isChecked
            )
            checkedItems = checkedItems.toMutableMap().apply {
                put(item, isChecked)
            }
        }
    }

    fun prepareForAdd() {
        title = ""
        description = ""
        time = ""
        editingItem = null
        isTitleError = false
        shouldCheckDuplicate = true
    }

    fun prepareForEdit(item: ToDoItem) {
        title = item.title
        description = item.description
        time = item.time
        editingItem = item
        isTitleError = false
        shouldCheckDuplicate = true
    }

    fun onSave() {
        if (title.isBlank()) {
            isTitleError = true
            return
        }

        shouldCheckDuplicate = false
        isTitleError = false

        viewModelScope.launch {
            if (editingItem != null) {
                val existingItems = database.toDoDao().getToDoItemsByDate(selectedDate)
                val itemToUpdate = existingItems.find {
                    it.title == editingItem?.title &&
                            it.description == editingItem?.description
                }

                itemToUpdate?.let {
                    val updatedItem = it.copy(
                        title = title,
                        description = description,
                        time = time,
                        date = selectedDate,
                        isChecked = checkedItems[editingItem] ?: false
                    )
                    database.toDoDao().updateToDoItem(updatedItem)

                    cancelReminder(it.title.hashCode())
                    if (time.isNotEmpty()) {
                        scheduleReminder(title, description, time)
                    }
                }
            } else {
                val newItem = ToDoItemEntity(
                    title = title,
                    description = description,
                    time = time,
                    date = selectedDate,
                    isChecked = false
                )
                database.toDoDao().insertToDoItem(newItem)

                if (time.isNotEmpty()) {
                    scheduleReminder(title, description, time)
                }
            }

            prepareForAdd()
            loadToDosForDate(selectedDate)
        }
    }

    fun onDelete(todo: ToDoItem) {
        viewModelScope.launch {
            val existingItems = database.toDoDao().getToDoItemsByDate(selectedDate)
            val itemToDelete = existingItems.find {
                it.title == todo.title &&
                        it.description == todo.description
            }

            itemToDelete?.let {
                database.toDoDao().deleteToDoItem(it)
                cancelReminder(it.title.hashCode())
                loadToDosForDate(selectedDate)
            }
        }
    }

    private fun scheduleReminder(title: String, description: String, timeString: String) {
        if (timeString.isEmpty()) return

        try {
            // Parse using neutral date format
            val dateFormat = SimpleDateFormat("yyyy-MM-dd h:mm a", Locale.US)
            val reminderTime = dateFormat.parse("$selectedDate $timeString")?.time ?: return

            if (reminderTime <= System.currentTimeMillis()) {
                return
            }

            val delay = reminderTime - System.currentTimeMillis()

            val inputData = Data.Builder()
                .putString("title", title)
                .putString("description", description)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag(title.hashCode().toString())
                .build()

            WorkManager.getInstance().enqueue(workRequest)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun cancelReminder(notificationId: Int) {
        WorkManager.getInstance().cancelAllWorkByTag(notificationId.toString())
    }
}