package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.FoodBannerEntity
import com.example.data.local.entity.FoodCategoryEntity
import com.example.data.local.entity.FoodOrderEntity
import com.example.data.local.entity.FoodProductEntity
import com.example.data.local.entity.FoodPromotionEntity
import com.example.data.local.entity.FoodRestaurantEntity
import com.example.data.local.entity.FoodReviewEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodRestaurantDao {
    @Query("SELECT * FROM food_restaurants ORDER BY rating DESC, id DESC")
    fun getAllRestaurants(): Flow<List<FoodRestaurantEntity>>

    @Query("SELECT * FROM food_restaurants ORDER BY rating DESC, id DESC")
    suspend fun getAllRestaurantsDirect(): List<FoodRestaurantEntity>

    @Query("SELECT * FROM food_restaurants WHERE status = 'TASDIQLANGAN' ORDER BY rating DESC, id DESC")
    fun getApprovedRestaurants(): Flow<List<FoodRestaurantEntity>>

    @Query("SELECT * FROM food_restaurants WHERE id = :id LIMIT 1")
    fun getRestaurantById(id: Long): Flow<FoodRestaurantEntity?>

    @Query("SELECT * FROM food_restaurants WHERE id = :id LIMIT 1")
    suspend fun getRestaurantByIdDirect(id: Long): FoodRestaurantEntity?

    @Query("SELECT * FROM food_restaurants WHERE userId = :userId LIMIT 1")
    fun getRestaurantByUserId(userId: Long): Flow<FoodRestaurantEntity?>

    @Query("SELECT * FROM food_restaurants WHERE userId = :userId LIMIT 1")
    suspend fun getRestaurantByUserIdDirect(userId: Long): FoodRestaurantEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRestaurant(restaurant: FoodRestaurantEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRestaurants(restaurants: List<FoodRestaurantEntity>)

    @Update
    suspend fun updateRestaurant(restaurant: FoodRestaurantEntity)

    @Query("UPDATE food_restaurants SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)

    @Query("DELETE FROM food_restaurants WHERE id = :id")
    suspend fun deleteRestaurant(id: Long)
}

@Dao
interface FoodCategoryDao {
    @Query("SELECT * FROM food_categories ORDER BY orderIndex ASC, id ASC")
    fun getAllCategories(): Flow<List<FoodCategoryEntity>>

    @Query("SELECT * FROM food_categories WHERE isActive = 1 ORDER BY orderIndex ASC, id ASC")
    fun getActiveCategories(): Flow<List<FoodCategoryEntity>>

    @Query("SELECT * FROM food_categories WHERE name = :name LIMIT 1")
    suspend fun getCategoryByName(name: String): FoodCategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: FoodCategoryEntity): Long

    @Update
    suspend fun updateCategory(category: FoodCategoryEntity)

    @Query("DELETE FROM food_categories WHERE id = :id")
    suspend fun deleteCategory(id: Long)
}

@Dao
interface FoodProductDao {
    @Query("SELECT * FROM food_products ORDER BY id DESC")
    fun getAllProducts(): Flow<List<FoodProductEntity>>

    @Query("SELECT * FROM food_products ORDER BY id DESC")
    suspend fun getAllProductsDirect(): List<FoodProductEntity>

    @Query("SELECT * FROM food_products WHERE status = 'APPROVED' AND isAvailable = 1 ORDER BY salesCount DESC, id DESC")
    fun getApprovedProducts(): Flow<List<FoodProductEntity>>

    @Query("SELECT * FROM food_products WHERE restaurantId = :restaurantId ORDER BY id DESC")
    fun getProductsByRestaurant(restaurantId: Long): Flow<List<FoodProductEntity>>

    @Query("SELECT * FROM food_products WHERE restaurantId = :restaurantId ORDER BY id DESC")
    suspend fun getProductsByRestaurantDirect(restaurantId: Long): List<FoodProductEntity>

    @Query("SELECT * FROM food_products WHERE restaurantId = :restaurantId AND status = 'APPROVED' AND isAvailable = 1 ORDER BY id DESC")
    fun getApprovedProductsByRestaurant(restaurantId: Long): Flow<List<FoodProductEntity>>

    @Query("SELECT * FROM food_products WHERE categoryName = :categoryName AND status = 'APPROVED' AND isAvailable = 1 ORDER BY id DESC")
    fun getApprovedProductsByCategory(categoryName: String): Flow<List<FoodProductEntity>>

    @Query("SELECT * FROM food_products WHERE id = :id LIMIT 1")
    suspend fun getProductByIdDirect(id: Long): FoodProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: FoodProductEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<FoodProductEntity>)

    @Update
    suspend fun updateProduct(product: FoodProductEntity)

    @Query("UPDATE food_products SET status = :status, rejectionReason = :reason WHERE id = :id")
    suspend fun updateModerationStatus(id: Long, status: String, reason: String)

    @Query("UPDATE food_products SET isAvailable = :isAvailable WHERE id = :id")
    suspend fun updateAvailability(id: Long, isAvailable: Boolean)

    @Query("UPDATE food_products SET salesCount = salesCount + :quantity WHERE id = :id")
    suspend fun incrementSalesCount(id: Long, quantity: Int)

    @Query("DELETE FROM food_products WHERE id = :id")
    suspend fun deleteProduct(id: Long)

    @Query("DELETE FROM food_products WHERE restaurantId = :restaurantId")
    suspend fun deleteProductsByRestaurant(restaurantId: Long)
}

@Dao
interface FoodOrderDao {
    @Query("SELECT * FROM food_orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<FoodOrderEntity>>

    @Query("SELECT * FROM food_orders WHERE restaurantId = :restaurantId ORDER BY createdAt DESC")
    fun getOrdersByRestaurant(restaurantId: Long): Flow<List<FoodOrderEntity>>

    @Query("SELECT * FROM food_orders WHERE userId = :userId ORDER BY createdAt DESC")
    fun getOrdersByUser(userId: Long): Flow<List<FoodOrderEntity>>

    @Query("SELECT * FROM food_orders WHERE id = :id LIMIT 1")
    fun getOrderById(id: Long): Flow<FoodOrderEntity?>

    @Query("SELECT * FROM food_orders WHERE id = :id LIMIT 1")
    suspend fun getOrderByIdDirect(id: Long): FoodOrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: FoodOrderEntity): Long

    @Query("UPDATE food_orders SET status = :status WHERE id = :id")
    suspend fun updateOrderStatus(id: Long, status: String)

    @Query("UPDATE food_orders SET status = :status, rejectionReason = :reason WHERE id = :id")
    suspend fun updateOrderRejection(id: Long, status: String, reason: String)
}

@Dao
interface FoodBannerDao {
    @Query("SELECT * FROM food_banners WHERE isActive = 1 ORDER BY id DESC")
    fun getActiveBanners(): Flow<List<FoodBannerEntity>>

    @Query("SELECT * FROM food_banners ORDER BY id DESC")
    fun getAllBanners(): Flow<List<FoodBannerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBanner(banner: FoodBannerEntity): Long

    @Update
    suspend fun updateBanner(banner: FoodBannerEntity)

    @Query("DELETE FROM food_banners WHERE id = :id")
    suspend fun deleteBanner(id: Long)
}

@Dao
interface FoodPromotionDao {
    @Query("SELECT * FROM food_promotions WHERE isActive = 1 ORDER BY id DESC")
    fun getActivePromotions(): Flow<List<FoodPromotionEntity>>

    @Query("SELECT * FROM food_promotions ORDER BY id DESC")
    fun getAllPromotions(): Flow<List<FoodPromotionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPromotion(promotion: FoodPromotionEntity): Long

    @Query("DELETE FROM food_promotions WHERE id = :id")
    suspend fun deletePromotion(id: Long)
}

@Dao
interface FoodReviewDao {
    @Query("SELECT * FROM food_reviews WHERE foodId = :foodId ORDER BY createdAt DESC")
    fun getReviewsForFood(foodId: Long): Flow<List<FoodReviewEntity>>

    @Query("SELECT * FROM food_reviews WHERE restaurantId = :restaurantId ORDER BY createdAt DESC")
    fun getReviewsForRestaurant(restaurantId: Long): Flow<List<FoodReviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: FoodReviewEntity): Long
}
