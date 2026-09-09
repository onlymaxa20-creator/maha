package com.example.ui.screens.customer

import android.app.Activity
import android.Manifest
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.PromoBannerEntity
import com.example.data.util.ImageStorageHelper
import com.example.data.util.UserLocationManager
import com.example.ui.components.CategoryChip
import com.example.ui.components.EcosystemHubBar
import com.example.ui.components.EmptyStateView
import com.example.ui.components.FlashSaleSection
import com.example.ui.components.Formatters
import com.example.ui.components.GagarinGoLogo
import com.example.ui.components.ProductCard
import com.example.ui.components.PromoBannerSection
import com.example.ui.components.RecommendationSection
import com.example.ui.components.UzumCategoryCard
import com.example.ui.theme.BorderColor
import com.example.ui.theme.CardSurface
import com.example.ui.theme.DarkBurgundy
import com.example.ui.theme.DarkText
import com.example.ui.theme.LightBackground
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueLight
import com.example.ui.theme.PrimaryBurgundy
import com.example.ui.theme.PrimaryBurgundyLight
import com.example.ui.theme.SecondaryNavy
import com.example.ui.theme.SecondaryText
import com.example.ui.theme.SlateGray
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SurfaceSubtle
import com.example.ui.viewmodels.AuthViewModel
import com.example.ui.viewmodels.EcosystemTab
import com.example.ui.viewmodels.EcosystemViewModel
import com.example.ui.viewmodels.FoodStoreViewModel
import com.example.ui.viewmodels.StoreViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    storeViewModel: StoreViewModel,
    authViewModel: AuthViewModel,
    ecosystemViewModel: EcosystemViewModel,
    foodStoreViewModel: FoodStoreViewModel,
    onProductClick: (ProductEntity) -> Unit,
    onNavigateToSellerAuth: () -> Unit,
    onNavigateToAdminAuth: () -> Unit,
    onNavigateToFoodSellerAuth: () -> Unit,
    onNavigateToFoodAdminAuth: () -> Unit,
    onNavigateToCart: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val searchQuery by storeViewModel.searchQuery.collectAsState()
    val searchResultState by storeViewModel.searchResultState.collectAsState()
    val categories by storeViewModel.categories.collectAsState()
    val selectedCategory by storeViewModel.selectedCategoryId.collectAsState()
    val products by storeViewModel.products.collectAsState()
    val favoriteProducts by storeViewModel.favoriteProducts.collectAsState()
    val promotionalProducts by storeViewModel.promotionalProducts.collectAsState()
    val personalizedRecommendations by storeViewModel.personalizedRecommendations.collectAsState()
    val purchaseBasedRecommendations by storeViewModel.purchaseBasedRecommendations.collectAsState()
    val promoBanners by storeViewModel.banners.collectAsState()
    val cartItems by storeViewModel.cartItems.collectAsState()
    val session by authViewModel.session.collectAsState()
    val currentTab by ecosystemViewModel.currentTab.collectAsState()
    val classifiedAds by ecosystemViewModel.ads.collectAsState()
    val foodProducts by foodStoreViewModel.displayedProducts.collectAsState()

    val context = LocalContext.current
    val favIds = favoriteProducts.map { it.id }.toSet()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val locationMgr = remember { UserLocationManager(context) }
    val currentAddress by locationMgr.currentAddress.collectAsState()
    val isGpsEnabled by locationMgr.isGpsEnabled.collectAsState()
    val isLocating by locationMgr.isLocating.collectAsState()
    val coordinates by locationMgr.coordinates.collectAsState()
    var showLocationEditDialog by remember { mutableStateOf(false) }
    var showGpsPromptDialog by remember { mutableStateOf(false) }
    var manualLocationInput by remember { mutableStateOf("") }

    DisposableEffect(locationMgr) {
        locationMgr.startMonitoring()
        onDispose {
            locationMgr.stopMonitoring()
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            coroutineScope.launch {
                locationMgr.fetchCurrentLocation(forceHighAccuracy = true)
            }
        }
    }

    LaunchedEffect(Unit) {
        if (locationMgr.hasLocationPermission()) {
            if (locationMgr.checkGpsState()) {
                locationMgr.fetchCurrentLocation(forceHighAccuracy = true)
            }
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    var showPortalSelector by remember { mutableStateOf(false) }
    var selectedBannerDetails by remember { mutableStateOf<PromoBannerEntity?>(null) }
    val sheetState = rememberModalBottomSheetState()
    var lastBackPressedTime by remember { androidx.compose.runtime.mutableLongStateOf(0L) }

    BackHandler {
        if (selectedBannerDetails != null) {
            selectedBannerDetails = null
        } else if (showPortalSelector) {
            showPortalSelector = false
        } else if (searchQuery.isNotEmpty()) {
            storeViewModel.setSearchQuery("")
        } else if (selectedCategory != null) {
            storeViewModel.selectCategory(null)
        } else if (ecosystemViewModel.popTab()) {
            // Popped tab from history (e.g. from services/jobs/ads/taxi/food back to home hub)
        } else {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastBackPressedTime < 2000) {
                (context as? Activity)?.finish()
            } else {
                lastBackPressedTime = currentTime
                Toast.makeText(context, "Ilovadan chiqish uchun yana bir marta bosing", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(Unit) {
        ecosystemViewModel.liveNotifications.collectLatest { event ->
            snackbarHostState.showSnackbar("${event.title}\n${event.message}")
        }
    }

    Box(modifier = modifier.fillMaxSize().background(LightBackground)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. TOP HEADER & BRANDING (Only shown on Home Hub and Bozor; other modules have their own dedicated TopAppBar)
            if (currentTab == EcosystemTab.HOME_HUB || currentTab == EcosystemTab.BOZOR) {
                Surface(
                    color = CardSurface,
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left: Back button (if in Bozor) or Hamburger Menu (Opens Admin & Seller portal switcher)
                            IconButton(
                                onClick = {
                                    if (currentTab == EcosystemTab.BOZOR) {
                                        ecosystemViewModel.setTab(EcosystemTab.HOME_HUB)
                                    } else {
                                        showPortalSelector = true
                                    }
                                },
                                modifier = Modifier.testTag("brand_portal_trigger")
                            ) {
                                Icon(
                                    imageVector = if (currentTab == EcosystemTab.BOZOR) Icons.AutoMirrored.Filled.ArrowBack else Icons.Filled.Menu,
                                    contentDescription = if (currentTab == EcosystemTab.BOZOR) "Orqaga" else "Menyu",
                                    tint = DarkText,
                                    modifier = Modifier.size(26.dp)
                                )
                            }

                        // Center: Gagarin Go logo typography
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showPortalSelector = true }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Gagarin",
                                fontWeight = FontWeight.Black,
                                fontSize = 23.sp,
                                color = Color(0xFF1F1F1F),
                                letterSpacing = (-0.5).sp
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Go",
                                fontWeight = FontWeight.Black,
                                fontStyle = FontStyle.Italic,
                                fontSize = 23.sp,
                                color = PrimaryBurgundy,
                                letterSpacing = (-0.5).sp
                            )
                        }

                        // Right: Plus badge button
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = PrimaryBurgundy,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    Toast.makeText(context, "Gagarin Go Plus xizmati tez orada ishga tushadi!", Toast.LENGTH_SHORT).show()
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Plus",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    // User Location Display Header directly below "Gagarin Go"
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            !isGpsEnabled -> {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color(0xFFFFF7ED),
                                    border = BorderStroke(1.dp, Color(0xFFFDBA74)),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .clickable { showGpsPromptDialog = true }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.LocationOff,
                                            contentDescription = null,
                                            tint = Color(0xFFC2410C),
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = "GPS o‘chirilgan • Yoqish",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFC2410C)
                                        )
                                    }
                                }
                            }
                            isLocating -> {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color(0xFFFDF2F4),
                                    border = BorderStroke(1.dp, PrimaryBurgundy.copy(alpha = 0.25f)),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .clickable {
                                            manualLocationInput = currentAddress
                                            showLocationEditDialog = true
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CircularProgressIndicator(
                                            strokeWidth = 2.dp,
                                            color = PrimaryBurgundy,
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "GPS orqali aniqlanmoqda...",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = PrimaryBurgundy
                                        )
                                    }
                                }
                            }
                            else -> {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color(0xFFF9FAFB),
                                    border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .clickable {
                                            manualLocationInput = currentAddress
                                            showLocationEditDialog = true
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .background(Color(0xFF22C55E), shape = CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Filled.LocationOn,
                                            contentDescription = null,
                                            tint = PrimaryBurgundy,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = currentAddress,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = DarkText,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Filled.KeyboardArrowDown,
                                            contentDescription = "Manzilni tanlash",
                                            tint = SecondaryText,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Search input with AI semantic indicator
                    if (currentTab == EcosystemTab.HOME_HUB || currentTab == EcosystemTab.BOZOR) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { storeViewModel.setSearchQuery(it) },
                            placeholder = {
                                Text(
                                    text = "Aqlli qidiruv: mahsulot, sheva yoki brend...",
                                    color = SlateGray,
                                    fontSize = 13.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = "Qidirish",
                                    tint = PrimaryBurgundy,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            trailingIcon = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { storeViewModel.setSearchQuery("") }) {
                                            Icon(
                                                imageVector = Icons.Filled.Close,
                                                contentDescription = "Tozalash",
                                                tint = SlateGray,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    } else {
                                        Icon(
                                            imageVector = Icons.Filled.AutoAwesome,
                                            contentDescription = "AI Qidiruv",
                                            tint = PrimaryBurgundy,
                                            modifier = Modifier
                                                .padding(end = 12.dp)
                                                .size(18.dp)
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SurfaceSubtle,
                                unfocusedContainerColor = SurfaceSubtle,
                                focusedBorderColor = PrimaryBurgundy,
                                unfocusedBorderColor = BorderColor
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("home_search_input")
                        )

                        // "Shuni nazarda tutdingizmi?" (Did you mean?) AI taklifi
                        searchResultState?.didYouMean?.let { suggestion ->
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFFEF3C7),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { storeViewModel.applyDidYouMean() }
                                    .testTag("did_you_mean_chip")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color(0xFFD97706),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Shuni nazarda tutdingizmi: ",
                                        fontSize = 12.sp,
                                        color = Color(0xFF92400E)
                                    )
                                    Text(
                                        text = suggestion,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = PrimaryBurgundy
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(
                                        text = "Tanlash",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFD97706)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

            // 2. TAB CONTENT
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ecosystem_tab_content",
                modifier = Modifier.fillMaxSize().weight(1f)
            ) { tab ->
                when (tab) {
                    EcosystemTab.HOME_HUB -> {
                        SuperAppHomeHub(
                            products = products,
                            foodProducts = foodProducts,
                            favoriteProductIds = favIds,
                            promoBanners = promoBanners,
                            onProductClick = onProductClick,
                            onAddToCart = { prod ->
                                storeViewModel.addToCart(prod.id, 1)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("${prod.name} savatga qo‘shildi")
                                }
                            },
                            onToggleFavorite = { prod -> storeViewModel.toggleFavorite(prod.id) },
                            onAddFoodToCart = { food ->
                                foodStoreViewModel.addToCart(food)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("${food.name} taom savatiga qo‘shildi")
                                }
                            },
                            onNavigateToTab = { targetTab ->
                                ecosystemViewModel.setTab(targetTab)
                            },
                            onPlusClick = {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Yaqin orada bu xizmat qo‘shiladi")
                                }
                            }
                        )
                    }

                    EcosystemTab.BOZOR -> {
                        val bozorBanners = remember(promoBanners) {
                            promoBanners.filter {
                                it.actionTag.isBlank() ||
                                it.actionTag.equals("ALL", ignoreCase = true) ||
                                it.actionTag.equals("BOZOR", ignoreCase = true)
                            }
                        }

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("home_products_grid")
                        ) {
                            // 3A. PROMO BANNERS CAROUSEL (BOZOR BO'LIMI REKLAMALARI)
                            if (bozorBanners.isNotEmpty() && searchQuery.isBlank() && selectedCategory == null) {
                                item(span = { GridItemSpan(2) }) {
                                    PromoBannerSection(
                                        banners = bozorBanners,
                                        onBannerClick = { banner ->
                                            val rawLink = banner.targetLink.trim()
                                            val link = rawLink.lowercase()
                                            when {
                                                link.startsWith("http://") || link.startsWith("https://") -> {
                                                    try {
                                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(rawLink))
                                                        context.startActivity(intent)
                                                    } catch (_: Exception) {}
                                                }
                                                link.startsWith("tel:") -> {
                                                    try {
                                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse(rawLink))
                                                        context.startActivity(intent)
                                                    } catch (_: Exception) {}
                                                }
                                                rawLink.startsWith("+998") || (rawLink.length in 9..13 && rawLink.filter { it.isDigit() }.length >= 9) -> {
                                                    try {
                                                        val cleanPhone = rawLink.filter { it.isDigit() || it == '+' }
                                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleanPhone"))
                                                        context.startActivity(intent)
                                                    } catch (_: Exception) {}
                                                }
                                                else -> {
                                                    selectedBannerDetails = banner
                                                }
                                            }
                                        },
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )
                                }
                            }

                            // 3B. CATEGORIES CAROUSEL
                            if (searchQuery.isBlank()) {
                                item(span = { GridItemSpan(2) }) {
                                    Column(modifier = Modifier.padding(bottom = 6.dp)) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Filled.GridView,
                                                    contentDescription = null,
                                                    tint = Color(0xFF6D28D9),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Bozor Kategoriyalari",
                                                    style = MaterialTheme.typography.titleMedium.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 15.sp
                                                    ),
                                                    color = SecondaryNavy
                                                )
                                            }
                                            if (selectedCategory != null) {
                                                Text(
                                                    text = "Barchasi",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = PrimaryBurgundy,
                                                    modifier = Modifier.clickable { storeViewModel.selectCategory(null) }
                                                )
                                            }
                                        }

                                        LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            contentPadding = PaddingValues(vertical = 4.dp)
                                        ) {
                                            item {
                                                UzumCategoryCard(
                                                    label = "Barchasi",
                                                    iconKey = "all",
                                                    isSelected = selectedCategory == null,
                                                    onClick = { storeViewModel.selectCategory(null) }
                                                )
                                            }

                                            items(categories, key = { it.id }) { cat ->
                                                UzumCategoryCard(
                                                    label = cat.name,
                                                    iconKey = cat.iconKey,
                                                    isSelected = selectedCategory == cat.id,
                                                    onClick = {
                                                        if (selectedCategory == cat.id) {
                                                            storeViewModel.selectCategory(null)
                                                        } else {
                                                            storeViewModel.selectCategory(cat.id)
                                                        }
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // 3C. FLASH SALE & PROMOTIONAL PRODUCTS
                            if (promotionalProducts.isNotEmpty() && searchQuery.isBlank() && selectedCategory == null) {
                                item(span = { GridItemSpan(2) }) {
                                    FlashSaleSection(
                                        promotionalProducts = promotionalProducts,
                                        onProductClick = onProductClick,
                                        onAddToCart = { product ->
                                            storeViewModel.addToCart(product.id, 1)
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("${product.name} savatga qo‘shildi")
                                            }
                                        },
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )
                                }
                            }

                            // 3C-1. PERSONALIZED RECOMMENDATIONS ("Siz uchun tavsiyalar")
                            if (personalizedRecommendations.isNotEmpty() && searchQuery.isBlank() && selectedCategory == null) {
                                item(span = { GridItemSpan(2) }) {
                                    RecommendationSection(
                                        title = "🎯 Siz uchun tavsiyalar",
                                        subtitle = "Qiziqishlaringiz va harakatlaringiz asosida saralangan",
                                        icon = Icons.Filled.AutoAwesome,
                                        iconColor = Color(0xFF7C3AED),
                                        products = personalizedRecommendations,
                                        favoriteIds = favIds,
                                        onProductClick = { product ->
                                            storeViewModel.trackRecommendationClick(product.id)
                                            onProductClick(product)
                                        },
                                        onAddToCart = { product ->
                                            storeViewModel.addToCart(product.id, 1)
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("${product.name} savatga qo‘shildi")
                                            }
                                        },
                                        onToggleFavorite = { product ->
                                            storeViewModel.toggleFavorite(product.id)
                                        },
                                        onItemImpression = { productId ->
                                            storeViewModel.trackRecommendationImpression(productId)
                                        },
                                        badgeText = "Tavsiya",
                                        badgeColor = Color(0xFF7C3AED),
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )
                                }
                            }

                            // 3C-2. PURCHASE-BASED RECOMMENDATIONS ("Xaridingizga mos mahsulotlar")
                            if (purchaseBasedRecommendations.isNotEmpty() && searchQuery.isBlank() && selectedCategory == null) {
                                item(span = { GridItemSpan(2) }) {
                                    RecommendationSection(
                                        title = "🛍️ Xaridingizga mos mahsulotlar",
                                        subtitle = "Oldingi buyurtmalaringizga mos tovarlar",
                                        icon = Icons.Outlined.ThumbUp,
                                        iconColor = PrimaryBurgundy,
                                        products = purchaseBasedRecommendations,
                                        favoriteIds = favIds,
                                        onProductClick = { product ->
                                            storeViewModel.trackRecommendationClick(product.id)
                                            onProductClick(product)
                                        },
                                        onAddToCart = { product ->
                                            storeViewModel.addToCart(product.id, 1)
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("${product.name} savatga qo‘shildi")
                                            }
                                        },
                                        onToggleFavorite = { product ->
                                            storeViewModel.toggleFavorite(product.id)
                                        },
                                        onItemImpression = { productId ->
                                            storeViewModel.trackRecommendationImpression(productId)
                                        },
                                        badgeText = "Mos keluvchi",
                                        badgeColor = PrimaryBurgundy,
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )
                                }
                            }

                            // 3D. SECTION HEADER
                            item(span = { GridItemSpan(2) }) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 6.dp, bottom = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Outlined.ShoppingBag,
                                            contentDescription = null,
                                            tint = PrimaryBurgundy,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (selectedCategory != null) {
                                                val catName = categories.find { it.id == selectedCategory }?.name ?: "Kategoriya"
                                                "$catName (${products.size})"
                                            } else if (searchQuery.isNotBlank()) {
                                                if (searchResultState?.isFallback == true) {
                                                    "Eng yaqin mos mahsulotlar (${products.size})"
                                                } else if (searchResultState?.isAiEnhanced == true) {
                                                    "AI qidiruv natijalari (${products.size})"
                                                } else {
                                                    "Qidiruv natijalari (${products.size})"
                                                }
                                            } else {
                                                "Bozor Mahsulotlari (${products.size})"
                                            },
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            ),
                                            color = SecondaryNavy
                                        )
                                    }

                                    if (selectedCategory != null || searchQuery.isNotBlank()) {
                                        Text(
                                            text = "Filtrni tozalash",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = PrimaryBurgundy,
                                            modifier = Modifier.clickable {
                                                storeViewModel.selectCategory(null)
                                                storeViewModel.setSearchQuery("")
                                            }
                                        )
                                    }
                                }
                            }

                            // 3E. PRODUCT ITEMS OR EMPTY STATE
                            if (products.isEmpty()) {
                                item(span = { GridItemSpan(2) }) {
                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = CardSurface),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 16.dp, bottom = 16.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(20.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = PrimaryBurgundyLight,
                                                modifier = Modifier.size(56.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Storefront,
                                                        contentDescription = null,
                                                        tint = PrimaryBurgundy,
                                                        modifier = Modifier.size(28.dp)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(12.dp))

                                            Text(
                                                text = if (searchQuery.isNotBlank() || selectedCategory != null) {
                                                    "Qidiruv natijasi topilmadi"
                                                } else {
                                                    "Bozorda mahsulotlar hozircha yo‘q"
                                                },
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 16.sp
                                                ),
                                                color = SecondaryNavy
                                            )

                                            Spacer(modifier = Modifier.height(6.dp))

                                             Text(
                                                text = if (searchQuery.isNotBlank() || selectedCategory != null) {
                                                    "Boshqa so‘z bilan qidirib ko‘ring yoki tozalang."
                                                } else {
                                                    "Bozorda yangi mahsulotlar tez orada qo‘shiladi."
                                                },
                                                fontSize = 13.sp,
                                                color = SlateGray,
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                            )

                                            if (searchQuery.isNotBlank() || selectedCategory != null) {
                                                Spacer(modifier = Modifier.height(14.dp))
                                                OutlinedButton(
                                                    onClick = {
                                                        storeViewModel.setSearchQuery("")
                                                        storeViewModel.selectCategory(null)
                                                    },
                                                    shape = RoundedCornerShape(10.dp)
                                                ) {
                                                    Text("Filtrni tozalash", color = PrimaryBurgundy)
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                items(products, key = { it.id }) { product ->
                                    ProductCard(
                                        product = product,
                                        isFavorite = favIds.contains(product.id),
                                        onProductClick = { onProductClick(product) },
                                        onAddToCart = {
                                            storeViewModel.addToCart(product.id, 1)
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("${product.name} savatga qo‘shildi")
                                            }
                                        },
                                        onToggleFavorite = {
                                            storeViewModel.toggleFavorite(product.id)
                                        }
                                    )
                                }

                                // Creator stamp
                                item(span = { GridItemSpan(2) }) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 16.dp, bottom = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "by: @mahmud.buriboyev",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = SlateGray
                                        )
                                    }
                                }
                            }
                        }
                    }

                    EcosystemTab.FOOD -> {
                        GagarinFoodScreen(
                            foodStoreViewModel = foodStoreViewModel,
                            authViewModel = authViewModel,
                            onNavigateToFoodSellerAuth = onNavigateToFoodSellerAuth,
                            onNavigateToFoodAdminAuth = onNavigateToFoodAdminAuth,
                            promoBanners = promoBanners,
                            onBack = { ecosystemViewModel.setTab(EcosystemTab.HOME_HUB) }
                        )
                    }

                    EcosystemTab.JOBS -> {
                        GagarinJobsScreen(
                            ecosystemViewModel = ecosystemViewModel,
                            authViewModel = authViewModel,
                            promoBanners = promoBanners,
                            onBack = { ecosystemViewModel.setTab(EcosystemTab.HOME_HUB) }
                        )
                    }

                    EcosystemTab.SERVICES -> {
                        GagarinServicesScreen(
                            ecosystemViewModel = ecosystemViewModel,
                            authViewModel = authViewModel,
                            promoBanners = promoBanners,
                            onBack = { ecosystemViewModel.setTab(EcosystemTab.HOME_HUB) }
                        )
                    }

                    EcosystemTab.ADS -> {
                        GagarinAdsScreen(
                            ecosystemViewModel = ecosystemViewModel,
                            authViewModel = authViewModel,
                            promoBanners = promoBanners,
                            onBack = { ecosystemViewModel.setTab(EcosystemTab.HOME_HUB) }
                        )
                    }
                }
            }
        }

        // 4. PORTAL SWITCHER BOTTOM SHEET (Opened when Gagarin Go logo/brand is clicked)
        if (showPortalSelector) {
            ModalBottomSheet(
                onDismissRequest = { showPortalSelector = false },
                sheetState = sheetState,
                containerColor = CardSurface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        GagarinGoLogo(size = 32.dp, tint = PrimaryBurgundy)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Gagarin Go Boshqaruv",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp
                                ),
                                color = DarkText
                            )
                            Text(
                                text = "Kerakli boshqaruv kabinetiga o‘ting",
                                fontSize = 12.sp,
                                color = SecondaryText
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 1. Sotuvchi Kabineti (Bozor)
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = SurfaceSubtle,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                showPortalSelector = false
                                onNavigateToSellerAuth()
                            }
                            .testTag("portal_sheet_seller")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(PrimaryBurgundyLight, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Storefront,
                                    contentDescription = null,
                                    tint = PrimaryBurgundy,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Sotuvchi Kabineti",
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkText
                                )
                                Text(
                                    text = "Bozor do‘koni va mahsulotlarni boshqarish",
                                    fontSize = 12.sp,
                                    color = SecondaryText
                                )
                            }
                            Icon(
                                imageVector = Icons.Filled.ChevronRight,
                                contentDescription = null,
                                tint = SecondaryText,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 2. Taomlar Oshxona Kabineti
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = SurfaceSubtle,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                showPortalSelector = false
                                onNavigateToFoodSellerAuth()
                            }
                            .testTag("portal_sheet_food_seller")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(Color(0xFFFFEDD5), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ShoppingBag,
                                    contentDescription = null,
                                    tint = Color(0xFFEA580C),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Oshxona / Kafe Kabineti",
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkText
                                )
                                Text(
                                    text = "Taomlar menyusi va yetkazish buyurtmalari",
                                    fontSize = 12.sp,
                                    color = SecondaryText
                                )
                            }
                            Icon(
                                imageVector = Icons.Filled.ChevronRight,
                                contentDescription = null,
                                tint = SecondaryText,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 3. Admin Paneli
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = SurfaceSubtle,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                showPortalSelector = false
                                onNavigateToAdminAuth()
                            }
                            .testTag("portal_sheet_admin")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(Color(0xFFEDE9FE), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = Color(0xFF6D28D9),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Bosh Admin Paneli",
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkText
                                )
                                Text(
                                    text = "Foydalanuvchilar, do‘konlar va tizim nazorati",
                                    fontSize = 12.sp,
                                    color = SecondaryText
                                )
                            }
                            Icon(
                                imageVector = Icons.Filled.ChevronRight,
                                contentDescription = null,
                                tint = SecondaryText,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // 5. PROMO BANNER DETAILS DIALOG
        selectedBannerDetails?.let { banner ->
            AlertDialog(
                onDismissRequest = { selectedBannerDetails = null },
                shape = RoundedCornerShape(16.dp),
                containerColor = CardSurface,
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = PrimaryBurgundy
                        ) {
                            Text(
                                text = banner.badgeText.ifBlank { "REKLAMA" },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        IconButton(onClick = { selectedBannerDetails = null }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Yopish",
                                tint = SlateGray
                            )
                        }
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (banner.imageUrl.isNotBlank()) {
                            val resolvedBannerImg = ImageStorageHelper.resolveImageModel(banner.imageUrl, LocalContext.current) ?: banner.imageUrl
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SurfaceSubtle)
                            ) {
                                SubcomposeAsyncImage(
                                    model = resolvedBannerImg,
                                    contentDescription = banner.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                    loading = {
                                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                        }
                                    }
                                )
                            }
                        }

                        Text(
                            text = banner.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = SecondaryNavy
                        )

                        val desc = banner.description.ifBlank { banner.name }
                        if (desc.isNotBlank()) {
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF4B5563),
                                lineHeight = 20.sp
                            )
                        }
                    }
                },
                confirmButton = {
                    val target = banner.targetLink.trim()
                    if (target.isNotBlank()) {
                        Button(
                            onClick = {
                                try {
                                    if (target.startsWith("http://") || target.startsWith("https://")) {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(target))
                                        context.startActivity(intent)
                                    } else if (target.startsWith("tel:")) {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse(target))
                                        context.startActivity(intent)
                                    } else {
                                        val clean = target.filter { it.isDigit() || it == '+' }
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$clean"))
                                        context.startActivity(intent)
                                    }
                                } catch (_: Exception) {}
                                selectedBannerDetails = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBurgundy),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Batafsil / Bog‘lanish", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = { selectedBannerDetails = null },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBurgundy),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Tushunarli", fontWeight = FontWeight.Bold)
                        }
                    }
                },
                dismissButton = {
                    if (banner.targetLink.isNotBlank()) {
                        TextButton(onClick = { selectedBannerDetails = null }) {
                            Text("Yopish", color = SlateGray)
                        }
                    }
                }
            )
        }

        // 6. GPS PROMPT DIALOG (When GPS is disabled)
        if (showGpsPromptDialog) {
            AlertDialog(
                onDismissRequest = { showGpsPromptDialog = false },
                shape = RoundedCornerShape(18.dp),
                containerColor = CardSurface,
                icon = {
                    Icon(
                        imageVector = Icons.Filled.LocationOff,
                        contentDescription = null,
                        tint = Color(0xFFC2410C),
                        modifier = Modifier.size(36.dp)
                    )
                },
                title = {
                    Text(
                        text = "GPS joylashuvni yoqing",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = DarkText
                    )
                },
                text = {
                    Text(
                        text = "Aniq yetkazib berish va Gagarin bo‘yicha joylashuvingizni avtomatik aniqlash uchun qurilma GPS xizmatini yoqing.",
                        fontSize = 13.5.sp,
                        color = SecondaryText,
                        lineHeight = 19.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showGpsPromptDialog = false
                            locationMgr.openLocationSettings()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBurgundy),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("GPS ni yoqish", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showGpsPromptDialog = false
                            manualLocationInput = currentAddress
                            showLocationEditDialog = true
                        }
                    ) {
                        Text("Qo‘lda kiritish", color = SecondaryText)
                    }
                }
            )
        }

        // 7. LOCATION EDIT & GPS REFRESH DIALOG
        if (showLocationEditDialog) {
            AlertDialog(
                onDismissRequest = { showLocationEditDialog = false },
                shape = RoundedCornerShape(18.dp),
                containerColor = CardSurface,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = PrimaryBurgundy,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Yetkazish manzili",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = DarkText
                        )
                    }
                },
                text = {
                    Column {
                        // GPS status badge
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isGpsEnabled) Color(0xFFF0FDF4) else Color(0xFFFFF7ED),
                            border = BorderStroke(
                                1.dp,
                                if (isGpsEnabled) Color(0xFFBBF7D0) else Color(0xFFFED7AA)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(
                                            if (isGpsEnabled) Color(0xFF22C55E) else Color(0xFFF97316),
                                            shape = CircleShape
                                        )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = if (isGpsEnabled) "GPS faol" else "GPS o‘chirilgan",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isGpsEnabled) Color(0xFF166534) else Color(0xFF9A3412)
                                    )
                                    if (coordinates != null) {
                                        Text(
                                            text = "Koordinata: ${String.format(java.util.Locale.US, "%.4f, %.4f", coordinates!!.first, coordinates!!.second)}",
                                            fontSize = 11.sp,
                                            color = SecondaryText
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Hozirgi manzilingiz yoki ko‘changizni kiriting:",
                            fontSize = 13.sp,
                            color = SecondaryText
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = manualLocationInput,
                            onValueChange = { manualLocationInput = it },
                            placeholder = { Text("Masalan: Gagarin sh., Navoiy ko'chasi, 14") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBurgundy,
                                unfocusedBorderColor = BorderColor
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        if (isGpsEnabled) {
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        locationMgr.fetchCurrentLocation(forceHighAccuracy = true)
                                        showLocationEditDialog = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFDF2F4)),
                                border = BorderStroke(1.dp, PrimaryBurgundy),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.MyLocation,
                                    contentDescription = null,
                                    tint = PrimaryBurgundy,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "GPS orqali qayta aniqlash",
                                    color = PrimaryBurgundy,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Button(
                                onClick = {
                                    showLocationEditDialog = false
                                    locationMgr.openLocationSettings()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFF7ED)),
                                border = BorderStroke(1.dp, Color(0xFFEA580C)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.LocationOff,
                                    contentDescription = null,
                                    tint = Color(0xFFEA580C),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "GPS sozlamalarini ochish",
                                    color = Color(0xFFEA580C),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (manualLocationInput.isNotBlank()) {
                                locationMgr.setManualAddress(manualLocationInput.trim())
                            }
                            showLocationEditDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBurgundy),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Saqlash", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLocationEditDialog = false }) {
                        Text("Bekor qilish", color = SecondaryText)
                    }
                }
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}
