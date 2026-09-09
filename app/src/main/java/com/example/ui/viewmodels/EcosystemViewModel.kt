package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.ClassifiedAdEntity
import com.example.data.local.entity.DeliveryOrderEntity
import com.example.data.local.entity.JobVacancyEntity
import com.example.data.local.entity.ServiceMasterEntity
import com.example.data.local.entity.TaxiRideEntity
import com.example.data.repository.EcosystemRepository
import com.example.data.repository.LiveServerNotification
import com.example.data.util.GagarinGeofenceManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class EcosystemTab {
    HOME_HUB,
    BOZOR,
    FOOD,
    JOBS,
    SERVICES,
    ADS
}

class EcosystemViewModel(
    private val ecosystemRepository: EcosystemRepository
) : ViewModel() {

    init {
        // Clear existing ads, vacancies and services list as requested
        viewModelScope.launch {
            try {
                ecosystemRepository.clearAllAdsJobsServices()
            } catch (_: Exception) {}
        }
    }

    // Active SuperApp Tab
    private val _currentTab = MutableStateFlow(EcosystemTab.HOME_HUB)
    val currentTab: StateFlow<EcosystemTab> = _currentTab.asStateFlow()
    private val tabHistory = mutableListOf<EcosystemTab>()

    fun setTab(tab: EcosystemTab) {
        if (_currentTab.value != tab) {
            tabHistory.add(_currentTab.value)
            _currentTab.value = tab
        }
    }

    fun popTab(): Boolean {
        if (tabHistory.isNotEmpty()) {
            val prev = tabHistory.removeAt(tabHistory.size - 1)
            _currentTab.value = prev
            return true
        } else if (_currentTab.value != EcosystemTab.HOME_HUB) {
            _currentTab.value = EcosystemTab.HOME_HUB
            return true
        }
        return false
    }

    // Live Server Broadcast Events
    val liveNotifications = ecosystemRepository.serverSyncEvents

    // 🚴‍♂️ COURIERS STATE
    val allCouriers = ecosystemRepository.allCouriers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun applyForCourierJob(
        userId: Long,
        fullName: String,
        phone: String,
        vehicleType: String,
        area: String,
        workSchedule: String,
        passportOrLicense: String,
        onComplete: (Boolean, String) -> Unit
    ) {
        if (fullName.isBlank() || phone.isBlank()) {
            onComplete(false, "Iltimos, ism va telefon raqamingizni kiriting!")
            return
        }
        viewModelScope.launch {
            try {
                ecosystemRepository.applyForCourierJob(
                    userId = userId,
                    fullName = fullName,
                    phone = phone,
                    vehicleType = vehicleType.ifBlank { "Velosiped / Skuter" },
                    area = area.ifBlank { "Gagarin shahri" },
                    workSchedule = workSchedule.ifBlank { "Erkin grafik" },
                    passportOrLicense = passportOrLicense
                )
                onComplete(true, "Arizangiz qabul qilindi! Siz endi Gagarin rasmiy kuryerisiz.")
            } catch (e: Exception) {
                onComplete(false, "Xatolik yuz berdi: ${e.localizedMessage}")
            }
        }
    }

    // 🚕 TAXI STATE
    val activeRide: StateFlow<TaxiRideEntity?> = ecosystemRepository.activeRide
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allRides: StateFlow<List<TaxiRideEntity>> = ecosystemRepository.allRides
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedTaxiTariff = MutableStateFlow("Ekonom") // "Ekonom", "Komfort", "Yuk Taksi", "Shaharlararo"
    val selectedTaxiTariff: StateFlow<String> = _selectedTaxiTariff.asStateFlow()

    fun selectTariff(tariff: String) {
        _selectedTaxiTariff.value = tariff
    }

    fun setTaxiTariff(tariff: String) {
        selectTariff(tariff)
    }

    fun requestTaxi(
        userId: Long,
        passengerName: String,
        passengerPhone: String,
        pickup: String,
        destination: String,
        tariff: String,
        price: Double,
        note: String,
        onComplete: (Boolean, String) -> Unit
    ) {
        if (pickup.isBlank() || destination.isBlank() || passengerPhone.isBlank()) {
            onComplete(false, "Iltimos, barcha maydonlarni to‘ldiring!")
            return
        }
        viewModelScope.launch {
            try {
                ecosystemRepository.requestTaxi(
                    userId = userId,
                    passengerName = passengerName.ifBlank { "Mijoz" },
                    passengerPhone = passengerPhone,
                    pickupAddress = pickup,
                    destinationAddress = destination,
                    tariff = tariff,
                    price = price,
                    passengerNote = note
                )
                onComplete(true, "Taksi chaqirildi! Haydovchi qidirilmoqda...")
            } catch (e: Exception) {
                onComplete(false, "Xatolik: ${e.localizedMessage}")
            }
        }
    }

    fun cancelActiveRide(rideId: Long) {
        viewModelScope.launch {
            ecosystemRepository.cancelTaxiRide(rideId)
        }
    }

    fun cancelTaxi(rideId: Long) {
        cancelActiveRide(rideId)
    }

    fun completeRide(rideId: Long) {
        viewModelScope.launch {
            ecosystemRepository.completeTaxiRide(rideId)
        }
    }

    // 📦 DELIVERY STATE
    val activeDelivery: StateFlow<DeliveryOrderEntity?> = ecosystemRepository.activeDelivery
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun requestDelivery(
        userId: Long,
        senderName: String,
        senderPhone: String,
        pickupAddress: String,
        receiverName: String,
        receiverPhone: String,
        dropoffAddress: String,
        parcelType: String,
        weightKg: String,
        comment: String,
        price: Double,
        onComplete: (Boolean, String) -> Unit
    ) {
        if (pickupAddress.isBlank() || dropoffAddress.isBlank() || receiverPhone.isBlank()) {
            onComplete(false, "Iltimos, manzil va telefon raqamlarni to‘liq kiriting!")
            return
        }
        viewModelScope.launch {
            try {
                ecosystemRepository.requestDelivery(
                    userId = userId,
                    senderName = senderName.ifBlank { "Jo‘natuvchi" },
                    senderPhone = senderPhone,
                    pickupAddress = pickupAddress,
                    receiverName = receiverName.ifBlank { "Qabul qiluvchi" },
                    receiverPhone = receiverPhone,
                    dropoffAddress = dropoffAddress,
                    parcelType = parcelType,
                    weightKg = weightKg,
                    comment = comment,
                    price = price
                )
                onComplete(true, "Kuryerlik so‘rovingiz qabul qilindi! Kuryer tayinlanmoqda.")
            } catch (e: Exception) {
                onComplete(false, "Xatolik: ${e.localizedMessage}")
            }
        }
    }

    // 💼 JOBS STATE
    private val _selectedJobCategory = MutableStateFlow("Barchasi")
    val selectedJobCategory: StateFlow<String> = _selectedJobCategory.asStateFlow()

    val jobs: StateFlow<List<JobVacancyEntity>> = _selectedJobCategory
        .flatMapLatest { category -> ecosystemRepository.getJobsByCategory(category) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectJobCategory(category: String) {
        _selectedJobCategory.value = category
    }

    fun postJob(
        userId: Long = 0,
        title: String,
        companyName: String,
        category: String,
        salaryText: String,
        jobType: String,
        location: String,
        contactPhone: String,
        contactPerson: String,
        requirements: String,
        description: String,
        onComplete: (Boolean, String, Long?) -> Unit = { _, _, _ -> }
    ) {
        if (title.isBlank() || contactPhone.isBlank() || salaryText.isBlank()) {
            onComplete(false, "Lavozim nomi, oylik maosh va telefonni to‘ldiring!", null)
            return
        }
        val targetLocation = location.ifBlank { "Gagarin shahri" }
        if (!GagarinGeofenceManager.isWithinServiceArea(targetLocation)) {
            onComplete(false, GagarinGeofenceManager.OUT_OF_BOUNDS_ERROR, null)
            return
        }
        viewModelScope.launch {
            try {
                val newJobId = ecosystemRepository.postJobVacancy(
                    userId = userId,
                    title = title,
                    companyName = companyName.ifBlank { "Gagarin ish beruvchisi" },
                    category = category,
                    salaryText = salaryText,
                    jobType = jobType,
                    location = targetLocation,
                    contactPhone = contactPhone,
                    contactPerson = contactPerson,
                    requirements = requirements,
                    description = description
                )
                onComplete(true, "Vakansiya Gagarin tarmog‘ida muvaffaqiyatli e'lon qilindi!", newJobId)
            } catch (e: Exception) {
                onComplete(false, "Xatolik: ${e.localizedMessage}", null)
            }
        }
    }

    fun deleteJob(jobId: Long, onComplete: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            try {
                ecosystemRepository.deleteJob(jobId)
                onComplete(true, "Vakansiya o‘chirildi")
            } catch (e: Exception) {
                onComplete(false, "Xatolik yuz berdi: ${e.localizedMessage}")
            }
        }
    }

    fun updateJob(
        job: JobVacancyEntity,
        onComplete: (Boolean, String) -> Unit
    ) {
        if (job.title.isBlank() || job.contactPhone.isBlank() || job.salaryText.isBlank()) {
            onComplete(false, "Lavozim nomi, oylik maosh va telefonni to‘ldiring!")
            return
        }
        if (!GagarinGeofenceManager.isWithinServiceArea(job.location)) {
            onComplete(false, GagarinGeofenceManager.OUT_OF_BOUNDS_ERROR)
            return
        }
        viewModelScope.launch {
            try {
                ecosystemRepository.updateJobVacancy(job)
                onComplete(true, "Vakansiya muvaffaqiyatli tahrirlandi!")
            } catch (e: Exception) {
                onComplete(false, "Tahrirlashda xatolik: ${e.localizedMessage}")
            }
        }
    }

    // 🏠 SERVICES STATE
    private val _selectedServiceCategory = MutableStateFlow("Barchasi")
    val selectedServiceCategory: StateFlow<String> = _selectedServiceCategory.asStateFlow()

    val serviceMasters: StateFlow<List<ServiceMasterEntity>> = _selectedServiceCategory
        .flatMapLatest { category -> ecosystemRepository.getServicesByCategory(category) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectServiceCategory(category: String) {
        _selectedServiceCategory.value = category
    }

    fun registerMaster(
        userId: Long = 0,
        masterName: String,
        category: String,
        phone: String,
        experienceYears: Int,
        priceRange: String,
        description: String,
        onComplete: (Boolean, String, Long?) -> Unit = { _, _, _ -> }
    ) {
        if (masterName.isBlank() || phone.isBlank()) {
            onComplete(false, "Ism va telefon raqamni kiriting!", null)
            return
        }
        viewModelScope.launch {
            try {
                val newMasterId = ecosystemRepository.registerServiceMaster(
                    userId = userId,
                    masterName = masterName,
                    category = category,
                    phone = phone,
                    experienceYears = experienceYears,
                    priceRange = priceRange,
                    description = description
                )
                onComplete(true, "Usta anketasi Gagarin xizmatlariga qo‘shildi!", newMasterId)
            } catch (e: Exception) {
                onComplete(false, "Xatolik: ${e.localizedMessage}", null)
            }
        }
    }

    fun updateServiceMaster(
        service: ServiceMasterEntity,
        onComplete: (Boolean, String) -> Unit
    ) {
        if (service.masterName.isBlank() || service.phone.isBlank()) {
            onComplete(false, "Ism va telefon raqamni kiriting!")
            return
        }
        viewModelScope.launch {
            try {
                ecosystemRepository.updateServiceMaster(service)
                onComplete(true, "Usta ma’lumotlari muvaffaqiyatli tahrirlandi!")
            } catch (e: Exception) {
                onComplete(false, "Tahrirlashda xatolik: ${e.localizedMessage}")
            }
        }
    }

    fun deleteServiceMaster(serviceId: Long, onComplete: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            try {
                ecosystemRepository.deleteServiceMaster(serviceId)
                onComplete(true, "Usta anketasi o‘chirildi")
            } catch (e: Exception) {
                onComplete(false, "Xatolik yuz berdi: ${e.localizedMessage}")
            }
        }
    }

    fun clearAllAdsJobsServices(onComplete: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            try {
                ecosystemRepository.clearAllAdsJobsServices()
                onComplete(true, "Barcha e'lonlar, vakansiyalar va ustalar ro'yxati tozalandi!")
            } catch (e: Exception) {
                onComplete(false, "Tozalashda xatolik: ${e.localizedMessage}")
            }
        }
    }

    // 📢 CLASSIFIED ADS (E'lonlar)
    private val _selectedAdCategory = MutableStateFlow("Barchasi")
    val selectedAdCategory: StateFlow<String> = _selectedAdCategory.asStateFlow()

    val ads: StateFlow<List<ClassifiedAdEntity>> = _selectedAdCategory
        .flatMapLatest { category -> ecosystemRepository.getAdsByCategory(category) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectAdCategory(category: String) {
        _selectedAdCategory.value = category
    }

    fun postAd(
        userId: Long,
        authorName: String,
        authorPhone: String,
        title: String,
        category: String,
        price: Double,
        isNegotiable: Boolean,
        location: String,
        description: String,
        imageUri: String,
        onComplete: (Boolean, String, Long?) -> Unit
    ) {
        if (title.isBlank() || authorPhone.isBlank() || price <= 0) {
            onComplete(false, "E'lon sarlavhasi, narxi va telefon raqamini kiriting!", null)
            return
        }
        val targetLocation = location.ifBlank { "Gagarin" }
        if (!GagarinGeofenceManager.isWithinServiceArea(targetLocation)) {
            onComplete(false, GagarinGeofenceManager.OUT_OF_BOUNDS_ERROR, null)
            return
        }
        viewModelScope.launch {
            try {
                val newAdId = ecosystemRepository.postClassifiedAd(
                    userId = userId,
                    authorName = authorName.ifBlank { "Foydalanuvchi" },
                    authorPhone = authorPhone,
                    title = title,
                    category = category,
                    price = price,
                    isNegotiable = isNegotiable,
                    location = targetLocation,
                    description = description,
                    imageUri = imageUri
                )
                onComplete(true, "E'loningiz Gagarin e'lonlar doskasiga joylashtirildi!", newAdId)
            } catch (e: Exception) {
                onComplete(false, "Xatolik: ${e.localizedMessage}", null)
            }
        }
    }

    fun updateAd(
        ad: ClassifiedAdEntity,
        onComplete: (Boolean, String) -> Unit
    ) {
        if (ad.title.isBlank() || ad.authorPhone.isBlank() || ad.price <= 0) {
            onComplete(false, "E'lon sarlavhasi, narxi va telefon raqamini kiriting!")
            return
        }
        if (!GagarinGeofenceManager.isWithinServiceArea(ad.location)) {
            onComplete(false, GagarinGeofenceManager.OUT_OF_BOUNDS_ERROR)
            return
        }
        viewModelScope.launch {
            try {
                ecosystemRepository.updateClassifiedAd(ad)
                onComplete(true, "E'lon muvaffaqiyatli tahrirlandi!")
            } catch (e: Exception) {
                onComplete(false, "Tahrirlashda xatolik: ${e.localizedMessage}")
            }
        }
    }

    fun viewAd(adId: Long) {
        viewModelScope.launch {
            ecosystemRepository.viewAd(adId)
        }
    }

    fun deleteAd(adId: Long, onComplete: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            try {
                ecosystemRepository.deleteAd(adId)
                onComplete(true, "E'lon muvaffaqiyatli o‘chirildi")
            } catch (e: Exception) {
                onComplete(false, "Xatolik yuz berdi: ${e.localizedMessage}")
            }
        }
    }

    class Factory(private val ecosystemRepository: EcosystemRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(EcosystemViewModel::class.java)) {
                return EcosystemViewModel(ecosystemRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
