package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "order_items",
    indices = [
        Index(value = ["orderId"]),
        Index(value = ["productId"]),
        Index(value = ["sellerId"])
    ]
)
data class OrderItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderId: Long,
    val productId: Long,
    val sellerId: Long,
    val sellerName: String = "",
    val productName: String,
    val productCategory: String = "",
    val unitPrice: Double,
    val quantity: Int,
    val unit: String = "dona",
    val itemTotal: Double
)
