package com.example.sweetbakes.content

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sweetbakes.ui.AddToDoScreen
import com.example.sweetbakes.ui.ToDoUI

sealed class Screen(val route: String) {
    object ToDoList : Screen("todo_list")
    object AddToDo : Screen("add_todo")
}

@Composable
fun ToDoContent(
    context: Context,
    localizedContext: Context
) {
    val navController = rememberNavController()
    val viewModel: ToDoViewModel = viewModel()
    viewModel.initDatabase(localizedContext)

    val onDateSelected = remember {
        { date: String ->
            viewModel.onDateChanged(date)
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.ToDoList.route
    ) {
        composable(Screen.ToDoList.route) {
            CompositionLocalProvider(LocalContext provides localizedContext) {
                ToDoUI(
                    selectedDate = viewModel.selectedDate,
                    todoList = viewModel.todoList,
                    checkedItems = viewModel.checkedItems,
                    onItemCheckedChange = { item, isChecked ->
                        viewModel.onItemCheckedChange(item, isChecked)
                    },
                    onAddClick = {
                        viewModel.prepareForAdd()
                        navController.navigate(Screen.AddToDo.route)
                    },
                    onEditClick = { item ->
                        viewModel.prepareForEdit(item)
                        navController.navigate(Screen.AddToDo.route)
                    },
                    onDateClick = {
                        showDatePicker(context, onDateSelected)
                    },
                    onDelete = { todo -> viewModel.onDelete(todo) }
                )
            }
        }

        composable(Screen.AddToDo.route) {
            CompositionLocalProvider(LocalContext provides localizedContext) {
                LaunchedEffect(Unit) {
                    viewModel.isTitleError = false
                }

                AddToDoScreen(
                    localizedContext = localizedContext,
                    onBackClick = { navController.popBackStack() },
                    title = viewModel.title,
                    onTitleChange = {
                        viewModel.title = it
                        viewModel.isTitleError = false
                    },
                    isTitleError = viewModel.isTitleError,
                    isTitleDuplicate = viewModel.isTitleDuplicate(),
                    description = viewModel.description,
                    onDescriptionChange = { viewModel.description = it },
                    time = viewModel.time,
                    onTimeChange = { viewModel.time = it },
                    onSave = {
                        viewModel.onSave()
                        navController.popBackStack()
                    },
                    editingItem = viewModel.editingItem,
                    activityContext = context
                )
            }
        }
    }
}