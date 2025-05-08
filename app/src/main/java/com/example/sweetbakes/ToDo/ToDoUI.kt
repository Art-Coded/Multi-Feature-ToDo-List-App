package com.example.sweetbakes.ui

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sweetbakes.R
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.rememberDismissState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.Locale


data class ToDoItem(
    val title: String,
    val description: String,
    val time: String,
    val date: String = "",
    val isChecked: Boolean = false
) {
    val id: String = "${title.hashCode()}-${description.hashCode()}-${time.hashCode()}-${date.hashCode()}"

    fun getFullTimestamp(): Long {
        val dateFormat = SimpleDateFormat("EEEE, MMM d, yyyy h:mm a", Locale.getDefault())
        return try {
            dateFormat.parse("$date $time")?.time ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ToDoUI(
    selectedDate: String,
    todoList: List<ToDoItem>,
    checkedItems: Map<ToDoItem, Boolean>,
    onItemCheckedChange: (ToDoItem, Boolean) -> Unit,
    onAddClick: () -> Unit,
    onEditClick: (ToDoItem) -> Unit,
    onDateClick: () -> Unit,
    onDelete: (ToDoItem) -> Unit,
) {
    val currentLocale = LocalContext.current.resources.configuration.locales[0] ?: Locale.getDefault()
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<ToDoItem?>(null) }


    val formattedDate = remember(selectedDate, currentLocale) {
        if (selectedDate.isEmpty()) ""
        else {
            try {
                val inputFormat = SimpleDateFormat("MMMM d, yyyy", Locale.US)
                val date = inputFormat.parse(selectedDate)


                val outputFormat = SimpleDateFormat("M d, yyyy", currentLocale)
                date?.let { outputFormat.format(it) } ?: selectedDate
            } catch (e: Exception) {
                selectedDate
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "To Do",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onAddClick) {
                        Icon(imageVector = Icons.Filled.Add, contentDescription = "Add To-Do")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "As of $formattedDate",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .clickable { onDateClick() }
                        .padding(8.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        if (todoList.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 140.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.icon),
                        contentDescription = "No To-Dos",
                        modifier = Modifier.size(150.dp),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface, blendMode = BlendMode.SrcIn)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = LocalContext.current.getString(R.string.middletext),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp)
                            .align(Alignment.CenterHorizontally),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(todoList, key = { it.id }) { todo ->
                val dismissState = rememberDismissState(
                    confirmStateChange = { dismissValue ->
                        if (dismissValue == DismissValue.DismissedToStart) {
                            itemToDelete = todo
                            showDeleteDialog = true
                        }
                        false
                    }
                )

                SwipeToDismiss(
                    state = dismissState,
                    directions = setOf(DismissDirection.EndToStart),
                    dismissThresholds = { FractionalThreshold(0.5f) },
                    background = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.Red)
                                .padding(8.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color.White,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    },
                    dismissContent = {
                        ToDoItemCard(
                            todo = todo,
                            isChecked = checkedItems[todo] ?: false,
                            onCheckedChange = { isChecked -> onItemCheckedChange(todo, isChecked) },
                            onEditClick = { onEditClick(todo) }
                        )
                    }
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(context.getString(R.string.confirm_deletion))},
            text = { Text(context.getString(R.string.delete_confirmation, itemToDelete?.title ?: "?")) },
            confirmButton = {
                Button(
                    onClick = {
                        itemToDelete?.let { onDelete(it) }
                        showDeleteDialog = false
                        itemToDelete = null
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
}

@Composable
fun ToDoItemCard(
    todo: ToDoItem,
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
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .graphicsLayer(alpha = if (isChecked) 0.5f else 1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = todo.title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onEditClick, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit To-Do",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Checkbox(
                    checked = isChecked,
                    onCheckedChange = onCheckedChange
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = todo.description,
                fontSize = 14.sp,
                modifier = Modifier.graphicsLayer(alpha = if (isChecked) 0.3f else 1f)
            )

            if (todo.time.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Text(
                        text = "${LocalContext.current.getString(R.string.remind_me)}: ${todo.time}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}