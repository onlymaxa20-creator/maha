package com.example.data.repository

import android.util.Log
import com.example.data.local.dao.FoodBannerDao
import com.example.data.local.dao.FoodCategoryDao
import com.example.data.local.dao.FoodOrderDao
import com.example.data.local.dao.FoodProductDao
import com.example.data.local.dao.FoodPromotionDao
import com.example.data.local.dao.FoodRestaurantDao
import com.example.data.local.dao.FoodReviewDao
import com.example.data.local.entity.FoodBannerEntity
import com.example.data.local.entity.FoodCategoryEntity
import com.example.data.local.entity.FoodOrderEntity
import com.example.data.local.entity.FoodProductEntity
import com.example.data.local.entity.FoodPromotionEntity
import com.example.data.local.entity.FoodRestaurantEntity
import com.example.data.local.entity.FoodReviewEntity
import com.example.data.remote.FirestoreSyncService
import com.example.data.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FoodRepository(
    private val restaurantDao: FoodRestaurantDao,
    private val categoryDao: FoodCategoryDao,
    private val productDao: FoodProductDao,
    private val orderDao: FoodOrderDao,
    private val bannerDao: FoodBannerDao,
    private val promotionDao: FoodPromotionDao,
    private val reviewDao: FoodReviewDao,
    private val firestoreSyncService: FirestoreSyncService = FirestoreSyncService()
) {
    private val TAG = "FoodRepository"
    private val _foodEvents = MutableSharedFlow<LiveServerNotification>(extraBufferCapacity = 20)
    val foodEvents = _foodEvents.asSharedFlow()

    /**
     * Starts listening to Firestore real-time snapshots for Restaurants, Food Products, and Orders.
     */
    fun startRealtimeSync(scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            try {
                firestoreSyncService.observeFoodRestaurantsRealtime().collectLatest { remoteRestaurants ->
                    try {
                        if (remoteRestaurants.isNotEmpty()) {
                            restaurantDao.insertRestaurants(remoteRestaurants)
                        }
                        val remoteIds = remoteRestaurants.map { it.id }.toSet()
                        val localRestaurants = restaurantDao.getAllRestaurantsDirect()
                        for (local in localRestaurants) {
                            if (!remoteIds.contains(local.id)) {
                                restaurantDao.deleteRestaurant(local.id)
                                productDao.deleteProductsByRestaurant(local.id)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to sync food restaurants: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Restaurants realtime sync notice: ${e.message}")
            }
        }

        scope.launch(Dispatchers.IO) {
            try {
                firestoreSyncService.observeFoodProductsRealtime().collectLatest { remoteProducts ->
                    try {
                        if (remoteProducts.isNotEmpty()) {
                            productDao.insertProducts(remoteProducts)
                        }
                        val remoteIds = remoteProducts.map { it.id }.toSet()
                        val localProducts = productDao.getAllProductsDirect()
                        for (local in localProducts) {
                            if (!remoteIds.contains(local.id)) {
                                productDao.deleteProduct(local.id)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to sync food products: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Food products realtime sync notice: ${e.message}")
            }
        }

        scope.launch(Dispatchers.IO) {
            try {
                firestoreSyncService.observeFoodOrdersRealtime().collectLatest { remoteOrders ->
                    for (remoteOrder in remoteOrders) {
                        try {
                            val local = orderDao.getOrderByIdDirect(remoteOrder.id)
                            if (local == null) {
                                orderDao.insertOrder(remoteOrder)
                                val rest = try { restaurantDao.getRestaurantByIdDirect(remoteOrder.restaurantId) } catch (_: Exception) { null }
                                val sellerUserId = rest?.userId
                                NotificationHelper.showNotification(
                                    title = "🍔 Yangi Taom Buyurtmasi #${remoteOrder.id}!",
                                    message = "${remoteOrder.restaurantName}: ${remoteOrder.itemsSummary} (${remoteOrder.customerName} - ${remoteOrder.customerPhone})",
                                    targetRole = "FOOD_SELLER",
                                    targetUserId = sellerUserId
                                )
                            } else if (local.status != remoteOrder.status) {
                                orderDao.updateOrderStatus(remoteOrder.id, remoteOrder.status)
                                
                                val restName = remoteOrder.restaurantName.ifBlank { "Oshxona" }
                                val (title, message) = when (remoteOrder.status) {
                                    "QABUL_QILINDI" -> Pair(
                                        "✅ Taom buyurtmangiz qabul qilindi!",
                                        "$restName buyurtmangizni qabul qildi va tez orada tayyorlashni boshlaydi."
                                    )
                                    "TAYYORLANMOQDA" -> Pair(
                                        "🍳 Buyurtmangiz tayyorlanmoqda!",
                                        "$restName: Taomingiz mehr bilan tayyorlanmoqda."
                                    )
                                    "TAYYOR" -> Pair(
                                        "🍱 Buyurtmangiz tayyor!",
                                        "$restName: Taomingiz tayyor bo‘ldi va kuryerga topshirilmoqda."
                                    )
                                    "YETKAZILMOQDA" -> Pair(
                                        "🛵 Buyurtmangiz yetkazilmoqda!",
                                        "$restName: Kuryer yo‘lga chiqdi, taomingiz issiq holatda tez orada yetkaziladi!"
                                    )
                                    "YETKAZILDI" -> Pair(
                                        "🎉 Buyurtmangiz yetkazildi!",
                                        "$restName: Taomingiz yetkazib berildi. Yoqimli ishtaha!"
                                    )
                                    "BEKOR_QILINDI" -> Pair(
                                        "❌ Buyurtmangiz bekor qilindi",
                                        "$restName buyurtmangizni bekor qildi. ${remoteOrder.rejectionReason}"
                                    )
                                    else -> Pair(
                                        "🍔 Buyurtma holati yangilandi",
                                        "Buyurtma #${remoteOrder.id} holati: ${remoteOrder.status}"
                                    )
                                }

                                NotificationHelper.showNotification(
                                    title = title,
                                    message = message,
                                    targetRole = "CUSTOMER",
                                    targetUserId = remoteOrder.userId
                                )
                            }
                        } catch (_: Exception) {}
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Food orders realtime sync notice: ${e.message}")
            }
        }
    }

    // --- CUSTOMER FLOW ---
    val approvedRestaurants: Flow<List<FoodRestaurantEntity>> = restaurantDao.getApprovedRestaurants()
    val activeCategories: Flow<List<FoodCategoryEntity>> = categoryDao.getActiveCategories()
    val approvedProducts: Flow<List<FoodProductEntity>> = productDao.getApprovedProducts()
    val activeBanners: Flow<List<FoodBannerEntity>> = bannerDao.getActiveBanners()
    val activePromotions: Flow<List<FoodPromotionEntity>> = promotionDao.getActivePromotions()

    fun getApprovedProductsByRestaurant(restaurantId: Long): Flow<List<FoodProductEntity>> =
        productDao.getApprovedProductsByRestaurant(restaurantId)

    fun getApprovedProductsByCategory(categoryName: String): Flow<List<FoodProductEntity>> =
        if (categoryName.isBlank() || categoryName == "Barchasi") productDao.getApprovedProducts()
        else productDao.getApprovedProductsByCategory(categoryName)

    fun getFoodReviews(foodId: Long): Flow<List<FoodReviewEntity>> =
        reviewDao.getReviewsForFood(foodId)

    fun getRestaurantReviews(restaurantId: Long): Flow<List<FoodReviewEntity>> =
        reviewDao.getReviewsForRestaurant(restaurantId)

    fun getUserOrders(userId: Long): Flow<List<FoodOrderEntity>> =
        orderDao.getOrdersByUser(userId)

    suspend fun placeFoodOrder(
        userId: Long,
        restaurantId: Long,
        restaurantName: String,
        customerName: String,
        customerPhone: String,
        deliveryAddress: String,
        itemsSummary: String,
        itemsJson: String = "",
        totalPrice: Double,
        customerNote: String = "",
        deliveryLatitude: Double = 40.6622,
        deliveryLongitude: Double = 68.1672,
        deliveryStreet: String = "",
        deliveryHouseNumber: String = "",
        deliveryLandmark: String = ""
    ): Result<Long> {
        if (customerPhone.isBlank() || customerPhone.length < 9) {
            return Result.failure(IllegalArgumentException("Telefon raqami kiritilishi shart"))
        }
        if (deliveryAddress.isBlank() || deliveryAddress.length < 3) {
            return Result.failure(IllegalArgumentException("Yetkazib berish manzili kiritilishi shart"))
        }

        val uniqueId = System.currentTimeMillis() * 1000L + (100..999).random()
        val order = FoodOrderEntity(
            id = uniqueId,
            userId = userId,
            restaurantId = restaurantId,
            restaurantName = restaurantName,
            customerName = customerName.ifBlank { "Mijoz" },
            customerPhone = customerPhone.trim(),
            deliveryAddress = deliveryAddress.trim(),
            deliveryLatitude = deliveryLatitude,
            deliveryLongitude = deliveryLongitude,
            deliveryStreet = deliveryStreet.trim(),
            deliveryHouseNumber = deliveryHouseNumber.trim(),
            deliveryLandmark = deliveryLandmark.trim(),
            itemsSummary = itemsSummary,
            itemsJson = itemsJson,
            totalPrice = totalPrice,
            deliveryFee = 0.0, // 100% BEPUL yetkazib berish
            paymentMethod = "NAQD_PUL", // Naqd pul to‘lovi
            status = "YANGI",
            customerNote = customerNote.trim()
        )

        val insertedId = orderDao.insertOrder(order)
        val finalOrderId = if (insertedId > 0) insertedId else uniqueId
        val savedOrder = order.copy(id = finalOrderId)

        try {
            firestoreSyncService.saveFoodOrder(savedOrder)
        } catch (_: Exception) {}

        // Look up seller user id to direct notification specifically to seller
        val rest = try { restaurantDao.getRestaurantByIdDirect(restaurantId) } catch (_: Exception) { null }
        val sellerUserId = rest?.userId

        // Emit notification strictly for seller
        NotificationHelper.showNotification(
            title = "🍔 Yangi Taom Buyurtmasi #${finalOrderId}!",
            message = "${savedOrder.restaurantName}: $itemsSummary ($customerName - ${customerPhone.trim()})",
            targetRole = "FOOD_SELLER",
            targetUserId = sellerUserId
        )

        _foodEvents.tryEmit(
            LiveServerNotification(
                title = "🍔 Yangi Taom Buyurtmasi #$finalOrderId",
                message = "$restaurantName: $itemsSummary | Jami: ${totalPrice.toLong()} so‘m (Yetkazish: BEPUL)"
            )
        )
        return Result.success(finalOrderId)
    }

    suspend fun addReview(
        orderId: Long,
        userId: Long,
        userName: String,
        restaurantId: Long,
        foodId: Long,
        rating: Int,
        comment: String
    ): Long {
        val review = FoodReviewEntity(
            orderId = orderId,
            userId = userId,
            userName = userName,
            restaurantId = restaurantId,
            foodId = foodId,
            rating = rating.coerceIn(1, 5),
            comment = comment.trim()
        )
        return reviewDao.insertReview(review)
    }

    // --- FOOD SELLER FLOW ---
    fun getRestaurantByUserId(userId: Long): Flow<FoodRestaurantEntity?> =
        restaurantDao.getRestaurantByUserId(userId)

    suspend fun getRestaurantByUserIdDirect(userId: Long): FoodRestaurantEntity? =
        restaurantDao.getRestaurantByUserIdDirect(userId)

    fun getProductsByRestaurant(restaurantId: Long): Flow<List<FoodProductEntity>> =
        productDao.getProductsByRestaurant(restaurantId)

    fun getOrdersByRestaurant(restaurantId: Long): Flow<List<FoodOrderEntity>> =
        orderDao.getOrdersByRestaurant(restaurantId)

    suspend fun registerOrUpdateRestaurant(restaurant: FoodRestaurantEntity): Long {
        val uniqueId = if (restaurant.id == 0L) System.currentTimeMillis() * 1000L + (100..999).random() else restaurant.id
        val target = restaurant.copy(id = uniqueId)
        val id = if (restaurant.id == 0L) {
            val inserted = restaurantDao.insertRestaurant(target)
            if (inserted > 0) inserted else uniqueId
        } else {
            restaurantDao.updateRestaurant(target)
            target.id
        }
        val finalRestaurant = target.copy(id = id)
        try {
            firestoreSyncService.saveFoodRestaurant(finalRestaurant)
        } catch (_: Exception) {}

        _foodEvents.tryEmit(
            LiveServerNotification(
                title = "🏪 Yangi Oshxona / Restoran ro‘yxatdan o‘tdi",
                message = "${finalRestaurant.name} (${finalRestaurant.ownerName}) ro‘yxatdan o‘tkazildi."
            )
        )
        return id
    }

    suspend fun addFoodProduct(product: FoodProductEntity): Long {
        val uniqueId = if (product.id == 0L) System.currentTimeMillis() * 1000L + (100..999).random() else product.id
        val target = product.copy(
            id = uniqueId,
            status = "APPROVED",
            isAvailable = true
        )
        val inserted = productDao.insertProduct(target)
        val finalId = if (inserted > 0) inserted else uniqueId
        val finalProduct = target.copy(id = finalId)

        try {
            firestoreSyncService.saveFoodProduct(finalProduct)
        } catch (_: Exception) {}

        _foodEvents.tryEmit(
            LiveServerNotification(
                title = "🍲 Yangi Taom Qo‘shildi",
                message = "${finalProduct.restaurantName}: ${finalProduct.name} menyuga qo‘shildi."
            )
        )
        return finalId
    }

    suspend fun updateFoodProduct(product: FoodProductEntity) {
        productDao.updateProduct(product)
        try {
            firestoreSyncService.saveFoodProduct(product)
        } catch (_: Exception) {}
    }

    suspend fun toggleFoodAvailability(productId: Long, isAvailable: Boolean) {
        productDao.updateAvailability(productId, isAvailable)
        val p = productDao.getProductByIdDirect(productId)
        if (p != null) {
            try {
                firestoreSyncService.saveFoodProduct(p.copy(isAvailable = isAvailable))
            } catch (_: Exception) {}
        }
    }

    suspend fun deleteFoodProduct(productId: Long) {
        productDao.deleteProduct(productId)
        try {
            firestoreSyncService.deleteFoodProduct(productId)
        } catch (_: Exception) {}
    }

    suspend fun updateOrderStatus(orderId: Long, status: String) {
        val existingOrder = orderDao.getOrderByIdDirect(orderId)
        val customerUserId = existingOrder?.userId
        val restaurantName = existingOrder?.restaurantName?.ifBlank { "Oshxona" } ?: "Oshxona"

        orderDao.updateOrderStatus(orderId, status)
        try {
            firestoreSyncService.updateFoodOrderStatus(orderId, status)
        } catch (_: Exception) {}

        val (title, message) = when (status) {
            "QABUL_QILINDI" -> Pair(
                "✅ Taom buyurtmangiz qabul qilindi!",
                "$restaurantName buyurtmangizni qabul qildi va tez orada tayyorlashni boshlaydi."
            )
            "TAYYORLANMOQDA" -> Pair(
                "🍳 Buyurtmangiz tayyorlanmoqda!",
                "$restaurantName: Taomingiz mehr bilan tayyorlanmoqda."
            )
            "TAYYOR" -> Pair(
                "🍱 Buyurtmangiz tayyor!",
                "$restaurantName: Taomingiz tayyor bo‘ldi va kuryerga topshirilmoqda."
            )
            "YETKAZILMOQDA" -> Pair(
                "🛵 Buyurtmangiz yetkazilmoqda!",
                "$restaurantName: Kuryer yo‘lga chiqdi, taomingiz issiq holatda tez orada yetkaziladi!"
            )
            "YETKAZILDI" -> Pair(
                "🎉 Buyurtmangiz yetkazildi!",
                "$restaurantName: Taomingiz muvaffaqiyatli yetkazib berildi. Yoqimli ishtaha!"
            )
            "BEKOR_QILINDI" -> Pair(
                "❌ Buyurtmangiz bekor qilindi",
                "$restaurantName buyurtmangizni bekor qildi."
            )
            else -> Pair(
                "🍔 Taom buyurtmangiz holati",
                "Buyurtma #$orderId holati: $status"
            )
        }

        // Notification for buyer (customer)
        NotificationHelper.showNotification(
            title = title,
            message = message,
            targetRole = "CUSTOMER",
            targetUserId = customerUserId
        )

        _foodEvents.tryEmit(
            LiveServerNotification(
                title = title,
                message = message
            )
        )
    }

    suspend fun rejectOrder(orderId: Long, reason: String) {
        val existingOrder = orderDao.getOrderByIdDirect(orderId)
        val customerUserId = existingOrder?.userId
        val restaurantName = existingOrder?.restaurantName?.ifBlank { "Oshxona" } ?: "Oshxona"

        orderDao.updateOrderRejection(orderId, "BEKOR_QILINDI", reason)
        try {
            firestoreSyncService.updateFoodOrderStatus(orderId, "BEKOR_QILINDI")
        } catch (_: Exception) {}

        val title = "❌ Taom buyurtmasi bekor qilindi"
        val message = "$restaurantName: Buyurtma #$orderId bekor qilindi. Sabab: $reason"

        NotificationHelper.showNotification(
            title = title,
            message = message,
            targetRole = "CUSTOMER",
            targetUserId = customerUserId
        )

        _foodEvents.tryEmit(
            LiveServerNotification(
                title = title,
                message = message
            )
        )
    }

    // --- FOOD ADMIN FLOW ---
    val allRestaurants: Flow<List<FoodRestaurantEntity>> = restaurantDao.getAllRestaurants()
    val allProducts: Flow<List<FoodProductEntity>> = productDao.getAllProducts()
    val allOrders: Flow<List<FoodOrderEntity>> = orderDao.getAllOrders()
    val allCategories: Flow<List<FoodCategoryEntity>> = categoryDao.getAllCategories()
    val allBanners: Flow<List<FoodBannerEntity>> = bannerDao.getAllBanners()
    val allPromotions: Flow<List<FoodPromotionEntity>> = promotionDao.getAllPromotions()

    suspend fun approveRestaurant(id: Long) {
        restaurantDao.updateStatus(id, "TASDIQLANGAN")
        val r = restaurantDao.getRestaurantByIdDirect(id)
        if (r != null) {
            try {
                firestoreSyncService.saveFoodRestaurant(r.copy(status = "TASDIQLANGAN"))
            } catch (_: Exception) {}
        }
    }

    suspend fun blockRestaurant(id: Long) {
        restaurantDao.updateStatus(id, "BLOKLANGAN")
        val r = restaurantDao.getRestaurantByIdDirect(id)
        if (r != null) {
            try {
                firestoreSyncService.saveFoodRestaurant(r.copy(status = "BLOKLANGAN"))
            } catch (_: Exception) {}
        }
    }

    suspend fun unblockRestaurant(id: Long) {
        restaurantDao.updateStatus(id, "TASDIQLANGAN")
        val r = restaurantDao.getRestaurantByIdDirect(id)
        if (r != null) {
            try {
                firestoreSyncService.saveFoodRestaurant(r.copy(status = "TASDIQLANGAN"))
            } catch (_: Exception) {}
        }
    }

    suspend fun deleteRestaurant(id: Long) {
        try {
            val restaurantProducts = productDao.getProductsByRestaurantDirect(id)
            for (p in restaurantProducts) {
                try {
                    firestoreSyncService.deleteFoodProduct(p.id)
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {}
        try {
            productDao.deleteProductsByRestaurant(id)
        } catch (_: Exception) {}
        restaurantDao.deleteRestaurant(id)
        try {
            firestoreSyncService.deleteFoodRestaurant(id)
        } catch (_: Exception) {}
    }

    suspend fun approveProduct(id: Long) {
        productDao.updateModerationStatus(id, "APPROVED", "")
        val p = productDao.getProductByIdDirect(id)
        if (p != null) {
            try {
                firestoreSyncService.saveFoodProduct(p.copy(status = "APPROVED", rejectionReason = ""))
            } catch (_: Exception) {}
        }
    }

    suspend fun rejectProduct(id: Long, reason: String) {
        productDao.updateModerationStatus(id, "REJECTED", reason)
        val p = productDao.getProductByIdDirect(id)
        if (p != null) {
            try {
                firestoreSyncService.saveFoodProduct(p.copy(status = "REJECTED", rejectionReason = reason))
            } catch (_: Exception) {}
        }
    }

    suspend fun createCategory(name: String, iconEmoji: String, orderIndex: Int): Long {
        val cat = FoodCategoryEntity(name = name.trim(), iconEmoji = iconEmoji.trim(), orderIndex = orderIndex)
        return categoryDao.insertCategory(cat)
    }

    suspend fun updateCategory(category: FoodCategoryEntity) {
        categoryDao.updateCategory(category)
    }

    suspend fun deleteCategory(id: Long) {
        categoryDao.deleteCategory(id)
    }

    suspend fun createBanner(
        title: String,
        description: String,
        imageUri: String,
        badgeText: String = "AKSIYA",
        restaurantId: Long? = null,
        foodId: Long? = null
    ): Long {
        val banner = FoodBannerEntity(
            title = title.trim(),
            description = description.trim(),
            imageUri = imageUri.trim(),
            badgeText = badgeText.trim(),
            restaurantId = restaurantId,
            foodId = foodId
        )
        return bannerDao.insertBanner(banner)
    }

    suspend fun deleteBanner(id: Long) {
        bannerDao.deleteBanner(id)
    }

    suspend fun createPromotion(
        title: String,
        discountPercentage: Int,
        foodId: Long,
        foodName: String,
        restaurantId: Long,
        restaurantName: String
    ): Long {
        val promo = FoodPromotionEntity(
            title = title.trim(),
            discountPercentage = discountPercentage,
            foodId = foodId,
            foodName = foodName,
            restaurantId = restaurantId,
            restaurantName = restaurantName
        )
        return promotionDao.insertPromotion(promo)
    }

    suspend fun deletePromotion(id: Long) {
        promotionDao.deletePromotion(id)
    }
}
