package com.barpos.data.database.entity

data class ProductSalesData(
    val productName: String,
    val totalQty: Int,
    val totalRevenue: Double
)

data class CategorySalesData(
    val categoryName: String,
    val totalRevenue: Double
)

data class MemberSalesData(
    val memberName: String,
    val totalSpent: Double
)

data class DailyRevenueData(
    val dayTimestamp: Long,
    val dailyTotal: Double
)
