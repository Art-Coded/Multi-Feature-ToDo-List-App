// ToDoDao.kt
package com.example.sweetbakes.data

import androidx.room.*
import com.example.sweetbakes.model.ToDoItemEntity

@Dao
interface ToDoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertToDoItem(item: ToDoItemEntity)

    @Update
    suspend fun updateToDoItem(item: ToDoItemEntity)

    @Delete
    suspend fun deleteToDoItem(item: ToDoItemEntity)

    @Query("SELECT * FROM todo_items WHERE date = :date")
    suspend fun getToDoItemsByDate(date: String): List<ToDoItemEntity>

    @Query("DELETE FROM todo_items WHERE date = :date")
    suspend fun deleteAllForDate(date: String)

    @Query("UPDATE todo_items SET isChecked = :isChecked WHERE title = :title AND description = :description AND date = :date")
    suspend fun updateCheckedState(title: String, description: String, date: String, isChecked: Boolean)
}