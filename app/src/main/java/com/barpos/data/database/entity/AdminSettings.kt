package com.barpos.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "admin_settings")
data class AdminSettings(
    @PrimaryKey val id: Int = 1,
    val adminPin: String = "1234",
    val mobilepayNumber: String = "",
    val barName: String = "Bar"
)
