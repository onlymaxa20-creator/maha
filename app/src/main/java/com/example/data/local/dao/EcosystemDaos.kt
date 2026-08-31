package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ClassifiedAdEntity
import com.example.data.local.entity.DeliveryOrderEntity
import com.example.data.local.entity.JobVacancyEntity
import com.example.data.local.entity.ServiceMasterEntity
import com.example.data.local.entity.TaxiRideEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaxiDao {
    @Query("SELECT * FROM taxi_rides ORDER BY createdAt DESC")
    fun getAllRides(): Flow<List<TaxiRideEntity>>

    @Query("SELECT * FROM taxi_rides WHERE status != 'COMPLETED' AND status != 'CANCELLED' ORDER BY createdAt DESC LIMIT 1")
    fun getActiveRide(): Flow<TaxiRideEntity?>

    @Query("SELECT * FROM taxi_rides WHERE id = :id")
    suspend fun getRideById(id: Long): TaxiRideEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRide(ride: TaxiRideEntity): Long

    @Update
    suspend fun updateRide(ride: TaxiRideEntity)

    @Query("DELETE FROM taxi_rides WHERE id = :id")
    suspend fun deleteRide(id: Long)
}

@Dao
interface DeliveryDao {
    @Query("SELECT * FROM delivery_orders ORDER BY createdAt DESC")
    fun getAllDeliveries(): Flow<List<DeliveryOrderEntity>>

    @Query("SELECT * FROM delivery_orders WHERE status != 'DELIVERED' AND status != 'CANCELLED' ORDER BY createdAt DESC LIMIT 1")
    fun getActiveDelivery(): Flow<DeliveryOrderEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDelivery(delivery: DeliveryOrderEntity): Long

    @Update
    suspend fun updateDelivery(delivery: DeliveryOrderEntity)
}

@Dao
interface JobDao {
    @Query("SELECT * FROM job_vacancies WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getAllJobs(): Flow<List<JobVacancyEntity>>

    @Query("SELECT * FROM job_vacancies WHERE category = :category AND isActive = 1 ORDER BY createdAt DESC")
    fun getJobsByCategory(category: String): Flow<List<JobVacancyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: JobVacancyEntity): Long

    @Update
    suspend fun updateJob(job: JobVacancyEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJobs(jobs: List<JobVacancyEntity>)

    @Query("DELETE FROM job_vacancies WHERE id = :id")
    suspend fun deleteJob(id: Long)

    @Query("DELETE FROM job_vacancies")
    suspend fun clearAllJobs()
}

@Dao
interface ServiceDao {
    @Query("SELECT * FROM service_masters ORDER BY rating DESC")
    fun getAllServices(): Flow<List<ServiceMasterEntity>>

    @Query("SELECT * FROM service_masters WHERE category = :category ORDER BY rating DESC")
    fun getServicesByCategory(category: String): Flow<List<ServiceMasterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertService(service: ServiceMasterEntity): Long

    @Update
    suspend fun updateService(service: ServiceMasterEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServices(services: List<ServiceMasterEntity>)

    @Query("DELETE FROM service_masters WHERE id = :id")
    suspend fun deleteService(id: Long)

    @Query("DELETE FROM service_masters")
    suspend fun clearAllServices()
}

@Dao
interface ClassifiedAdDao {
    @Query("SELECT * FROM classified_ads ORDER BY createdAt DESC")
    fun getAllAds(): Flow<List<ClassifiedAdEntity>>

    @Query("SELECT * FROM classified_ads WHERE category = :category ORDER BY createdAt DESC")
    fun getAdsByCategory(category: String): Flow<List<ClassifiedAdEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAd(ad: ClassifiedAdEntity): Long

    @Update
    suspend fun updateAd(ad: ClassifiedAdEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAds(ads: List<ClassifiedAdEntity>)

    @Query("UPDATE classified_ads SET viewsCount = viewsCount + 1 WHERE id = :id")
    suspend fun incrementViews(id: Long)

    @Query("DELETE FROM classified_ads WHERE id = :id")
    suspend fun deleteAd(id: Long)

    @Query("DELETE FROM classified_ads")
    suspend fun clearAllAds()
}

@Dao
interface CourierDao {
    @Query("SELECT * FROM courier_applications ORDER BY createdAt DESC")
    fun getAllCouriers(): Flow<List<com.example.data.local.entity.CourierApplicationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourier(courier: com.example.data.local.entity.CourierApplicationEntity): Long

    @Query("SELECT * FROM courier_applications WHERE userId = :userId LIMIT 1")
    suspend fun getCourierByUserId(userId: Long): com.example.data.local.entity.CourierApplicationEntity?
}
