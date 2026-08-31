package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "support_info")
data class SupportInfoEntity(
    @PrimaryKey
    val id: Long = 1,
    val phone: String = "+998 90 123 45 67",
    val secondaryPhone: String = "+998 91 987 65 43",
    val telegramUsername: String = "@GagarinGoSupport",
    val telegramChannel: String = "@gagarin_go_app",
    val workingHours: String = "Har kuni: 08:00 - 22:00",
    val address: String = "Gagarin shahri, Do‘stlik shoh ko‘chasi 12-uy",
    val description: String = "Gagarin Go xizmatlari bo‘yicha savollaringiz bo‘lsa, biz bilan bog‘laning!",
    // App Versioning & In-App Update Policy
    val latestVersionCode: Int = 1,
    val latestVersionName: String = "1.0",
    val minRequiredVersionCode: Int = 1,
    val isForceUpdate: Boolean = false,
    val updateTitle: String = "Ilovaning yangi versiyasi chiqdi! 🚀",
    val updateMessage: String = "Gagarin Go ilovasida yangi imkoniyatlar qo‘shildi va xatoliklar to‘g‘rilandi. Davom etish uchun ilovani yangilang.",
    val releaseNotes: String = "• Qulay va tezkor yetkazib berish tizimi\n• E'lonlarni tahrirlash va boshqarish imkoniyati\n• Yangilangan tezkor buyurtma va to‘lov tizimi",
    val playStoreUrl: String = "https://play.google.com/store/apps/details?id=com.aistudio.gagarinsavdo.vzkylm",
    val telegramApkUrl: String = "https://t.me/gagarin_go_app",
    val updatedAt: Long = System.currentTimeMillis()
)
