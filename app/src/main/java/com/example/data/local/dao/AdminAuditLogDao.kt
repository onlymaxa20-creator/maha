package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.AdminAuditLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AdminAuditLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AdminAuditLogEntity): Long

    @Query("SELECT * FROM admin_audit_logs ORDER BY timestamp DESC")
    fun getAllAuditLogs(): Flow<List<AdminAuditLogEntity>>

    @Query("SELECT * FROM admin_audit_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentAuditLogs(limit: Int = 100): Flow<List<AdminAuditLogEntity>>

    @Query("DELETE FROM admin_audit_logs")
    suspend fun clearAuditLogs()
}
