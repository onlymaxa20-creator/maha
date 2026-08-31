package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.CartItemEntity
import com.example.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

data class CartItemWithProduct(
    val cartItem: CartItemEntity,
    val product: ProductEntity
)

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items WHERE customerId = :customerId ORDER BY addedAt DESC")
    fun getCartItems(customerId: Long): Flow<List<CartItemEntity>>

    @Query("SELECT * FROM cart_items WHERE customerId = :customerId AND productId = :productId LIMIT 1")
    suspend fun getCartItem(customerId: Long, productId: Long): CartItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(item: CartItemEntity): Long

    @Update
    suspend fun updateCartItem(item: CartItemEntity)

    @Delete
    suspend fun deleteCartItem(item: CartItemEntity)

    @Query("DELETE FROM cart_items WHERE id = :cartItemId")
    suspend fun deleteCartItemById(cartItemId: Long)

    @Query("DELETE FROM cart_items WHERE customerId = :customerId")
    suspend fun clearCartForCustomer(customerId: Long)

    @Query("SELECT COUNT(*) FROM cart_items WHERE customerId = :customerId")
    fun getCartCount(customerId: Long): Flow<Int>
}
