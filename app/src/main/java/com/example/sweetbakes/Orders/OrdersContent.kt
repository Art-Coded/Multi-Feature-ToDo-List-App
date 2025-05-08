package com.example.sweetbakes.content

import android.content.Context
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sweetbakes.SharedViewModel
import com.example.sweetbakes.SharedViewModelFactory
import com.example.sweetbakes.model.OrdersItem
import com.example.sweetbakes.ui.OrdersUI

@Composable
fun OrdersContent(
    context: Context,
    sharedViewModel: SharedViewModel = viewModel(factory = SharedViewModelFactory(context))
) {
    val orders by sharedViewModel.orders.collectAsState(emptyList())
    var showEditPopup by remember { mutableStateOf(false) }
    var editingOrder by remember { mutableStateOf<OrdersItem?>(null) }

    var editTitle by remember { mutableStateOf("") }
    var editDescription by remember { mutableStateOf("") }
    var editPriority by remember { mutableStateOf("Medium") }
    var editDueDate by remember { mutableStateOf("") }
    var editReminders by remember { mutableStateOf("") }

    OrdersUI(
        ordersList = orders,
        onDeleteOrder = { orderToDelete ->
            sharedViewModel.deleteOrder(orderToDelete)
        },
        onEditOrder = { orderToEdit ->
            editingOrder = orderToEdit
            editTitle = orderToEdit.title
            editDescription = orderToEdit.description
            editPriority = orderToEdit.priority
            editDueDate = orderToEdit.dueDate
            editReminders = orderToEdit.reminders
            showEditPopup = true
        },
        showEditPopup = showEditPopup,
        onDismissEditPopup = { showEditPopup = false },
        editTitle = editTitle,
        onEditTitleChange = { editTitle = it },
        editDescription = editDescription,
        onEditDescriptionChange = { editDescription = it },
        editPriority = editPriority,
        onEditPriorityChange = { editPriority = it },
        editDueDate = editDueDate,
        onEditDueDateChange = { editDueDate = it },
        editReminders = editReminders,
        onEditRemindersChange = { editReminders = it },
        onSaveEdit = {
            val updatedOrder = editingOrder?.copy(
                title = editTitle,
                description = editDescription,
                priority = editPriority,
                dueDate = editDueDate,
                reminders = editReminders,
                notificationScheduled = editReminders.isNotEmpty()
            )

            if (updatedOrder != null) {
                sharedViewModel.updateOrder(updatedOrder)
            }
            showEditPopup = false
        },
        onDateClick = {
        },
        onTimeClick = {
        },
        sharedViewModel = sharedViewModel
    )
}