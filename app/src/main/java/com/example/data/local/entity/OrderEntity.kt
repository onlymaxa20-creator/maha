package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "orders",
    indices = [
        Index(value = ["customerId"]),
        Index(value = ["orderNumber"], unique = true)
    ]
)
data class OrderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderNumber: String,
    val customerId: Long,
    val customerName: String,
    val customerPhone: String, // Mandatory validation
    val customerEmail: String = "",
    val deliveryCity: String = "Gagarin shahri",
    val deliveryDistrict: String = "Mirzacho‘l tumani",
    val deliveryAddress: String, // e.g. "Do'stlik ko'chasi, 14-uy"
    val deliveryLatitude: Double = 40.6622,
    val deliveryLongitude: Double = 68.1672,
    val deliveryStreet: String = "",
    val deliveryHouseNumber: String = "",
    val deliveryLandmark: String = "",
    val deliveryNotes: String = "",
    val isFreeDelivery: Boolean = true,
    val totalAmount: Double,
    val status: String = "Yangi", // "Yangi", "Tayyorlanmoqda", "Yetkazilmoqda", "Yetkazildi", "Bekor qilindi"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
