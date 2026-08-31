package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE isAvailable = 1 AND stock > 0 ORDER BY createdAt DESC")
    fun getAllAvailableProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE (isPromotional = 1 OR (originalPrice > price AND originalPrice > 0)) AND isAvailable = 1 AND stock > 0 ORDER BY createdAt DESC")
    fun getPromotionalProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products ORDER BY createdAt DESC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products ORDER BY createdAt DESC")
    suspend fun getAllProductsDirect(): List<ProductEntity>

    @Query("SELECT * FROM products WHERE categoryId = :categoryId AND isAvailable = 1 AND stock > 0 ORDER BY createdAt DESC")
    fun getAvailableProductsByCategory(categoryId: Long): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE sellerId = :sellerId ORDER BY createdAt DESC")
    fun getProductsBySeller(sellerId: Long): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE (name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR categoryName LIKE '%' || :query || '%') AND isAvailable = 1 AND stock > 0 ORDER BY createdAt DESC")
    fun searchProducts(query: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    fun getProductById(id: Long): Flow<ProductEntity?>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductByIdDirect(id: Long): ProductEntity?

    @Query("SELECT * FROM products WHERE id IN (:ids)")
    suspend fun getProductsByIds(ids: List<Long>): List<ProductEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProductById(id: Long)

    @Query("SELECT * FROM products WHERE sellerId = :sellerId")
    suspend fun getProductsBySellerDirect(sellerId: Long): List<ProductEntity>

    @Query("DELETE FROM products WHERE sellerId = :sellerId")
    suspend fun deleteProductsBySeller(sellerId: Long)

    @Query("SELECT COUNT(*) FROM products")
    fun getProductsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM products WHERE isAvailable = 1 AND stock > 0")
    fun getAvailableProductsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM products WHERE sellerId = :sellerId")
    fun getProductsCountBySeller(sellerId: Long): Flow<Int>

    @Query("UPDATE products SET stock = :stock WHERE id = :productId")
    suspend fun updateStock(productId: Long, stock: Int)

    @Query("UPDATE products SET isAvailable = :isAvailable WHERE id = :productId")
    suspend fun updateAvailability(productId: Long, isAvailable: Boolean)
}
