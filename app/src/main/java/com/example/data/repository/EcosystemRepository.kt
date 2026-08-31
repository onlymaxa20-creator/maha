package com.example.data.repository

import android.util.Log
import com.example.data.local.dao.ClassifiedAdDao
import com.example.data.local.dao.CourierDao
import com.example.data.local.dao.DeliveryDao
import com.example.data.local.dao.JobDao
import com.example.data.local.dao.ServiceDao
import com.example.data.local.dao.TaxiDao
import com.example.data.local.entity.ClassifiedAdEntity
import com.example.data.local.entity.CourierApplicationEntity
import com.example.data.local.entity.DeliveryOrderEntity
import com.example.data.local.entity.JobVacancyEntity
import com.example.data.local.entity.ServiceMasterEntity
import com.example.data.local.entity.TaxiRideEntity
import com.example.data.remote.FirestoreSyncService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class LiveServerNotification(
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

class EcosystemRepository(
    private val taxiDao: TaxiDao,
    private val deliveryDao: DeliveryDao,
    private val jobDao: JobDao,
    private val serviceDao: ServiceDao,
    private val classifiedAdDao: ClassifiedAdDao,
    private val courierDao: CourierDao,
    private val firestoreSyncService: FirestoreSyncService = FirestoreSyncService()
) {
    private val TAG = "EcosystemRepository"
    private val _serverSyncEvents = MutableSharedFlow<LiveServerNotification>(extraBufferCapacity = 10)
    val serverSyncEvents = _serverSyncEvents.asSharedFlow()

    /**
     * Starts listening to Firestore real-time snapshots for Classified Ads, Jobs, and Services.
     */
    fun startRealtimeSync(scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            try {
                firestoreSyncService.observeAdsRealtime().collectLatest { remoteAds ->
                    if (remoteAds.isNotEmpty()) {
                        try {
                            classifiedAdDao.insertAds(remoteAds)
                            Log.d(TAG, "Synchronized ${remoteAds.size} classified ads from Firestore.")
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to insert remote ads: ${e.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Ads realtime sync notice: ${e.message}")
            }
        }

        scope.launch(Dispatchers.IO) {
            try {
                firestoreSyncService.observeJobsRealtime().collectLatest { remoteJobs ->
                    if (remoteJobs.isNotEmpty()) {
                        try {
                            jobDao.insertJobs(remoteJobs)
                        } catch (_: Exception) {}
                    }
                }
            } catch (_: Exception) {}
        }

        scope.launch(Dispatchers.IO) {
            try {
                firestoreSyncService.observeServicesRealtime().collectLatest { remoteServices ->
                    if (remoteServices.isNotEmpty()) {
                        try {
                            serviceDao.insertServices(remoteServices)
                        } catch (_: Exception) {}
                    }
                }
            } catch (_: Exception) {}
        }
    }

    // 🚴‍♂️ COURIER WORK APPLICATIONS
    val allCouriers: Flow<List<CourierApplicationEntity>> = courierDao.getAllCouriers()

    suspend fun applyForCourierJob(
        userId: Long,
        fullName: String,
        phone: String,
        vehicleType: String,
        area: String,
        workSchedule: String,
        passportOrLicense: String
    ): Long {
        val app = CourierApplicationEntity(
            userId = userId,
            fullName = fullName,
            phone = phone,
            vehicleType = vehicleType,
            area = area,
            workSchedule = workSchedule,
            passportOrLicense = passportOrLicense,
            status = "FAOL"
        )
        val id = courierDao.insertCourier(app)

        // Also register in services master as a courier for public hiring
        serviceDao.insertService(
            ServiceMasterEntity(
                masterName = "$fullName (Kuryer)",
                category = "Kuryerlik",
                phone = phone,
                experienceYears = 2,
                rating = 5.0,
                reviewsCount = 1,
                priceRange = "Yetkazish bepul / Shartnoma asosida",
                description = "Gagarin va Mirzacho‘l bo‘ylab tezkor yetkazib berish. Transport: $vehicleType. Ish tartibi: $workSchedule.",
                isAvailable = true
            )
        )

        _serverSyncEvents.tryEmit(
            LiveServerNotification(
                title = "🚴‍♂️ Kuryerlik arizasi qabul qilindi!",
                message = "$fullName muvaffaqiyatli Gagarin kuryerlar safiga qo‘shildi ($vehicleType)."
            )
        )
        return id
    }

    // 🚕 TAXI
    val activeRide: Flow<TaxiRideEntity?> = taxiDao.getActiveRide()
    val allRides: Flow<List<TaxiRideEntity>> = taxiDao.getAllRides()

    suspend fun requestTaxi(
        userId: Long,
        passengerName: String,
        passengerPhone: String,
        pickupAddress: String,
        destinationAddress: String,
        tariff: String,
        price: Double,
        passengerNote: String = ""
    ): Long {
        val ride = TaxiRideEntity(
            userId = userId,
            pickupAddress = pickupAddress,
            destinationAddress = destinationAddress,
            tariff = tariff,
            estimatedPrice = price,
            passengerPhone = passengerPhone,
            passengerName = passengerName,
            passengerNote = passengerNote,
            status = "SEARCHING"
        )
        val rideId = taxiDao.insertRide(ride)

        _serverSyncEvents.tryEmit(
            LiveServerNotification(
                title = "🚕 Taksi buyurtmasi yuborildi",
                message = "Haydovchi qidirilmoqda: $pickupAddress ➔ $destinationAddress"
            )
        )

        // Simulate real-time driver acceptance after 4 seconds
        CoroutineScope(Dispatchers.IO).launch {
            delay(4000)
            val updatedRide = ride.copy(
                id = rideId,
                status = "ACCEPTED",
                driverName = "Sherzod Qo‘chqorov",
                driverPhone = "+998 90 555 12 34",
                carModel = when (tariff) {
                    "Komfort" -> "Cobalt (Oq)"
                    "Yuk Taksi" -> "Labo (Kumush)"
                    "Shaharlararo" -> "Lacetti (Qora)"
                    else -> "Nexia 3 (Oq)"
                },
                carPlate = "25 A 777 AA"
            )
            taxiDao.updateRide(updatedRide)
            _serverSyncEvents.tryEmit(
                LiveServerNotification(
                    title = "🚕 Haydovchi topildi!",
                    message = "${updatedRide.carModel} (${updatedRide.carPlate}) 3 daqiqada yetib keladi."
                )
            )

            delay(6000)
            val inTripRide = updatedRide.copy(status = "ARRIVED")
            taxiDao.updateRide(inTripRide)
            _serverSyncEvents.tryEmit(
                LiveServerNotification(
                    title = "🚕 Taksi yetib keldi",
                    message = "Mashina sizni kutmoqda: $pickupAddress"
                )
            )
        }

        return rideId
    }

    suspend fun cancelTaxiRide(rideId: Long) {
        val ride = taxiDao.getRideById(rideId)
        if (ride != null) {
            taxiDao.updateRide(ride.copy(status = "CANCELLED"))
        }
    }

    suspend fun completeTaxiRide(rideId: Long) {
        val ride = taxiDao.getRideById(rideId)
        if (ride != null) {
            taxiDao.updateRide(ride.copy(status = "COMPLETED"))
        }
    }

    // 📦 DELIVERY
    val activeDelivery: Flow<DeliveryOrderEntity?> = deliveryDao.getActiveDelivery()
    val allDeliveries: Flow<List<DeliveryOrderEntity>> = deliveryDao.getAllDeliveries()

    suspend fun requestDelivery(
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
        price: Double
    ): Long {
        val delivery = DeliveryOrderEntity(
            userId = userId,
            senderName = senderName,
            senderPhone = senderPhone,
            pickupAddress = pickupAddress,
            receiverName = receiverName,
            receiverPhone = receiverPhone,
            dropoffAddress = dropoffAddress,
            parcelType = parcelType,
            weightKg = weightKg,
            comment = comment,
            estimatedPrice = price,
            status = "SEARCHING"
        )
        val id = deliveryDao.insertDelivery(delivery)

        _serverSyncEvents.tryEmit(
            LiveServerNotification(
                title = "📦 Posilka yetkazish so‘rovi berildi",
                message = "$pickupAddress ➔ $dropoffAddress"
            )
        )

        CoroutineScope(Dispatchers.IO).launch {
            delay(3500)
            val updated = delivery.copy(
                id = id,
                status = "ASSIGNED",
                courierName = "Javohir Kuryer (Mototsikl / Damas)",
                courierPhone = "+998 93 123 78 90"
            )
            deliveryDao.updateDelivery(updated)
            _serverSyncEvents.tryEmit(
                LiveServerNotification(
                    title = "📦 Kuryer biriktirildi",
                    message = "Kuryer Javohir posilkani olish uchun yo‘lda!"
                )
            )
        }

        return id
    }

    // 💼 JOBS
    val allJobs: Flow<List<JobVacancyEntity>> = jobDao.getAllJobs()

    fun getJobsByCategory(category: String): Flow<List<JobVacancyEntity>> =
        if (category.isBlank() || category == "Barchasi") jobDao.getAllJobs()
        else jobDao.getJobsByCategory(category)

    suspend fun postJobVacancy(
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
        description: String
    ): Long {
        val uniqueId = System.currentTimeMillis() * 1000L + (100..999).random()
        val job = JobVacancyEntity(
            id = uniqueId,
            userId = userId,
            title = title,
            companyName = companyName,
            category = category,
            salaryText = salaryText,
            jobType = jobType,
            location = location,
            contactPhone = contactPhone,
            contactPerson = contactPerson,
            requirements = requirements,
            description = description
        )
        val id = jobDao.insertJob(job)
        val savedJob = job.copy(id = if (id > 0) id else uniqueId)

        try {
            firestoreSyncService.saveJob(savedJob)
        } catch (_: Exception) {}

        _serverSyncEvents.tryEmit(
            LiveServerNotification(
                title = "💼 Yangi vakansiya e'lon qilindi!",
                message = "$companyName: $title ($salaryText) Gagarin tarmog‘ida chop etildi."
            )
        )
        return savedJob.id
    }

    suspend fun deleteJob(jobId: Long) {
        jobDao.deleteJob(jobId)
        try {
            firestoreSyncService.deleteJob(jobId)
        } catch (_: Exception) {}
    }

    suspend fun updateJobVacancy(job: JobVacancyEntity) {
        jobDao.updateJob(job)
        try {
            firestoreSyncService.saveJob(job)
        } catch (_: Exception) {}

        _serverSyncEvents.tryEmit(
            LiveServerNotification(
                title = "💼 Vakansiya tahrirlandi",
                message = "${job.companyName}: ${job.title} ma’lumotlari yangilandi"
            )
        )
    }

    // 🏠 SERVICES
    val allServices: Flow<List<ServiceMasterEntity>> = serviceDao.getAllServices()

    fun getServicesByCategory(category: String): Flow<List<ServiceMasterEntity>> =
        if (category.isBlank() || category == "Barchasi") serviceDao.getAllServices()
        else serviceDao.getServicesByCategory(category)

    suspend fun registerServiceMaster(
        userId: Long = 0,
        masterName: String,
        category: String,
        phone: String,
        experienceYears: Int,
        priceRange: String,
        description: String
    ): Long {
        val uniqueId = System.currentTimeMillis() * 1000L + (100..999).random()
        val service = ServiceMasterEntity(
            id = uniqueId,
            userId = userId,
            masterName = masterName,
            category = category,
            phone = phone,
            experienceYears = experienceYears,
            priceRange = priceRange,
            description = description
        )
        val id = serviceDao.insertService(service)
        val savedService = service.copy(id = if (id > 0) id else uniqueId)

        try {
            firestoreSyncService.saveService(savedService)
        } catch (_: Exception) {}

        _serverSyncEvents.tryEmit(
            LiveServerNotification(
                title = "🏠 Yangi usta ro‘yxatdan o‘tdi",
                message = "$masterName ($category) o‘z xizmatlarini taklif qilmoqda."
            )
        )
        return savedService.id
    }

    suspend fun deleteServiceMaster(serviceId: Long) {
        serviceDao.deleteService(serviceId)
        try {
            firestoreSyncService.deleteService(serviceId)
        } catch (_: Exception) {}
    }

    suspend fun updateServiceMaster(service: ServiceMasterEntity) {
        serviceDao.updateService(service)
        try {
            firestoreSyncService.saveService(service)
        } catch (_: Exception) {}

        _serverSyncEvents.tryEmit(
            LiveServerNotification(
                title = "🏠 Usta ma’lumotlari tahrirlandi",
                message = "${service.masterName} (${service.category}) ma’lumotlari yangilandi"
            )
        )
    }

    suspend fun clearAllAdsJobsServices() {
        try {
            classifiedAdDao.clearAllAds()
            jobDao.clearAllJobs()
            serviceDao.clearAllServices()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear ads/jobs/services locally: ${e.message}")
        }
        try {
            firestoreSyncService.clearAllAdsRemote()
            firestoreSyncService.clearAllJobsRemote()
            firestoreSyncService.clearAllServicesRemote()
        } catch (_: Exception) {}
    }

    // 📢 CLASSIFIED ADS (E'lonlar)
    val allAds: Flow<List<ClassifiedAdEntity>> = classifiedAdDao.getAllAds()

    fun getAdsByCategory(category: String): Flow<List<ClassifiedAdEntity>> =
        if (category.isBlank() || category == "Barchasi") classifiedAdDao.getAllAds()
        else classifiedAdDao.getAdsByCategory(category)

    suspend fun postClassifiedAd(
        userId: Long,
        authorName: String,
        authorPhone: String,
        title: String,
        category: String,
        price: Double,
        isNegotiable: Boolean,
        location: String,
        description: String,
        imageUri: String
    ): Long {
        val uniqueId = System.currentTimeMillis() * 1000L + (100..999).random()
        val ad = ClassifiedAdEntity(
            id = uniqueId,
            userId = userId,
            authorName = authorName,
            authorPhone = authorPhone,
            title = title,
            category = category,
            price = price,
            isNegotiable = isNegotiable,
            location = location,
            description = description,
            imageUri = imageUri
        )
        val id = classifiedAdDao.insertAd(ad)
        val savedAd = ad.copy(id = if (id > 0) id else uniqueId)

        try {
            firestoreSyncService.saveAd(savedAd)
        } catch (e: Exception) {
            Log.w(TAG, "Ad saved locally; cloud sync notice: ${e.message}")
        }

        _serverSyncEvents.tryEmit(
            LiveServerNotification(
                title = "📢 Yangi e'lon joylandi",
                message = "$authorName: $title narxi ${price.toLong()} so‘m"
            )
        )
        return savedAd.id
    }

    suspend fun updateClassifiedAd(ad: ClassifiedAdEntity) {
        classifiedAdDao.updateAd(ad)
        try {
            firestoreSyncService.saveAd(ad)
        } catch (_: Exception) {}

        _serverSyncEvents.tryEmit(
            LiveServerNotification(
                title = "✏️ E'lon tahrirlandi",
                message = "${ad.authorName}: ${ad.title} ma’lumotlari yangilandi"
            )
        )
    }

    suspend fun viewAd(adId: Long) {
        classifiedAdDao.incrementViews(adId)
    }

    suspend fun deleteAd(adId: Long) {
        classifiedAdDao.deleteAd(adId)
        try {
            firestoreSyncService.deleteAd(adId)
        } catch (_: Exception) {}
    }
}
