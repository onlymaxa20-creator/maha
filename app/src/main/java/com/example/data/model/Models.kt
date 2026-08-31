package com.example.data.model

import com.example.data.local.entity.CartItemEntity
import com.example.data.local.entity.OrderEntity
import com.example.data.local.entity.OrderItemEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.UserEntity

enum class UserRole {
    CUSTOMER,
    BOZOR_SELLER,
    FOODS_SELLER,
    FOODS_ADMIN,
    SUPER_ADMIN,
    SELLER, // compatibility alias
    ADMIN // compatibility alias
}

data class AuthSession(
    val user: UserEntity? = null,
    val role: UserRole = UserRole.CUSTOMER,
    val isLoggedIn: Boolean = false
)

data class FoodAdminDashboardStats(
    val totalRestaurants: Int = 0,
    val activeRestaurants: Int = 0,
    val pendingRestaurants: Int = 0,
    val totalFoodProducts: Int = 0,
    val pendingProducts: Int = 0,
    val totalOrders: Int = 0,
    val completedOrders: Int = 0,
    val inProgressOrders: Int = 0,
    val cancelledOrders: Int = 0,
    val totalRevenue: Double = 0.0,
    val grossOrderValue: Double = 0.0,
    val inProgressRevenue: Double = 0.0,
    val todayRevenue: Double = 0.0,
    val todayOrdersCount: Int = 0,
    val monthlyRevenue: Double = 0.0,
    val monthlyOrdersCount: Int = 0,
    val averageOrderValue: Double = 0.0
)

data class FoodRestaurantSalesStat(
    val restaurantId: Long = 0L,
    val restaurantName: String = "",
    val totalOrders: Int = 0,
    val completedOrders: Int = 0,
    val pendingOrders: Int = 0,
    val totalRevenue: Double = 0.0,
    val monthlyRevenue: Double = 0.0,
    val monthlyOrders: Int = 0,
    val category: String = ""
)

data class FoodSellerDashboardStats(
    val todayOrdersCount: Int = 0,
    val activeProductsCount: Int = 0,
    val pendingOrdersCount: Int = 0,
    val completedOrdersCount: Int = 0,
    val totalRevenue: Double = 0.0
)

data class CartItemDetail(
    val cartItem: CartItemEntity,
    val product: ProductEntity
) {
    val totalPrice: Double get() = product.price * cartItem.quantity
}

data class OrderDetail(
    val order: OrderEntity,
    val items: List<OrderItemEntity>
)

data class SellerOrderDetail(
    val order: OrderEntity,
    val sellerItems: List<OrderItemEntity>,
    val sellerTotal: Double
)

data class AdminDashboardStats(
    val totalCustomers: Int = 0,
    val totalSellers: Int = 0,
    val activeSellers: Int = 0,
    val totalProducts: Int = 0,
    val totalOrders: Int = 0,
    val completedOrders: Int = 0,
    val cancelledOrders: Int = 0,
    val todayRevenue: Double = 0.0,
    val weeklyRevenue: Double = 0.0,
    val monthlyRevenue: Double = 0.0,
    val totalRevenue: Double = 0.0,
    val platformRevenue: Double = 0.0,
    val activeOrdersCount: Int = 0
)

data class SellerRevenueStats(
    val sellerId: Long,
    val sellerName: String,
    val storeName: String,
    val phone: String,
    val role: String,
    val isActive: Boolean,
    val totalOrdersCount: Int = 0,
    val completedOrdersCount: Int = 0,
    val cancelledOrdersCount: Int = 0,
    val totalSales: Double = 0.0,
    val platformCommission: Double = 0.0,
    val sellerNetRevenue: Double = 0.0,
    val monthlySales: Double = 0.0,
    val monthlyCommission: Double = 0.0,
    val monthlyNetRevenue: Double = 0.0,
    val monthlyOrdersCount: Int = 0,
    val lastOrderDate: Long? = null,
    val productsCount: Int = 0
)

data class SellerDashboardStats(
    val myProductsCount: Int = 0,
    val myOrdersCount: Int = 0,
    val myActiveOrdersCount: Int = 0,
    val myRevenue: Double = 0.0
)
