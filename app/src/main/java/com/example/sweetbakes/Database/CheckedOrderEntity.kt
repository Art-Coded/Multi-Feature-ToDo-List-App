package com.example.sweetbakes.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "checked_orders")
data class CheckedOrderEntity(
    @PrimaryKey
    val orderId: String,
    val orderTitle: String,
    val orderPriority: String
)