package com.barpos.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.barpos.data.database.dao.*
import com.barpos.data.database.entity.*

@Database(
    entities = [
        Member::class,
        Category::class,
        Product::class,
        Transaction::class,
        TransactionItem::class,
        AdminSettings::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun memberDao(): MemberDao
    abstract fun categoryDao(): CategoryDao
    abstract fun productDao(): ProductDao
    abstract fun transactionDao(): TransactionDao
    abstract fun settingsDao(): SettingsDao
}
