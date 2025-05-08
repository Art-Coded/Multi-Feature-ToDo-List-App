package com.example.sweetbakes.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface OrderCountsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCounts(counts: OrderCountEntity)

    @Update
    suspend fun updateCounts(counts: OrderCountEntity)

    @Query("SELECT * FROM order_counts LIMIT 1")
    suspend fun getCounts(): OrderCountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCounts(counts: OrderCountEntity)
}