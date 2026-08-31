package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.PromoBannerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PromoBannerDao {
    @Query("SELECT * FROM promo_banners WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getActiveBanners(): Flow<List<PromoBannerEntity>>

    @Query("SELECT * FROM promo_banners ORDER BY createdAt DESC")
    fun getAllBanners(): Flow<List<PromoBannerEntity>>

    @Query("SELECT * FROM promo_banners ORDER BY createdAt DESC")
    suspend fun getAllBannersDirect(): List<PromoBannerEntity>

    @Query("SELECT * FROM promo_banners WHERE id = :id")
    suspend fun getBannerById(id: Long): PromoBannerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBanner(banner: PromoBannerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBanners(banners: List<PromoBannerEntity>)

    @Update
    suspend fun updateBanner(banner: PromoBannerEntity)

    @Delete
    suspend fun deleteBanner(banner: PromoBannerEntity)

    @Query("DELETE FROM promo_banners WHERE id = :id")
    suspend fun deleteBannerById(id: Long)

    @Query("UPDATE promo_banners SET isActive = :isActive WHERE id = :id")
    suspend fun setBannerActiveStatus(id: Long, isActive: Boolean)
}
