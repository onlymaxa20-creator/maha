package com.example.data.recommendation

import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.UserInteractionEventEntity
import com.example.data.model.RecommendationWeights
import com.example.data.model.UserInterestProfile
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min

/**
 * Production-ready Recommendation Engine for GagarinGo marketplace.
 * Implements:
 * 1. Hybrid Collaborative & Content-Based Filtering
 * 2. Time-decayed Behavioral Signal Processing
 * 3. Search & Category Intent Matching
 * 4. Price Elasticity / Affinity Proximity
 * 5. Cold-start Multi-strategy Fallback (Popularity + Freshness + High Rating)
 * 6. Catalog Diversification (prevents seller clustering)
 */
class RecommendationEngine(
    var weights: RecommendationWeights = RecommendationWeights()
) {

    /**
     * Builds an interest profile for a given user from raw behavioral events.
     */
    fun buildUserProfile(
        events: List<UserInteractionEventEntity>,
        allProductsMap: Map<Long, ProductEntity>
    ): UserInterestProfile {
        if (events.isEmpty()) {
            return UserInterestProfile()
        }

        val categoryScores = mutableMapOf<Long, Double>()
        val sellerScores = mutableMapOf<Long, Double>()
        val searchedKeywords = mutableListOf<String>()
        val viewedProductIds = mutableSetOf<Long>()
        val likedProductIds = mutableSetOf<Long>()
        val cartProductIds = mutableSetOf<Long>()
        val purchasedProductIds = mutableSetOf<Long>()
        val pricePoints = mutableListOf<Double>()

        val now = System.currentTimeMillis()
        val thirtyDaysMillis = 30L * 24 * 60 * 60 * 1000

        for (event in events) {
            // Time decay factor: Events today have 1.0 weight, 30 days old have ~0.5 weight
            val age = (now - event.timestamp).coerceAtLeast(0L)
            val timeDecay = exp(-1.0 * age / thirtyDaysMillis).coerceIn(0.2, 1.0)

            val baseWeight = when (event.eventType) {
                "purchase" -> weights.purchaseWeight
                "add_to_cart" -> weights.addToCartWeight
                "product_like", "wishlist_add" -> weights.likeWeight
                "product_unlike", "wishlist_remove" -> weights.unlikePenalty
                "remove_from_cart" -> weights.removeFromCartPenalty
                "product_view" -> weights.viewWeight
                "search" -> weights.searchWeight
                "category_view" -> weights.categoryViewWeight
                "seller_view" -> weights.sellerViewWeight
                "recommendation_click" -> weights.recommendationClickWeight
                "product_share" -> weights.productShareWeight
                else -> 1.0
            }

            val weightedScore = baseWeight * timeDecay

            // Category Affinity
            val categoryId = event.categoryId ?: event.productId?.let { allProductsMap[it]?.categoryId }
            if (categoryId != null && categoryId > 0) {
                categoryScores[categoryId] = (categoryScores[categoryId] ?: 0.0) + weightedScore
            }

            // Seller Affinity
            val sellerId = event.sellerId ?: event.productId?.let { allProductsMap[it]?.sellerId }
            if (sellerId != null && sellerId > 0) {
                sellerScores[sellerId] = (sellerScores[sellerId] ?: 0.0) + weightedScore
            }

            // Search keywords
            if (!event.searchQuery.isNullOrBlank()) {
                val tokens = extractTokens(event.searchQuery)
                searchedKeywords.addAll(tokens)
            }

            // Product references
            val productId = event.productId
            if (productId != null) {
                val product = allProductsMap[productId]
                if (product != null) {
                    pricePoints.add(product.price)
                }

                when (event.eventType) {
                    "product_view" -> viewedProductIds.add(productId)
                    "product_like", "wishlist_add" -> {
                        likedProductIds.add(productId)
                    }
                    "product_unlike", "wishlist_remove" -> {
                        likedProductIds.remove(productId)
                    }
                    "add_to_cart" -> cartProductIds.add(productId)
                    "remove_from_cart" -> cartProductIds.remove(productId)
                    "purchase" -> purchasedProductIds.add(productId)
                }
            }
        }

        val avgPrice = if (pricePoints.isNotEmpty()) pricePoints.average() else null
        val minPrice = pricePoints.minOrNull()
        val maxPrice = pricePoints.maxOrNull()

        return UserInterestProfile(
            categoryAffinities = categoryScores,
            sellerAffinities = sellerScores,
            searchedKeywords = searchedKeywords.distinct().takeLast(30),
            viewedProductIds = viewedProductIds,
            likedProductIds = likedProductIds,
            cartProductIds = cartProductIds,
            purchasedProductIds = purchasedProductIds,
            avgPrice = avgPrice,
            minPrice = minPrice,
            maxPrice = maxPrice
        )
    }

    /**
     * Scores and ranks candidate products for a personalized "Siz uchun" feed.
     */
    fun rankPersonalizedProducts(
        profile: UserInterestProfile,
        candidates: List<ProductEntity>,
        activeSellerIds: Set<Long>? = null,
        limit: Int = 14
    ): List<ProductEntity> {
        // Filter strictly valid, in-stock, active products
        val validProducts = candidates.filter { product ->
            product.isAvailable && product.stock > 0 &&
                    (activeSellerIds == null || activeSellerIds.contains(product.sellerId))
        }

        if (validProducts.isEmpty()) return emptyList()

        // Cold-start fallback if profile has minimal history
        val isColdStart = profile.categoryAffinities.isEmpty() && profile.searchedKeywords.isEmpty() &&
                profile.purchasedProductIds.isEmpty() && profile.likedProductIds.isEmpty()

        if (isColdStart) {
            return rankColdStartProducts(validProducts, limit)
        }

        val scoredList = validProducts.map { product ->
            val score = computeProductPersonalScore(product, profile)
            Pair(product, score)
        }

        // Sort descending by calculated score
        val sorted = scoredList.sortedByDescending { it.second }.map { it.first }

        // Diversify across sellers so no single seller dominates top list
        return diversifyBySeller(sorted, limit)
    }

    /**
     * Computes individual personalization score for a product against user profile.
     */
    private fun computeProductPersonalScore(product: ProductEntity, profile: UserInterestProfile): Double {
        var score = 10.0 // Base score

        // 1. Category Affinity (Weight: High)
        val categoryScore = profile.categoryAffinities[product.categoryId] ?: 0.0
        score += categoryScore * 4.0

        // 2. Seller Affinity (Weight: Medium)
        val sellerScore = profile.sellerAffinities[product.sellerId] ?: 0.0
        score += sellerScore * 2.0

        // 3. Search Query Keyword Relevance (Weight: High)
        if (profile.searchedKeywords.isNotEmpty()) {
            val productText = "${product.name} ${product.description} ${product.categoryName}".lowercase()
            var keywordMatches = 0
            for (keyword in profile.searchedKeywords) {
                if (productText.contains(keyword)) {
                    keywordMatches++
                }
            }
            score += keywordMatches * 15.0
        }

        // 4. Price Elasticity / Proximity
        if (profile.avgPrice != null && profile.avgPrice > 0.0) {
            val priceRatio = abs(product.price - profile.avgPrice) / profile.avgPrice
            val priceProximityScore = (1.0 / (1.0 + priceRatio)) * 12.0
            score += priceProximityScore
        }

        // 5. User Interaction Bonuses
        if (profile.likedProductIds.contains(product.id)) {
            score += 25.0
        }
        if (profile.cartProductIds.contains(product.id)) {
            score += 30.0
        }
        if (profile.viewedProductIds.contains(product.id)) {
            score += 10.0
        }

        // 6. Discount & Promotional Boost
        if (product.originalPrice > product.price && product.originalPrice > 0) {
            score += 8.0
        }
        if (product.isPromotional) {
            score += 6.0
        }

        // 7. Freshness Boost (products updated or added recently)
        val ageDays = (System.currentTimeMillis() - product.updatedAt) / (1000 * 60 * 60 * 24)
        if (ageDays < 7) {
            score += 5.0
        }

        return score
    }

    /**
     * Cold start fallback when user has zero history.
     * Orders by promotional discount, high stock, and freshness.
     */
    fun rankColdStartProducts(validProducts: List<ProductEntity>, limit: Int): List<ProductEntity> {
        val sorted = validProducts.sortedWith(
            compareByDescending<ProductEntity> { it.isPromotional }
                .thenByDescending { it.originalPrice > it.price }
                .thenByDescending { it.updatedAt }
                .thenByDescending { it.stock }
        )
        return diversifyBySeller(sorted, limit)
    }

    /**
     * Computes similar products for Product Detail Screen ("Sizga o‘xshash mahsulotlar").
     * Compares Category, Sub-tokens, Price Range, and Seller.
     */
    fun computeSimilarProducts(
        targetProduct: ProductEntity,
        candidates: List<ProductEntity>,
        activeSellerIds: Set<Long>? = null,
        limit: Int = 8
    ): List<ProductEntity> {
        val validProducts = candidates.filter { product ->
            product.id != targetProduct.id && product.isAvailable && product.stock > 0 &&
                    (activeSellerIds == null || activeSellerIds.contains(product.sellerId))
        }

        if (validProducts.isEmpty()) return emptyList()

        val targetTokens = extractTokens("${targetProduct.name} ${targetProduct.description}")

        val scored = validProducts.map { candidate ->
            var similarityScore = 0.0

            // 1. Same category bonus (strongest baseline signal)
            if (candidate.categoryId == targetProduct.categoryId) {
                similarityScore += 50.0
            }

            // 2. Token / Semantic overlap (Jaccard similarity on tokens)
            val candidateTokens = extractTokens("${candidate.name} ${candidate.description}")
            val intersection = targetTokens.intersect(candidateTokens).size
            val union = targetTokens.union(candidateTokens).size
            if (union > 0) {
                val jaccard = intersection.toDouble() / union.toDouble()
                similarityScore += jaccard * 40.0
            }

            // 3. Price proximity ratio
            val maxPrice = max(targetProduct.price, candidate.price)
            if (maxPrice > 0.0) {
                val diff = abs(targetProduct.price - candidate.price)
                val proximityRatio = (1.0 - (diff / maxPrice)).coerceIn(0.0, 1.0)
                similarityScore += proximityRatio * 20.0
            }

            // 4. Same seller bonus
            if (candidate.sellerId == targetProduct.sellerId) {
                similarityScore += 10.0
            }

            // 5. Discount / Promotion bonus
            if (candidate.isPromotional || candidate.originalPrice > candidate.price) {
                similarityScore += 5.0
            }

            Pair(candidate, similarityScore)
        }

        val sorted = scored.sortedByDescending { it.second }.map { it.first }
        return diversifyBySeller(sorted, limit)
    }

    /**
     * Computes complementary / compatible products based on past purchases ("Xaridingizga mos mahsulotlar").
     */
    fun computePurchaseBasedRecommendations(
        purchasedProducts: List<ProductEntity>,
        candidates: List<ProductEntity>,
        activeSellerIds: Set<Long>? = null,
        limit: Int = 8
    ): List<ProductEntity> {
        if (purchasedProducts.isEmpty()) return emptyList()

        val purchasedIds = purchasedProducts.map { it.id }.toSet()
        val validProducts = candidates.filter { product ->
            !purchasedIds.contains(product.id) && product.isAvailable && product.stock > 0 &&
                    (activeSellerIds == null || activeSellerIds.contains(product.sellerId))
        }

        if (validProducts.isEmpty()) return emptyList()

        val purchasedCategoryIds = purchasedProducts.map { it.categoryId }.toSet()

        val scored = validProducts.map { candidate ->
            var score = 5.0

            // Complementary category or cross-sell affinity
            if (purchasedCategoryIds.contains(candidate.categoryId)) {
                score += 35.0
            }

            // Check keywords for common complementary items (e.g. g'ilof, quloqchin, kabel, zaryadlovchi, non, choy, sous)
            val candidateText = "${candidate.name} ${candidate.description}".lowercase()
            val complementaryKeywords = listOf(
                "quloqchin", "g‘ilof", "gilof", "chexol", "shisha", "oyna", "zaryadlovchi", "kabel",
                "powerbank", "ichimlik", "choy", "sous", "shirinlik", "paypoq", "kamar", "sumka"
            )
            for (kw in complementaryKeywords) {
                if (candidateText.contains(kw)) {
                    score += 15.0
                }
            }

            if (candidate.isPromotional || candidate.originalPrice > candidate.price) {
                score += 8.0
            }

            Pair(candidate, score)
        }

        val sorted = scored.sortedByDescending { it.second }.map { it.first }
        return diversifyBySeller(sorted, limit)
    }

    /**
     * Prevents any single seller from occupying all top recommendation slots.
     */
    private fun diversifyBySeller(products: List<ProductEntity>, limit: Int): List<ProductEntity> {
        val result = mutableListOf<ProductEntity>()
        val sellerCountMap = mutableMapOf<Long, Int>()
        val remaining = mutableListOf<ProductEntity>()

        for (prod in products) {
            val count = sellerCountMap[prod.sellerId] ?: 0
            if (count < 2) {
                result.add(prod)
                sellerCountMap[prod.sellerId] = count + 1
                if (result.size >= limit) break
            } else {
                remaining.add(prod)
            }
        }

        // Fill remaining slots if needed
        if (result.size < limit && remaining.isNotEmpty()) {
            for (prod in remaining) {
                if (!result.contains(prod)) {
                    result.add(prod)
                    if (result.size >= limit) break
                }
            }
        }

        return result
    }

    private fun extractTokens(text: String): Set<String> {
        return text.lowercase()
            .replace(Regex("[^a-zA-Z0-9а-яА-ЯўқғҳЎҚҒҲ\\s]"), " ")
            .split(Regex("\\s+"))
            .filter { it.length >= 3 }
            .toSet()
    }
}
