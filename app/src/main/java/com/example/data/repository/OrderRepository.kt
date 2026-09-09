package com.example.data.repository

import android.util.Log
import com.example.data.local.dao.CartDao
import com.example.data.local.dao.OrderDao
import com.example.data.local.dao.ProductDao
import com.example.data.local.dao.UserDao
import com.example.data.local.entity.OrderEntity
import com.example.data.local.entity.OrderItemEntity
import com.example.data.model.AdminDashboardStats
import com.example.data.model.CartItemDetail
import com.example.data.model.OrderDetail
import com.example.data.model.SellerDashboardStats
import com.example.data.model.SellerOrderDetail
import com.example.data.model.SellerRevenueStats
import com.example.data.remote.FirestoreProductService
import com.example.data.remote.FirestoreSyncService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OrderRepository(
    private val orderDao: OrderDao,
    private val cartDao: CartDao,
    private val productDao: ProductDao,
    private val userDao: UserDao,
    private val firestoreSyncService: FirestoreSyncService = FirestoreSyncService(),
    private val firestoreProductService: FirestoreProductService = FirestoreProductService()
) {
    private val TAG = "OrderRepository"

    /**
     * Starts listening to Firestore real-time snapshots for Orders and Order Items.
     * When any customer places an order or any seller/admin updates the order status,
     * it instantly merges into Room DB so customer & seller screens update live.
     */
    fun startRealtimeOrderSync(scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            try {
                firestoreSyncService.observeOrdersRealtime().collect { remoteOrdersWithItems ->
                    for ((remoteOrder, remoteItems) in remoteOrdersWithItems) {
                        try {
                            val local = orderDao.getOrderByIdDirect(remoteOrder.id)
                            if (local == null) {
                                orderDao.insertOrder(remoteOrder)
                                if (remoteItems.isNotEmpty()) {
                                    orderDao.insertOrderItems(remoteItems)
                                }
                                val itemsSummary = if (remoteItems.isNotEmpty()) {
                                    remoteItems.joinToString(", ") { "${it.productName} (${it.quantity} ${it.unit})" }
                                } else {
                                    "Yangi mahsulotlar"
                                }
                                com.example.data.util.NotificationHelper.showNotification(
                                    title = "🛒 Yangi Buyurtma #${remoteOrder.orderNumber}!",
                                    message = "${remoteOrder.customerName} (${remoteOrder.customerPhone}): $itemsSummary",
                                    targetRole = "SELLER"
                                )
                            } else {
                                if (local.status != remoteOrder.status || local.updatedAt < remoteOrder.updatedAt) {
                                    orderDao.updateOrderStatus(remoteOrder.id, remoteOrder.status)
                                    
                                    val (title, msg) = when (remoteOrder.status) {
                                        "Tayyorlanmoqda" -> Pair(
                                            "🍳 Buyurtmangiz tayyorlanmoqda!",
                                            "Sizning #${remoteOrder.orderNumber} raqamli buyurtmangiz tayyorlanmoqda. Tez orada yetkaziladi!"
                                        )
                                        "Yetkazilmoqda" -> Pair(
                                            "🛵 Buyurtmangiz yetkazilmoqda!",
                                            "Sizning #${remoteOrder.orderNumber} raqamli buyurtmangiz yo‘lga chiqdi. Kuryer yetkazmoqda!"
                                        )
                                        "Yetkazildi" -> Pair(
                                            "🎉 Buyurtmangiz yetkazildi!",
                                            "Sizning #${remoteOrder.orderNumber} raqamli buyurtmangiz yetkazib berildi. Rahmat!"
                                        )
                                        "Bekor qilindi" -> Pair(
                                            "❌ Buyurtmangiz bekor qilindi",
                                            "Sizning #${remoteOrder.orderNumber} raqamli buyurtmangiz bekor qilindi."
                                        )
                                        else -> Pair(
                                            "📦 Buyurtma holati yangilandi",
                                            "Buyurtma #${remoteOrder.orderNumber} holati «${remoteOrder.status}» ga o‘zgardi."
                                        )
                                    }
                                    com.example.data.util.NotificationHelper.showNotification(
                                        title = title,
                                        message = msg,
                                        targetRole = "CUSTOMER",
                                        targetUserId = remoteOrder.customerId
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error syncing order ${remoteOrder.id}: ${e.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Orders realtime sync notice: ${e.message}")
            }
        }
    }
    /**
     * Places order from cart.
     * Enforces all business rules:
     * 1. Phone number is mandatory
     * 2. Delivery is FREE
     * 3. Delivery address is in Gagarin city, Mirzacho‘l district
     * 4. Deducts product stock in database
     * 5. Clears customer cart
     */
    suspend fun placeOrder(
        customerId: Long,
        customerName: String,
        customerPhone: String,
        customerEmail: String,
        deliveryAddress: String,
        deliveryNotes: String,
        cartItems: List<CartItemDetail>,
        deliveryLatitude: Double = 40.6622,
        deliveryLongitude: Double = 68.1672,
        deliveryStreet: String = "",
        deliveryHouseNumber: String = "",
        deliveryLandmark: String = ""
    ): Result<OrderEntity> {
        val trimmedPhone = customerPhone.trim()
        if (trimmedPhone.isEmpty() || trimmedPhone.length < 9) {
            return Result.failure(IllegalArgumentException("Buyurtma berish uchun telefon raqamingizni kiriting"))
        }

        val trimmedAddress = deliveryAddress.trim()
        if (trimmedAddress.isEmpty()) {
            return Result.failure(IllegalArgumentException("Gagarin shahridagi yetkazib berish manzilini kiriting"))
        }

        if (cartItems.isEmpty()) {
            return Result.failure(IllegalArgumentException("Savatda mahsulotlar yo‘q"))
        }

        // Validate stock
        for (item in cartItems) {
            val dbProduct = productDao.getProductByIdDirect(item.product.id)
                ?: return Result.failure(IllegalArgumentException("${item.product.name} topilmadi"))
            if (dbProduct.stock < item.cartItem.quantity) {
                return Result.failure(IllegalArgumentException("${item.product.name} dan yetarli qoldiq mavjud emas (Mavjud: ${dbProduct.stock})"))
            }
        }

        val totalSum = cartItems.sumOf { it.totalPrice }
        val orderTime = System.currentTimeMillis()
        val randomSuffix = (1000..9999).random()
        val orderNumber = "GAG-${SimpleDateFormat("yyMMdd", Locale.getDefault()).format(Date(orderTime))}-$randomSuffix"

        val order = OrderEntity(
            orderNumber = orderNumber,
            customerId = customerId,
            customerName = if (customerName.isNotBlank()) customerName else "Xaridor #$customerId",
            customerPhone = trimmedPhone,
            customerEmail = customerEmail,
            deliveryCity = "Gagarin shahri",
            deliveryDistrict = "Mirzacho‘l tumani",
            deliveryAddress = trimmedAddress,
            deliveryLatitude = deliveryLatitude,
            deliveryLongitude = deliveryLongitude,
            deliveryStreet = deliveryStreet.trim(),
            deliveryHouseNumber = deliveryHouseNumber.trim(),
            deliveryLandmark = deliveryLandmark.trim(),
            deliveryNotes = deliveryNotes.trim(),
            isFreeDelivery = true,
            totalAmount = totalSum,
            status = "Yangi",
            createdAt = orderTime,
            updatedAt = orderTime
        )

        val orderId = orderDao.insertOrder(order)

        // Create order items and deduct stock
        val orderItems = cartItems.map { detail ->
            // Deduct stock
            val newStock = (detail.product.stock - detail.cartItem.quantity).coerceAtLeast(0)
            val isNowAvailable = newStock > 0
            productDao.updateStock(detail.product.id, newStock)
            productDao.updateAvailability(detail.product.id, isNowAvailable)

            // Real-time sync of updated product stock to Firestore so seller & other buyers see it immediately
            try {
                val updatedProduct = detail.product.copy(
                    stock = newStock,
                    isAvailable = isNowAvailable,
                    updatedAt = System.currentTimeMillis()
                )
                firestoreProductService.saveProduct(updatedProduct)
            } catch (e: Exception) {
                Log.w(TAG, "Stock update Firestore notice: ${e.message}")
            }

            OrderItemEntity(
                orderId = orderId,
                productId = detail.product.id,
                sellerId = detail.product.sellerId,
                sellerName = detail.product.sellerName,
                productName = detail.product.name,
                productCategory = detail.product.categoryName,
                unitPrice = detail.product.price,
                quantity = detail.cartItem.quantity,
                unit = detail.product.unit,
                itemTotal = detail.totalPrice
            )
        }

        orderDao.insertOrderItems(orderItems)
        cartDao.clearCartForCustomer(customerId)

        // Also ensure user's saved phone in profile is updated
        userDao.updatePhone(customerId, trimmedPhone)

        val createdOrder = order.copy(id = orderId)
        try {
            firestoreSyncService.saveOrderWithItems(createdOrder, orderItems)
        } catch (e: Exception) {
            Log.w(TAG, "Order saved locally; Firestore sync notice: ${e.message}")
        }

        // Notification for sellers
        com.example.data.util.NotificationHelper.showNotification(
            title = "📦 Yangi Buyurtma!",
            message = "Sizda yangi buyurtma bor! Buyurtma: #$orderNumber ($customerName - $trimmedPhone)",
            targetRole = "SELLER"
        )

        return Result.success(createdOrder)
    }

    fun getCustomerOrders(customerId: Long): Flow<List<OrderDetail>> {
        return orderDao.getOrdersByCustomer(customerId).map { orders ->
            orders.map { order ->
                val items = orderDao.getOrderItemsDirect(order.id)
                OrderDetail(order = order, items = items)
            }
        }
    }

    fun getAllOrders(): Flow<List<OrderDetail>> {
        return orderDao.getAllOrders().map { orders ->
            orders.map { order ->
                val items = orderDao.getOrderItemsDirect(order.id)
                OrderDetail(order = order, items = items)
            }
        }
    }

    fun getSellerOrders(sellerId: Long): Flow<List<SellerOrderDetail>> {
        return orderDao.getOrdersForSeller(sellerId).map { orders ->
            orders.map { order ->
                val sellerItems = orderDao.getOrderItemsDirect(order.id).filter { it.sellerId == sellerId }
                val sellerTotal = sellerItems.sumOf { it.itemTotal }
                SellerOrderDetail(
                    order = order,
                    sellerItems = sellerItems,
                    sellerTotal = sellerTotal
                )
            }
        }
    }

    suspend fun updateOrderStatus(orderId: Long, newStatus: String): Result<Unit> {
        val validStatuses = listOf("Yangi", "Tayyorlanmoqda", "Yetkazilmoqda", "Yetkazildi", "Bekor qilindi")
        if (!validStatuses.contains(newStatus)) {
            return Result.failure(IllegalArgumentException("Noto‘g‘ri buyurtma holati"))
        }
        val order = orderDao.getOrderByIdDirect(orderId)
        orderDao.updateOrderStatus(orderId, newStatus)
        try {
            firestoreSyncService.updateOrderStatus(orderId, newStatus)
        } catch (e: Exception) {
            Log.w(TAG, "Status updated locally; Firestore sync notice: ${e.message}")
        }

        // Notification for buyer with order number and user targeting
        val orderNum = order?.orderNumber ?: "#$orderId"
        val (title, msg) = when (newStatus) {
            "Tayyorlanmoqda" -> Pair(
                "🍳 Buyurtmangiz tayyorlanmoqda!",
                "Sizning $orderNum raqamli buyurtmangiz tayyorlanmoqda. Tez orada yetkaziladi!"
            )
            "Yetkazilmoqda" -> Pair(
                "🛵 Buyurtmangiz yetkazilmoqda!",
                "Sizning $orderNum raqamli buyurtmangiz yo‘lga chiqdi. Kuryer yetkazmoqda!"
            )
            "Yetkazildi" -> Pair(
                "🎉 Buyurtmangiz yetkazildi!",
                "Sizning $orderNum raqamli buyurtmangiz yetkazildi. Gagarin Go xizmatidan foydalanganingiz uchun rahmat!"
            )
            "Bekor qilindi" -> Pair(
                "❌ Buyurtmangiz bekor qilindi",
                "Sizning $orderNum raqamli buyurtmangiz bekor qilindi."
            )
            else -> Pair(
                "🚚 Buyurtmangiz holati o‘zgardi",
                "Sizning $orderNum raqamli buyurtmangiz holati «$newStatus» ga o‘zgartirildi."
            )
        }
        com.example.data.util.NotificationHelper.showNotification(
            title = title,
            message = msg,
            targetRole = "CUSTOMER",
            targetUserId = order?.customerId
        )

        return Result.success(Unit)
    }

    /**
     * Customer cancels an order before it reaches "Yetkazilmoqda".
     * Rule: Order can be cancelled while status is "Yangi" or "Tayyorlanmoqda".
     * Once it is "Yetkazilmoqda" (courier on the way), it CANNOT be cancelled.
     * Both customer and sellers receive real-time notifications.
     */
    suspend fun cancelOrderByCustomer(orderId: Long, reason: String = "Mijoz tomonidan bekor qilindi"): Result<Unit> {
        val order = orderDao.getOrderByIdDirect(orderId)
            ?: return Result.failure(IllegalArgumentException("Buyurtma topilmadi"))

        val statusClean = order.status.trim()
        if (statusClean.equals("Yetkazilmoqda", ignoreCase = true)) {
            return Result.failure(IllegalStateException("Buyurtma allaqachon yo‘lga chiqqan (yetkazilmoqda). Kuryer yetib bormoqda, shuning uchun bekor qilib bo‘lmaydi."))
        }
        if (statusClean.equals("Yetkazildi", ignoreCase = true)) {
            return Result.failure(IllegalStateException("Buyurtma allaqachon yetkazib berilgan."))
        }
        if (statusClean.equals("Bekor qilindi", ignoreCase = true)) {
            return Result.failure(IllegalStateException("Ushbu buyurtma allaqachon bekor qilingan."))
        }

        // Restore product stock in database
        val items = orderDao.getOrderItemsDirect(orderId)
        for (item in items) {
            try {
                productDao.increaseStock(item.productId, item.quantity)
            } catch (e: Exception) {
                Log.w(TAG, "Could not restore stock for product ${item.productId}: ${e.message}")
            }
        }

        // Update local Room database
        orderDao.updateOrderStatus(orderId, "Bekor qilindi")

        // Sync to Firestore
        try {
            firestoreSyncService.updateOrderStatus(orderId, "Bekor qilindi")
        } catch (e: Exception) {
            Log.w(TAG, "Order cancel status updated locally; Firestore sync notice: ${e.message}")
        }

        val orderNum = order.orderNumber

        // 1. Notification for customer (confirmation)
        com.example.data.util.NotificationHelper.showNotification(
            title = "❌ Buyurtmangiz bekor qilindi",
            message = "Sizning #$orderNum raqamli buyurtmangiz muvaffaqiyatli bekor qilindi.",
            targetRole = "CUSTOMER",
            targetUserId = order.customerId
        )

        // 2. Notification for sellers
        com.example.data.util.NotificationHelper.showNotification(
            title = "⚠️ Buyurtma bekor qilindi",
            message = "Buyurtma #$orderNum (${order.customerName}) xaridor tomonidan bekor qilindi.",
            targetRole = "SELLER"
        )

        return Result.success(Unit)
    }

    fun getAdminStats(): Flow<AdminDashboardStats> {
        return combine(
            userDao.getCustomersCount(),
            userDao.getSellersCount(),
            userDao.getActiveSellersCount(),
            productDao.getProductsCount(),
            orderDao.getAllOrders()
        ) { customers, sellers, activeSellers, products, orders ->
            val calendar = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, 0)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }
            val startOfToday = calendar.timeInMillis
            val startOfWeek = startOfToday - (6L * 24 * 60 * 60 * 1000)
            val startOfMonth = startOfToday - (29L * 24 * 60 * 60 * 1000)

            val completedOrdersList = orders.filter { it.status == "Yetkazildi" }
            val cancelledOrdersList = orders.filter { it.status == "Bekor qilindi" }
            val activeOrdersList = orders.filter { it.status != "Yetkazildi" && it.status != "Bekor qilindi" }

            val totalRevenue = completedOrdersList.sumOf { it.totalAmount }
            val todayRevenue = completedOrdersList.filter { it.createdAt >= startOfToday }.sumOf { it.totalAmount }
            val weeklyRevenue = completedOrdersList.filter { it.createdAt >= startOfWeek }.sumOf { it.totalAmount }
            val monthlyRevenue = completedOrdersList.filter { it.createdAt >= startOfMonth }.sumOf { it.totalAmount }
            val platformRevenue = totalRevenue * 0.05 // 5% platform standard commission

            AdminDashboardStats(
                totalCustomers = customers,
                totalSellers = sellers,
                activeSellers = activeSellers,
                totalProducts = products,
                totalOrders = orders.size,
                completedOrders = completedOrdersList.size,
                cancelledOrders = cancelledOrdersList.size,
                todayRevenue = todayRevenue,
                weeklyRevenue = weeklyRevenue,
                monthlyRevenue = monthlyRevenue,
                totalRevenue = totalRevenue,
                platformRevenue = platformRevenue,
                activeOrdersCount = activeOrdersList.size
            )
        }
    }

    fun getAllSellersRevenueStats(): Flow<List<SellerRevenueStats>> {
        return combine(
            userDao.getAllSellers(),
            orderDao.getAllOrders(),
            orderDao.getAllOrderItems(),
            productDao.getAllProducts()
        ) { sellers, orders, orderItems, products ->
            val orderMap = orders.associateBy { it.id }

            val calendar = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, 0)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }
            val startOfToday = calendar.timeInMillis
            val startOfMonth = startOfToday - (29L * 24 * 60 * 60 * 1000)

            sellers.map { seller ->
                val sellerItems = orderItems.filter { it.sellerId == seller.id }
                val sellerOrderIds = sellerItems.map { it.orderId }.distinct()
                val sellerOrders = sellerOrderIds.mapNotNull { orderMap[it] }

                val completedOrderIds = sellerOrders.filter { it.status == "Yetkazildi" }.map { it.id }.toSet()
                val cancelledOrderIds = sellerOrders.filter { it.status == "Bekor qilindi" }.map { it.id }.toSet()

                // Sum of itemTotals for completed orders only (Faqat qabul qilingan/yetkazilgan buyurtmalar)
                val totalSales = sellerItems
                    .filter { completedOrderIds.contains(it.orderId) }
                    .sumOf { it.itemTotal }

                val platformCommission = totalSales * 0.05 // 5% commission
                val sellerNetRevenue = totalSales - platformCommission

                // 1 oylik savdo va daromad hisoblash
                val monthlyCompletedOrders = sellerOrders.filter { it.status == "Yetkazildi" && it.createdAt >= startOfMonth }
                val monthlyCompletedOrderIds = monthlyCompletedOrders.map { it.id }.toSet()
                val monthlySales = sellerItems
                    .filter { monthlyCompletedOrderIds.contains(it.orderId) }
                    .sumOf { it.itemTotal }
                val monthlyCommission = monthlySales * 0.05
                val monthlyNetRevenue = monthlySales - monthlyCommission
                val monthlyOrdersCount = monthlyCompletedOrders.size

                val lastOrderDate = sellerOrders.maxOfOrNull { it.createdAt }
                val sellerProductsCount = products.count { it.sellerId == seller.id }

                SellerRevenueStats(
                    sellerId = seller.id,
                    sellerName = seller.fullName.ifBlank { seller.storeName },
                    storeName = seller.storeName,
                    phone = seller.phone,
                    role = seller.role,
                    isActive = seller.isActive,
                    totalOrdersCount = sellerOrders.size,
                    completedOrdersCount = completedOrderIds.size,
                    cancelledOrdersCount = cancelledOrderIds.size,
                    totalSales = totalSales,
                    platformCommission = platformCommission,
                    sellerNetRevenue = sellerNetRevenue,
                    monthlySales = monthlySales,
                    monthlyCommission = monthlyCommission,
                    monthlyNetRevenue = monthlyNetRevenue,
                    monthlyOrdersCount = monthlyOrdersCount,
                    lastOrderDate = lastOrderDate,
                    productsCount = sellerProductsCount
                )
            }
        }
    }

    fun getSellerStats(sellerId: Long): Flow<SellerDashboardStats> {
        return combine(
            productDao.getProductsCountBySeller(sellerId),
            orderDao.getSellerOrdersCount(sellerId),
            orderDao.getSellerActiveOrdersCount(sellerId),
            orderDao.getSellerCompletedRevenue(sellerId)
        ) { products, orders, active, revenue ->
            SellerDashboardStats(
                myProductsCount = products,
                myOrdersCount = orders,
                myActiveOrdersCount = active,
                myRevenue = revenue ?: 0.0
            )
        }
    }
}
