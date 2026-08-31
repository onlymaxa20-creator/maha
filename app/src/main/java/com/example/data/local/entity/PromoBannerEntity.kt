package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "promo_banners")
data class PromoBannerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String = "", // Reklama nomi / Kampaniya nomi
    val title: String, // Sarlavha
    val description: String = "", // Tavsif / Subtitle
    val imageUrl: String = "", // Real Cloud / Persistent storage image URI
    val targetLink: String = "", // Havola yoki bog‘lanadigan sahifa (bozor, food, https://...)
    val startDate: Long = System.currentTimeMillis(), // Boshlanish sanasi
    val endDate: Long = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000), // Tugash sanasi (default 30 kun)
    val badgeText: String = "REKLAMA", // Yorliq (AKSIYA, CHEGIRMA, YANGI)
    val gradientType: String = "purple", // "burgundy", "purple", "blue", "orange", "emerald", "rose"
    val actionTag: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

