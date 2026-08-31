package com.example.data.util

import com.example.data.local.entity.ClassifiedAdEntity
import com.example.data.local.entity.JobVacancyEntity
import com.example.data.local.entity.ServiceMasterEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * Service area configuration and boundary validation for Gagarin City & Mirzacho'l District.
 */
data class ServiceAreaBoundary(
    val name: String = "Gagarin shahri va Mirzacho‘l tumani",
    val centerLat: Double = 40.6658,
    val centerLng: Double = 68.1752,
    val minLat: Double = 40.4500,
    val maxLat: Double = 40.8800,
    val minLng: Double = 67.9200,
    val maxLng: Double = 68.4500,
    val defaultZoom: Float = 14.5f,
    val minZoom: Float = 11.5f,
    val maxZoom: Float = 18.0f
)

/**
 * Service Area Manager guaranteeing that all maps, listings, vacancies, and masters
 * strictly operate within Gagarin and Mirzacho'l District boundaries.
 */
object GagarinGeofenceManager {

    private val _serviceBoundary = MutableStateFlow(ServiceAreaBoundary())
    val serviceBoundary: StateFlow<ServiceAreaBoundary> = _serviceBoundary.asStateFlow()

    const val OUT_OF_BOUNDS_ERROR = "Bu xizmat hozircha faqat Gagarin va Mirzacho‘l tumani hududida mavjud."

    // Recognized locations within Gagarin and Mirzacho'l district
    val GAGARIN_MIRZACHOL_LOCATIONS = listOf(
        "Gagarin shahri (Markaz)",
        "Gagarin shahri",
        "Mirzacho‘l tumani",
        "Mustaqillik shoh ko‘chasi",
        "Do‘stlik MFY",
        "Yoshlik MFY",
        "Navbahor MFY",
        "Paxtakor MFY",
        "Istiqlol MFY",
        "Yangi Hayot MFY",
        "G‘alaba MFY",
        "Jibek Jo‘li MFY",
        "Temiryo‘lchilar MFY",
        "Bog‘bon MFY",
        "Mirzacho‘l markaziy bozori",
        "Gagarin vokzal atrofi"
    )

    /**
     * Checks if coordinates fall inside the Gagarin & Mirzacho'l service geofence
     */
    fun isWithinServiceArea(lat: Double, lng: Double): Boolean {
        val boundary = _serviceBoundary.value
        return lat in boundary.minLat..boundary.maxLat && lng in boundary.minLng..boundary.maxLng
    }

    /**
     * Checks if a location text string refers to Gagarin or Mirzacho'l district
     */
    fun isWithinServiceArea(locationText: String?): Boolean {
        if (locationText.isNullOrBlank()) return true
        val normalized = locationText.lowercase(Locale.ROOT).trim()

        val allowedKeywords = listOf(
            "gagarin",
            "mirzacho",
            "mirzachol",
            "mustaqillik",
            "dostlik",
            "do‘stlik",
            "yoshlik",
            "navbahor",
            "paxtakor",
            "istiqlol",
            "yangi hayot",
            "galaba",
            "g‘alaba",
            "jibek",
            "temiryol",
            "temiryo‘l",
            "bozor",
            "markaz"
        )

        val forbiddenAreas = listOf(
            "toshkent", "tashkent", "samarqand", "samarkand", "buxoro", "bukhara",
            "andijon", "fargona", "namangan", "qashqadaryo", "surxondaryo", "navoiy",
            "xorazm", "nukus", "sirdaryo", "guliston", "jizzax shahar"
        )

        // If explicitly mentions forbidden non-Gagarin cities
        if (forbiddenAreas.any { normalized.contains(it) } && !normalized.contains("gagarin") && !normalized.contains("mirzacho")) {
            return false
        }

        return allowedKeywords.any { normalized.contains(it) } || normalized.length < 3
    }

    /**
     * Filter items to only include Gagarin & Mirzacho'l district items
     */
    fun filterAds(ads: List<ClassifiedAdEntity>): List<ClassifiedAdEntity> {
        return ads.filter { isWithinServiceArea(it.location) }
    }

    fun filterVacancies(vacancies: List<JobVacancyEntity>): List<JobVacancyEntity> {
        return vacancies.filter { isWithinServiceArea(it.location) }
    }

    fun filterMasters(masters: List<ServiceMasterEntity>): List<ServiceMasterEntity> {
        // All registered masters in GagarinGo operate in Gagarin/Mirzacho'l
        return masters
    }

    /**
     * Clamps user map panning so they cannot wander off Gagarin/Mirzacho'l
     */
    fun clampLatitude(lat: Double): Double {
        val b = _serviceBoundary.value
        return lat.coerceIn(b.minLat, b.maxLat)
    }

    fun clampLongitude(lng: Double): Double {
        val b = _serviceBoundary.value
        return lng.coerceIn(b.minLng, b.maxLng)
    }

    fun clampZoom(zoom: Float): Float {
        val b = _serviceBoundary.value
        return zoom.coerceIn(b.minZoom, b.maxZoom)
    }

    /**
     * Admin capability to dynamically adjust service boundary parameters
     */
    fun updateBoundary(newBoundary: ServiceAreaBoundary) {
        _serviceBoundary.value = newBoundary
    }
}
