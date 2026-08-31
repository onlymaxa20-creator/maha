package com.example.data.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.local.AppDatabase
import com.example.data.remote.FirestoreSyncService
import com.example.data.util.NotificationHelper

class OrderSyncWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "OrderSyncWorker"
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting background order synchronization worker...")
        return try {
            NotificationHelper.init(context)
            val database = AppDatabase.getInstance(context)
            val orderDao = database.orderDao()
            val foodOrderDao = database.foodOrderDao()
            val firestoreSyncService = FirestoreSyncService()

            // 1. Sync Marketplace Orders
            val remoteOrdersWithItems = firestoreSyncService.fetchOrdersOnce()
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
                        // Alert seller of brand new incoming order
                        NotificationHelper.showNotification(
                            context = context,
                            title = "🛒 Yangi Buyurtma #${remoteOrder.orderNumber}!",
                            message = "${remoteOrder.customerName} (${remoteOrder.customerPhone}): $itemsSummary",
                            targetRole = "SELLER",
                            notificationId = (remoteOrder.id % 100000).toInt()
                        )
                    } else if (local.status != remoteOrder.status || local.updatedAt < remoteOrder.updatedAt) {
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

                        NotificationHelper.showNotification(
                            context = context,
                            title = title,
                            message = msg,
                            targetRole = "CUSTOMER",
                            targetUserId = remoteOrder.customerId,
                            notificationId = (remoteOrder.id % 100000).toInt()
                        )
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error syncing individual order in background: ${e.message}")
                }
            }

            // 2. Sync Food Orders
            val remoteFoodOrders = firestoreSyncService.fetchFoodOrdersOnce()
            for (remoteFood in remoteFoodOrders) {
                try {
                    val local = foodOrderDao.getOrderByIdDirect(remoteFood.id)
                    val restName = remoteFood.restaurantName.ifBlank { "Oshxona" }
                    if (local == null) {
                        foodOrderDao.insertOrder(remoteFood)
                        // Alert Food Seller of incoming food order
                        NotificationHelper.showNotification(
                            context = context,
                            title = "🍔 Yangi Taom Buyurtmasi #${remoteFood.id}!",
                            message = "$restName: ${remoteFood.itemsSummary} (${remoteFood.customerName} - ${remoteFood.customerPhone})",
                            targetRole = "FOOD_SELLER",
                            notificationId = (remoteFood.id % 100000).toInt()
                        )
                    } else if (local.status != remoteFood.status) {
                        foodOrderDao.updateOrderStatus(remoteFood.id, remoteFood.status)

                        val (title, message) = when (remoteFood.status) {
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
                                "$restName buyurtmangizni bekor qildi. ${remoteFood.rejectionReason}"
                            )
                            else -> Pair(
                                "🍔 Buyurtma holati yangilandi",
                                "Buyurtma #${remoteFood.id} holati: ${remoteFood.status}"
                            )
                        }

                        NotificationHelper.showNotification(
                            context = context,
                            title = title,
                            message = message,
                            targetRole = "CUSTOMER",
                            targetUserId = remoteFood.userId,
                            notificationId = (remoteFood.id % 100000).toInt()
                        )
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error syncing individual food order in background: ${e.message}")
                }
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Worker failed to sync orders: ${e.message}", e)
            Result.retry()
        }
    }
}
