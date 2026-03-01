package com.barpos.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.barpos.data.database.entity.CategorySalesData
import com.barpos.data.database.entity.DailyRevenueData
import com.barpos.data.database.entity.MemberSalesData
import com.barpos.data.database.entity.ProductSalesData
import com.barpos.data.database.entity.Transaction
import com.barpos.data.database.entity.TransactionItem
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert
    suspend fun insertTransaction(transaction: Transaction): Long

    @Insert
    suspend fun insertTransactionItems(items: List<TransactionItem>)

    @androidx.room.Transaction
    suspend fun insertTransactionWithItems(
        transaction: Transaction,
        items: List<TransactionItem>
    ) {
        val transactionId = insertTransaction(transaction)
        val itemsWithId = items.map { it.copy(transactionId = transactionId) }
        insertTransactionItems(itemsWithId)
    }

    @Query("SELECT * FROM transactions ORDER BY createdAt DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE memberId = :memberId ORDER BY createdAt DESC")
    fun getTransactionsByMember(memberId: Long): Flow<List<Transaction>>

    @Query("SELECT * FROM transaction_items WHERE transactionId = :transactionId")
    suspend fun getTransactionItems(transactionId: Long): List<TransactionItem>

    @Query("SELECT * FROM transactions WHERE createdAt BETWEEN :startDate AND :endDate ORDER BY createdAt DESC")
    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<Transaction>>

    @Query("SELECT SUM(totalAmount) FROM transactions WHERE type = 'PURCHASE' AND createdAt BETWEEN :startDate AND :endDate")
    suspend fun getTotalRevenue(startDate: Long, endDate: Long): Double?

    @Query("""
        SELECT ti.productName, SUM(ti.quantity) as totalQty, SUM(ti.quantity * ti.unitPrice) as totalRevenue
        FROM transaction_items ti
        INNER JOIN transactions t ON ti.transactionId = t.id
        WHERE t.type = 'PURCHASE' AND t.createdAt BETWEEN :startDate AND :endDate
        GROUP BY ti.productName
        ORDER BY totalQty DESC
        LIMIT :limit
    """)
    suspend fun getTopProducts(startDate: Long, endDate: Long, limit: Int = 10): List<ProductSalesData>

    @Query("""
        SELECT c.name as categoryName, SUM(ti.quantity * ti.unitPrice) as totalRevenue
        FROM transaction_items ti
        INNER JOIN transactions t ON ti.transactionId = t.id
        INNER JOIN products p ON ti.productId = p.id
        INNER JOIN categories c ON p.categoryId = c.id
        WHERE t.type = 'PURCHASE' AND t.createdAt BETWEEN :startDate AND :endDate
        GROUP BY c.name
        ORDER BY totalRevenue DESC
    """)
    suspend fun getSalesByCategory(startDate: Long, endDate: Long): List<CategorySalesData>

    @Query("""
        SELECT m.name as memberName, SUM(t.totalAmount) as totalSpent
        FROM transactions t
        INNER JOIN members m ON t.memberId = m.id
        WHERE t.type = 'PURCHASE' AND t.createdAt BETWEEN :startDate AND :endDate
        GROUP BY m.name
        ORDER BY totalSpent DESC
    """)
    suspend fun getSalesByMember(startDate: Long, endDate: Long): List<MemberSalesData>

    @Query("""
        SELECT (createdAt / 86400000) * 86400000 as dayTimestamp, SUM(totalAmount) as dailyTotal
        FROM transactions
        WHERE type = 'PURCHASE' AND createdAt BETWEEN :startDate AND :endDate
        GROUP BY dayTimestamp
        ORDER BY dayTimestamp
    """)
    suspend fun getDailyRevenue(startDate: Long, endDate: Long): List<DailyRevenueData>
}
