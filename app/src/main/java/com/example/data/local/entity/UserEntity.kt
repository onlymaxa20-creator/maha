package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["login"], unique = true),
        Index(value = ["email"], unique = false)
    ]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val role: String, // "CUSTOMER", "SELLER", "ADMIN"
    val login: String, // email for customer, username for seller/admin
    val email: String = "",
    val passwordHash: String,
    val salt: String,
    val fullName: String = "",
    val phone: String = "",
    val savedAddress: String = "",
    val storeName: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
