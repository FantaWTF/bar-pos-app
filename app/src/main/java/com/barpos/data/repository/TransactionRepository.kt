package com.barpos.data.repository

import com.barpos.data.database.dao.TransactionDao
import com.barpos.data.database.entity.*
import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val transactionDao: TransactionDao) {
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()

    fun getTransactionsByMember(memberId: Long): Flow<List<Transaction>> =
        transactionDao.getTransactionsByMember(memberId)

    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<Transaction>> =
        transactionDao.getTransactionsByDateRange(startDate, endDate)

    suspend fun insertTransactionWithItems(transaction: Transaction, items: List<TransactionItem>) =
        transactionDao.insertTransactionWithItems(transaction, items)

    suspend fun getTransactionItems(transactionId: Long): List<TransactionItem> =
        transactionDao.getTransactionItems(transactionId)

    suspend fun insertPayment(transaction: Transaction) {
        transactionDao.insertTransaction(transaction)
    }

    suspend fun getTotalRevenue(startDate: Long, endDate: Long): Double =
        transactionDao.getTotalRevenue(startDate, endDate) ?: 0.0

    suspend fun getTopProducts(startDate: Long, endDate: Long, limit: Int = 10): List<ProductSalesData> =
        transactionDao.getTopProducts(startDate, endDate, limit)

    suspend fun getSalesByCategory(startDate: Long, endDate: Long): List<CategorySalesData> =
        transactionDao.getSalesByCategory(startDate, endDate)

    suspend fun getSalesByMember(startDate: Long, endDate: Long): List<MemberSalesData> =
        transactionDao.getSalesByMember(startDate, endDate)

    suspend fun getDailyRevenue(startDate: Long, endDate: Long): List<DailyRevenueData> =
        transactionDao.getDailyRevenue(startDate, endDate)
}
