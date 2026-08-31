package com.example.data.repository

import android.util.Log
import com.example.data.local.dao.SupportDao
import com.example.data.local.entity.SupportInfoEntity
import com.example.data.remote.FirestoreSyncService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SupportRepository(
    private val supportDao: SupportDao,
    private val firestoreSyncService: FirestoreSyncService = FirestoreSyncService()
) {
    private val TAG = "SupportRepository"

    val defaultSupportInfo = SupportInfoEntity(
        id = 1,
        phone = "+998 90 123 45 67",
        secondaryPhone = "+998 91 987 65 43",
        telegramUsername = "@GagarinGoSupport",
        telegramChannel = "@gagarin_go_app",
        workingHours = "Har kuni: 08:00 - 22:00",
        address = "Gagarin shahri, Do‘stlik shoh ko‘chasi 12-uy",
        description = "Gagarin Go xizmatlari bo‘yicha savollaringiz bo‘lsa, istalgan vaqtda biz bilan bog‘laning!"
    )

    /**
     * Starts listening to Firestore real-time snapshots for Support & App Update Configuration.
     * When Admin updates contact phones, Telegram channels, working hours, or forces an in-app update,
     * it instantly reflects across all user devices in real-time.
     */
    fun startRealtimeSupportSync(scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            try {
                firestoreSyncService.observeSupportInfoRealtime().collectLatest { remoteInfo ->
                    if (remoteInfo != null) {
                        try {
                            supportDao.insertOrUpdateSupportInfo(remoteInfo)
                            Log.d(TAG, "Synchronized support and update config from Firestore.")
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to insert remote support info: ${e.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Support info realtime sync notice: ${e.message}")
            }
        }
    }

    fun getSupportInfo(): Flow<SupportInfoEntity> {
        return supportDao.getSupportInfo().map { entity ->
            entity ?: defaultSupportInfo
        }
    }

    suspend fun getSupportInfoDirect(): SupportInfoEntity {
        return supportDao.getSupportInfoDirect() ?: defaultSupportInfo
    }

    suspend fun updateSupportInfo(
        phone: String,
        secondaryPhone: String,
        telegramUsername: String,
        telegramChannel: String,
        workingHours: String,
        address: String,
        description: String
    ): Result<SupportInfoEntity> {
        val current = getSupportInfoDirect()
        val updated = current.copy(
            phone = phone.trim().ifEmpty { defaultSupportInfo.phone },
            secondaryPhone = secondaryPhone.trim(),
            telegramUsername = telegramUsername.trim().ifEmpty { defaultSupportInfo.telegramUsername },
            telegramChannel = telegramChannel.trim().ifEmpty { defaultSupportInfo.telegramChannel },
            workingHours = workingHours.trim().ifEmpty { defaultSupportInfo.workingHours },
            address = address.trim().ifEmpty { defaultSupportInfo.address },
            description = description.trim().ifEmpty { defaultSupportInfo.description },
            updatedAt = System.currentTimeMillis()
        )
        supportDao.insertOrUpdateSupportInfo(updated)
        try {
            firestoreSyncService.saveSupportInfo(updated)
        } catch (e: Exception) {
            Log.w(TAG, "Support info saved locally; Firestore sync notice: ${e.message}")
        }
        return Result.success(updated)
    }

    suspend fun updateAppVersionConfig(
        latestVersionCode: Int,
        latestVersionName: String,
        minRequiredVersionCode: Int,
        isForceUpdate: Boolean,
        updateTitle: String,
        updateMessage: String,
        releaseNotes: String,
        playStoreUrl: String,
        telegramApkUrl: String
    ): Result<SupportInfoEntity> {
        val current = getSupportInfoDirect()
        val updated = current.copy(
            latestVersionCode = latestVersionCode,
            latestVersionName = latestVersionName.trim().ifEmpty { current.latestVersionName },
            minRequiredVersionCode = minRequiredVersionCode,
            isForceUpdate = isForceUpdate,
            updateTitle = updateTitle.trim().ifEmpty { current.updateTitle },
            updateMessage = updateMessage.trim().ifEmpty { current.updateMessage },
            releaseNotes = releaseNotes.trim().ifEmpty { current.releaseNotes },
            playStoreUrl = playStoreUrl.trim().ifEmpty { current.playStoreUrl },
            telegramApkUrl = telegramApkUrl.trim().ifEmpty { current.telegramApkUrl },
            updatedAt = System.currentTimeMillis()
        )
        supportDao.insertOrUpdateSupportInfo(updated)
        try {
            firestoreSyncService.saveSupportInfo(updated)
        } catch (e: Exception) {
            Log.w(TAG, "App version config saved locally; Firestore sync notice: ${e.message}")
        }
        return Result.success(updated)
    }
}
