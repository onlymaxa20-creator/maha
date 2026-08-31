package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.data.AppContainer
import com.example.data.util.NotificationHelper
import com.example.data.worker.SyncScheduler
import com.example.ui.navigation.MainAppNavigation
import com.example.ui.theme.GagarinSavdoTheme
import com.example.ui.viewmodels.AdminViewModel
import com.example.ui.viewmodels.AuthViewModel
import com.example.ui.viewmodels.EcosystemViewModel
import com.example.ui.viewmodels.FoodAdminViewModel
import com.example.ui.viewmodels.FoodSellerViewModel
import com.example.ui.viewmodels.FoodStoreViewModel
import com.example.ui.viewmodels.SellerViewModel
import com.example.ui.viewmodels.StoreViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var appContainer: AppContainer

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                NotificationHelper.init(applicationContext)
            }
        }

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModel.Factory(appContainer.authRepository)
    }

    private val storeViewModel: StoreViewModel by viewModels {
        StoreViewModel.Factory(
            productRepository = appContainer.productRepository,
            cartRepository = appContainer.cartRepository,
            favoriteRepository = appContainer.favoriteRepository,
            orderRepository = appContainer.orderRepository,
            bannerRepository = appContainer.bannerRepository,
            reviewRepository = appContainer.reviewRepository,
            supportRepository = appContainer.supportRepository,
            recommendationRepository = appContainer.recommendationRepository
        )
    }

    private val sellerViewModel: SellerViewModel by viewModels {
        SellerViewModel.Factory(
            productRepository = appContainer.productRepository,
            orderRepository = appContainer.orderRepository
        )
    }

    private val adminViewModel: AdminViewModel by viewModels {
        AdminViewModel.Factory(
            authRepository = appContainer.authRepository,
            productRepository = appContainer.productRepository,
            orderRepository = appContainer.orderRepository,
            bannerRepository = appContainer.bannerRepository,
            supportRepository = appContainer.supportRepository,
            auditRepository = appContainer.auditRepository,
            recommendationRepository = appContainer.recommendationRepository
        )
    }

    private val ecosystemViewModel: EcosystemViewModel by viewModels {
        EcosystemViewModel.Factory(
            ecosystemRepository = appContainer.ecosystemRepository
        )
    }

    private val foodStoreViewModel: FoodStoreViewModel by viewModels {
        FoodStoreViewModel.Factory(
            foodRepository = appContainer.foodRepository
        )
    }

    private val foodSellerViewModel: FoodSellerViewModel by viewModels {
        FoodSellerViewModel.Factory(
            foodRepository = appContainer.foodRepository
        )
    }

    private val foodAdminViewModel: FoodAdminViewModel by viewModels {
        FoodAdminViewModel.Factory(
            foodRepository = appContainer.foodRepository
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        try {
            com.google.firebase.FirebaseApp.initializeApp(applicationContext)
        } catch (_: Exception) {}

        appContainer = AppContainer(applicationContext)
        NotificationHelper.init(applicationContext)

        // Request POST_NOTIFICATIONS permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Schedule background worker and run immediate sync
        SyncScheduler.schedulePeriodicSync(applicationContext)
        SyncScheduler.enqueueImmediateSync(applicationContext)

        appContainer.productRepository.startRealtimeSync(lifecycleScope)
        appContainer.productRepository.startRealtimeCategorySync(lifecycleScope)
        appContainer.bannerRepository.startRealtimeBannerSync(lifecycleScope)
        appContainer.ecosystemRepository.startRealtimeSync(lifecycleScope)
        appContainer.authRepository.startRealtimeUserSync(lifecycleScope)
        appContainer.orderRepository.startRealtimeOrderSync(lifecycleScope)
        appContainer.foodRepository.startRealtimeSync(lifecycleScope)
        appContainer.reviewRepository.startRealtimeReviewSync(lifecycleScope)
        appContainer.supportRepository.startRealtimeSupportSync(lifecycleScope)

        // Keep NotificationHelper synced with the current login session & role in real-time
        lifecycleScope.launch {
            appContainer.authRepository.currentSession.collect { session ->
                NotificationHelper.updateSession(session)
            }
        }

        // When internet comes online, trigger immediate sync for orders & notifications
        lifecycleScope.launch {
            appContainer.networkMonitor.isConnected.collect { isConnected ->
                if (isConnected) {
                    SyncScheduler.enqueueImmediateSync(applicationContext)
                }
            }
        }

        setContent {
            GagarinSavdoTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainAppNavigation(
                        authViewModel = authViewModel,
                        storeViewModel = storeViewModel,
                        sellerViewModel = sellerViewModel,
                        adminViewModel = adminViewModel,
                        ecosystemViewModel = ecosystemViewModel,
                        foodStoreViewModel = foodStoreViewModel,
                        foodSellerViewModel = foodSellerViewModel,
                        foodAdminViewModel = foodAdminViewModel,
                        networkMonitor = appContainer.networkMonitor
                    )
                }
            }
        }
    }
}
