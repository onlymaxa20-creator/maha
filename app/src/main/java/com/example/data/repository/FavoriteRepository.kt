package com.example.data.repository

import com.example.data.local.dao.FavoriteDao
import com.example.data.local.dao.ProductDao
import com.example.data.local.entity.FavoriteItemEntity
import com.example.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FavoriteRepository(
    private val favoriteDao: FavoriteDao,
    private val productDao: ProductDao
) {
    fun isFavorite(customerId: Long, productId: Long): Flow<Boolean> =
        favoriteDao.isFavorite(customerId, productId)

    fun getFavoriteProductIds(customerId: Long): Flow<List<Long>> =
        favoriteDao.getFavoriteProductIds(customerId)

    fun getFavoriteProducts(customerId: Long): Flow<List<ProductEntity>> {
        return favoriteDao.getFavorites(customerId).map { favorites ->
            val productIds = favorites.map { it.productId }
            if (productIds.isEmpty()) emptyList() else productDao.getProductsByIds(productIds)
        }
    }

    suspend fun toggleFavorite(customerId: Long, productId: Long) {
        val currentFavs = favoriteDao.getFavorites(customerId)
        // Check if exists
        try {
            favoriteDao.addFavorite(FavoriteItemEntity(customerId = customerId, productId = productId))
        } catch (e: Exception) {
            favoriteDao.removeFavorite(customerId, productId)
        }
    }

    suspend fun removeFavorite(customerId: Long, productId: Long) {
        favoriteDao.removeFavorite(customerId, productId)
    }
}
