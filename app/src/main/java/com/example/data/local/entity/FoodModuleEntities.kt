package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "food_restaurants",
    indices = [Index(value = ["userId"])]
)
data class FoodRestaurantEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long = 0,
    val name: String,
    val ownerName: String,
    val phone: String,
    val address: String,
    val logoUri: String = "",
    val coverUri: String = "",
    val description: String = "",
    val workingHours: String = "09:00 - 23:00",
    val category: String = "Milliy taomlar",
    val deliveryInfo: String = "Yetkazib berish 100% BEPUL (0 so‘m)",
    val status: String = "TASDIQLANGAN", // "KUTILMOQDA", "TASDIQLANGAN", "RAD_ETILGAN", "BLOKLANGAN"
    val rating: Double = 5.0,
    val reviewsCount: Int = 0,
    val isOpen: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "food_categories")
data class FoodCategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val iconEmoji: String = "🍽️",
    val orderIndex: Int = 0,
    val isActive: Boolean = true
)

@Entity(
    tableName = "food_products",
    indices = [
        Index(value = ["restaurantId"]),
        Index(value = ["categoryId"])
    ]
)
data class FoodProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val restaurantId: Long,
    val restaurantName: String,
    val categoryId: Long,
    val categoryName: String,
    val name: String,
    val description: String = "",
    val price: Double,
    val imageUri: String = "",
    val preparationTime: String = "20-30 daqiqa",
    val ingredients: String = "",
    val isAvailable: Boolean = true,
    val status: String = "APPROVED", // "DRAFT", "PENDING_APPROVAL", "APPROVED", "REJECTED"
    val rejectionReason: String = "",
    val rating: Double = 5.0,
    val reviewsCount: Int = 0,
    val salesCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "food_orders",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["restaurantId"])
    ]
)
data class FoodOrderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long = 0,
    val restaurantId: Long = 0,
    val restaurantName: String,
    val customerName: String,
    val customerPhone: String,
    val deliveryAddress: String,
    val deliveryLatitude: Double = 40.6622,
    val deliveryLongitude: Double = 68.1672,
    val deliveryStreet: String = "",
    val deliveryHouseNumber: String = "",
    val deliveryLandmark: String = "",
    val itemsSummary: String,
    val itemsJson: String = "",
    val totalPrice: Double,
    val deliveryFee: Double = 0.0, // 100% BEPUL yetkazib berish
    val paymentMethod: String = "NAQD_PUL", // Faqat naqd pul
    val status: String = "YANGI", // "YANGI", "QABUL_QILINDI", "TAYYORLANMOQDA", "TAYYOR", "YETKAZILMOQDA", "YETKAZILDI", "BEKOR_QILINDI"
    val rejectionReason: String = "",
    val customerNote: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "food_banners")
data class FoodBannerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val imageUri: String = "",
    val restaurantId: Long? = null,
    val foodId: Long? = null,
    val badgeText: String = "AKSIYA",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "food_promotions")
data class FoodPromotionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val discountPercentage: Int = 10,
    val foodId: Long = 0,
    val foodName: String = "",
    val restaurantId: Long = 0,
    val restaurantName: String = "",
    val isActive: Boolean = true
)

@Entity(tableName = "food_reviews")
data class FoodReviewEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderId: Long = 0,
    val userId: Long = 0,
    val userName: String,
    val restaurantId: Long = 0,
    val foodId: Long = 0,
    val rating: Int = 5,
    val comment: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
