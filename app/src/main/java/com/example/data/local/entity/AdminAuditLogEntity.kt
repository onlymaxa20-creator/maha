package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "admin_audit_logs",
    indices = [
        Index(value = ["timestamp"]),
        Index(value = ["adminId"])
    ]
)
data class AdminAuditLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val adminId: Long,
    val adminLogin: String,
    val action: String,
    val recordId: String = "",
    val details: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
