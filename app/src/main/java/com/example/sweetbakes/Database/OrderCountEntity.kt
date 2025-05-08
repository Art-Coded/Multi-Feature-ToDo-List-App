package com.example.sweetbakes.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "order_counts")
data class OrderCountEntity(
    @PrimaryKey val id: Int = 0,
    val pendingOrderCount: Int = 0,
    val highPriorityOrderCount: Int = 0,
    val completedOrderCount: Int = 0
)