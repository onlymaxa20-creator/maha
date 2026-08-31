package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.SupportInfoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SupportDao {

    @Query("SELECT * FROM support_info WHERE id = 1 LIMIT 1")
    fun getSupportInfo(): Flow<SupportInfoEntity?>

    @Query("SELECT * FROM support_info WHERE id = 1 LIMIT 1")
    suspend fun getSupportInfoDirect(): SupportInfoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSupportInfo(info: SupportInfoEntity)

    @Update
    suspend fun updateSupportInfo(info: SupportInfoEntity)
}
