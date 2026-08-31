package com.example.data.repository

import com.example.data.local.dao.CartDao
import com.example.data.local.dao.ProductDao
import com.example.data.local.entity.CartItemEntity
import com.example.data.model.CartItemDetail
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CartRepository(
    private val cartDao: CartDao,
    private val productDao: ProductDao
) {
    fun getCartDetails(customerId: Long): Flow<List<CartItemDetail>> {
        return cartDao.getCartItems(customerId).map { cartItems ->
            val productIds = cartItems.map { it.productId }
            val products = if (productIds.isNotEmpty()) productDao.getProductsByIds(productIds) else emptyList()
            val productMap = products.associateBy { it.id }

            cartItems.mapNotNull { item ->
                val product = productMap[item.productId]
                if (product != null && product.isAvailable && product.stock > 0) {
                    CartItemDetail(cartItem = item, product = product)
                } else {
                    null
                }
            }
        }
    }

    fun getCartCount(customerId: Long): Flow<Int> = cartDao.getCartCount(customerId)

    suspend fun addToCart(customerId: Long, productId: Long, quantity: Int = 1): Result<Unit> {
        val product = productDao.getProductByIdDirect(productId)
            ?: return Result.failure(IllegalArgumentException("Mahsulot topilmadi"))

        if (!product.isAvailable || product.stock <= 0) {
            return Result.failure(IllegalArgumentException("Mahsulot hozirda sotuvda mavjud emas"))
        }

        val existing = cartDao.getCartItem(customerId, productId)
        if (existing != null) {
            val newQty = (existing.quantity + quantity).coerceAtMost(product.stock)
            cartDao.updateCartItem(existing.copy(quantity = newQty))
        } else {
            val initialQty = quantity.coerceAtMost(product.stock).coerceAtLeast(1)
            cartDao.insertCartItem(CartItemEntity(customerId = customerId, productId = productId, quantity = initialQty))
        }
        return Result.success(Unit)
    }

    suspend fun updateQuantity(cartItemId: Long, newQuantity: Int) {
        if (newQuantity <= 0) {
            cartDao.deleteCartItemById(cartItemId)
        } else {
            // update
            cartDao.updateCartItem(CartItemEntity(id = cartItemId, customerId = 0, productId = 0, quantity = newQuantity))
        }
    }

    suspend fun updateCartItemQuantity(item: CartItemEntity, newQuantity: Int, maxStock: Int) {
        if (newQuantity <= 0) {
            cartDao.deleteCartItem(item)
        } else {
            val safeQty = newQuantity.coerceAtMost(maxStock)
            cartDao.updateCartItem(item.copy(quantity = safeQty))
        }
    }

    suspend fun removeCartItem(item: CartItemEntity) = cartDao.deleteCartItem(item)
    suspend fun clearCart(customerId: Long) = cartDao.clearCartForCustomer(customerId)
}
