package com.example.ui.screens.customer

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.PromoBannerEntity
import com.example.data.util.ImageStorageHelper
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

    val context = LocalContext.current
    val favIds = favoriteProducts.map { it.id }.toSet()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

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
            // Popped tab from history (e.g. from services/jobs/ads/taxi/food back to bozor)
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
            // 1. TOP HEADER & BRANDING
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .clickable { showPortalSelector = true }
                        ) {
                            GagarinGoLogo(size = 34.dp, tint = PrimaryBurgundy)
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Text(
                                        text = "GAGARIN",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 16.sp,
                                        color = DarkText,
                                        letterSpacing = 0.3.sp,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "GO",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 17.sp,
                                        color = PrimaryBurgundy,
                                        letterSpacing = 0.3.sp,
                                        maxLines = 1
                                    )
                                }
                                Text(
                                    text = "Mirzacho‘l Savdo",
                                    fontSize = 10.5.sp,
                                    color = SecondaryText,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Right Action Buttons: Prominent Sotuvchi and Admin portal buttons
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = PrimaryBurgundyLight,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { onNavigateToSellerAuth() }
                                    .testTag("portal_seller_btn")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Storefront,
                                        contentDescription = "Sotuvchi",
                                        tint = PrimaryBurgundy,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Sotuvchi",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryBurgundy,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFFEDE9FE),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { onNavigateToAdminAuth() }
                                    .testTag("portal_admin_btn")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.AdminPanelSettings,
                                        contentDescription = "Admin",
                                        tint = Color(0xFF6D28D9),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Admin",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF6D28D9),
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                    }

                    // Search input on Bozor
                    if (currentTab == EcosystemTab.BOZOR) {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { storeViewModel.setSearchQuery(it) },
                            placeholder = {
                                Text(
                                    text = "Mahsulot, ish, usta yoki e’lon qidiring",
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
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { storeViewModel.setSearchQuery("") }) {
                                        Icon(
                                            imageVector = Icons.Filled.Close,
                                            contentDescription = "Tozalash",
                                            tint = SlateGray,
                                            modifier = Modifier.size(18.dp)
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
                    }
                }
            }

            // 2. ECOSYSTEM MODULES BAR (BOZOR, FOOD, ADS, SERVICES, JOBS)
            EcosystemHubBar(
                currentTab = currentTab,
                onTabSelected = { tab -> ecosystemViewModel.setTab(tab) },
                modifier = Modifier.testTag("ecosystem_hub_bar")
            )

            // 3. TAB CONTENT
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ecosystem_tab_content",
                modifier = Modifier.fillMaxSize().weight(1f)
            ) { tab ->
                when (tab) {
                    EcosystemTab.BOZOR -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("home_products_grid")
                        ) {
                            // 3A. PROMO BANNERS CAROUSEL
                            if (promoBanners.isNotEmpty() && searchQuery.isBlank() && selectedCategory == null) {
                                item(span = { GridItemSpan(2) }) {
                                    PromoBannerSection(
                                        banners = promoBanners,
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
                                                "Qidiruv natijalari (${products.size})"
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
                                                    "Demo mahsulotlar olib tashlangan. Sotuvchi kabinetiga kirib, o‘z haqiqiy mahsulotlaringizni qo‘shishingiz mumkin."
                                                },
                                                fontSize = 13.sp,
                                                color = SlateGray,
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                            )

                                            Spacer(modifier = Modifier.height(16.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Button(
                                                    onClick = onNavigateToSellerAuth,
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = PrimaryBurgundy,
                                                        contentColor = Color.White
                                                    ),
                                                    shape = RoundedCornerShape(10.dp),
                                                    modifier = Modifier.weight(1f).testTag("empty_state_seller_btn")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Storefront,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "Sotuvchi Kabineti",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    )
                                                }

                                                Button(
                                                    onClick = onNavigateToAdminAuth,
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = Color(0xFF6D28D9),
                                                        contentColor = Color.White
                                                    ),
                                                    shape = RoundedCornerShape(10.dp),
                                                    modifier = Modifier.weight(1f).testTag("empty_state_admin_btn")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.AdminPanelSettings,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "Admin Paneli",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    )
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
                            onNavigateToFoodAdminAuth = onNavigateToFoodAdminAuth
                        )
                    }

                    EcosystemTab.JOBS -> {
                        GagarinJobsScreen(
                            ecosystemViewModel = ecosystemViewModel,
                            authViewModel = authViewModel
                        )
                    }

                    EcosystemTab.SERVICES -> {
                        GagarinServicesScreen(
                            ecosystemViewModel = ecosystemViewModel,
                            authViewModel = authViewModel
                        )
                    }

                    EcosystemTab.ADS -> {
                        GagarinAdsScreen(
                            ecosystemViewModel = ecosystemViewModel,
                            authViewModel = authViewModel
                        )
                    }
                }
            }
        }

        // 4. PORTAL SWITCHER BOTTOM SHEET
        if (showPortalSelector) {
            ModalBottomSheet(
                onDismissRequest = { showPortalSelector = false },
                sheetState = sheetState,
                containerColor = CardSurface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "Gagarin Go Xizmatlar Paneli",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        ),
                        color = SecondaryNavy,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceSubtle,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showPortalSelector = false
                                onNavigateToSellerAuth()
                            }
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Storefront,
                                contentDescription = null,
                                tint = PrimaryBurgundy,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Sotuvchi Kabineti (Bozor)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SecondaryNavy
                                )
                                Text(
                                    text = "Mahsulotlar va buyurtmalarni boshqarish",
                                    fontSize = 12.sp,
                                    color = SlateGray
                                )
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceSubtle,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showPortalSelector = false
                                onNavigateToFoodSellerAuth()
                            }
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ShoppingBag,
                                contentDescription = null,
                                tint = Color(0xFFEA580C),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Oshxona / Kafe Kabineti (Gagarin Taomlar)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SecondaryNavy
                                )
                                Text(
                                    text = "Taomlar menyusi va yetkazish buyurtmalari",
                                    fontSize = 12.sp,
                                    color = SlateGray
                                )
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceSubtle,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showPortalSelector = false
                                onNavigateToAdminAuth()
                            }
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AdminPanelSettings,
                                contentDescription = null,
                                tint = DarkBurgundy,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Umumiy Admin Paneli",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SecondaryNavy
                                )
                                Text(
                                    text = "Bozor, foydalanuvchilar va tizim nazorati",
                                    fontSize = 12.sp,
                                    color = SlateGray
                                )
                            }
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

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}
