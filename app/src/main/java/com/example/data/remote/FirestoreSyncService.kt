package com.example.data.remote

import android.util.Log
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.ClassifiedAdEntity
import com.example.data.local.entity.FoodOrderEntity
import com.example.data.local.entity.FoodProductEntity
import com.example.data.local.entity.FoodRestaurantEntity
import com.example.data.local.entity.JobVacancyEntity
import com.example.data.local.entity.OrderEntity
import com.example.data.local.entity.OrderItemEntity
import com.example.data.local.entity.PromoBannerEntity
import com.example.data.local.entity.ReviewEntity
import com.example.data.local.entity.ServiceMasterEntity
import com.example.data.local.entity.SupportInfoEntity
import com.example.data.local.entity.UserEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Universal Real-time Firebase Firestore synchronization service across all user devices.
 * Powers immediate updates for:
 * 1. Users (Customers, Sellers, Admins)
 * 2. Orders & Order Status (Live order tracking)
 * 3. Categories
 * 4. Customer Reviews & Ratings
 * 5. Classified Ads, Jobs & Master Services
 */
class FirestoreSyncService {
    companion object {
        private const val TAG = "FirestoreSyncService"
        const val COLLECTION_USERS = "users"
        const val COLLECTION_ORDERS = "orders"
        const val COLLECTION_CATEGORIES = "categories"
        const val COLLECTION_REVIEWS = "reviews"
        const val COLLECTION_ADS = "classified_ads"
        const val COLLECTION_JOBS = "job_vacancies"
        const val COLLECTION_SERVICES = "service_masters"
        const val COLLECTION_BANNERS = "promo_banners"
        const val COLLECTION_FOOD_RESTAURANTS = "food_restaurants"
        const val COLLECTION_FOOD_PRODUCTS = "food_products"
        const val COLLECTION_FOOD_ORDERS = "food_orders"
        const val COLLECTION_SUPPORT_CONFIG = "app_support_config"
    }

    private val firestore: FirebaseFirestore?
        get() {
            return try {
                FirebaseFirestore.getInstance()
            } catch (e: Exception) {
                Log.w(TAG, "Firebase Firestore is not initialized: ${e.message}")
                null
            }
        }

    // ==================== REAL-TIME USERS ====================

    fun observeUsersRealtime(): Flow<List<UserEntity>> = callbackFlow {
        val db = firestore
        if (db == null) {
            awaitClose { }
            return@callbackFlow
        }

        var listener: ListenerRegistration? = null
        try {
            listener = db.collection(COLLECTION_USERS)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Firestore Users snapshot error: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val usersList = mutableListOf<UserEntity>()
                        for (doc in snapshot.documents) {
                            try {
                                val id = (doc.get("id") as? Number)?.toLong() ?: doc.id.toLongOrNull() ?: 0L
                                val role = doc.getString("role") ?: "CUSTOMER"
                                val login = doc.getString("login") ?: ""
                                val email = doc.getString("email") ?: ""
                                val passwordHash = doc.getString("passwordHash") ?: ""
                                val salt = doc.getString("salt") ?: ""
                                val fullName = doc.getString("fullName") ?: ""
                                val phone = doc.getString("phone") ?: ""
                                val storeName = doc.getString("storeName") ?: ""
                                val savedAddress = doc.getString("savedAddress") ?: ""
                                val isActive = doc.getBoolean("isActive") ?: true
                                val createdAt = (doc.get("createdAt") as? Number)?.toLong() ?: System.currentTimeMillis()

                                if (id > 0 && login.isNotBlank()) {
                                    usersList.add(
                                        UserEntity(
                                            id = id,
                                            role = role,
                                            login = login,
                                            email = email,
                                            passwordHash = passwordHash,
                                            salt = salt,
                                            fullName = fullName,
                                            phone = phone,
                                            storeName = storeName,
                                            savedAddress = savedAddress,
                                            isActive = isActive,
                                            createdAt = createdAt
                                        )
                                    )
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error parsing user document ${doc.id}: ${e.message}")
                            }
                        }
                        trySend(usersList)
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to attach Users listener: ${e.message}")
        }

        awaitClose {
            listener?.remove()
        }
    }

    suspend fun saveUser(user: UserEntity): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            val map = hashMapOf<String, Any>(
                "id" to user.id,
                "role" to user.role,
                "login" to user.login,
                "email" to user.email,
                "passwordHash" to user.passwordHash,
                "salt" to user.salt,
                "fullName" to user.fullName,
                "phone" to user.phone,
                "storeName" to user.storeName,
                "savedAddress" to user.savedAddress,
                "isActive" to user.isActive,
                "createdAt" to user.createdAt
            )
            val docId = if (user.phone.isNotBlank()) user.phone else user.id.toString()
            db.collection(COLLECTION_USERS)
                .document(docId)
                .set(map, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save user to Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun deleteUser(userId: Long, phone: String = "", login: String = ""): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            if (phone.isNotBlank()) {
                db.collection(COLLECTION_USERS).document(phone).delete().await()
            }
            if (userId > 0) {
                db.collection(COLLECTION_USERS).document(userId.toString()).delete().await()
            }
            if (login.isNotBlank()) {
                db.collection(COLLECTION_USERS).document(login).delete().await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete user from Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    // ==================== REAL-TIME ORDERS ====================

    fun observeOrdersRealtime(): Flow<List<Pair<OrderEntity, List<OrderItemEntity>>>> = callbackFlow {
        val db = firestore
        if (db == null) {
            awaitClose { }
            return@callbackFlow
        }

        var listener: ListenerRegistration? = null
        try {
            listener = db.collection(COLLECTION_ORDERS)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Firestore Orders snapshot error: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val orderList = mutableListOf<Pair<OrderEntity, List<OrderItemEntity>>>()
                        for (doc in snapshot.documents) {
                            try {
                                val id = (doc.get("id") as? Number)?.toLong() ?: doc.id.toLongOrNull() ?: 0L
                                val orderNumber = doc.getString("orderNumber") ?: "ORD-$id"
                                val customerId = (doc.get("customerId") as? Number)?.toLong() ?: 0L
                                val customerName = doc.getString("customerName") ?: ""
                                val customerPhone = doc.getString("customerPhone") ?: ""
                                val customerEmail = doc.getString("customerEmail") ?: ""
                                val deliveryCity = doc.getString("deliveryCity") ?: "Gagarin shahri"
                                val deliveryDistrict = doc.getString("deliveryDistrict") ?: "Mirzacho‘l tumani"
                                val deliveryAddress = doc.getString("deliveryAddress") ?: ""
                                val deliveryLatitude = (doc.get("deliveryLatitude") as? Number)?.toDouble() ?: 40.6622
                                val deliveryLongitude = (doc.get("deliveryLongitude") as? Number)?.toDouble() ?: 68.1672
                                val deliveryStreet = doc.getString("deliveryStreet") ?: ""
                                val deliveryHouseNumber = doc.getString("deliveryHouseNumber") ?: ""
                                val deliveryLandmark = doc.getString("deliveryLandmark") ?: ""
                                val deliveryNotes = doc.getString("deliveryNotes") ?: ""
                                val isFreeDelivery = doc.getBoolean("isFreeDelivery") ?: true
                                val totalAmount = (doc.get("totalAmount") as? Number)?.toDouble() ?: 0.0
                                val status = doc.getString("status") ?: "Yangi"
                                val createdAt = (doc.get("createdAt") as? Number)?.toLong() ?: System.currentTimeMillis()
                                val updatedAt = (doc.get("updatedAt") as? Number)?.toLong() ?: System.currentTimeMillis()

                                val itemsRaw = doc.get("items") as? List<Map<String, Any>> ?: emptyList()
                                val itemsList = itemsRaw.mapNotNull { itemMap ->
                                    try {
                                        OrderItemEntity(
                                            id = (itemMap["id"] as? Number)?.toLong() ?: 0L,
                                            orderId = id,
                                            productId = (itemMap["productId"] as? Number)?.toLong() ?: 0L,
                                            sellerId = (itemMap["sellerId"] as? Number)?.toLong() ?: 1L,
                                            sellerName = itemMap["sellerName"] as? String ?: "",
                                            productName = itemMap["productName"] as? String ?: "",
                                            productCategory = itemMap["productCategory"] as? String ?: "",
                                            unitPrice = (itemMap["unitPrice"] as? Number)?.toDouble() ?: 0.0,
                                            quantity = (itemMap["quantity"] as? Number)?.toInt() ?: 1,
                                            unit = itemMap["unit"] as? String ?: "dona",
                                            itemTotal = (itemMap["itemTotal"] as? Number)?.toDouble() ?: 0.0
                                        )
                                    } catch (_: Exception) {
                                        null
                                    }
                                }

                                if (id > 0) {
                                    val order = OrderEntity(
                                        id = id,
                                        orderNumber = orderNumber,
                                        customerId = customerId,
                                        customerName = customerName,
                                        customerPhone = customerPhone,
                                        customerEmail = customerEmail,
                                        deliveryCity = deliveryCity,
                                        deliveryDistrict = deliveryDistrict,
                                        deliveryAddress = deliveryAddress,
                                        deliveryLatitude = deliveryLatitude,
                                        deliveryLongitude = deliveryLongitude,
                                        deliveryStreet = deliveryStreet,
                                        deliveryHouseNumber = deliveryHouseNumber,
                                        deliveryLandmark = deliveryLandmark,
                                        deliveryNotes = deliveryNotes,
                                        isFreeDelivery = isFreeDelivery,
                                        totalAmount = totalAmount,
                                        status = status,
                                        createdAt = createdAt,
                                        updatedAt = updatedAt
                                    )
                                    orderList.add(Pair(order, itemsList))
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error parsing order document ${doc.id}: ${e.message}")
                            }
                        }
                        trySend(orderList)
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to attach Orders listener: ${e.message}")
        }

        awaitClose {
            listener?.remove()
        }
    }

    suspend fun fetchOrdersOnce(): List<Pair<OrderEntity, List<OrderItemEntity>>> {
        val db = firestore ?: return emptyList()
        return try {
            val snapshot = db.collection(COLLECTION_ORDERS).get().await()
            val orderList = mutableListOf<Pair<OrderEntity, List<OrderItemEntity>>>()
            for (doc in snapshot.documents) {
                try {
                    val id = (doc.get("id") as? Number)?.toLong() ?: doc.id.toLongOrNull() ?: 0L
                    val orderNumber = doc.getString("orderNumber") ?: "ORD-$id"
                    val customerId = (doc.get("customerId") as? Number)?.toLong() ?: 0L
                    val customerName = doc.getString("customerName") ?: ""
                    val customerPhone = doc.getString("customerPhone") ?: ""
                    val customerEmail = doc.getString("customerEmail") ?: ""
                    val deliveryCity = doc.getString("deliveryCity") ?: "Gagarin shahri"
                    val deliveryDistrict = doc.getString("deliveryDistrict") ?: "Mirzacho‘l tumani"
                    val deliveryAddress = doc.getString("deliveryAddress") ?: ""
                    val deliveryLatitude = (doc.get("deliveryLatitude") as? Number)?.toDouble() ?: 40.6622
                    val deliveryLongitude = (doc.get("deliveryLongitude") as? Number)?.toDouble() ?: 68.1672
                    val deliveryStreet = doc.getString("deliveryStreet") ?: ""
                    val deliveryHouseNumber = doc.getString("deliveryHouseNumber") ?: ""
                    val deliveryLandmark = doc.getString("deliveryLandmark") ?: ""
                    val deliveryNotes = doc.getString("deliveryNotes") ?: ""
                    val isFreeDelivery = doc.getBoolean("isFreeDelivery") ?: true
                    val totalAmount = (doc.get("totalAmount") as? Number)?.toDouble() ?: 0.0
                    val status = doc.getString("status") ?: "Yangi"
                    val createdAt = (doc.get("createdAt") as? Number)?.toLong() ?: System.currentTimeMillis()
                    val updatedAt = (doc.get("updatedAt") as? Number)?.toLong() ?: System.currentTimeMillis()

                    val itemsRaw = doc.get("items") as? List<Map<String, Any>> ?: emptyList()
                    val itemsList = itemsRaw.mapNotNull { itemMap ->
                        try {
                            OrderItemEntity(
                                id = (itemMap["id"] as? Number)?.toLong() ?: 0L,
                                orderId = id,
                                productId = (itemMap["productId"] as? Number)?.toLong() ?: 0L,
                                sellerId = (itemMap["sellerId"] as? Number)?.toLong() ?: 1L,
                                sellerName = itemMap["sellerName"] as? String ?: "",
                                productName = itemMap["productName"] as? String ?: "",
                                productCategory = itemMap["productCategory"] as? String ?: "",
                                unitPrice = (itemMap["unitPrice"] as? Number)?.toDouble() ?: 0.0,
                                quantity = (itemMap["quantity"] as? Number)?.toInt() ?: 1,
                                unit = itemMap["unit"] as? String ?: "dona",
                                itemTotal = (itemMap["itemTotal"] as? Number)?.toDouble() ?: 0.0
                            )
                        } catch (_: Exception) {
                            null
                        }
                    }

                    if (id > 0) {
                        val order = OrderEntity(
                            id = id,
                            orderNumber = orderNumber,
                            customerId = customerId,
                            customerName = customerName,
                            customerPhone = customerPhone,
                            customerEmail = customerEmail,
                            deliveryCity = deliveryCity,
                            deliveryDistrict = deliveryDistrict,
                            deliveryAddress = deliveryAddress,
                            deliveryLatitude = deliveryLatitude,
                            deliveryLongitude = deliveryLongitude,
                            deliveryStreet = deliveryStreet,
                            deliveryHouseNumber = deliveryHouseNumber,
                            deliveryLandmark = deliveryLandmark,
                            deliveryNotes = deliveryNotes,
                            isFreeDelivery = isFreeDelivery,
                            totalAmount = totalAmount,
                            status = status,
                            createdAt = createdAt,
                            updatedAt = updatedAt
                        )
                        orderList.add(Pair(order, itemsList))
                    }
                } catch (_: Exception) {}
            }
            orderList
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching orders once: ${e.message}")
            emptyList()
        }
    }

    suspend fun saveOrderWithItems(order: OrderEntity, items: List<OrderItemEntity>): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            val itemsData = items.map { item ->
                hashMapOf(
                    "id" to item.id,
                    "orderId" to item.orderId,
                    "productId" to item.productId,
                    "sellerId" to item.sellerId,
                    "sellerName" to item.sellerName,
                    "productName" to item.productName,
                    "productCategory" to item.productCategory,
                    "unitPrice" to item.unitPrice,
                    "quantity" to item.quantity,
                    "unit" to item.unit,
                    "itemTotal" to item.itemTotal
                )
            }
            val map = hashMapOf<String, Any>(
                "id" to order.id,
                "orderNumber" to order.orderNumber,
                "customerId" to order.customerId,
                "customerName" to order.customerName,
                "customerPhone" to order.customerPhone,
                "customerEmail" to order.customerEmail,
                "deliveryCity" to order.deliveryCity,
                "deliveryDistrict" to order.deliveryDistrict,
                "deliveryAddress" to order.deliveryAddress,
                "deliveryLatitude" to order.deliveryLatitude,
                "deliveryLongitude" to order.deliveryLongitude,
                "deliveryStreet" to order.deliveryStreet,
                "deliveryHouseNumber" to order.deliveryHouseNumber,
                "deliveryLandmark" to order.deliveryLandmark,
                "deliveryNotes" to order.deliveryNotes,
                "isFreeDelivery" to order.isFreeDelivery,
                "totalAmount" to order.totalAmount,
                "status" to order.status,
                "createdAt" to order.createdAt,
                "updatedAt" to order.updatedAt,
                "items" to itemsData
            )
            db.collection(COLLECTION_ORDERS)
                .document(order.id.toString())
                .set(map, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save order to Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun updateOrderStatus(orderId: Long, newStatus: String): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            db.collection(COLLECTION_ORDERS)
                .document(orderId.toString())
                .update(
                    mapOf(
                        "status" to newStatus,
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update order status in Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    // ==================== REAL-TIME CATEGORIES ====================

    fun observeCategoriesRealtime(): Flow<List<CategoryEntity>> = callbackFlow {
        val db = firestore
        if (db == null) {
            awaitClose { }
            return@callbackFlow
        }

        var listener: ListenerRegistration? = null
        try {
            listener = db.collection(COLLECTION_CATEGORIES)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Categories snapshot error: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val categoryList = mutableListOf<CategoryEntity>()
                        for (doc in snapshot.documents) {
                            try {
                                val id = (doc.get("id") as? Number)?.toLong() ?: doc.id.toLongOrNull() ?: 0L
                                val name = doc.getString("name") ?: ""
                                val iconKey = doc.getString("iconKey") ?: "general"
                                if (id > 0 && name.isNotBlank()) {
                                    categoryList.add(CategoryEntity(id = id, name = name, iconKey = iconKey))
                                }
                            } catch (_: Exception) {}
                        }
                        trySend(categoryList)
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Categories listener error: ${e.message}")
        }

        awaitClose { listener?.remove() }
    }

    suspend fun saveCategory(category: CategoryEntity): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            db.collection(COLLECTION_CATEGORIES)
                .document(category.id.toString())
                .set(
                    mapOf(
                        "id" to category.id,
                        "name" to category.name,
                        "iconKey" to category.iconKey
                    ),
                    SetOptions.merge()
                ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCategory(categoryId: Long): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            db.collection(COLLECTION_CATEGORIES)
                .document(categoryId.toString())
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== REAL-TIME REVIEWS ====================

    fun observeReviewsRealtime(): Flow<List<ReviewEntity>> = callbackFlow {
        val db = firestore
        if (db == null) {
            awaitClose { }
            return@callbackFlow
        }

        var listener: ListenerRegistration? = null
        try {
            listener = db.collection(COLLECTION_REVIEWS)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Reviews snapshot error: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val reviewList = mutableListOf<ReviewEntity>()
                        for (doc in snapshot.documents) {
                            try {
                                val id = (doc.get("id") as? Number)?.toLong() ?: doc.id.toLongOrNull() ?: 0L
                                val productId = (doc.get("productId") as? Number)?.toLong() ?: 0L
                                val userId = (doc.get("userId") as? Number)?.toLong() ?: 0L
                                val userName = doc.getString("userName") ?: "Mijoz"
                                val userPhone = doc.getString("userPhone") ?: ""
                                val rating = (doc.get("rating") as? Number)?.toInt() ?: 5
                                val comment = doc.getString("comment") ?: ""
                                val createdAt = (doc.get("createdAt") as? Number)?.toLong() ?: System.currentTimeMillis()

                                if (id > 0 && productId > 0) {
                                    reviewList.add(
                                        ReviewEntity(
                                            id = id,
                                            productId = productId,
                                            userId = userId,
                                            userName = userName,
                                            userPhone = userPhone,
                                            rating = rating,
                                            comment = comment,
                                            createdAt = createdAt
                                        )
                                    )
                                }
                            } catch (_: Exception) {}
                        }
                        trySend(reviewList)
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Reviews listener error: ${e.message}")
        }

        awaitClose { listener?.remove() }
    }

    suspend fun saveReview(review: ReviewEntity): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            db.collection(COLLECTION_REVIEWS)
                .document(review.id.toString())
                .set(
                    mapOf(
                        "id" to review.id,
                        "productId" to review.productId,
                        "userId" to review.userId,
                        "userName" to review.userName,
                        "userPhone" to review.userPhone,
                        "rating" to review.rating,
                        "comment" to review.comment,
                        "createdAt" to review.createdAt
                    ),
                    SetOptions.merge()
                ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== REAL-TIME CLASSIFIED ADS ====================

    fun observeAdsRealtime(): Flow<List<ClassifiedAdEntity>> = callbackFlow {
        val db = firestore
        if (db == null) {
            awaitClose { }
            return@callbackFlow
        }

        var listener: ListenerRegistration? = null
        try {
            listener = db.collection(COLLECTION_ADS)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshot != null) {
                        val list = mutableListOf<ClassifiedAdEntity>()
                        for (doc in snapshot.documents) {
                            try {
                                val id = (doc.get("id") as? Number)?.toLong() ?: doc.id.toLongOrNull() ?: 0L
                                val userId = (doc.get("userId") as? Number)?.toLong() ?: 0L
                                val authorName = doc.getString("authorName") ?: ""
                                val authorPhone = doc.getString("authorPhone") ?: ""
                                val title = doc.getString("title") ?: ""
                                val category = doc.getString("category") ?: "Boshqa"
                                val price = (doc.get("price") as? Number)?.toDouble() ?: 0.0
                                val isNegotiable = doc.getBoolean("isNegotiable") ?: true
                                val location = doc.getString("location") ?: "Gagarin"
                                val description = doc.getString("description") ?: ""
                                val imageUri = doc.getString("imageUri") ?: ""
                                val viewsCount = (doc.get("viewsCount") as? Number)?.toInt() ?: 0
                                val createdAt = (doc.get("createdAt") as? Number)?.toLong() ?: System.currentTimeMillis()

                                if (id > 0 && title.isNotBlank()) {
                                    list.add(
                                        ClassifiedAdEntity(
                                            id = id,
                                            userId = userId,
                                            authorName = authorName,
                                            authorPhone = authorPhone,
                                            title = title,
                                            category = category,
                                            price = price,
                                            isNegotiable = isNegotiable,
                                            location = location,
                                            description = description,
                                            imageUri = imageUri,
                                            viewsCount = viewsCount,
                                            createdAt = createdAt
                                        )
                                    )
                                }
                            } catch (_: Exception) {}
                        }
                        trySend(list)
                    }
                }
        } catch (_: Exception) {}

        awaitClose { listener?.remove() }
    }

    suspend fun saveAd(ad: ClassifiedAdEntity): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            db.collection(COLLECTION_ADS)
                .document(ad.id.toString())
                .set(
                    mapOf(
                        "id" to ad.id,
                        "userId" to ad.userId,
                        "authorName" to ad.authorName,
                        "authorPhone" to ad.authorPhone,
                        "title" to ad.title,
                        "category" to ad.category,
                        "price" to ad.price,
                        "isNegotiable" to ad.isNegotiable,
                        "location" to ad.location,
                        "description" to ad.description,
                        "imageUri" to ad.imageUri,
                        "viewsCount" to ad.viewsCount,
                        "createdAt" to ad.createdAt
                    ),
                    SetOptions.merge()
                ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAd(adId: Long): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            db.collection(COLLECTION_ADS).document(adId.toString()).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clearAllAdsRemote(): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            val snapshot = db.collection(COLLECTION_ADS).get().await()
            for (doc in snapshot.documents) {
                doc.reference.delete().await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== REAL-TIME JOBS (VAKANSIYALAR) ====================

    fun observeJobsRealtime(): Flow<List<JobVacancyEntity>> = callbackFlow {
        val db = firestore
        if (db == null) {
            awaitClose { }
            return@callbackFlow
        }

        var listener: ListenerRegistration? = null
        try {
            listener = db.collection(COLLECTION_JOBS)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshot != null) {
                        val list = mutableListOf<JobVacancyEntity>()
                        for (doc in snapshot.documents) {
                            try {
                                val id = (doc.get("id") as? Number)?.toLong() ?: doc.id.toLongOrNull() ?: 0L
                                val userId = (doc.get("userId") as? Number)?.toLong() ?: 0L
                                val title = doc.getString("title") ?: ""
                                val companyName = doc.getString("companyName") ?: ""
                                val category = doc.getString("category") ?: "Boshqa"
                                val salaryText = doc.getString("salaryText") ?: "Kelishilgan"
                                val jobType = doc.getString("jobType") ?: "To‘liq bandlik"
                                val location = doc.getString("location") ?: "Gagarin"
                                val contactPhone = doc.getString("contactPhone") ?: ""
                                val contactPerson = doc.getString("contactPerson") ?: ""
                                val requirements = doc.getString("requirements") ?: ""
                                val description = doc.getString("description") ?: ""
                                val isActive = doc.getBoolean("isActive") ?: true
                                val createdAt = (doc.get("createdAt") as? Number)?.toLong() ?: System.currentTimeMillis()

                                if (id > 0 && title.isNotBlank()) {
                                    list.add(
                                        JobVacancyEntity(
                                            id = id,
                                            userId = userId,
                                            title = title,
                                            companyName = companyName,
                                            category = category,
                                            salaryText = salaryText,
                                            jobType = jobType,
                                            location = location,
                                            contactPhone = contactPhone,
                                            contactPerson = contactPerson,
                                            requirements = requirements,
                                            description = description,
                                            isActive = isActive,
                                            createdAt = createdAt
                                        )
                                    )
                                }
                            } catch (_: Exception) {}
                        }
                        trySend(list)
                    }
                }
        } catch (_: Exception) {}

        awaitClose { listener?.remove() }
    }

    suspend fun saveJob(job: JobVacancyEntity): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            db.collection(COLLECTION_JOBS)
                .document(job.id.toString())
                .set(
                    mapOf(
                        "id" to job.id,
                        "userId" to job.userId,
                        "title" to job.title,
                        "companyName" to job.companyName,
                        "category" to job.category,
                        "salaryText" to job.salaryText,
                        "jobType" to job.jobType,
                        "location" to job.location,
                        "contactPhone" to job.contactPhone,
                        "contactPerson" to job.contactPerson,
                        "requirements" to job.requirements,
                        "description" to job.description,
                        "isActive" to job.isActive,
                        "createdAt" to job.createdAt
                    ),
                    SetOptions.merge()
                ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteJob(jobId: Long): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            db.collection(COLLECTION_JOBS).document(jobId.toString()).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clearAllJobsRemote(): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            val snapshot = db.collection(COLLECTION_JOBS).get().await()
            for (doc in snapshot.documents) {
                doc.reference.delete().await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== REAL-TIME SERVICES (USTALAR) ====================

    fun observeServicesRealtime(): Flow<List<ServiceMasterEntity>> = callbackFlow {
        val db = firestore
        if (db == null) {
            awaitClose { }
            return@callbackFlow
        }

        var listener: ListenerRegistration? = null
        try {
            listener = db.collection(COLLECTION_SERVICES)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshot != null) {
                        val list = mutableListOf<ServiceMasterEntity>()
                        for (doc in snapshot.documents) {
                            try {
                                val id = (doc.get("id") as? Number)?.toLong() ?: doc.id.toLongOrNull() ?: 0L
                                val userId = (doc.get("userId") as? Number)?.toLong() ?: 0L
                                val masterName = doc.getString("masterName") ?: ""
                                val category = doc.getString("category") ?: "Boshqa"
                                val phone = doc.getString("phone") ?: ""
                                val experienceYears = (doc.get("experienceYears") as? Number)?.toInt() ?: 1
                                val priceRange = doc.getString("priceRange") ?: "Kelishilgan holda"
                                val description = doc.getString("description") ?: ""
                                val rating = (doc.get("rating") as? Number)?.toDouble() ?: 4.9
                                val reviewsCount = (doc.get("reviewsCount") as? Number)?.toInt() ?: 10
                                val isAvailable = doc.getBoolean("isAvailable") ?: true
                                val imageUri = doc.getString("imageUri") ?: ""

                                if (id > 0 && masterName.isNotBlank()) {
                                    list.add(
                                        ServiceMasterEntity(
                                            id = id,
                                            userId = userId,
                                            masterName = masterName,
                                            category = category,
                                            phone = phone,
                                            experienceYears = experienceYears,
                                            priceRange = priceRange,
                                            description = description,
                                            rating = rating,
                                            reviewsCount = reviewsCount,
                                            isAvailable = isAvailable,
                                            imageUri = imageUri
                                        )
                                    )
                                }
                            } catch (_: Exception) {}
                        }
                        trySend(list)
                    }
                }
        } catch (_: Exception) {}

        awaitClose { listener?.remove() }
    }

    suspend fun saveService(service: ServiceMasterEntity): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            db.collection(COLLECTION_SERVICES)
                .document(service.id.toString())
                .set(
                    mapOf(
                        "id" to service.id,
                        "userId" to service.userId,
                        "masterName" to service.masterName,
                        "category" to service.category,
                        "phone" to service.phone,
                        "experienceYears" to service.experienceYears,
                        "priceRange" to service.priceRange,
                        "description" to service.description,
                        "rating" to service.rating,
                        "reviewsCount" to service.reviewsCount,
                        "isAvailable" to service.isAvailable,
                        "imageUri" to service.imageUri
                    ),
                    SetOptions.merge()
                ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteService(serviceId: Long): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            db.collection(COLLECTION_SERVICES).document(serviceId.toString()).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clearAllServicesRemote(): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            val snapshot = db.collection(COLLECTION_SERVICES).get().await()
            for (doc in snapshot.documents) {
                doc.reference.delete().await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== REAL-TIME PROMO BANNERS (REKLAMALAR) ====================

    fun observeBannersRealtime(): Flow<List<PromoBannerEntity>> = callbackFlow {
        val db = firestore
        if (db == null) {
            awaitClose { }
            return@callbackFlow
        }

        var listener: ListenerRegistration? = null
        try {
            listener = db.collection(COLLECTION_BANNERS)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Banners snapshot error: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val list = mutableListOf<PromoBannerEntity>()
                        for (doc in snapshot.documents) {
                            try {
                                val id = (doc.get("id") as? Number)?.toLong() ?: doc.id.toLongOrNull() ?: 0L
                                val name = doc.getString("name") ?: ""
                                val title = doc.getString("title") ?: ""
                                val description = doc.getString("description") ?: ""
                                val imageUrl = doc.getString("imageUrl") ?: ""
                                val targetLink = doc.getString("targetLink") ?: ""
                                val startDate = (doc.get("startDate") as? Number)?.toLong() ?: System.currentTimeMillis()
                                val endDate = (doc.get("endDate") as? Number)?.toLong() ?: (System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000))
                                val badgeText = doc.getString("badgeText") ?: "REKLAMA"
                                val gradientType = doc.getString("gradientType") ?: "purple"
                                val actionTag = doc.getString("actionTag") ?: ""
                                val isActive = doc.getBoolean("isActive") ?: true
                                val createdAt = (doc.get("createdAt") as? Number)?.toLong() ?: System.currentTimeMillis()

                                if (id > 0 && title.isNotBlank()) {
                                    list.add(
                                        PromoBannerEntity(
                                            id = id,
                                            name = name,
                                            title = title,
                                            description = description,
                                            imageUrl = imageUrl,
                                            targetLink = targetLink,
                                            startDate = startDate,
                                            endDate = endDate,
                                            badgeText = badgeText,
                                            gradientType = gradientType,
                                            actionTag = actionTag,
                                            isActive = isActive,
                                            createdAt = createdAt
                                        )
                                    )
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error parsing banner ${doc.id}: ${e.message}")
                            }
                        }
                        trySend(list)
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to attach Banners listener: ${e.message}")
        }

        awaitClose { listener?.remove() }
    }

    suspend fun saveBanner(banner: PromoBannerEntity): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            val map = hashMapOf<String, Any>(
                "id" to banner.id,
                "name" to banner.name,
                "title" to banner.title,
                "description" to banner.description,
                "imageUrl" to banner.imageUrl,
                "targetLink" to banner.targetLink,
                "startDate" to banner.startDate,
                "endDate" to banner.endDate,
                "badgeText" to banner.badgeText,
                "gradientType" to banner.gradientType,
                "actionTag" to banner.actionTag,
                "isActive" to banner.isActive,
                "createdAt" to banner.createdAt
            )
            db.collection(COLLECTION_BANNERS)
                .document(banner.id.toString())
                .set(map, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save banner to Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun deleteBanner(bannerId: Long): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            db.collection(COLLECTION_BANNERS)
                .document(bannerId.toString())
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete banner from Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun updateBannerStatus(bannerId: Long, isActive: Boolean): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            db.collection(COLLECTION_BANNERS)
                .document(bannerId.toString())
                .update("isActive", isActive)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update banner status: ${e.message}", e)
            Result.failure(e)
        }
    }

    // ==================== REAL-TIME FOOD RESTAURANTS ====================

    fun observeFoodRestaurantsRealtime(): Flow<List<FoodRestaurantEntity>> = callbackFlow {
        val db = firestore
        if (db == null) {
            awaitClose { }
            return@callbackFlow
        }

        var listener: ListenerRegistration? = null
        try {
            listener = db.collection(COLLECTION_FOOD_RESTAURANTS)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshot != null) {
                        val list = mutableListOf<FoodRestaurantEntity>()
                        for (doc in snapshot.documents) {
                            try {
                                val id = (doc.get("id") as? Number)?.toLong() ?: doc.id.toLongOrNull() ?: 0L
                                val userId = (doc.get("userId") as? Number)?.toLong() ?: 0L
                                val name = doc.getString("name") ?: ""
                                val ownerName = doc.getString("ownerName") ?: ""
                                val phone = doc.getString("phone") ?: ""
                                val address = doc.getString("address") ?: ""
                                val logoUri = doc.getString("logoUri") ?: ""
                                val coverUri = doc.getString("coverUri") ?: ""
                                val description = doc.getString("description") ?: ""
                                val workingHours = doc.getString("workingHours") ?: "09:00 - 23:00"
                                val category = doc.getString("category") ?: "Milliy taomlar"
                                val deliveryInfo = doc.getString("deliveryInfo") ?: "Yetkazib berish 100% BEPUL (0 so‘m)"
                                val status = doc.getString("status") ?: "TASDIQLANGAN"
                                val rating = (doc.get("rating") as? Number)?.toDouble() ?: 5.0
                                val reviewsCount = (doc.get("reviewsCount") as? Number)?.toInt() ?: 0
                                val isOpen = doc.getBoolean("isOpen") ?: true
                                val createdAt = (doc.get("createdAt") as? Number)?.toLong() ?: System.currentTimeMillis()

                                if (id > 0 && name.isNotBlank()) {
                                    list.add(
                                        FoodRestaurantEntity(
                                            id = id,
                                            userId = userId,
                                            name = name,
                                            ownerName = ownerName,
                                            phone = phone,
                                            address = address,
                                            logoUri = logoUri,
                                            coverUri = coverUri,
                                            description = description,
                                            workingHours = workingHours,
                                            category = category,
                                            deliveryInfo = deliveryInfo,
                                            status = status,
                                            rating = rating,
                                            reviewsCount = reviewsCount,
                                            isOpen = isOpen,
                                            createdAt = createdAt
                                        )
                                    )
                                }
                            } catch (_: Exception) {}
                        }
                        trySend(list)
                    }
                }
        } catch (_: Exception) {}

        awaitClose { listener?.remove() }
    }

    suspend fun saveFoodRestaurant(restaurant: FoodRestaurantEntity): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            db.collection(COLLECTION_FOOD_RESTAURANTS)
                .document(restaurant.id.toString())
                .set(
                    mapOf(
                        "id" to restaurant.id,
                        "userId" to restaurant.userId,
                        "name" to restaurant.name,
                        "ownerName" to restaurant.ownerName,
                        "phone" to restaurant.phone,
                        "address" to restaurant.address,
                        "logoUri" to restaurant.logoUri,
                        "coverUri" to restaurant.coverUri,
                        "description" to restaurant.description,
                        "workingHours" to restaurant.workingHours,
                        "category" to restaurant.category,
                        "deliveryInfo" to restaurant.deliveryInfo,
                        "status" to restaurant.status,
                        "rating" to restaurant.rating,
                        "reviewsCount" to restaurant.reviewsCount,
                        "isOpen" to restaurant.isOpen,
                        "createdAt" to restaurant.createdAt
                    ),
                    SetOptions.merge()
                ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteFoodRestaurant(restaurantId: Long): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            db.collection(COLLECTION_FOOD_RESTAURANTS).document(restaurantId.toString()).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== REAL-TIME FOOD PRODUCTS ====================

    fun observeFoodProductsRealtime(): Flow<List<FoodProductEntity>> = callbackFlow {
        val db = firestore
        if (db == null) {
            awaitClose { }
            return@callbackFlow
        }

        var listener: ListenerRegistration? = null
        try {
            listener = db.collection(COLLECTION_FOOD_PRODUCTS)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshot != null) {
                        val list = mutableListOf<FoodProductEntity>()
                        for (doc in snapshot.documents) {
                            try {
                                val id = (doc.get("id") as? Number)?.toLong() ?: doc.id.toLongOrNull() ?: 0L
                                val restaurantId = (doc.get("restaurantId") as? Number)?.toLong() ?: 0L
                                val restaurantName = doc.getString("restaurantName") ?: ""
                                val categoryId = (doc.get("categoryId") as? Number)?.toLong() ?: 0L
                                val categoryName = doc.getString("categoryName") ?: "Milliy taomlar"
                                val name = doc.getString("name") ?: ""
                                val description = doc.getString("description") ?: ""
                                val price = (doc.get("price") as? Number)?.toDouble() ?: 0.0
                                val imageUri = doc.getString("imageUri") ?: ""
                                val preparationTime = doc.getString("preparationTime") ?: "20-30 daqiqa"
                                val ingredients = doc.getString("ingredients") ?: ""
                                val isAvailable = doc.getBoolean("isAvailable") ?: true
                                val status = doc.getString("status") ?: "APPROVED"
                                val rejectionReason = doc.getString("rejectionReason") ?: ""
                                val rating = (doc.get("rating") as? Number)?.toDouble() ?: 5.0
                                val reviewsCount = (doc.get("reviewsCount") as? Number)?.toInt() ?: 0
                                val salesCount = (doc.get("salesCount") as? Number)?.toInt() ?: 0
                                val createdAt = (doc.get("createdAt") as? Number)?.toLong() ?: System.currentTimeMillis()

                                if (id > 0 && name.isNotBlank()) {
                                    list.add(
                                        FoodProductEntity(
                                            id = id,
                                            restaurantId = restaurantId,
                                            restaurantName = restaurantName,
                                            categoryId = categoryId,
                                            categoryName = categoryName,
                                            name = name,
                                            description = description,
                                            price = price,
                                            imageUri = imageUri,
                                            preparationTime = preparationTime,
                                            ingredients = ingredients,
                                            isAvailable = isAvailable,
                                            status = status,
                                            rejectionReason = rejectionReason,
                                            rating = rating,
                                            reviewsCount = reviewsCount,
                                            salesCount = salesCount,
                                            createdAt = createdAt
                                        )
                                    )
                                }
                            } catch (_: Exception) {}
                        }
                        trySend(list)
                    }
                }
        } catch (_: Exception) {}

        awaitClose { listener?.remove() }
    }

    suspend fun saveFoodProduct(product: FoodProductEntity): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            db.collection(COLLECTION_FOOD_PRODUCTS)
                .document(product.id.toString())
                .set(
                    mapOf(
                        "id" to product.id,
                        "restaurantId" to product.restaurantId,
                        "restaurantName" to product.restaurantName,
                        "categoryId" to product.categoryId,
                        "categoryName" to product.categoryName,
                        "name" to product.name,
                        "description" to product.description,
                        "price" to product.price,
                        "imageUri" to product.imageUri,
                        "preparationTime" to product.preparationTime,
                        "ingredients" to product.ingredients,
                        "isAvailable" to product.isAvailable,
                        "status" to product.status,
                        "rejectionReason" to product.rejectionReason,
                        "rating" to product.rating,
                        "reviewsCount" to product.reviewsCount,
                        "salesCount" to product.salesCount,
                        "createdAt" to product.createdAt
                    ),
                    SetOptions.merge()
                ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteFoodProduct(productId: Long): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            db.collection(COLLECTION_FOOD_PRODUCTS).document(productId.toString()).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== REAL-TIME FOOD ORDERS ====================

    fun observeFoodOrdersRealtime(): Flow<List<FoodOrderEntity>> = callbackFlow {
        val db = firestore
        if (db == null) {
            awaitClose { }
            return@callbackFlow
        }

        var listener: ListenerRegistration? = null
        try {
            listener = db.collection(COLLECTION_FOOD_ORDERS)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshot != null) {
                        val list = mutableListOf<FoodOrderEntity>()
                        for (doc in snapshot.documents) {
                            try {
                                val id = (doc.get("id") as? Number)?.toLong() ?: doc.id.toLongOrNull() ?: 0L
                                val userId = (doc.get("userId") as? Number)?.toLong() ?: 0L
                                val restaurantId = (doc.get("restaurantId") as? Number)?.toLong() ?: 0L
                                val restaurantName = doc.getString("restaurantName") ?: ""
                                val customerName = doc.getString("customerName") ?: ""
                                val customerPhone = doc.getString("customerPhone") ?: ""
                                val deliveryAddress = doc.getString("deliveryAddress") ?: ""
                                val deliveryLatitude = (doc.get("deliveryLatitude") as? Number)?.toDouble() ?: 40.6622
                                val deliveryLongitude = (doc.get("deliveryLongitude") as? Number)?.toDouble() ?: 68.1672
                                val deliveryStreet = doc.getString("deliveryStreet") ?: ""
                                val deliveryHouseNumber = doc.getString("deliveryHouseNumber") ?: ""
                                val deliveryLandmark = doc.getString("deliveryLandmark") ?: ""
                                val itemsSummary = doc.getString("itemsSummary") ?: ""
                                val itemsJson = doc.getString("itemsJson") ?: ""
                                val totalPrice = (doc.get("totalPrice") as? Number)?.toDouble() ?: 0.0
                                val deliveryFee = (doc.get("deliveryFee") as? Number)?.toDouble() ?: 0.0
                                val paymentMethod = doc.getString("paymentMethod") ?: "NAQD_PUL"
                                val status = doc.getString("status") ?: "YANGI"
                                val rejectionReason = doc.getString("rejectionReason") ?: ""
                                val customerNote = doc.getString("customerNote") ?: ""
                                val createdAt = (doc.get("createdAt") as? Number)?.toLong() ?: System.currentTimeMillis()

                                if (id > 0) {
                                    list.add(
                                        FoodOrderEntity(
                                            id = id,
                                            userId = userId,
                                            restaurantId = restaurantId,
                                            restaurantName = restaurantName,
                                            customerName = customerName,
                                            customerPhone = customerPhone,
                                            deliveryAddress = deliveryAddress,
                                            deliveryLatitude = deliveryLatitude,
                                            deliveryLongitude = deliveryLongitude,
                                            deliveryStreet = deliveryStreet,
                                            deliveryHouseNumber = deliveryHouseNumber,
                                            deliveryLandmark = deliveryLandmark,
                                            itemsSummary = itemsSummary,
                                            itemsJson = itemsJson,
                                            totalPrice = totalPrice,
                                            deliveryFee = deliveryFee,
                                            paymentMethod = paymentMethod,
                                            status = status,
                                            rejectionReason = rejectionReason,
                                            customerNote = customerNote,
                                            createdAt = createdAt
                                        )
                                    )
                                }
                            } catch (_: Exception) {}
                        }
                        trySend(list)
                    }
                }
        } catch (_: Exception) {}

        awaitClose { listener?.remove() }
    }

    suspend fun fetchFoodOrdersOnce(): List<FoodOrderEntity> {
        val db = firestore ?: return emptyList()
        return try {
            val snapshot = db.collection(COLLECTION_FOOD_ORDERS).get().await()
            val list = mutableListOf<FoodOrderEntity>()
            for (doc in snapshot.documents) {
                try {
                    val id = (doc.get("id") as? Number)?.toLong() ?: doc.id.toLongOrNull() ?: 0L
                    val userId = (doc.get("userId") as? Number)?.toLong() ?: 0L
                    val restaurantId = (doc.get("restaurantId") as? Number)?.toLong() ?: 0L
                    val restaurantName = doc.getString("restaurantName") ?: ""
                    val customerName = doc.getString("customerName") ?: ""
                    val customerPhone = doc.getString("customerPhone") ?: ""
                    val deliveryAddress = doc.getString("deliveryAddress") ?: ""
                    val deliveryLatitude = (doc.get("deliveryLatitude") as? Number)?.toDouble() ?: 40.6622
                    val deliveryLongitude = (doc.get("deliveryLongitude") as? Number)?.toDouble() ?: 68.1672
                    val deliveryStreet = doc.getString("deliveryStreet") ?: ""
                    val deliveryHouseNumber = doc.getString("deliveryHouseNumber") ?: ""
                    val deliveryLandmark = doc.getString("deliveryLandmark") ?: ""
                    val itemsSummary = doc.getString("itemsSummary") ?: ""
                    val itemsJson = doc.getString("itemsJson") ?: ""
                    val totalPrice = (doc.get("totalPrice") as? Number)?.toDouble() ?: 0.0
                    val deliveryFee = (doc.get("deliveryFee") as? Number)?.toDouble() ?: 0.0
                    val paymentMethod = doc.getString("paymentMethod") ?: "NAQD_PUL"
                    val status = doc.getString("status") ?: "YANGI"
                    val rejectionReason = doc.getString("rejectionReason") ?: ""
                    val customerNote = doc.getString("customerNote") ?: ""
                    val createdAt = (doc.get("createdAt") as? Number)?.toLong() ?: System.currentTimeMillis()

                    if (id > 0) {
                        list.add(
                            FoodOrderEntity(
                                id = id,
                                userId = userId,
                                restaurantId = restaurantId,
                                restaurantName = restaurantName,
                                customerName = customerName,
                                customerPhone = customerPhone,
                                deliveryAddress = deliveryAddress,
                                deliveryLatitude = deliveryLatitude,
                                deliveryLongitude = deliveryLongitude,
                                deliveryStreet = deliveryStreet,
                                deliveryHouseNumber = deliveryHouseNumber,
                                deliveryLandmark = deliveryLandmark,
                                itemsSummary = itemsSummary,
                                itemsJson = itemsJson,
                                totalPrice = totalPrice,
                                deliveryFee = deliveryFee,
                                paymentMethod = paymentMethod,
                                status = status,
                                rejectionReason = rejectionReason,
                                customerNote = customerNote,
                                createdAt = createdAt
                            )
                        )
                    }
                } catch (_: Exception) {}
            }
            list
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching food orders once: ${e.message}")
            emptyList()
        }
    }

    suspend fun saveFoodOrder(order: FoodOrderEntity): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            db.collection(COLLECTION_FOOD_ORDERS)
                .document(order.id.toString())
                .set(
                    mapOf(
                        "id" to order.id,
                        "userId" to order.userId,
                        "restaurantId" to order.restaurantId,
                        "restaurantName" to order.restaurantName,
                        "customerName" to order.customerName,
                        "customerPhone" to order.customerPhone,
                        "deliveryAddress" to order.deliveryAddress,
                        "deliveryLatitude" to order.deliveryLatitude,
                        "deliveryLongitude" to order.deliveryLongitude,
                        "deliveryStreet" to order.deliveryStreet,
                        "deliveryHouseNumber" to order.deliveryHouseNumber,
                        "deliveryLandmark" to order.deliveryLandmark,
                        "itemsSummary" to order.itemsSummary,
                        "itemsJson" to order.itemsJson,
                        "totalPrice" to order.totalPrice,
                        "deliveryFee" to order.deliveryFee,
                        "paymentMethod" to order.paymentMethod,
                        "status" to order.status,
                        "rejectionReason" to order.rejectionReason,
                        "customerNote" to order.customerNote,
                        "createdAt" to order.createdAt
                    ),
                    SetOptions.merge()
                ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateFoodOrderStatus(orderId: Long, status: String): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            db.collection(COLLECTION_FOOD_ORDERS)
                .document(orderId.toString())
                .update("status", status)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ----------------------------------------------------
    // APP SUPPORT & VERSION UPDATE CONFIG SYNC
    // ----------------------------------------------------
    fun observeSupportInfoRealtime(): Flow<SupportInfoEntity?> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        var listener: ListenerRegistration? = null
        try {
            listener = db.collection(COLLECTION_SUPPORT_CONFIG)
                .document("config_main")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Support info snapshot listen failed: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        try {
                            val data = snapshot.data ?: return@addSnapshotListener
                            val entity = SupportInfoEntity(
                                id = (data["id"] as? Long) ?: 1L,
                                phone = (data["phone"] as? String) ?: "+998 90 123 45 67",
                                secondaryPhone = (data["secondaryPhone"] as? String) ?: "",
                                telegramUsername = (data["telegramUsername"] as? String) ?: "@GagarinGoSupport",
                                telegramChannel = (data["telegramChannel"] as? String) ?: "@gagarin_go_app",
                                workingHours = (data["workingHours"] as? String) ?: "Har kuni: 08:00 - 22:00",
                                address = (data["address"] as? String) ?: "Gagarin shahri, Do‘stlik shoh ko‘chasi 12-uy",
                                description = (data["description"] as? String) ?: "Gagarin Go xizmatlari bo‘yicha savollaringiz bo‘lsa, istalgan vaqtda biz bilan bog‘laning!",
                                latestVersionCode = (data["latestVersionCode"] as? Long)?.toInt() ?: 1,
                                latestVersionName = (data["latestVersionName"] as? String) ?: "1.0.0",
                                minRequiredVersionCode = (data["minRequiredVersionCode"] as? Long)?.toInt() ?: 1,
                                isForceUpdate = (data["isForceUpdate"] as? Boolean) ?: false,
                                updateTitle = (data["updateTitle"] as? String) ?: "Ilovaning yangi versiyasi mavjud! 🚀",
                                updateMessage = (data["updateMessage"] as? String) ?: "Gagarin Go ilovasining so‘nggi versiyasiga yangilab, yangi imkoniyatlardan foydalaning.",
                                releaseNotes = (data["releaseNotes"] as? String) ?: "Kichik xatoliklar tuzatildi va tezlik oshirildi.",
                                playStoreUrl = (data["playStoreUrl"] as? String) ?: "https://play.google.com/store/apps/details?id=com.example",
                                telegramApkUrl = (data["telegramApkUrl"] as? String) ?: "https://t.me/gagarin_go_app",
                                updatedAt = (data["updatedAt"] as? Long) ?: System.currentTimeMillis()
                            )
                            trySend(entity)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error mapping support info snapshot: ${e.message}")
                        }
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to start support info snapshot listener: ${e.message}")
        }

        awaitClose {
            listener?.remove()
        }
    }

    suspend fun saveSupportInfo(info: SupportInfoEntity): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            db.collection(COLLECTION_SUPPORT_CONFIG)
                .document("config_main")
                .set(
                    mapOf(
                        "id" to info.id,
                        "phone" to info.phone,
                        "secondaryPhone" to info.secondaryPhone,
                        "telegramUsername" to info.telegramUsername,
                        "telegramChannel" to info.telegramChannel,
                        "workingHours" to info.workingHours,
                        "address" to info.address,
                        "description" to info.description,
                        "latestVersionCode" to info.latestVersionCode,
                        "latestVersionName" to info.latestVersionName,
                        "minRequiredVersionCode" to info.minRequiredVersionCode,
                        "isForceUpdate" to info.isForceUpdate,
                        "updateTitle" to info.updateTitle,
                        "updateMessage" to info.updateMessage,
                        "releaseNotes" to info.releaseNotes,
                        "playStoreUrl" to info.playStoreUrl,
                        "telegramApkUrl" to info.telegramApkUrl,
                        "updatedAt" to info.updatedAt
                    ),
                    SetOptions.merge()
                ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
