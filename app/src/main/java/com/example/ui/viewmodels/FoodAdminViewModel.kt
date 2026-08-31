package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.FoodBannerEntity
import com.example.data.local.entity.FoodCategoryEntity
import com.example.data.local.entity.FoodOrderEntity
import com.example.data.local.entity.FoodProductEntity
import com.example.data.local.entity.FoodPromotionEntity
import com.example.data.local.entity.FoodRestaurantEntity
import com.example.data.model.FoodAdminDashboardStats
import com.example.data.repository.FoodRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FoodAdminViewModel(
    private val foodRepository: FoodRepository
) : ViewModel() {

    val allRestaurants: StateFlow<List<FoodRestaurantEntity>> =
        foodRepository.allRestaurants.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProducts: StateFlow<List<FoodProductEntity>> =
        foodRepository.allProducts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOrders: StateFlow<List<FoodOrderEntity>> =
        foodRepository.allOrders.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCategories: StateFlow<List<FoodCategoryEntity>> =
        foodRepository.allCategories.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBanners: StateFlow<List<FoodBannerEntity>> =
        foodRepository.allBanners.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPromotions: StateFlow<List<FoodPromotionEntity>> =
        foodRepository.allPromotions.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dashboardStats: StateFlow<FoodAdminDashboardStats> = combine(
        allRestaurants,
        allProducts,
        allOrders
    ) { restaurants, products, orders ->
        val activeRest = restaurants.count { it.status == "TASDIQLANGAN" }
        val pendingRest = restaurants.count { it.status == "KUTILMOQDA" }
        val pendingProd = products.count { it.status == "PENDING_APPROVAL" }
        val completedOrd = orders.count { it.status == "YETKAZILDI" }
        val cancelledOrd = orders.count { it.status == "BEKOR_QILINDI" }
        val inProgressOrd = orders.count {
            it.status == "YANGI" || it.status == "QABUL_QILINDI" || it.status == "TAYYORLANMOQDA" || it.status == "TAYYOR" || it.status == "YETKAZILMOQDA"
        }

        val completedOrdersList = orders.filter { it.status == "YETKAZILDI" }
        val totalRev = completedOrdersList.sumOf { it.totalPrice }
        val grossVal = orders.sumOf { it.totalPrice }
        val inProgressRev = orders.filter {
            it.status == "YANGI" || it.status == "QABUL_QILINDI" || it.status == "TAYYORLANMOQDA" || it.status == "TAYYOR" || it.status == "YETKAZILMOQDA"
        }.sumOf { it.totalPrice }

        val startOfToday = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis
        val startOfMonth = startOfToday - (29L * 24 * 60 * 60 * 1000)

        val todayOrders = orders.filter { it.createdAt >= startOfToday }
        val todayRev = todayOrders.filter { it.status == "YETKAZILDI" }.sumOf { it.totalPrice }
        
        val monthlyOrders = orders.filter { it.createdAt >= startOfMonth }
        val monthlyRev = monthlyOrders.filter { it.status == "YETKAZILDI" }.sumOf { it.totalPrice }
        val monthlyCompletedCount = monthlyOrders.count { it.status == "YETKAZILDI" }

        val aov = if (completedOrdersList.isNotEmpty()) totalRev / completedOrdersList.size else 0.0

        FoodAdminDashboardStats(
            totalRestaurants = restaurants.size,
            activeRestaurants = activeRest,
            pendingRestaurants = pendingRest,
            totalFoodProducts = products.size,
            pendingProducts = pendingProd,
            totalOrders = orders.size,
            completedOrders = completedOrd,
            inProgressOrders = inProgressOrd,
            cancelledOrders = cancelledOrd,
            totalRevenue = totalRev,
            grossOrderValue = grossVal,
            inProgressRevenue = inProgressRev,
            todayRevenue = todayRev,
            todayOrdersCount = todayOrders.size,
            monthlyRevenue = monthlyRev,
            monthlyOrdersCount = monthlyCompletedCount,
            averageOrderValue = aov
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FoodAdminDashboardStats())

    val restaurantSalesStats: StateFlow<List<com.example.data.model.FoodRestaurantSalesStat>> = combine(
        allRestaurants,
        allOrders
    ) { restaurants, orders ->
        val startOfToday = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis
        val startOfMonth = startOfToday - (29L * 24 * 60 * 60 * 1000)

        restaurants.map { rest ->
            val restOrders = orders.filter { it.restaurantId == rest.id || it.restaurantName == rest.name }
            val completed = restOrders.count { it.status == "YETKAZILDI" }
            val pending = restOrders.count {
                it.status == "YANGI" || it.status == "QABUL_QILINDI" || it.status == "TAYYORLANMOQDA" || it.status == "TAYYOR" || it.status == "YETKAZILMOQDA"
            }
            // Faqat qabul qilingan va yetkazilgan buyurtmalarning summasi
            val rev = restOrders.filter { it.status == "YETKAZILDI" }.sumOf { it.totalPrice }

            val monthlyCompletedOrders = restOrders.filter { it.status == "YETKAZILDI" && it.createdAt >= startOfMonth }
            val monthlyRev = monthlyCompletedOrders.sumOf { it.totalPrice }
            val monthlyOrdersCount = monthlyCompletedOrders.size

            com.example.data.model.FoodRestaurantSalesStat(
                restaurantId = rest.id,
                restaurantName = rest.name,
                totalOrders = restOrders.size,
                completedOrders = completed,
                pendingOrders = pending,
                totalRevenue = rev,
                monthlyRevenue = monthlyRev,
                monthlyOrders = monthlyOrdersCount,
                category = rest.category
            )
        }.sortedByDescending { it.totalRevenue }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateOrderStatus(orderId: Long, status: String) {
        viewModelScope.launch {
            foodRepository.updateOrderStatus(orderId, status)
        }
    }

    fun approveRestaurant(id: Long) {
        viewModelScope.launch { foodRepository.approveRestaurant(id) }
    }

    fun blockRestaurant(id: Long) {
        viewModelScope.launch { foodRepository.blockRestaurant(id) }
    }

    fun unblockRestaurant(id: Long) {
        viewModelScope.launch { foodRepository.unblockRestaurant(id) }
    }

    fun deleteRestaurant(id: Long) {
        viewModelScope.launch { foodRepository.deleteRestaurant(id) }
    }

    fun approveProduct(id: Long) {
        viewModelScope.launch { foodRepository.approveProduct(id) }
    }

    fun rejectProduct(id: Long, reason: String) {
        viewModelScope.launch { foodRepository.rejectProduct(id, reason) }
    }

    fun deleteProduct(id: Long) {
        viewModelScope.launch { foodRepository.deleteFoodProduct(id) }
    }

    fun createCategory(name: String, iconEmoji: String, orderIndex: Int) {
        viewModelScope.launch { foodRepository.createCategory(name, iconEmoji, orderIndex) }
    }

    fun deleteCategory(id: Long) {
        viewModelScope.launch { foodRepository.deleteCategory(id) }
    }

    fun createBanner(title: String, description: String, imageUri: String, badgeText: String = "AKSIYA") {
        viewModelScope.launch { foodRepository.createBanner(title, description, imageUri, badgeText) }
    }

    fun deleteBanner(id: Long) {
        viewModelScope.launch { foodRepository.deleteBanner(id) }
    }

    fun createPromotion(title: String, discountPercentage: Int, foodName: String, restaurantName: String) {
        viewModelScope.launch {
            foodRepository.createPromotion(
                title = title,
                discountPercentage = discountPercentage,
                foodId = 0L,
                foodName = foodName,
                restaurantId = 0L,
                restaurantName = restaurantName
            )
        }
    }

    fun deletePromotion(id: Long) {
        viewModelScope.launch { foodRepository.deletePromotion(id) }
    }

    class Factory(
        private val foodRepository: FoodRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FoodAdminViewModel(foodRepository) as T
        }
    }
}
