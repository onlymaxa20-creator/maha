package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reviews",
    indices = [
        Index(value = ["productId"]),
        Index(value = ["userId"])
    ]
)
data class ReviewEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productId: Long,
    val userId: Long = 0,
    val userName: String,
    val userPhone: String = "",
    val rating: Int, // 1 to 5 stars
    val comment: String,
    val createdAt: Long = System.currentTimeMillis()
)
