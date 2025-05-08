package com.example.sweetbakes.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.sweetbakes.model.SettingsEntity

@Dao
interface SettingsDao {
    @Query("SELECT isDarkMode FROM settings WHERE id = 1")
    suspend fun getDarkMode(): Boolean?

    @Query("SELECT language FROM settings WHERE id = 1")
    suspend fun getLanguage(): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: SettingsEntity)
}