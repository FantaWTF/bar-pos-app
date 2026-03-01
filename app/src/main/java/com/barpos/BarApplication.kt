package com.barpos

import android.app.Application
import androidx.room.Room
import com.barpos.data.database.AppDatabase
import com.barpos.data.repository.MemberRepository
import com.barpos.data.repository.ProductRepository
import com.barpos.data.repository.SettingsRepository
import com.barpos.data.repository.TransactionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BarApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "bar_pos_database"
        ).build()
    }

    val memberRepository by lazy { MemberRepository(database.memberDao()) }
    val productRepository by lazy { ProductRepository(database.productDao(), database.categoryDao()) }
    val transactionRepository by lazy { TransactionRepository(database.transactionDao()) }
    val settingsRepository by lazy { SettingsRepository(database.settingsDao()) }

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            settingsRepository.initializeDefaults()
        }
    }
}
