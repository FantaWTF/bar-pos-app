package com.barpos.ui.pos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.barpos.BarApplication
import com.barpos.data.database.entity.*
import com.barpos.data.repository.MemberRepository
import com.barpos.data.repository.ProductRepository
import com.barpos.data.repository.SettingsRepository
import com.barpos.data.repository.TransactionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class POSViewModel(
    private val memberRepository: MemberRepository,
    private val productRepository: ProductRepository,
    private val transactionRepository: TransactionRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val members = memberRepository.activeMembers.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val categories = productRepository.allCategories.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val products = productRepository.activeProducts.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val settings = settingsRepository.settings.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), null
    )

    private val _selectedMember = MutableStateFlow<Member?>(null)
    val selectedMember: StateFlow<Member?> = _selectedMember.asStateFlow()

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    val selectedCategoryId: StateFlow<Long?> = _selectedCategoryId.asStateFlow()

    val cartTotal: StateFlow<Double> = _cartItems.map { items ->
        items.sumOf { it.totalPrice }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val filteredProducts: StateFlow<List<Product>> = combine(
        products, _selectedCategoryId
    ) { allProducts, categoryId ->
        if (categoryId == null) allProducts
        else allProducts.filter { it.categoryId == categoryId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectMember(member: Member?) {
        _selectedMember.value = member
    }

    fun refreshSelectedMember() {
        val current = _selectedMember.value ?: return
        viewModelScope.launch {
            _selectedMember.value = memberRepository.getById(current.id)
        }
    }

    fun selectCategory(categoryId: Long?) {
        _selectedCategoryId.value = categoryId
    }

    fun addToCart(product: Product) {
        val current = _cartItems.value.toMutableList()
        val existingIndex = current.indexOfFirst { it.product.id == product.id }
        if (existingIndex >= 0) {
            current[existingIndex] = current[existingIndex].copy(
                quantity = current[existingIndex].quantity + 1
            )
        } else {
            current.add(CartItem(product))
        }
        _cartItems.value = current
    }

    fun removeFromCart(productId: Long) {
        _cartItems.value = _cartItems.value.filter { it.product.id != productId }
    }

    fun incrementQuantity(productId: Long) {
        _cartItems.value = _cartItems.value.map {
            if (it.product.id == productId) it.copy(quantity = it.quantity + 1) else it
        }
    }

    fun decrementQuantity(productId: Long) {
        _cartItems.value = _cartItems.value.mapNotNull {
            if (it.product.id == productId) {
                if (it.quantity > 1) it.copy(quantity = it.quantity - 1) else null
            } else it
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    fun addToAccount(onSuccess: () -> Unit) {
        val member = _selectedMember.value ?: return
        val items = _cartItems.value
        if (items.isEmpty()) return

        val total = items.sumOf { it.totalPrice }

        viewModelScope.launch {
            val transaction = Transaction(
                memberId = member.id,
                type = TransactionType.PURCHASE,
                totalAmount = total
            )
            val transactionItems = items.map { cartItem ->
                TransactionItem(
                    productId = cartItem.product.id,
                    productName = cartItem.product.name,
                    quantity = cartItem.quantity,
                    unitPrice = cartItem.product.price
                )
            }
            transactionRepository.insertTransactionWithItems(transaction, transactionItems)
            memberRepository.subtractBalance(member.id, total)
            _selectedMember.value = memberRepository.getById(member.id)
            clearCart()
            onSuccess()
        }
    }

    class Factory(private val application: BarApplication) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return POSViewModel(
                application.memberRepository,
                application.productRepository,
                application.transactionRepository,
                application.settingsRepository
            ) as T
        }
    }
}
