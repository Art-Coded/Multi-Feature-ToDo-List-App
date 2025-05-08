package com.example.sweetbakes.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderStatesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateState(state: OrderStateEntity)

    @Query("SELECT * FROM order_states WHERE orderKey = :key")
    suspend fun getStateByKey(key: String): OrderStateEntity?

    @Query("SELECT * FROM order_states")
    fun getAllStates(): Flow<List<OrderStateEntity>>

    @Query("DELETE FROM order_states WHERE orderKey = :key")
    suspend fun deleteStateByKey(key: String)

    // Add to OrderStatesDao.kt
    @Query("DELETE FROM order_states")
    suspend fun clearAllStates()
}