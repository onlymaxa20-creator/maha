package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.local.entity.OrderEntity
import com.example.data.local.entity.OrderItemEntity
import kotlinx.coroutines.flow.Flow

data class OrderWithItems(
    val order: OrderEntity,
    val items: List<OrderItemEntity>
)

@Dao
interface OrderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItems(items: List<OrderItemEntity>)

    @Query("SELECT * FROM orders WHERE id = :id")
    fun getOrderById(id: Long): Flow<OrderEntity?>

    @Query("SELECT * FROM orders WHERE id = :id")
    suspend fun getOrderByIdDirect(id: Long): OrderEntity?

    @Query("SELECT * FROM orders WHERE customerId = :customerId ORDER BY createdAt DESC")
    fun getOrdersByCustomer(customerId: Long): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    fun getOrderItems(orderId: Long): Flow<List<OrderItemEntity>>

    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    suspend fun getOrderItemsDirect(orderId: Long): List<OrderItemEntity>

    @Query("SELECT DISTINCT o.* FROM orders o INNER JOIN order_items oi ON o.id = oi.orderId WHERE oi.sellerId = :sellerId ORDER BY o.createdAt DESC")
    fun getOrdersForSeller(sellerId: Long): Flow<List<OrderEntity>>

    @Query("SELECT * FROM order_items WHERE orderId = :orderId AND sellerId = :sellerId")
    fun getOrderItemsForSeller(orderId: Long, sellerId: Long): Flow<List<OrderItemEntity>>

    @Query("UPDATE orders SET status = :status, updatedAt = :updatedAt WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: Long, status: String, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM orders")
    fun getTotalOrdersCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM orders WHERE status = :status")
    fun getOrdersCountByStatus(status: String): Flow<Int>

    @Query("SELECT COUNT(DISTINCT o.id) FROM orders o INNER JOIN order_items oi ON o.id = oi.orderId WHERE oi.sellerId = :sellerId")
    fun getSellerOrdersCount(sellerId: Long): Flow<Int>

    @Query("SELECT COUNT(DISTINCT o.id) FROM orders o INNER JOIN order_items oi ON o.id = oi.orderId WHERE oi.sellerId = :sellerId AND o.status != 'Bekor qilindi' AND o.status != 'Yetkazildi'")
    fun getSellerActiveOrdersCount(sellerId: Long): Flow<Int>

    // Real database revenue calculation only from completed/valid orders
    @Query("SELECT SUM(totalAmount) FROM orders WHERE status = 'Yetkazildi'")
    fun getTotalCompletedRevenue(): Flow<Double?>

    @Query("SELECT SUM(totalAmount) FROM orders WHERE status = 'Yetkazildi' AND createdAt >= :sinceTimestamp")
    fun getCompletedRevenueSince(sinceTimestamp: Long): Flow<Double?>

    @Query("SELECT * FROM order_items")
    fun getAllOrderItems(): Flow<List<OrderItemEntity>>

    @Query("SELECT * FROM order_items")
    suspend fun getAllOrderItemsDirect(): List<OrderItemEntity>

    @Query("SELECT SUM(oi.itemTotal) FROM order_items oi INNER JOIN orders o ON oi.orderId = o.id WHERE oi.sellerId = :sellerId AND o.status = 'Yetkazildi'")
    fun getSellerCompletedRevenue(sellerId: Long): Flow<Double?>

    @Query("SELECT COUNT(DISTINCT o.id) FROM orders o INNER JOIN order_items oi ON o.id = oi.orderId WHERE oi.sellerId = :sellerId AND o.status = 'Yetkazildi'")
    fun getSellerCompletedOrdersCount(sellerId: Long): Flow<Int>

    @Query("SELECT COUNT(DISTINCT o.id) FROM orders o INNER JOIN order_items oi ON o.id = oi.orderId WHERE oi.sellerId = :sellerId AND o.status = 'Bekor qilindi'")
    fun getSellerCancelledOrdersCount(sellerId: Long): Flow<Int>
}
