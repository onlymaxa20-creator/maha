package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cart_items",
    indices = [
        Index(value = ["customerId", "productId"], unique = true)
    ]
)
data class CartItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerId: Long,
    val productId: Long,
    val quantity: Int = 1,
    val addedAt: Long = System.currentTimeMillis()
)
