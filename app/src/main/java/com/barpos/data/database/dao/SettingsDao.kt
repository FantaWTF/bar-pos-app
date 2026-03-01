package com.barpos.data.database.dao

import androidx.room.*
import com.barpos.data.database.entity.AdminSettings
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Query("SELECT * FROM admin_settings WHERE id = 1")
    fun getSettings(): Flow<AdminSettings?>

    @Query("SELECT * FROM admin_settings WHERE id = 1")
    suspend fun getSettingsSync(): AdminSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(settings: AdminSettings)
}
