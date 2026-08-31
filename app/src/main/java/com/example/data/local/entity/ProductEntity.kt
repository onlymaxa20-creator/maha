package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "products",
    indices = [
        Index(value = ["sellerId"]),
        Index(value = ["categoryId"])
    ]
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sellerId: Long,
    val sellerName: String,
    val categoryId: Long,
    val categoryName: String,
    val name: String,
    val description: String = "",
    val price: Double, // Real price in so'm
    val originalPrice: Double = 0.0, // Strikethrough price if discounted
    val discountPercent: Int = 0, // e.g. 20 for -20%
    val isPromotional: Boolean = false, // If marked as special offer / Aksiya
    val stock: Int = 1,
    val unit: String = "dona", // "dona", "kg", "litr", "paket", etc.
    val imageUri: String = "",
    val isAvailable: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
