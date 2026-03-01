package com.barpos.ui.pos

import com.barpos.data.database.entity.Product

data class CartItem(
    val product: Product,
    val quantity: Int = 1
) {
    val totalPrice: Double get() = product.price * quantity
}
