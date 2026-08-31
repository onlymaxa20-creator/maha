package com.example.data

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.network.NetworkConnectivityMonitor
import com.example.data.repository.AuthRepository
import com.example.data.repository.BannerRepository
import com.example.data.repository.CartRepository
import com.example.data.repository.EcosystemRepository
import com.example.data.repository.FavoriteRepository
import com.example.data.repository.FoodRepository
import com.example.data.repository.OrderRepository
import com.example.data.repository.ProductRepository
import com.example.data.repository.ReviewRepository

class AppContainer(context: Context) {
    val database: AppDatabase = AppDatabase.getInstance(context)

    val networkMonitor: NetworkConnectivityMonitor by lazy {
        NetworkConnectivityMonitor(context)
    }

    val foodRepository: FoodRepository by lazy {
        FoodRepository(
            restaurantDao = database.foodRestaurantDao(),
            categoryDao = database.foodCategoryDao(),
            productDao = database.foodProductDao(),
            orderDao = database.foodOrderDao(),
            bannerDao = database.foodBannerDao(),
            promotionDao = database.foodPromotionDao(),
            reviewDao = database.foodReviewDao()
        )
    }

    val authRepository: AuthRepository by lazy {
        AuthRepository(database.userDao(), context)
    }

    val productRepository: ProductRepository by lazy {
        ProductRepository(database.productDao(), database.categoryDao())
    }

    val reviewRepository: ReviewRepository by lazy {
        ReviewRepository(database.reviewDao())
    }

    val bannerRepository: BannerRepository by lazy {
        BannerRepository(database.promoBannerDao())
    }

    val cartRepository: CartRepository by lazy {
        CartRepository(database.cartDao(), database.productDao())
    }

    val favoriteRepository: FavoriteRepository by lazy {
        FavoriteRepository(database.favoriteDao(), database.productDao())
    }

    val orderRepository: OrderRepository by lazy {
        OrderRepository(
            orderDao = database.orderDao(),
            cartDao = database.cartDao(),
            productDao = database.productDao(),
            userDao = database.userDao()
        )
    }

    val ecosystemRepository: EcosystemRepository by lazy {
        EcosystemRepository(
            taxiDao = database.taxiDao(),
            deliveryDao = database.deliveryDao(),
            jobDao = database.jobDao(),
            serviceDao = database.serviceDao(),
            classifiedAdDao = database.classifiedAdDao(),
            courierDao = database.courierDao()
        )
    }

    val supportRepository: com.example.data.repository.SupportRepository by lazy {
        com.example.data.repository.SupportRepository(database.supportDao())
    }

    val auditRepository: com.example.data.repository.AdminAuditRepository by lazy {
        com.example.data.repository.AdminAuditRepository(database.adminAuditLogDao())
    }

    val recommendationRepository: com.example.data.repository.RecommendationRepository by lazy {
        com.example.data.repository.RecommendationRepository(
            eventDao = database.userInteractionEventDao(),
            productDao = database.productDao(),
            userDao = database.userDao(),
            categoryDao = database.categoryDao(),
            orderDao = database.orderDao()
        )
    }
}
