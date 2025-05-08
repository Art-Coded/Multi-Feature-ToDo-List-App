package com.example.sweetbakes.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.sweetbakes.model.*

@Database(
    entities = [
        OrdersItem::class,
        CheckedOrderEntity::class,
        OrderStateEntity::class,
        OrderCountEntity::class,
        ToDoItemEntity::class,
        IngredientEntity::class,
        SettingsEntity::class

    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ordersDao(): OrdersDao
    abstract fun checkedOrdersDao(): CheckedOrdersDao
    abstract fun orderStatesDao(): OrderStatesDao
    abstract fun orderCountsDao(): OrderCountsDao
    abstract fun toDoDao(): ToDoDao
    abstract fun ingredientDao(): IngredientDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sweetbakes_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}