package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.UserInteractionEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserInteractionEventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: UserInteractionEventEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<UserInteractionEventEntity>)

    @Query("SELECT * FROM user_interaction_events WHERE userId = :userId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentEventsByUserId(userId: Long, limit: Int = 300): List<UserInteractionEventEntity>

    @Query("SELECT * FROM user_interaction_events WHERE userId = :userId ORDER BY timestamp DESC")
    fun observeEventsByUserId(userId: Long): Flow<List<UserInteractionEventEntity>>

    @Query("SELECT * FROM user_interaction_events WHERE userId = :userId AND eventType = :eventType ORDER BY timestamp DESC")
    suspend fun getEventsByUserIdAndType(userId: Long, eventType: String): List<UserInteractionEventEntity>

    @Query("SELECT * FROM user_interaction_events ORDER BY timestamp DESC LIMIT :limit")
    fun observeAllEvents(limit: Int = 500): Flow<List<UserInteractionEventEntity>>

    @Query("SELECT * FROM user_interaction_events ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getAllEventsDirect(limit: Int = 1000): List<UserInteractionEventEntity>

    @Query("SELECT productId, COUNT(*) as cnt FROM user_interaction_events WHERE productId IS NOT NULL AND eventType = 'product_view' GROUP BY productId ORDER BY cnt DESC LIMIT :limit")
    suspend fun getMostViewedProductIds(limit: Int = 20): List<ProductCountResult>

    @Query("SELECT productId, COUNT(*) as cnt FROM user_interaction_events WHERE productId IS NOT NULL AND (eventType = 'product_like' OR eventType = 'wishlist_add') GROUP BY productId ORDER BY cnt DESC LIMIT :limit")
    suspend fun getMostLikedProductIds(limit: Int = 20): List<ProductCountResult>

    @Query("SELECT productId, COUNT(*) as cnt FROM user_interaction_events WHERE productId IS NOT NULL AND eventType = 'add_to_cart' GROUP BY productId ORDER BY cnt DESC LIMIT :limit")
    suspend fun getMostAddedToCartProductIds(limit: Int = 20): List<ProductCountResult>

    @Query("SELECT productId, COUNT(*) as cnt FROM user_interaction_events WHERE productId IS NOT NULL AND eventType = 'purchase' GROUP BY productId ORDER BY cnt DESC LIMIT :limit")
    suspend fun getMostPurchasedProductIds(limit: Int = 20): List<ProductCountResult>

    @Query("SELECT categoryId, COUNT(*) as cnt FROM user_interaction_events WHERE categoryId IS NOT NULL GROUP BY categoryId ORDER BY cnt DESC LIMIT :limit")
    suspend fun getMostPopularCategoryIds(limit: Int = 10): List<CategoryCountResult>

    @Query("SELECT COUNT(*) FROM user_interaction_events WHERE eventType = 'recommendation_impression'")
    suspend fun getImpressionCount(): Int

    @Query("SELECT COUNT(*) FROM user_interaction_events WHERE eventType = 'recommendation_click'")
    suspend fun getClickCount(): Int

    @Query("DELETE FROM user_interaction_events WHERE productId = :productId")
    suspend fun deleteEventsForProduct(productId: Long)

    @Query("DELETE FROM user_interaction_events WHERE sellerId = :sellerId")
    suspend fun deleteEventsForSeller(sellerId: Long)

    @Query("DELETE FROM user_interaction_events WHERE timestamp < :cutoffTimestamp")
    suspend fun deleteOldEvents(cutoffTimestamp: Long)
}

data class ProductCountResult(
    val productId: Long,
    val cnt: Int
)

data class CategoryCountResult(
    val categoryId: Long,
    val cnt: Int
)
