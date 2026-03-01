package com.barpos.data.repository

import com.barpos.data.database.dao.CategoryDao
import com.barpos.data.database.dao.ProductDao
import com.barpos.data.database.entity.Category
import com.barpos.data.database.entity.Product
import kotlinx.coroutines.flow.Flow

class ProductRepository(
    private val productDao: ProductDao,
    private val categoryDao: CategoryDao
) {
    val activeProducts: Flow<List<Product>> = productDao.getActiveProducts()
    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
    val allCategories: Flow<List<Category>> = categoryDao.getAllCategories()

    fun getProductsByCategory(categoryId: Long): Flow<List<Product>> =
        productDao.getProductsByCategory(categoryId)

    suspend fun insertProduct(product: Product): Long = productDao.insert(product)
    suspend fun updateProduct(product: Product) = productDao.update(product)
    suspend fun insertCategory(category: Category): Long = categoryDao.insert(category)
    suspend fun updateCategory(category: Category) = categoryDao.update(category)
    suspend fun deleteCategory(category: Category) = categoryDao.delete(category)
}
