package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.BuildConfig
import com.example.data.local.entity.ProductEntity
import com.example.data.model.UserRole
import com.example.data.network.NetworkConnectivityMonitor
import com.example.data.util.InAppNotification
import com.example.data.util.NotificationHelper
import com.example.ui.components.AppUpdateDialog
import com.example.ui.screens.admin.AdminAuthScreen
import com.example.ui.screens.admin.AdminDashboardScreen
import com.example.ui.screens.customer.CartScreen
import com.example.ui.screens.customer.CategoriesScreen
import com.example.ui.screens.customer.CustomerAuthScreen
import com.example.ui.screens.customer.FavoritesScreen
import com.example.ui.screens.customer.HomeScreen
import com.example.ui.screens.customer.ProductDetailScreen
import com.example.ui.screens.customer.ProfileScreen
import com.example.ui.screens.food.FoodAdminAuthScreen
import com.example.ui.screens.food.FoodSellerAuthScreen
import com.example.ui.screens.food.FoodsAdminScreen
import com.example.ui.screens.food.FoodsSellerScreen
import com.example.ui.screens.offline.OfflineScreen
import com.example.ui.screens.seller.SellerAuthScreen
import com.example.ui.screens.seller.SellerDashboardScreen
import com.example.ui.screens.splash.SplashScreen
import com.example.ui.theme.CardSurface
import com.example.ui.theme.DarkText
import com.example.ui.theme.LightBackground
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueLight
import com.example.ui.theme.PrimaryBurgundy
import com.example.ui.theme.PrimaryBurgundyLight
import com.example.ui.theme.SecondaryNavy
import com.example.ui.theme.SecondaryText
import com.example.ui.theme.SlateGray
import com.example.ui.viewmodels.AdminViewModel
import com.example.ui.viewmodels.AuthViewModel
import com.example.ui.viewmodels.EcosystemViewModel
import com.example.ui.viewmodels.FoodAdminViewModel
import com.example.ui.viewmodels.FoodSellerViewModel
import com.example.ui.viewmodels.FoodStoreViewModel
import com.example.ui.viewmodels.SellerViewModel
import com.example.ui.viewmodels.StoreViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class AppScreen {
    SPLASH,
    STOREFRONT,
    PRODUCT_DETAIL,
    CUSTOMER_AUTH,
    SELLER_AUTH,
    SELLER_DASHBOARD,
    ADMIN_AUTH,
    ADMIN_DASHBOARD,
    FOOD_SELLER_AUTH,
    FOOD_SELLER_DASHBOARD,
    FOOD_ADMIN_AUTH,
    FOOD_ADMIN_DASHBOARD
}

data class BottomNavItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

@Composable
fun MainAppNavigation(
    authViewModel: AuthViewModel,
    storeViewModel: StoreViewModel,
    sellerViewModel: SellerViewModel,
    adminViewModel: AdminViewModel,
    ecosystemViewModel: EcosystemViewModel,
    foodStoreViewModel: FoodStoreViewModel,
    foodSellerViewModel: FoodSellerViewModel,
    foodAdminViewModel: FoodAdminViewModel,
    networkMonitor: NetworkConnectivityMonitor,
    modifier: Modifier = Modifier
) {
    val session by authViewModel.session.collectAsState()
    val cartItems by storeViewModel.cartItems.collectAsState()
    val isConnected by networkMonitor.isConnected.collectAsState(initial = true)
    val supportInfo by storeViewModel.supportInfo.collectAsState()

    var currentScreen by remember { mutableStateOf(AppScreen.SPLASH) }
    var selectedBottomTab by remember { mutableIntStateOf(0) } // 0: Home, 1: Favorites, 2: Cart, 3: Profile
    var selectedProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var hasHandledInitialRoute by remember { mutableStateOf(false) }
    var isCheckingNetwork by remember { mutableStateOf(false) }
    var hasDismissedOptionalUpdate by remember { mutableStateOf(false) }
    var activeNotification by remember { mutableStateOf<InAppNotification?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        NotificationHelper.inAppNotifications.collect { notification ->
            val isTargeted = NotificationHelper.shouldDeliverNotification(
                targetRole = notification.targetRole,
                targetUserId = notification.targetUserId
            )
            if (isTargeted) {
                activeNotification = notification
                delay(5000)
                if (activeNotification?.id == notification.id) {
                    activeNotification = null
                }
            }
        }
    }

    val screenBackStack = remember { mutableListOf<AppScreen>() }
    val bottomTabBackStack = remember { mutableListOf<Int>() }
    var profileInitialTab by remember { mutableIntStateOf(0) }

    fun navigateToScreen(screen: AppScreen) {
        if (currentScreen != screen) {
            screenBackStack.add(currentScreen)
            currentScreen = screen
        }
    }

    fun popScreen(): Boolean {
        if (screenBackStack.isNotEmpty()) {
            currentScreen = screenBackStack.removeAt(screenBackStack.size - 1)
            return true
        } else if (currentScreen != AppScreen.STOREFRONT && currentScreen != AppScreen.SPLASH) {
            currentScreen = AppScreen.STOREFRONT
            return true
        }
        return false
    }

    fun selectTab(tab: Int) {
        if (selectedBottomTab != tab) {
            bottomTabBackStack.add(selectedBottomTab)
            selectedBottomTab = tab
        }
    }

    fun popBottomTab(): Boolean {
        if (bottomTabBackStack.isNotEmpty()) {
            selectedBottomTab = bottomTabBackStack.removeAt(bottomTabBackStack.size - 1)
            return true
        } else if (selectedBottomTab != 0) {
            selectedBottomTab = 0
            return true
        }
        return false
    }

    // Handle back button on sub-screens and tabs
    BackHandler(enabled = currentScreen != AppScreen.STOREFRONT && currentScreen != AppScreen.SPLASH) {
        popScreen()
    }

    BackHandler(enabled = currentScreen == AppScreen.STOREFRONT && selectedBottomTab != 0) {
        popBottomTab()
    }

    LaunchedEffect(session.user?.id) {
        storeViewModel.setCustomerId(session.user?.id)
    }

    LaunchedEffect(session.isLoggedIn, session.role) {
        if (!hasHandledInitialRoute && session.isLoggedIn && currentScreen != AppScreen.SPLASH) {
            hasHandledInitialRoute = true
            when (session.role) {
                UserRole.BOZOR_SELLER, UserRole.SELLER -> {
                    screenBackStack.clear()
                    currentScreen = AppScreen.SELLER_DASHBOARD
                }
                UserRole.FOODS_SELLER -> {
                    screenBackStack.clear()
                    currentScreen = AppScreen.FOOD_SELLER_DASHBOARD
                }
                UserRole.SUPER_ADMIN, UserRole.ADMIN -> {
                    screenBackStack.clear()
                    currentScreen = AppScreen.ADMIN_DASHBOARD
                }
                UserRole.FOODS_ADMIN -> {
                    screenBackStack.clear()
                    currentScreen = AppScreen.FOOD_ADMIN_DASHBOARD
                }
                UserRole.CUSTOMER -> { /* stays on STOREFRONT */ }
            }
        }
    }

    // Network check full screen if disconnected
    if (!isConnected && currentScreen != AppScreen.SPLASH) {
        OfflineScreen(
            isChecking = isCheckingNetwork,
            onRetry = {
                scope.launch {
                    isCheckingNetwork = true
                    networkMonitor.checkRealInternetAndBackend()
                    kotlinx.coroutines.delay(1000)
                    isCheckingNetwork = false
                }
            }
        )
        return
    }

    val bottomNavItems = listOf(
        BottomNavItem("Bosh sahifa", Icons.Filled.Home, Icons.Outlined.Home, "nav_home"),
        BottomNavItem("Kategoriyalar", Icons.Filled.Category, Icons.Outlined.Category, "nav_categories"),
        BottomNavItem("Savat", Icons.Filled.ShoppingCart, Icons.Outlined.ShoppingCart, "nav_cart"),
        BottomNavItem("Sevimlilar", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder, "nav_favorites"),
        BottomNavItem("Profil", Icons.Filled.Person, Icons.Outlined.Person, "nav_profile")
    )

    Box(modifier = modifier.fillMaxSize().background(LightBackground)) {
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "ScreenTransition"
        ) { screen ->
            when (screen) {
                AppScreen.SPLASH -> {
                    SplashScreen(
                        onSplashFinished = {
                            hasHandledInitialRoute = true
                            if (session.isLoggedIn) {
                                when (session.role) {
                                    UserRole.BOZOR_SELLER, UserRole.SELLER -> currentScreen = AppScreen.SELLER_DASHBOARD
                                    UserRole.FOODS_SELLER -> currentScreen = AppScreen.FOOD_SELLER_DASHBOARD
                                    UserRole.SUPER_ADMIN, UserRole.ADMIN -> currentScreen = AppScreen.ADMIN_DASHBOARD
                                    UserRole.FOODS_ADMIN -> currentScreen = AppScreen.FOOD_ADMIN_DASHBOARD
                                    UserRole.CUSTOMER -> currentScreen = AppScreen.STOREFRONT
                                }
                            } else {
                                currentScreen = AppScreen.STOREFRONT
                            }
                        }
                    )
                }

                AppScreen.STOREFRONT -> {
                    Scaffold(
                        bottomBar = {
                            NavigationBar(
                                containerColor = CardSurface,
                                tonalElevation = 0.dp
                            ) {
                                bottomNavItems.forEachIndexed { index, item ->
                                    val isSelected = selectedBottomTab == index
                                    NavigationBarItem(
                                        selected = isSelected,
                                        onClick = { selectTab(index) },
                                        icon = {
                                            if (index == 2 && cartItems.isNotEmpty()) {
                                                BadgedBox(
                                                    badge = {
                                                        Badge(
                                                            containerColor = PrimaryBlue,
                                                            contentColor = Color.White
                                                        ) {
                                                            Text(
                                                                text = "${cartItems.sumOf { it.cartItem.quantity }}",
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                                        contentDescription = item.title
                                                    )
                                                }
                                            } else {
                                                Icon(
                                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                                    contentDescription = item.title
                                                )
                                            }
                                        },
                                        label = {
                                            Text(
                                                text = item.title,
                                                fontSize = 11.5.sp,
                                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = PrimaryBurgundy,
                                            selectedTextColor = PrimaryBurgundy,
                                            unselectedIconColor = SecondaryText,
                                            unselectedTextColor = SecondaryText,
                                            indicatorColor = PrimaryBurgundyLight
                                        ),
                                        modifier = Modifier.testTag(item.testTag)
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        when (selectedBottomTab) {
                            0 -> HomeScreen(
                                storeViewModel = storeViewModel,
                                authViewModel = authViewModel,
                                ecosystemViewModel = ecosystemViewModel,
                                foodStoreViewModel = foodStoreViewModel,
                                onProductClick = { product ->
                                    selectedProduct = product
                                    navigateToScreen(AppScreen.PRODUCT_DETAIL)
                                },
                                onNavigateToSellerAuth = {
                                    if (session.isLoggedIn && (session.role == UserRole.SELLER || session.role == UserRole.BOZOR_SELLER)) {
                                        navigateToScreen(AppScreen.SELLER_DASHBOARD)
                                    } else {
                                        navigateToScreen(AppScreen.SELLER_AUTH)
                                    }
                                },
                                onNavigateToAdminAuth = {
                                    if (session.isLoggedIn && (session.role == UserRole.ADMIN || session.role == UserRole.SUPER_ADMIN)) {
                                        navigateToScreen(AppScreen.ADMIN_DASHBOARD)
                                    } else {
                                        navigateToScreen(AppScreen.ADMIN_AUTH)
                                    }
                                },
                                onNavigateToFoodSellerAuth = {
                                    if (session.isLoggedIn && session.role == UserRole.FOODS_SELLER) {
                                        navigateToScreen(AppScreen.FOOD_SELLER_DASHBOARD)
                                    } else {
                                        navigateToScreen(AppScreen.FOOD_SELLER_AUTH)
                                    }
                                },
                                onNavigateToFoodAdminAuth = {
                                    if (session.isLoggedIn && session.role == UserRole.FOODS_ADMIN) {
                                        navigateToScreen(AppScreen.FOOD_ADMIN_DASHBOARD)
                                    } else {
                                        navigateToScreen(AppScreen.FOOD_ADMIN_AUTH)
                                    }
                                },
                                onNavigateToCart = {
                                    selectTab(2)
                                },
                                modifier = Modifier.padding(innerPadding)
                            )
                            1 -> CategoriesScreen(
                                storeViewModel = storeViewModel,
                                onProductClick = { product ->
                                    selectedProduct = product
                                    navigateToScreen(AppScreen.PRODUCT_DETAIL)
                                },
                                modifier = Modifier.padding(innerPadding)
                            )
                            2 -> CartScreen(
                                storeViewModel = storeViewModel,
                                authViewModel = authViewModel,
                                onNavigateToAuth = { navigateToScreen(AppScreen.CUSTOMER_AUTH) },
                                onOrderPlaced = {
                                    profileInitialTab = 1
                                    selectTab(4)
                                },
                                onExploreProducts = { selectTab(0) },
                                modifier = Modifier.padding(innerPadding)
                            )
                            3 -> FavoritesScreen(
                                storeViewModel = storeViewModel,
                                onProductClick = { product ->
                                    selectedProduct = product
                                    navigateToScreen(AppScreen.PRODUCT_DETAIL)
                                },
                                onExploreProducts = { selectTab(0) },
                                modifier = Modifier.padding(innerPadding)
                            )
                            4 -> ProfileScreen(
                                authViewModel = authViewModel,
                                storeViewModel = storeViewModel,
                                foodStoreViewModel = foodStoreViewModel,
                                onNavigateToAuth = { navigateToScreen(AppScreen.CUSTOMER_AUTH) },
                                onNavigateToSeller = {
                                    if (session.isLoggedIn && (session.role == UserRole.SELLER || session.role == UserRole.BOZOR_SELLER)) {
                                        navigateToScreen(AppScreen.SELLER_DASHBOARD)
                                    } else {
                                        navigateToScreen(AppScreen.SELLER_AUTH)
                                    }
                                },
                                onNavigateToAdmin = {
                                    if (session.isLoggedIn && (session.role == UserRole.ADMIN || session.role == UserRole.SUPER_ADMIN)) {
                                        navigateToScreen(AppScreen.ADMIN_DASHBOARD)
                                    } else {
                                        navigateToScreen(AppScreen.ADMIN_AUTH)
                                    }
                                },
                                initialTab = profileInitialTab,
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                }

                AppScreen.PRODUCT_DETAIL -> {
                    if (selectedProduct != null) {
                        ProductDetailScreen(
                            product = selectedProduct!!,
                            storeViewModel = storeViewModel,
                            authViewModel = authViewModel,
                            onBack = { popScreen() },
                            onProductClick = { recProduct ->
                                selectedProduct = recProduct
                            }
                        )
                    } else {
                        popScreen()
                    }
                }

                AppScreen.CUSTOMER_AUTH -> {
                    CustomerAuthScreen(
                        authViewModel = authViewModel,
                        onAuthSuccess = {
                            screenBackStack.clear()
                            currentScreen = AppScreen.STOREFRONT
                        },
                        onBack = { popScreen() }
                    )
                }

                AppScreen.SELLER_AUTH -> {
                    SellerAuthScreen(
                        authViewModel = authViewModel,
                        onAuthSuccess = {
                            screenBackStack.clear()
                            currentScreen = AppScreen.SELLER_DASHBOARD
                        },
                        onBack = { popScreen() }
                    )
                }

                AppScreen.SELLER_DASHBOARD -> {
                    if (session.user != null && (session.role == UserRole.SELLER || session.role == UserRole.BOZOR_SELLER)) {
                        SellerDashboardScreen(
                            sellerUser = session.user!!,
                            sellerViewModel = sellerViewModel,
                            authViewModel = authViewModel,
                            onLogout = {
                                screenBackStack.clear()
                                currentScreen = AppScreen.STOREFRONT
                            }
                        )
                    } else {
                        currentScreen = AppScreen.SELLER_AUTH
                    }
                }

                AppScreen.ADMIN_AUTH -> {
                    AdminAuthScreen(
                        authViewModel = authViewModel,
                        onAuthSuccess = {
                            screenBackStack.clear()
                            currentScreen = AppScreen.ADMIN_DASHBOARD
                        },
                        onBack = { popScreen() }
                    )
                }

                AppScreen.ADMIN_DASHBOARD -> {
                    if (session.user != null && (session.role == UserRole.ADMIN || session.role == UserRole.SUPER_ADMIN)) {
                        AdminDashboardScreen(
                            adminUser = session.user!!,
                            adminViewModel = adminViewModel,
                            authViewModel = authViewModel,
                            onLogout = {
                                screenBackStack.clear()
                                currentScreen = AppScreen.STOREFRONT
                            }
                        )
                    } else {
                        currentScreen = AppScreen.ADMIN_AUTH
                    }
                }

                AppScreen.FOOD_SELLER_AUTH -> {
                    FoodSellerAuthScreen(
                        authViewModel = authViewModel,
                        onAuthSuccess = {
                            screenBackStack.clear()
                            currentScreen = AppScreen.FOOD_SELLER_DASHBOARD
                        },
                        onBack = { popScreen() }
                    )
                }

                AppScreen.FOOD_SELLER_DASHBOARD -> {
                    if (session.user != null && session.role == UserRole.FOODS_SELLER) {
                        FoodsSellerScreen(
                            sellerUser = session.user!!,
                            foodSellerViewModel = foodSellerViewModel,
                            authViewModel = authViewModel,
                            onLogout = {
                                screenBackStack.clear()
                                currentScreen = AppScreen.STOREFRONT
                            }
                        )
                    } else {
                        currentScreen = AppScreen.FOOD_SELLER_AUTH
                    }
                }

                AppScreen.FOOD_ADMIN_AUTH -> {
                    FoodAdminAuthScreen(
                        authViewModel = authViewModel,
                        onAuthSuccess = {
                            screenBackStack.clear()
                            currentScreen = AppScreen.FOOD_ADMIN_DASHBOARD
                        },
                        onBack = { popScreen() }
                    )
                }

                AppScreen.FOOD_ADMIN_DASHBOARD -> {
                    if (session.user != null && (session.role == UserRole.FOODS_ADMIN || session.role == UserRole.ADMIN || session.role == UserRole.SUPER_ADMIN)) {
                        FoodsAdminScreen(
                            adminUser = session.user!!,
                            foodAdminViewModel = foodAdminViewModel,
                            authViewModel = authViewModel,
                            onLogout = {
                                screenBackStack.clear()
                                currentScreen = AppScreen.STOREFRONT
                            }
                        )
                    } else {
                        currentScreen = AppScreen.FOOD_ADMIN_AUTH
                    }
                }
            }
        }

        // Automatic In-App Update Prompt (Mandatory / Recommended)
        val currentVersionCode = BuildConfig.VERSION_CODE
        val hasNewUpdate = supportInfo.latestVersionCode > currentVersionCode
        val isMandatory = currentVersionCode < supportInfo.minRequiredVersionCode || (supportInfo.isForceUpdate && hasNewUpdate)

        if (hasNewUpdate && currentScreen != AppScreen.SPLASH && (isMandatory || !hasDismissedOptionalUpdate)) {
            AppUpdateDialog(
                supportInfo = supportInfo,
                onDismiss = {
                    hasDismissedOptionalUpdate = true
                }
            )
        }

        // Global In-App Notification Banner (Orders, Status Updates, Codes)
        AnimatedVisibility(
            visible = activeNotification != null,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 44.dp, start = 14.dp, end = 14.dp)
                .zIndex(200f)
        ) {
            activeNotification?.let { notif ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SecondaryNavy),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if ((notif.targetRole == "SELLER" || notif.targetRole == "FOOD_SELLER" || notif.targetRole == "FOODS_SELLER") && session.isLoggedIn) {
                                if (session.role == UserRole.FOODS_SELLER) {
                                    currentScreen = AppScreen.FOOD_SELLER_DASHBOARD
                                } else {
                                    currentScreen = AppScreen.SELLER_DASHBOARD
                                }
                            }
                            activeNotification = null
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(PrimaryBurgundy, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Notifications,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = notif.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = notif.message,
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.88f),
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(
                            onClick = { activeNotification = null },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Yopish",
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
