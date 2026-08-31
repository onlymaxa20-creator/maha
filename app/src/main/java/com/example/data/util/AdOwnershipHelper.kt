package com.example.data.util

import android.content.Context
import android.content.SharedPreferences
import com.example.data.local.entity.ClassifiedAdEntity
import com.example.data.local.entity.JobVacancyEntity
import com.example.data.local.entity.ServiceMasterEntity
import com.example.data.model.AuthSession
import com.example.data.model.UserRole

object AdOwnershipHelper {
    private const val PREFS_NAME = "gagarin_ad_ownership_prefs"
    private const val KEY_MY_ADS = "my_posted_ad_ids"
    private const val KEY_MY_JOBS = "my_posted_job_ids"
    private const val KEY_MY_MASTERS = "my_posted_master_ids"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // --- CLASSIFIED ADS (E'LONLAR) ---
    fun registerMyAd(context: Context, adId: Long) {
        if (adId <= 0L) return
        val prefs = getPrefs(context)
        val currentSet = prefs.getStringSet(KEY_MY_ADS, emptySet())?.toMutableSet() ?: mutableSetOf()
        currentSet.add(adId.toString())
        prefs.edit().putStringSet(KEY_MY_ADS, currentSet).apply()
    }

    fun removeMyAd(context: Context, adId: Long) {
        if (adId <= 0L) return
        val prefs = getPrefs(context)
        val currentSet = prefs.getStringSet(KEY_MY_ADS, emptySet())?.toMutableSet() ?: return
        if (currentSet.remove(adId.toString())) {
            prefs.edit().putStringSet(KEY_MY_ADS, currentSet).apply()
        }
    }

    fun getMyPostedAdIds(context: Context): Set<Long> {
        val prefs = getPrefs(context)
        val stringSet = prefs.getStringSet(KEY_MY_ADS, emptySet()) ?: emptySet()
        return stringSet.mapNotNull { it.toLongOrNull() }.toSet()
    }

    fun isMyAd(context: Context, adId: Long): Boolean {
        if (adId <= 0L) return false
        val myIds = getMyPostedAdIds(context)
        return myIds.contains(adId)
    }

    // --- JOBS & VACANCIES (VAKANSIYALAR) ---
    fun registerMyJob(context: Context, jobId: Long) {
        if (jobId <= 0L) return
        val prefs = getPrefs(context)
        val currentSet = prefs.getStringSet(KEY_MY_JOBS, emptySet())?.toMutableSet() ?: mutableSetOf()
        currentSet.add(jobId.toString())
        prefs.edit().putStringSet(KEY_MY_JOBS, currentSet).apply()
    }

    fun removeMyJob(context: Context, jobId: Long) {
        if (jobId <= 0L) return
        val prefs = getPrefs(context)
        val currentSet = prefs.getStringSet(KEY_MY_JOBS, emptySet())?.toMutableSet() ?: return
        if (currentSet.remove(jobId.toString())) {
            prefs.edit().putStringSet(KEY_MY_JOBS, currentSet).apply()
        }
    }

    fun isMyJob(context: Context, jobId: Long): Boolean {
        if (jobId <= 0L) return false
        val prefs = getPrefs(context)
        val stringSet = prefs.getStringSet(KEY_MY_JOBS, emptySet()) ?: emptySet()
        return stringSet.contains(jobId.toString())
    }

    // --- SERVICE MASTERS (USTALAR) ---
    fun registerMyMaster(context: Context, masterId: Long) {
        if (masterId <= 0L) return
        val prefs = getPrefs(context)
        val currentSet = prefs.getStringSet(KEY_MY_MASTERS, emptySet())?.toMutableSet() ?: mutableSetOf()
        currentSet.add(masterId.toString())
        prefs.edit().putStringSet(KEY_MY_MASTERS, currentSet).apply()
    }

    fun removeMyMaster(context: Context, masterId: Long) {
        if (masterId <= 0L) return
        val prefs = getPrefs(context)
        val currentSet = prefs.getStringSet(KEY_MY_MASTERS, emptySet())?.toMutableSet() ?: return
        if (currentSet.remove(masterId.toString())) {
            prefs.edit().putStringSet(KEY_MY_MASTERS, currentSet).apply()
        }
    }

    fun isMyMaster(context: Context, masterId: Long): Boolean {
        if (masterId <= 0L) return false
        val prefs = getPrefs(context)
        val stringSet = prefs.getStringSet(KEY_MY_MASTERS, emptySet()) ?: emptySet()
        return stringSet.contains(masterId.toString())
    }

    private fun normalizePhone(phone: String?): String {
        if (phone.isNullOrBlank()) return ""
        return phone.replace("[^0-9]".toRegex(), "")
    }

    /**
     * Returns true ONLY if current user or device is the actual creator of this ad.
     * Admins who did not create the ad cannot delete it.
     */
    fun isAdCreator(context: Context, ad: ClassifiedAdEntity, session: AuthSession): Boolean {
        // 1. Local device record (the ad was posted on this device)
        if (isMyAd(context, ad.id)) {
            return true
        }

        // 2. User ID match (when posted while logged in)
        if (session.isLoggedIn && session.user != null) {
            val user = session.user
            if (user.id > 0L && user.id == ad.userId) {
                return true
            }

            // 3. Phone number match
            val userPhoneNorm = normalizePhone(user.phone)
            val adPhoneNorm = normalizePhone(ad.authorPhone)
            if (userPhoneNorm.isNotBlank() && adPhoneNorm.isNotBlank() && userPhoneNorm == adPhoneNorm) {
                return true
            }

            // 4. Full name match (fallback)
            if (user.fullName.isNotBlank() && ad.authorName.isNotBlank() &&
                user.fullName.trim().equals(ad.authorName.trim(), ignoreCase = true)
            ) {
                return true
            }
        }

        return false
    }

    fun canDeleteAd(context: Context, ad: ClassifiedAdEntity, session: AuthSession): Boolean {
        return isAdCreator(context, ad, session)
    }

    fun canManageAd(context: Context, ad: ClassifiedAdEntity, session: AuthSession): Boolean {
        return isAdCreator(context, ad, session)
    }

    /**
     * Returns true ONLY if current user or device is the actual creator of this job vacancy.
     * "vakansiya ustalar bolimiga qoyilgan narsani ham faqat qoygan odam ochirsin"
     */
    fun isJobCreator(context: Context, job: JobVacancyEntity, session: AuthSession): Boolean {
        // 1. Local device record (posted on this device)
        if (isMyJob(context, job.id)) {
            return true
        }

        // 2. User ID match (when posted while logged in)
        if (session.isLoggedIn && session.user != null) {
            val user = session.user
            if (user.id > 0L && user.id == job.userId) {
                return true
            }

            // 3. Phone number match
            val userPhoneNorm = normalizePhone(user.phone)
            val jobPhoneNorm = normalizePhone(job.contactPhone)
            if (userPhoneNorm.isNotBlank() && jobPhoneNorm.isNotBlank() && userPhoneNorm == jobPhoneNorm) {
                return true
            }

            // 4. Contact person match
            if (user.fullName.isNotBlank() && job.contactPerson.isNotBlank() &&
                user.fullName.trim().equals(job.contactPerson.trim(), ignoreCase = true)
            ) {
                return true
            }
        }

        return false
    }

    fun canDeleteJob(context: Context, job: JobVacancyEntity, session: AuthSession): Boolean {
        return isJobCreator(context, job, session)
    }

    fun canManageJob(context: Context, job: JobVacancyEntity, session: AuthSession): Boolean {
        return isJobCreator(context, job, session)
    }

    /**
     * Returns true ONLY if current user or device is the actual creator of this master/craftsman profile.
     * "vakansiya ustalar bolimiga qoyilgan narsani ham faqat qoygan odam ochirsin"
     */
    fun isMasterCreator(context: Context, master: ServiceMasterEntity, session: AuthSession): Boolean {
        // 1. Local device record (posted on this device)
        if (isMyMaster(context, master.id)) {
            return true
        }

        // 2. User ID match
        if (session.isLoggedIn && session.user != null) {
            val user = session.user
            if (user.id > 0L && user.id == master.userId) {
                return true
            }

            // 3. Phone match
            val userPhoneNorm = normalizePhone(user.phone)
            val masterPhoneNorm = normalizePhone(master.phone)
            if (userPhoneNorm.isNotBlank() && masterPhoneNorm.isNotBlank() && userPhoneNorm == masterPhoneNorm) {
                return true
            }

            // 4. Name match
            if (user.fullName.isNotBlank() && master.masterName.isNotBlank() &&
                user.fullName.trim().equals(master.masterName.trim(), ignoreCase = true)
            ) {
                return true
            }
        }

        return false
    }

    fun canDeleteMaster(context: Context, master: ServiceMasterEntity, session: AuthSession): Boolean {
        return isMasterCreator(context, master, session)
    }

    fun canManageMaster(context: Context, master: ServiceMasterEntity, session: AuthSession): Boolean {
        return isMasterCreator(context, master, session)
    }
}
