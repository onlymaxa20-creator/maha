package com.example.data.repository

import android.util.Log
import com.example.data.local.dao.PromoBannerDao
import com.example.data.local.entity.PromoBannerEntity
import com.example.data.remote.FirestoreSyncService
import com.example.data.util.ImageStorageHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class BannerRepository(
    private val promoBannerDao: PromoBannerDao,
    private val firestoreSyncService: FirestoreSyncService = FirestoreSyncService()
) {
    private val TAG = "BannerRepository"

    /**
     * Starts listening to Firestore real-time snapshots for Advertisements/Promos.
     * When Admin adds, modifies, or deletes a banner on any device, it instantly syncs
     * into Room DB on all user devices.
     */
    fun startRealtimeBannerSync(scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            try {
                firestoreSyncService.observeBannersRealtime().collectLatest { remoteBanners ->
                    if (remoteBanners.isNotEmpty()) {
                        try {
                            promoBannerDao.insertBanners(remoteBanners)
                            val remoteIds = remoteBanners.map { it.id }.toSet()
                            val localBanners = promoBannerDao.getAllBannersDirect()
                            for (local in localBanners) {
                                if (!remoteIds.contains(local.id)) {
                                    promoBannerDao.deleteBannerById(local.id)
                                }
                            }
                            Log.d(TAG, "Synchronized ${remoteBanners.size} banners from Firestore.")
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to insert Firestore banners: ${e.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Banners realtime sync notice: ${e.message}")
            }
        }
    }

    fun getActiveBanners(): Flow<List<PromoBannerEntity>> = promoBannerDao.getActiveBanners()

    fun getAllBanners(): Flow<List<PromoBannerEntity>> = promoBannerDao.getAllBanners()

    suspend fun getBannerById(id: Long): PromoBannerEntity? = promoBannerDao.getBannerById(id)

    suspend fun addBanner(
        name: String,
        title: String,
        description: String = "",
        imageUrl: String = "",
        targetLink: String = "",
        startDate: Long = System.currentTimeMillis(),
        endDate: Long = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000),
        badgeText: String = "REKLAMA",
        gradientType: String = "purple",
        actionTag: String = "",
        isActive: Boolean = true
    ): Result<Long> {
        val trimmedTitle = title.trim()
        if (trimmedTitle.isEmpty()) {
            return Result.failure(IllegalArgumentException("Reklama sarlavhasini kiriting"))
        }

        val uniqueId = System.currentTimeMillis() * 1000L + (100..999).random()

        val entity = PromoBannerEntity(
            id = uniqueId,
            name = name.trim().ifBlank { trimmedTitle },
            title = trimmedTitle,
            description = description.trim(),
            imageUrl = imageUrl.trim(),
            targetLink = targetLink.trim(),
            startDate = startDate,
            endDate = endDate,
            badgeText = badgeText.trim().ifBlank { "REKLAMA" },
            gradientType = gradientType.ifBlank { "purple" },
            actionTag = actionTag.trim(),
            isActive = isActive,
            createdAt = System.currentTimeMillis()
        )
        val id = promoBannerDao.insertBanner(entity)
        val savedBanner = entity.copy(id = if (id > 0) id else uniqueId)

        try {
            firestoreSyncService.saveBanner(savedBanner)
        } catch (e: Exception) {
            Log.w(TAG, "Banner saved locally; Firestore sync notice: ${e.message}")
        }

        return Result.success(savedBanner.id)
    }

    suspend fun updateBanner(banner: PromoBannerEntity): Result<Unit> {
        if (banner.title.trim().isEmpty()) {
            return Result.failure(IllegalArgumentException("Reklama sarlavhasini kiriting"))
        }
        promoBannerDao.updateBanner(banner)
        try {
            firestoreSyncService.saveBanner(banner)
        } catch (e: Exception) {
            Log.w(TAG, "Banner updated locally; Firestore sync notice: ${e.message}")
        }
        return Result.success(Unit)
    }

    suspend fun deleteBanner(banner: PromoBannerEntity) {
        if (banner.imageUrl.isNotBlank()) {
            ImageStorageHelper.deleteImageFile(banner.imageUrl)
        }
        promoBannerDao.deleteBanner(banner)
        try {
            firestoreSyncService.deleteBanner(banner.id)
        } catch (e: Exception) {
            Log.w(TAG, "Banner deleted locally; Firestore sync notice: ${e.message}")
        }
    }

    suspend fun deleteBannerById(id: Long) {
        val existing = promoBannerDao.getBannerById(id)
        if (existing != null && existing.imageUrl.isNotBlank()) {
            ImageStorageHelper.deleteImageFile(existing.imageUrl)
        }
        promoBannerDao.deleteBannerById(id)
        try {
            firestoreSyncService.deleteBanner(id)
        } catch (e: Exception) {
            Log.w(TAG, "Banner deleted locally; Firestore sync notice: ${e.message}")
        }
    }

    suspend fun toggleActive(id: Long, currentStatus: Boolean) {
        val newStatus = !currentStatus
        promoBannerDao.setBannerActiveStatus(id, newStatus)
        try {
            firestoreSyncService.updateBannerStatus(id, newStatus)
        } catch (e: Exception) {
            Log.w(TAG, "Banner status updated locally; Firestore sync notice: ${e.message}")
        }
    }
}

