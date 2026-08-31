package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.CartDao
import com.example.data.local.dao.CategoryDao
import com.example.data.local.dao.ClassifiedAdDao
import com.example.data.local.dao.DeliveryDao
import com.example.data.local.dao.FavoriteDao
import com.example.data.local.dao.JobDao
import com.example.data.local.dao.OrderDao
import com.example.data.local.dao.ProductDao
import com.example.data.local.dao.PromoBannerDao
import com.example.data.local.dao.ReviewDao
import com.example.data.local.dao.ServiceDao
import com.example.data.local.dao.TaxiDao
import com.example.data.local.dao.UserDao
import com.example.data.local.entity.CartItemEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.ClassifiedAdEntity
import com.example.data.local.entity.DeliveryOrderEntity
import com.example.data.local.entity.FavoriteItemEntity
import com.example.data.local.entity.FoodBannerEntity
import com.example.data.local.entity.FoodCategoryEntity
import com.example.data.local.entity.FoodOrderEntity
import com.example.data.local.entity.FoodProductEntity
import com.example.data.local.entity.FoodPromotionEntity
import com.example.data.local.entity.FoodRestaurantEntity
import com.example.data.local.entity.FoodReviewEntity
import com.example.data.local.entity.JobVacancyEntity
import com.example.data.local.entity.OrderEntity
import com.example.data.local.entity.OrderItemEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.PromoBannerEntity
import com.example.data.local.entity.ReviewEntity
import com.example.data.local.entity.ServiceMasterEntity
import com.example.data.local.entity.TaxiRideEntity
import com.example.data.local.entity.UserEntity
import com.example.data.util.SecurityUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Database(
    entities = [
        UserEntity::class,
        CategoryEntity::class,
        ProductEntity::class,
        CartItemEntity::class,
        FavoriteItemEntity::class,
        OrderEntity::class,
        OrderItemEntity::class,
        PromoBannerEntity::class,
        ReviewEntity::class,
        TaxiRideEntity::class,
        DeliveryOrderEntity::class,
        JobVacancyEntity::class,
        ServiceMasterEntity::class,
        ClassifiedAdEntity::class,
        com.example.data.local.entity.CourierApplicationEntity::class,
        FoodRestaurantEntity::class,
        FoodCategoryEntity::class,
        FoodProductEntity::class,
        FoodOrderEntity::class,
        FoodBannerEntity::class,
        FoodPromotionEntity::class,
        FoodReviewEntity::class,
        com.example.data.local.entity.SupportInfoEntity::class,
        com.example.data.local.entity.AdminAuditLogEntity::class,
        com.example.data.local.entity.UserInteractionEventEntity::class
    ],
    version = 13,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun productDao(): ProductDao
    abstract fun cartDao(): CartDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun orderDao(): OrderDao
    abstract fun promoBannerDao(): PromoBannerDao
    abstract fun reviewDao(): ReviewDao
    abstract fun taxiDao(): TaxiDao
    abstract fun deliveryDao(): DeliveryDao
    abstract fun jobDao(): JobDao
    abstract fun serviceDao(): ServiceDao
    abstract fun classifiedAdDao(): ClassifiedAdDao
    abstract fun courierDao(): com.example.data.local.dao.CourierDao
    abstract fun foodRestaurantDao(): com.example.data.local.dao.FoodRestaurantDao
    abstract fun foodCategoryDao(): com.example.data.local.dao.FoodCategoryDao
    abstract fun foodProductDao(): com.example.data.local.dao.FoodProductDao
    abstract fun foodOrderDao(): com.example.data.local.dao.FoodOrderDao
    abstract fun foodBannerDao(): com.example.data.local.dao.FoodBannerDao
    abstract fun foodPromotionDao(): com.example.data.local.dao.FoodPromotionDao
    abstract fun foodReviewDao(): com.example.data.local.dao.FoodReviewDao
    abstract fun supportDao(): com.example.data.local.dao.SupportDao
    abstract fun adminAuditLogDao(): com.example.data.local.dao.AdminAuditLogDao
    abstract fun userInteractionEventDao(): com.example.data.local.dao.UserInteractionEventDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gagarin_savdo_store.db"
                ).fallbackToDestructiveMigration()
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            val database = getInstance(context)
                            seedInitialSystemData(database, context, isFirstCreate = true)
                        }
                    }

                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            val database = getInstance(context)
                            seedInitialSystemData(database, context, isFirstCreate = false)
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }

        private val seedMutex = Mutex()

        suspend fun seedInitialSystemData(database: AppDatabase, context: Context? = null, isFirstCreate: Boolean = false) {
            seedMutex.withLock {
                val deletedKeys = if (context != null) {
                    try {
                        context.getSharedPreferences("gagarin_auth_prefs", Context.MODE_PRIVATE)
                            .getStringSet("deleted_user_keys", emptySet()) ?: emptySet()
                    } catch (_: Exception) { emptySet() }
                } else emptySet()

            // Seed Super Admin: maha10 / maha1010 ONLY IF NOT EXISTS (guarantee admin access)
            val existingMahaAdmin = database.userDao().getUserByLogin("maha10")
            if (existingMahaAdmin == null && !deletedKeys.contains("login_maha10")) {
                val salt = SecurityUtils.generateSalt()
                val passwordHash = SecurityUtils.hashPassword("maha1010", salt)
                database.userDao().insertUser(
                    UserEntity(
                        role = "SUPER_ADMIN",
                        login = "maha10",
                        email = "maha10@gagarin.uz",
                        passwordHash = passwordHash,
                        salt = salt,
                        fullName = "Bosh Administrator",
                        phone = "+998 90 123 45 67",
                        savedAddress = "Gagarin shahri, Mirzacho‘l tumani"
                    )
                )
            }

            // Cleanup old legacy test admin and seller if present
            try {
                val legacyAdmin = database.userDao().getUserByLogin("admin")
                if (legacyAdmin != null && legacyAdmin.login != "maha10") {
                    database.userDao().deleteUser(legacyAdmin)
                }
                val legacySeller = database.userDao().getUserByLogin("seller")
                if (legacySeller != null) {
                    database.userDao().deleteUser(legacySeller)
                }
            } catch (e: Exception) {
                // Ignore
            }

            // If this is NOT the very first database creation, do NOT recreate deleted sellers, restaurants, or categories!
            if (!isFirstCreate) {
                return
            }

            // Seed Food Admin if not exists
            val existingFoodAdmin = database.userDao().getUserByLogin("foodadmin")
            if (existingFoodAdmin == null && !deletedKeys.contains("login_foodadmin")) {
                val foodAdminSalt = SecurityUtils.generateSalt()
                val foodAdminHash = SecurityUtils.hashPassword("foodadmin123", foodAdminSalt)
                database.userDao().insertUser(
                    UserEntity(
                        role = "FOODS_ADMIN",
                        login = "foodadmin",
                        email = "foodadmin@gagarin.uz",
                        passwordHash = foodAdminHash,
                        salt = foodAdminSalt,
                        fullName = "Gagarin Food Administrator",
                        phone = "+998 90 999 88 77",
                        savedAddress = "Gagarin shahri"
                    )
                )
            }

            // Seed default Foods seller if not exists (only on fresh database creation and not marked deleted)
            val existingFoodSeller = database.userDao().getUserByLogin("foodseller")
            if (existingFoodSeller == null && !deletedKeys.contains("login_foodseller") && !deletedKeys.contains("phone_+998 93 111 22 33")) {
                val fsSalt = SecurityUtils.generateSalt()
                val fsHash = SecurityUtils.hashPassword("foodseller123", fsSalt)
                database.userDao().insertUser(
                    UserEntity(
                        role = "FOODS_SELLER",
                        login = "foodseller",
                        email = "foodseller@gagarin.uz",
                        passwordHash = fsHash,
                        salt = fsSalt,
                        fullName = "Gagarin Osh Markazi (Oshpaz)",
                        phone = "+998 93 111 22 33",
                        storeName = "Gagarin Osh Markazi",
                        savedAddress = "Gagarin shahri, Mustaqillik ko‘chasi"
                    )
                )
            }

            // Cleanup any duplicate categories if exist
            try {
                database.categoryDao().deleteDuplicates()
            } catch (e: Exception) {
                // Ignore
            }

            // Seed comprehensive standard Bozor categories uniquely
            val defaultCategories = listOf(
                CategoryEntity(name = "Smartfonlar va Gadjetlar", iconKey = "smartphones", sortOrder = 1),
                CategoryEntity(name = "Kompyuterlar va Noutbuklar", iconKey = "computers", sortOrder = 2),
                CategoryEntity(name = "Maishiy texnika", iconKey = "appliances", sortOrder = 3),
                CategoryEntity(name = "Oziq-ovqat va Ichimliklar", iconKey = "food", sortOrder = 4),
                CategoryEntity(name = "Kiyim-kechak", iconKey = "clothing", sortOrder = 5),
                CategoryEntity(name = "Poyabzallar", iconKey = "shoes", sortOrder = 6),
                CategoryEntity(name = "Go‘zallik va Parvarish", iconKey = "beauty", sortOrder = 7),
                CategoryEntity(name = "Salomatlik va Dorixona", iconKey = "health", sortOrder = 8),
                CategoryEntity(name = "Uy-ro‘zg‘or va Oshxona", iconKey = "home", sortOrder = 9),
                CategoryEntity(name = "Bolalar mahsulotlari", iconKey = "kids", sortOrder = 10),
                CategoryEntity(name = "Avtomobil jihozlari", iconKey = "auto", sortOrder = 11),
                CategoryEntity(name = "Sport va Dam olish", iconKey = "sport", sortOrder = 12),
                CategoryEntity(name = "Qurilish va Ta’mirlash", iconKey = "tools", sortOrder = 13),
                CategoryEntity(name = "Kantselyariya va Kitoblar", iconKey = "books", sortOrder = 14),
                CategoryEntity(name = "Zargarlik va Aksessuarlar", iconKey = "jewelry", sortOrder = 15),
                CategoryEntity(name = "Hayvonlar uchun tovarlar", iconKey = "pets", sortOrder = 16),
                CategoryEntity(name = "Bog‘dorchilik va Tomorqa", iconKey = "garden", sortOrder = 17),
                CategoryEntity(name = "Hunarmandchilik va Xobbi", iconKey = "hobby", sortOrder = 18),
                CategoryEntity(name = "Aqlli uy va Xavfsizlik", iconKey = "smarthome", sortOrder = 19),
                CategoryEntity(name = "Boshqa mahsulotlar", iconKey = "other", sortOrder = 20)
            )

            for (cat in defaultCategories) {
                val existing = database.categoryDao().getCategoryByName(cat.name)
                if (existing == null) {
                    database.categoryDao().insertCategory(cat)
                }
            }

            // Seed default Food Categories
            val defaultFoodCategories = listOf(
                com.example.data.local.entity.FoodCategoryEntity(name = "Pitsa", iconEmoji = "🍕", orderIndex = 1),
                com.example.data.local.entity.FoodCategoryEntity(name = "Burger & Fast Food", iconEmoji = "🍔", orderIndex = 2),
                com.example.data.local.entity.FoodCategoryEntity(name = "Tovuq & Gril", iconEmoji = "🍗", orderIndex = 3),
                com.example.data.local.entity.FoodCategoryEntity(name = "Milliy taomlar", iconEmoji = "🍲", orderIndex = 4),
                com.example.data.local.entity.FoodCategoryEntity(name = "Somsa & Pirojki", iconEmoji = "🥟", orderIndex = 5),
                com.example.data.local.entity.FoodCategoryEntity(name = "Shirinlik & Desert", iconEmoji = "🍰", orderIndex = 6),
                com.example.data.local.entity.FoodCategoryEntity(name = "Ichimliklar", iconEmoji = "🥤", orderIndex = 7),
                com.example.data.local.entity.FoodCategoryEntity(name = "Issiq Non & Tandir", iconEmoji = "🥖", orderIndex = 8)
            )

            for (fc in defaultFoodCategories) {
                val existing = database.foodCategoryDao().getCategoryByName(fc.name)
                if (existing == null) {
                    database.foodCategoryDao().insertCategory(fc)
                }
            }

            // Seed default Support Info if not exists
            val existingSupport = database.supportDao().getSupportInfoDirect()
            if (existingSupport == null) {
                database.supportDao().insertOrUpdateSupportInfo(
                    com.example.data.local.entity.SupportInfoEntity(
                        id = 1,
                        phone = "+998 90 123 45 67",
                        secondaryPhone = "+998 91 987 65 43",
                        telegramUsername = "@GagarinGoSupport",
                        telegramChannel = "@gagarin_go_app",
                        telegramApkUrl = "https://t.me/gagarin_go_app",
                        workingHours = "Har kuni: 08:00 - 22:00",
                        address = "Gagarin shahri, Do‘stlik shoh ko‘chasi 12-uy",
                        description = "Gagarin Savdo va Gagarin Food xizmatlari bo‘yicha barcha savol va takliflaringiz bo‘yicha biz bilan bog‘laning!"
                    )
                )
            }
            }
        }
    }
}
