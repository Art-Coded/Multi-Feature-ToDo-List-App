package com.example.sweetbakes.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckedOrdersDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckedOrder(checkedOrder: CheckedOrderEntity)

    @Delete
    suspend fun deleteCheckedOrder(checkedOrder: CheckedOrderEntity)

    @Query("SELECT * FROM checked_orders")
    fun getAllCheckedOrders(): Flow<List<CheckedOrderEntity>>

    @Query("DELETE FROM checked_orders")
    suspend fun clearAllCheckedOrders()
}