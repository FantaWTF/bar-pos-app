package com.barpos.data.repository

import com.barpos.data.database.dao.SettingsDao
import com.barpos.data.database.entity.AdminSettings
import kotlinx.coroutines.flow.Flow

class SettingsRepository(private val settingsDao: SettingsDao) {
    val settings: Flow<AdminSettings?> = settingsDao.getSettings()

    suspend fun getSettingsSync(): AdminSettings? = settingsDao.getSettingsSync()
    suspend fun updateSettings(settings: AdminSettings) = settingsDao.upsert(settings)

    suspend fun initializeDefaults() {
        if (settingsDao.getSettingsSync() == null) {
            settingsDao.upsert(AdminSettings())
        }
    }
}
