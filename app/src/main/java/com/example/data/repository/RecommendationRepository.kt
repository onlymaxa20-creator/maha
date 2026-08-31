package com.example.data.repository

import android.util.Log
import com.example.data.local.dao.CategoryDao
import com.example.data.local.dao.OrderDao
import com.example.data.local.dao.ProductDao
import com.example.data.local.dao.UserDao
import com.example.data.local.dao.UserInteractionEventDao
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.UserInteractionEventEntity
import com.example.data.model.RecommendationAnalyticsModel
import com.example.data.model.RecommendationWeights
import com.example.data.model.UserInterestProfile
import com.example.data.recommendation.RecommendationEngine
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecommendationRepository(
    private val eventDao: UserInteractionEventDao,
    private val productDao: ProductDao,
    private val userDao: UserDao,
    private val categoryDao: CategoryDao,
    private val orderDao: OrderDao? = null
) {
    private val TAG = "RecommendationRepo"
    private val engine = RecommendationEngine()
    private val repoScope = CoroutineScope(Dispatchers.IO)

    fun updateWeights(weights: RecommendationWeights) {
        engine.weights = weights
    }

    /**
     * Records a real-time behavioral event.
     * Writes to Room DB and pushes to Firebase Firestore asynchronously without blocking the UI thread.
     */
    fun trackEvent(
        userId: Long,
        eventType: String,
        productId: Long? = null,
        categoryId: Long? = null,
        sellerId: Long? = null,
        searchQuery: String? = null
    ) {
        repoScope.launch {
            try {
                val event = UserInteractionEventEntity(
                    userId = userId,
                    productId = productId,
                    eventType = eventType,
                    categoryId = categoryId,
                    sellerId = sellerId,
                    searchQuery = searchQuery,
                    timestamp = System.currentTimeMillis()
                )
                eventDao.insertEvent(event)

                // Push to Firestore asynchronously
                try {
                    val firestore = FirebaseFirestore.getInstance()
                    val map = hashMapOf(
                        "userId" to event.userId,
                        "productId" to (event.productId ?: 0L),
                        "eventType" to event.eventType,
                        "categoryId" to (event.categoryId ?: 0L),
                        "sellerId" to (event.sellerId ?: 0L),
                        "searchQuery" to (event.searchQuery ?: ""),
                        "timestamp" to event.timestamp
                    )
                    firestore.collection("recommendation_events")
                        .add(map)
                } catch (e: Exception) {
                    // Firebase may be offline or uninitialized; local Room ensures 100% offline-first reliability
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error tracking event: ${e.message}")
            }
        }
    }

    /**
     * Returns Personalized Products for Home Screen ("Siz uchun").
     * Combines available products, active sellers, and user interaction events.
     */
    fun getPersonalizedRecommendations(userId: Long?, limit: Int = 12): Flow<List<ProductEntity>> {
        return combine(
            productDao.getAllAvailableProducts(),
            userDao.getAllSellers()
        ) { products, sellers ->
            val activeSellerIds = sellers.filter { it.isActive }.map { it.id }.toSet()
            val validProducts = products.filter { activeSellerIds.contains(it.sellerId) }

            if (validProducts.isEmpty()) {
                return@combine emptyList<ProductEntity>()
            }

            val productsMap = products.associateBy { it.id }

            val profile = if (userId != null && userId > 0) {
                val userEvents = try {
                    eventDao.getRecentEventsByUserId(userId, limit = 200)
                } catch (_: Exception) {
                    emptyList()
                }
                engine.buildUserProfile(userEvents, productsMap)
            } else {
                UserInterestProfile()
            }

            engine.rankPersonalizedProducts(
                profile = profile,
                candidates = validProducts,
                activeSellerIds = activeSellerIds,
                limit = limit
            )
        }.flowOn(Dispatchers.Default)
    }

    /**
     * Returns Similar Products for Product Detail Screen ("Sizga o‘xshash mahsulotlar").
     */
    fun getSimilarProducts(productId: Long, limit: Int = 8): Flow<List<ProductEntity>> {
        return combine(
            productDao.getAllAvailableProducts(),
            userDao.getAllSellers()
        ) { products, sellers ->
            val activeSellerIds = sellers.filter { it.isActive }.map { it.id }.toSet()
            val target = products.find { it.id == productId } ?: productDao.getProductByIdDirect(productId)
            if (target == null) {
                return@combine emptyList<ProductEntity>()
            }

            engine.computeSimilarProducts(
                targetProduct = target,
                candidates = products,
                activeSellerIds = activeSellerIds,
                limit = limit
            )
        }.flowOn(Dispatchers.Default)
    }

    /**
     * Returns Complementary / Related Products based on past purchases ("Xaridingizga mos mahsulotlar").
     */
    fun getPurchasedBasedRecommendations(userId: Long?, limit: Int = 8): Flow<List<ProductEntity>> {
        return combine(
            productDao.getAllAvailableProducts(),
            userDao.getAllSellers()
        ) { products, sellers ->
            val activeSellerIds = sellers.filter { it.isActive }.map { it.id }.toSet()
            if (userId == null || userId <= 0) {
                return@combine emptyList<ProductEntity>()
            }

            val purchaseEvents = try {
                eventDao.getEventsByUserIdAndType(userId, "purchase")
            } catch (_: Exception) {
                emptyList()
            }

            val purchasedProductIds = purchaseEvents.mapNotNull { it.productId }.distinct()
            if (purchasedProductIds.isEmpty()) {
                return@combine emptyList<ProductEntity>()
            }

            val purchasedProducts = productDao.getProductsByIds(purchasedProductIds)
            if (purchasedProducts.isEmpty()) {
                return@combine emptyList<ProductEntity>()
            }

            engine.computePurchaseBasedRecommendations(
                purchasedProducts = purchasedProducts,
                candidates = products,
                activeSellerIds = activeSellerIds,
                limit = limit
            )
        }.flowOn(Dispatchers.Default)
    }

    /**
     * Returns popular / trending products in a specific category (excluding currently viewed product).
     */
    fun getCategoryPopularProducts(
        categoryId: Long,
        excludeProductId: Long? = null,
        limit: Int = 8
    ): Flow<List<ProductEntity>> {
        return combine(
            productDao.getAvailableProductsByCategory(categoryId),
            userDao.getAllSellers()
        ) { products, sellers ->
            val activeSellerIds = sellers.filter { it.isActive }.map { it.id }.toSet()
            val filtered = products.filter {
                (excludeProductId == null || it.id != excludeProductId) &&
                        activeSellerIds.contains(it.sellerId)
            }
            engine.rankColdStartProducts(filtered, limit)
        }.flowOn(Dispatchers.Default)
    }

    /**
     * Computes real-time admin analytics for recommendation performance.
     */
    fun getRecommendationAnalytics(): Flow<RecommendationAnalyticsModel> = flow {
        val totalEvents = eventDao.getAllEventsDirect(limit = 1000).size
        val mostViewed = eventDao.getMostViewedProductIds(10)
        val mostLiked = eventDao.getMostLikedProductIds(10)
        val mostAddedToCart = eventDao.getMostAddedToCartProductIds(10)
        val mostPurchased = eventDao.getMostPurchasedProductIds(10)
        val popularCategories = eventDao.getMostPopularCategoryIds(10)
        val impressions = eventDao.getImpressionCount()
        val clicks = eventDao.getClickCount()

        val allProductIds = (mostViewed.map { it.productId } +
                mostLiked.map { it.productId } +
                mostAddedToCart.map { it.productId } +
                mostPurchased.map { it.productId }).distinct()

        val productsMap = if (allProductIds.isNotEmpty()) {
            productDao.getProductsByIds(allProductIds).associateBy { it.id }
        } else {
            emptyMap()
        }

        val allCategoryIds = popularCategories.map { it.categoryId }.distinct()
        val categoriesMap = if (allCategoryIds.isNotEmpty()) {
            categoryDao.getAllCategoriesDirect().associateBy { it.id }
        } else {
            emptyMap()
        }

        val topViewed = mostViewed.mapNotNull { result ->
            productsMap[result.productId]?.let { Pair(it, result.cnt) }
        }
        val topLiked = mostLiked.mapNotNull { result ->
            productsMap[result.productId]?.let { Pair(it, result.cnt) }
        }
        val topCart = mostAddedToCart.mapNotNull { result ->
            productsMap[result.productId]?.let { Pair(it, result.cnt) }
        }
        val topPurchased = mostPurchased.mapNotNull { result ->
            productsMap[result.productId]?.let { Pair(it, result.cnt) }
        }
        val topCats = popularCategories.mapNotNull { result ->
            categoriesMap[result.categoryId]?.let { Pair(it, result.cnt) }
        }

        val ctr = if (impressions > 0) (clicks.toDouble() / impressions.toDouble()) * 100.0 else 0.0

        emit(
            RecommendationAnalyticsModel(
                totalEvents = totalEvents,
                topViewedProducts = topViewed,
                topLikedProducts = topLiked,
                topCartProducts = topCart,
                topPurchasedProducts = topPurchased,
                topCategories = topCats,
                impressionsCount = impressions,
                clicksCount = clicks,
                clickThroughRate = ctr
            )
        )
    }.flowOn(Dispatchers.IO)

    suspend fun cleanupSellerEvents(sellerId: Long) {
        withContext(Dispatchers.IO) {
            eventDao.deleteEventsForSeller(sellerId)
        }
    }
}
