package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.ReviewEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewDao {
    @Query("SELECT * FROM reviews WHERE productId = :productId ORDER BY createdAt DESC")
    fun getReviewsForProduct(productId: Long): Flow<List<ReviewEntity>>

    @Query("SELECT * FROM reviews ORDER BY createdAt DESC")
    fun getAllReviews(): Flow<List<ReviewEntity>>

    @Query("SELECT AVG(rating) FROM reviews WHERE productId = :productId")
    fun getAverageRatingForProduct(productId: Long): Flow<Double?>

    @Query("SELECT COUNT(*) FROM reviews WHERE productId = :productId")
    fun getReviewCountForProduct(productId: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ReviewEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviews(reviews: List<ReviewEntity>)

    @Query("DELETE FROM reviews WHERE id = :id")
    suspend fun deleteReview(id: Long)
}
