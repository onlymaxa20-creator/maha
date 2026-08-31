package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Stores real-time user behavior events for Personalized Product Recommendations.
 * Supports events:
 * - product_view
 * - product_like
 * - product_unlike
 * - add_to_cart
 * - remove_from_cart
 * - purchase
 * - search
 * - category_view
 * - seller_view
 * - product_share
 * - wishlist_add
 * - wishlist_remove
 * - recommendation_impression
 * - recommendation_click
 */
@Entity(
    tableName = "user_interaction_events",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["productId"]),
        Index(value = ["eventType"]),
        Index(value = ["categoryId"]),
        Index(value = ["sellerId"]),
        Index(value = ["timestamp"])
    ]
)
data class UserInteractionEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long = 0L, // 0 for guest / unauthenticated
    val productId: Long? = null,
    val eventType: String, // e.g. "product_view", "add_to_cart", etc.
    val categoryId: Long? = null,
    val sellerId: Long? = null,
    val searchQuery: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
