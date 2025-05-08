package com.example.sweetbakes.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "order_states")
data class OrderStateEntity(
    @PrimaryKey
    val orderKey: String,
    val isChecked: Boolean
)