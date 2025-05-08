package com.example.sweetbakes.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sweetbakes.R
import com.example.sweetbakes.SharedViewModel
import com.example.sweetbakes.SharedViewModelFactory
import com.example.sweetbakes.model.OrdersItem
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersUI(
    ordersList: List<OrdersItem>,
    onDeleteOrder: (OrdersItem) -> Unit,
    onEditOrder: (OrdersItem) -> Unit,
    showEditPopup: Boolean,
    onDismissEditPopup: () -> Unit,
    editTitle: String,
    onEditTitleChange: (String) -> Unit,
    editDescription: String,
    onEditDescriptionChange: (String) -> Unit,
    editPriority: String,
    onEditPriorityChange: (String) -> Unit,
    editDueDate: String,
    onEditDueDateChange: (String) -> Unit,
    editReminders: String,
    onEditRemindersChange: (String) -> Unit,
    onSaveEdit: () -> Unit,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit,
    sharedViewModel: SharedViewModel = viewModel(factory = SharedViewModelFactory(LocalContext.current))
) {
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    var orderToDelete by remember { mutableStateOf<OrdersItem?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var selectedSort by remember { mutableStateOf("Default") }
    var lastMidnightCheck by remember { mutableStateOf(LocalDate.now()) }

    val priorityMap = mapOf("High" to 3, "Medium" to 2, "Low" to 1)

    val checkedStates by sharedViewModel.checkedStates.collectAsState()

    LaunchedEffect(Unit) {
        while (true) {
            val currentDate = LocalDate.now()

            if (currentDate.isAfter(lastMidnightCheck)) {

                sharedViewModel.deleteCheckedOrders()

                sharedViewModel.clearAllCheckedOrders()

                lastMidnightCheck = currentDate
            }
            delay(60_000)
        }
    }

    val sortedOrders = remember(selectedSort, ordersList, checkedStates) {
        ordersList.sortedWith(compareBy<OrdersItem> { checkedStates[it.getKey()] ?: false }
            .thenBy {
                when (selectedSort) {
                    "A - Z" -> it.title
                    "Z - A" -> it.title
                    "Low to High (Priority)" -> priorityMap[it.priority] ?: 0
                    "High to Low (Priority)" -> -(priorityMap[it.priority] ?: 0)
                    else -> 0
                }
            }.let { comparator ->
                when (selectedSort) {
                    "Z - A" -> comparator.reversed()
                    else -> comparator
                }
            }
        )
    }

    val pendingOrderCount = sortedOrders.count { !(checkedStates[it.getKey()] ?: false) }

    val highPriorityOrderCount = sortedOrders.count { it.priority == "High" && !(checkedStates[it.getKey()] ?: false) }

    LaunchedEffect(pendingOrderCount, highPriorityOrderCount) {
        sharedViewModel.updatePendingOrderCount(pendingOrderCount)
        sharedViewModel.updateHighPriorityOrderCount(highPriorityOrderCount)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = context.getString(R.string.orders_today),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = selectedSort,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                label = { Text("Sort by") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                listOf(
                    "A - Z",
                    "Z - A",
                    "Low to High (Priority)",
                    "High to Low (Priority)"
                ).forEach { sortOption ->
                    DropdownMenuItem(
                        text = { Text(sortOption) },
                        onClick = {
                            selectedSort = sortOption
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (ordersList.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 99.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.icon),
                    contentDescription = "No Orders",
                    modifier = Modifier.size(150.dp),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface, blendMode = BlendMode.SrcIn)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = context.getString(R.string.no_orders),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(
                    items = sortedOrders,
                    key = { _, order -> order.id }
                ) { index, order ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { dismissValue ->
                            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                orderToDelete = order
                                showDeleteDialog = true
                            }
                            false
                        }
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Red, shape = RoundedCornerShape(16.dp))
                                    .padding(8.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Color.White,
                                    modifier = Modifier.padding(16.dp))
                            }
                        },
                        content = {
                            val isChecked = checkedStates[order.id] ?: false

                            OrderItem(
                                order = order,
                                isChecked = isChecked,
                                onCheckedChange = { newCheckedState ->
                                    sharedViewModel.updateCheckedState(order, newCheckedState)
                                },
                                onEditClick = {
                                    onEditOrder(order)
                                }
                            )
                        }
                    )
                }
            }
        }
    }

    orderToDelete?.title

    if (showDeleteDialog && orderToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(text = context.getString(R.string.confirm_deletion)) },
            text = { Text(context.getString(R.string.delete_confirmation, orderToDelete?.title ?: "?")) },
            confirmButton = {
                Button(
                    onClick = {
                        orderToDelete?.let { onDeleteOrder(it) }
                        showDeleteDialog = false
                        orderToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showEditPopup) {
        AlertDialog(
            onDismissRequest = onDismissEditPopup,
            title = { Text(context.getString(R.string.edit_order)) },
            text = {
                Column(modifier = Modifier.padding(8.dp)) {
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { newTitle ->
                            if (newTitle.length <= 18) {
                                onEditTitleChange(newTitle)
                            }
                        },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = editTitle.length > 18,
                        supportingText = {
                            if (editTitle.length > 18) {
                                Text(
                                    text = context.getString(R.string.title_characters),
                                    color = MaterialTheme.colorScheme.error
                                )
                            } else {
                                Text(
                                    text = "${editTitle.length}/18",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = editDescription,
                        onValueChange = onEditDescriptionChange,
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth())

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(context.getString(R.string.select_priority), fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = editPriority == "Low",
                                onCheckedChange = { onEditPriorityChange("Low") }
                            )
                            Text("Low")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = editPriority == "Medium",
                                onCheckedChange = { onEditPriorityChange("Medium") }
                            )
                            Text("Medium")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = editPriority == "High",
                                onCheckedChange = { onEditPriorityChange("High") }
                            )
                            Text("High")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (editDueDate.isNotEmpty() && editDueDate != "No due date") {
                                "Due Date: $editDueDate"
                            } else {
                                "No due date"
                            },
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .clickable { onDateClick() }
                                .weight(1f)
                        )
                        if (editDueDate.isNotEmpty() && editDueDate != "No due date") {
                            IconButton(
                                onClick = { onEditDueDateChange("No due date") }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Clear Due Date",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (editReminders.isNotEmpty()) {
                                "${context.getString(R.string.remind_me)} $editReminders"
                            } else {
                                "No reminders"
                            },
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .clickable { onTimeClick() }
                                .weight(1f)
                        )
                        if (editReminders.isNotEmpty()) {
                            IconButton(
                                onClick = { onEditRemindersChange("") }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Clear Reminders",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = onSaveEdit,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    )
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissEditPopup) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun OrderItem(
    order: OrdersItem,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .graphicsLayer {
                    alpha = if (isChecked) 0.4f else 1f
                }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = order.title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 8.dp))
                    IconButton(onClick = onEditClick, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Order",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Checkbox(
                    checked = isChecked,
                    onCheckedChange = onCheckedChange,
                    modifier = Modifier.graphicsLayer { alpha = 1f }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = order.description,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {
                val priorityColor = when (order.priority) {
                    "High" -> Color.Red
                    "Medium" -> MaterialTheme.colorScheme.onSurface
                    else -> Color.Gray
                }
                val priorityFontWeight = when (order.priority) {
                    "High", "Medium" -> FontWeight.Bold
                    else -> FontWeight.Medium
                }

                Row {
                    Text(
                        text = "Priority level: ",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = order.priority,
                        fontSize = 14.sp,
                        fontWeight = priorityFontWeight,
                        color = priorityColor
                    )
                }

                if (order.dueDate.isNotEmpty() && order.dueDate != "No due date") {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Due Date: ${order.dueDate}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (order.reminders.isNotEmpty() && order.reminders != "No reminders") {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${LocalContext.current.getString(R.string.remind_me)} ${order.reminders}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}
