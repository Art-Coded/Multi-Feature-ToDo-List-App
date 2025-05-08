package com.example.sweetbakes.Home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sweetbakes.data.AppDatabase

@Composable
fun RestockContent() {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val viewModel: RestockViewModel = viewModel(
        factory = RestockViewModelFactory(database)
    )

    RestockUI(viewModel = viewModel)
}