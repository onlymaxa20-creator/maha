package com.example.data.repository

import com.example.data.local.dao.AdminAuditLogDao
import com.example.data.local.entity.AdminAuditLogEntity
import kotlinx.coroutines.flow.Flow

class AdminAuditRepository(
    private val auditDao: AdminAuditLogDao
) {
    fun getAllLogs(): Flow<List<AdminAuditLogEntity>> = auditDao.getAllAuditLogs()

    fun getRecentLogs(limit: Int = 100): Flow<List<AdminAuditLogEntity>> = auditDao.getRecentAuditLogs(limit)

    suspend fun logAction(
        adminId: Long,
        adminLogin: String,
        action: String,
        recordId: String = "",
        details: String = ""
    ): Long {
        val log = AdminAuditLogEntity(
            adminId = adminId,
            adminLogin = adminLogin.ifBlank { "admin" },
            action = action,
            recordId = recordId,
            details = details,
            timestamp = System.currentTimeMillis()
        )
        return auditDao.insertAuditLog(log)
    }

    suspend fun clearLogs() {
        auditDao.clearAuditLogs()
    }
}
