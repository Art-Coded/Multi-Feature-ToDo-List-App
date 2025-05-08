package com.example.sweetbakes.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.sweetbakes.model.OrdersItem
import kotlinx.coroutines.flow.Flow

@Dao
interface OrdersDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrdersItem)

    @Update
    suspend fun updateOrder(order: OrdersItem)

    @Delete
    suspend fun deleteOrder(order: OrdersItem)

    @Query("SELECT * FROM ordersitem ORDER BY CASE priority WHEN 'High' THEN 1 WHEN 'Medium' THEN 2 ELSE 3 END")
    fun getAllOrders(): Flow<List<OrdersItem>>

    @Query("SELECT * FROM ordersitem WHERE id = :id")
    suspend fun getOrderById(id: String): OrdersItem?

    @Query("DELETE FROM ordersitem WHERE id IN (:ids)")
    suspend fun deleteOrdersByIds(ids: List<String>)

    @Query("""
        SELECT ordersitem.*, 
               CASE WHEN order_states.isChecked IS NULL THEN 0 ELSE order_states.isChecked END as isChecked
        FROM ordersitem
        LEFT JOIN order_states ON ordersitem.id = order_states.orderKey
    """)
    fun getOrdersWithStates(): Flow<List<OrderWithState>>
}

data class OrderWithState(
    @Embedded val order: OrdersItem,
    val isChecked: Boolean
)