package com.example.sweetbakes.Home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.sweetbakes.data.AppDatabase

class RestockViewModelFactory(
    private val database: AppDatabase
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RestockViewModel::class.java)) {
            return RestockViewModel(database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}