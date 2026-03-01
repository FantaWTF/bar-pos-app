package com.barpos.data.database.dao

import androidx.room.*
import com.barpos.data.database.entity.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE isActive = 1 ORDER BY name")
    fun getActiveProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products ORDER BY name")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE categoryId = :categoryId AND isActive = 1 ORDER BY name")
    fun getProductsByCategory(categoryId: Long): Flow<List<Product>>

    @Insert
    suspend fun insert(product: Product): Long

    @Update
    suspend fun update(product: Product)
}
