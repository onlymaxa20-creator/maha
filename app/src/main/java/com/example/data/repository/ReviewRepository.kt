package com.example.data.repository

import android.util.Log
import com.example.data.local.dao.ReviewDao
import com.example.data.local.entity.ReviewEntity
import com.example.data.remote.FirestoreSyncService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReviewRepository(
    private val reviewDao: ReviewDao,
    private val firestoreSyncService: FirestoreSyncService = FirestoreSyncService()
) {
    private val TAG = "ReviewRepository"

    /**
     * Starts listening to Firestore for real-time reviews from customers.
     */
    fun startRealtimeReviewSync(scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            try {
                firestoreSyncService.observeReviewsRealtime().collect { remoteReviews ->
                    for (rev in remoteReviews) {
                        try {
                            reviewDao.insertReview(rev)
                        } catch (_: Exception) {}
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Reviews realtime sync notice: ${e.message}")
            }
        }
    }

    fun getReviewsForProduct(productId: Long): Flow<List<ReviewEntity>> =
        reviewDao.getReviewsForProduct(productId)

    fun getAllReviews(): Flow<List<ReviewEntity>> =
        reviewDao.getAllReviews()

    fun getAverageRatingForProduct(productId: Long): Flow<Double?> =
        reviewDao.getAverageRatingForProduct(productId)

    fun getReviewCountForProduct(productId: Long): Flow<Int> =
        reviewDao.getReviewCountForProduct(productId)

    suspend fun addReview(
        productId: Long,
        userId: Long,
        userName: String,
        userPhone: String,
        rating: Int,
        comment: String
    ): Long = withContext(Dispatchers.IO) {
        val review = ReviewEntity(
            productId = productId,
            userId = userId,
            userName = userName.ifBlank { "Mijoz" },
            userPhone = userPhone,
            rating = rating.coerceIn(1, 5),
            comment = comment.trim()
        )
        val id = reviewDao.insertReview(review)
        try {
            firestoreSyncService.saveReview(review.copy(id = id))
        } catch (e: Exception) {
            Log.w(TAG, "Review saved locally; cloud sync notice: ${e.message}")
        }
        id
    }

    suspend fun deleteReview(reviewId: Long) = withContext(Dispatchers.IO) {
        reviewDao.deleteReview(reviewId)
    }
}
