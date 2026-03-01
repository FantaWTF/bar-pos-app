package com.barpos.ui.admin.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.barpos.BarApplication
import com.barpos.data.database.entity.Category
import com.barpos.data.database.entity.Product
import com.barpos.data.repository.ProductRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProductManagementViewModel(
    private val productRepository: ProductRepository
) : ViewModel() {

    val products = productRepository.allProducts.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val categories = productRepository.allCategories.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    fun addProduct(name: String, price: Double, categoryId: Long?) {
        if (name.isBlank() || price <= 0) return
        viewModelScope.launch {
            productRepository.insertProduct(
                Product(name = name.trim(), price = price, categoryId = categoryId)
            )
        }
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch {
            productRepository.updateProduct(product)
        }
    }

    fun toggleProductActive(product: Product) {
        viewModelScope.launch {
            productRepository.updateProduct(product.copy(isActive = !product.isActive))
        }
    }

    fun addCategory(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            productRepository.insertCategory(Category(name = name.trim()))
        }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch {
            productRepository.updateCategory(category)
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            productRepository.deleteCategory(category)
        }
    }

    class Factory(private val application: BarApplication) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProductManagementViewModel(application.productRepository) as T
        }
    }
}
