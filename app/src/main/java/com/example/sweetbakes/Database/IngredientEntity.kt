package com.example.sweetbakes.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ingredients")
data class IngredientEntity(
    @PrimaryKey
    val name: String,
    val quantity: Int
)