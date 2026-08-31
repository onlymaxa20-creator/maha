package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "taxi_rides")
data class TaxiRideEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long = 0,
    val pickupAddress: String,
    val destinationAddress: String,
    val pickupLat: Float = 40.5892f,
    val pickupLng: Float = 68.1750f,
    val destLat: Float = 40.5980f,
    val destLng: Float = 68.1820f,
    val tariff: String, // "Ekonom", "Komfort", "Yuk Taksi", "Shaharlararo"
    val estimatedPrice: Double,
    val passengerPhone: String,
    val passengerName: String,
    val passengerNote: String = "",
    val driverName: String = "",
    val driverPhone: String = "",
    val carModel: String = "",
    val carPlate: String = "",
    val status: String = "SEARCHING", // "SEARCHING", "ACCEPTED", "ARRIVED", "IN_TRIP", "COMPLETED", "CANCELLED"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "courier_applications")
data class CourierApplicationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long = 0,
    val fullName: String,
    val phone: String,
    val vehicleType: String, // "Velosiped / Skuter", "Motosikl", "Yengil mashina (Matiz/Spark/Cobalt)", "Damas / Labo", "Piyoda"
    val area: String = "Gagarin shahri",
    val workSchedule: String = "Erkin grafik", // "To'liq kun", "Erkin grafik", "Kechki smena"
    val passportOrLicense: String = "",
    val status: String = "FAOL", // "FAOL", "KUTILMOQDA", "TASDIQLANGAN"
    val completedDeliveries: Int = 0,
    val rating: Double = 5.0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "delivery_orders")
data class DeliveryOrderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long = 0,
    val senderName: String,
    val senderPhone: String,
    val pickupAddress: String,
    val receiverName: String,
    val receiverPhone: String,
    val dropoffAddress: String,
    val parcelType: String, // "Hujjatlar", "Kichik posilka", "Og'ir yuk", "Oziq-ovqat"
    val weightKg: String,
    val comment: String = "",
    val estimatedPrice: Double,
    val courierName: String = "",
    val courierPhone: String = "",
    val status: String = "SEARCHING", // "SEARCHING", "ASSIGNED", "PICKED_UP", "DELIVERED", "CANCELLED"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "job_vacancies")
data class JobVacancyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long = 0,
    val title: String,
    val companyName: String,
    val category: String, // "Savdo & Do'kon", "Haydovchilik", "Ishlab chiqarish", "Qurilish & Usta", "Oshxona & Kafe", "Moliya & IT", "Boshqa"
    val salaryText: String, // "3 500 000 - 5 000 000 so'm"
    val jobType: String = "To'liq stavka", // "To'liq stavka", "Yarim stavka", "Erkin grafik", "Bir martalik ish"
    val location: String = "Gagarin shahri",
    val contactPhone: String,
    val contactPerson: String,
    val requirements: String = "",
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)

@Entity(tableName = "service_masters")
data class ServiceMasterEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long = 0,
    val masterName: String,
    val category: String, // "Santexnik", "Elektrik", "Usta / Remont", "Maishiy texnika", "Tozalash", "Konditsioner", "Mebel", "Repetitor"
    val phone: String,
    val experienceYears: Int = 5,
    val rating: Double = 4.9,
    val reviewsCount: Int = 24,
    val priceRange: String = "Kelishilgan holda",
    val description: String = "",
    val isAvailable: Boolean = true,
    val imageUri: String = ""
)

@Entity(tableName = "classified_ads")
data class ClassifiedAdEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long = 0,
    val authorName: String,
    val authorPhone: String,
    val title: String,
    val category: String, // "Avtomobil", "Ko'chmas mulk", "Elektronika", "Kiyim-kechak", "Chorva", "Uy jihozlari", "Boshqa"
    val price: Double,
    val isNegotiable: Boolean = true,
    val location: String = "Gagarin",
    val description: String,
    val imageUri: String = "",
    val viewsCount: Int = 12,
    val createdAt: Long = System.currentTimeMillis()
)
