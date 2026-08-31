package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.FavoriteItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorite_items WHERE customerId = :customerId ORDER BY addedAt DESC")
    fun getFavorites(customerId: Long): Flow<List<FavoriteItemEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_items WHERE customerId = :customerId AND productId = :productId)")
    fun isFavorite(customerId: Long, productId: Long): Flow<Boolean>

    @Query("SELECT productId FROM favorite_items WHERE customerId = :customerId")
    fun getFavoriteProductIds(customerId: Long): Flow<List<Long>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addFavorite(favorite: FavoriteItemEntity): Long

    @Query("DELETE FROM favorite_items WHERE customerId = :customerId AND productId = :productId")
    suspend fun removeFavorite(customerId: Long, productId: Long)

    @Delete
    suspend fun deleteFavorite(favorite: FavoriteItemEntity)
}
