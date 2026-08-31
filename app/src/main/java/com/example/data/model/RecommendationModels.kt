package com.example.data.model

import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.ProductEntity

/**
 * Configurable weights for recommendation calculation.
 */
data class RecommendationWeights(
    val purchaseWeight: Double = 10.0,
    val addToCartWeight: Double = 7.0,
    val likeWeight: Double = 5.0,
    val viewWeight: Double = 3.0,
    val searchWeight: Double = 2.5,
    val categoryViewWeight: Double = 1.5,
    val sellerViewWeight: Double = 1.5,
    val recommendationClickWeight: Double = 2.0,
    val productShareWeight: Double = 3.5,
    val unlikePenalty: Double = -4.0,
    val removeFromCartPenalty: Double = -3.0
)

/**
 * Aggregated interest profile of a user based on real interaction history.
 */
data class UserInterestProfile(
    val categoryAffinities: Map<Long, Double> = emptyMap(),
    val sellerAffinities: Map<Long, Double> = emptyMap(),
    val searchedKeywords: List<String> = emptyList(),
    val viewedProductIds: Set<Long> = emptySet(),
    val likedProductIds: Set<Long> = emptySet(),
    val cartProductIds: Set<Long> = emptySet(),
    val purchasedProductIds: Set<Long> = emptySet(),
    val avgPrice: Double? = null,
    val minPrice: Double? = null,
    val maxPrice: Double? = null
)

/**
 * Admin analytics data model for recommendation performance and customer behaviors.
 */
data class RecommendationAnalyticsModel(
    val totalEvents: Int = 0,
    val topViewedProducts: List<Pair<ProductEntity, Int>> = emptyList(),
    val topLikedProducts: List<Pair<ProductEntity, Int>> = emptyList(),
    val topCartProducts: List<Pair<ProductEntity, Int>> = emptyList(),
    val topPurchasedProducts: List<Pair<ProductEntity, Int>> = emptyList(),
    val topCategories: List<Pair<CategoryEntity, Int>> = emptyList(),
    val impressionsCount: Int = 0,
    val clicksCount: Int = 0,
    val clickThroughRate: Double = 0.0
)
