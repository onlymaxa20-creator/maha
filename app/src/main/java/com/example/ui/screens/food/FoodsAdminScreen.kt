package com.example.ui.screens.food

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.layout.ContentScale
import coil.compose.SubcomposeAsyncImage
import com.example.data.util.ImageStorageHelper
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import android.content.Intent
import android.net.Uri
import com.example.data.local.entity.FoodProductEntity
import com.example.data.local.entity.UserEntity
import com.example.data.local.entity.FoodOrderEntity
import com.example.ui.theme.CardSurface
import com.example.ui.theme.LightBackground
import com.example.ui.theme.OrangeAmber
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SecondaryNavy
import com.example.ui.theme.SlateGray
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SurfaceSubtle
import com.example.ui.theme.BorderColor
import com.example.ui.viewmodels.AuthViewModel
import com.example.ui.viewmodels.FoodAdminViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodsAdminScreen(
    adminUser: UserEntity,
    foodAdminViewModel: FoodAdminViewModel,
    authViewModel: AuthViewModel,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stats by foodAdminViewModel.dashboardStats.collectAsState()
    val restaurants by foodAdminViewModel.allRestaurants.collectAsState()
    val restaurantSales by foodAdminViewModel.restaurantSalesStats.collectAsState()
    val products by foodAdminViewModel.allProducts.collectAsState()
    val orders by foodAdminViewModel.allOrders.collectAsState()
    val categories by foodAdminViewModel.allCategories.collectAsState()
    val banners by foodAdminViewModel.allBanners.collectAsState()

    var currentTab by remember { mutableIntStateOf(0) } // 0: Stats, 1: Restaurants, 2: Products, 3: Categories, 4: Banners, 5: Orders

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showAddBannerDialog by remember { mutableStateOf(false) }
    var showAddRestaurantDialog by remember { mutableStateOf(false) }
    var rejectingProduct by remember { mutableStateOf<FoodProductEntity?>(null) }
    var rejectionReasonInput by remember { mutableStateOf("") }
    var orderSearchQuery by remember { mutableStateOf("") }
    var selectedOrderFilter by remember { mutableStateOf("BARCHASI") } // BARCHASI, BUGUN, YETKAZILDI, JARAYONDA, YANGI, BEKOR_QILINDI
    var statsPeriod by remember { mutableStateOf("1_OY") } // 1_OY, BUGUN, HAMMASI

    BackHandler {
        if (rejectingProduct != null) {
            rejectingProduct = null
        } else if (showAddRestaurantDialog) {
            showAddRestaurantDialog = false
        } else if (showAddBannerDialog) {
            showAddBannerDialog = false
        } else if (showAddCategoryDialog) {
            showAddCategoryDialog = false
        } else if (orderSearchQuery.isNotEmpty()) {
            orderSearchQuery = ""
        } else if (currentTab != 0) {
            currentTab = 0
        } else {
            onLogout()
        }
    }

    val context = LocalContext.current
    val formatter = remember { NumberFormat.getNumberInstance(Locale("uz", "UZ")) }
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("uz", "UZ")) }

    val topDishes = remember(orders) {
        val countMap = mutableMapOf<String, Int>()
        val revenueMap = mutableMapOf<String, Double>()
        orders.filter { it.status != "BEKOR_QILINDI" }.forEach { ord ->
            ord.itemsSummary.split(",").forEach { item ->
                val clean = item.trim()
                if (clean.isNotBlank()) {
                    countMap[clean] = (countMap[clean] ?: 0) + 1
                    revenueMap[clean] = (revenueMap[clean] ?: 0.0) + (ord.totalPrice / (ord.itemsSummary.split(",").size.coerceAtLeast(1)))
                }
            }
        }
        countMap.entries.sortedByDescending { it.value }.take(5).map { entry ->
            Triple(entry.key, entry.value, revenueMap[entry.key] ?: 0.0)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Gagarin Food Admin", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text("Boshqaruv Paneli", fontSize = 12.sp, color = SlateGray)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        authViewModel.logout()
                        onLogout()
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Chiqish",
                            tint = Color(0xFFD32F2F)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CardSurface,
                    titleContentColor = SecondaryNavy
                )
            )
        },
        bottomBar = {
            NavigationBar(containerColor = CardSurface, tonalElevation = 0.dp) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    icon = { Icon(Icons.Default.TrendingUp, contentDescription = "Statistika") },
                    label = { Text("Statistika", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = OrangeAmber, selectedTextColor = OrangeAmber)
                )
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    icon = { Icon(Icons.Default.Store, contentDescription = "Oshxonalar") },
                    label = { Text("Oshxonalar (${restaurants.size})", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = OrangeAmber, selectedTextColor = OrangeAmber)
                )
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 },
                    icon = { Icon(Icons.Default.Fastfood, contentDescription = "Taomlar") },
                    label = { Text("Taomlar (${products.size})", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = OrangeAmber, selectedTextColor = OrangeAmber)
                )
                NavigationBarItem(
                    selected = currentTab == 3,
                    onClick = { currentTab = 3 },
                    icon = { Icon(Icons.Default.Category, contentDescription = "Toifalar") },
                    label = { Text("Toifalar", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = OrangeAmber, selectedTextColor = OrangeAmber)
                )
                NavigationBarItem(
                    selected = currentTab == 4,
                    onClick = { currentTab = 4 },
                    icon = { Icon(Icons.Default.LocalOffer, contentDescription = "Aksiya") },
                    label = { Text("Aksiya", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = OrangeAmber, selectedTextColor = OrangeAmber)
                )
                NavigationBarItem(
                    selected = currentTab == 5,
                    onClick = { currentTab = 5 },
                    icon = { Icon(Icons.Default.ListAlt, contentDescription = "Buyurtmalar") },
                    label = { Text("Buyurtmalar (${orders.size})", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = OrangeAmber, selectedTextColor = OrangeAmber)
                )
            }
        },
        floatingActionButton = {
            if (currentTab == 1) {
                FloatingActionButton(
                    onClick = { showAddRestaurantDialog = true },
                    containerColor = OrangeAmber,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Oshxona qo‘shish")
                }
            } else if (currentTab == 3) {
                FloatingActionButton(
                    onClick = { showAddCategoryDialog = true },
                    containerColor = OrangeAmber,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Toifa qo‘shish")
                }
            } else if (currentTab == 4) {
                FloatingActionButton(
                    onClick = { showAddBannerDialog = true },
                    containerColor = OrangeAmber,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Banner qo‘shish")
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(LightBackground)
                .padding(innerPadding)
        ) {
            when (currentTab) {
                0 -> {
                    // STATS & SALES ANALYTICS
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Title & Period Selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Umumiy Savdo va Moliya",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SecondaryNavy
                                )
                                Text(
                                    "Barcha oshxonalar savdo aylanmasi",
                                    fontSize = 12.sp,
                                    color = SlateGray
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(CardSurface)
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (statsPeriod == "1_OY") OrangeAmber else Color.Transparent,
                                    modifier = Modifier.clickable { statsPeriod = "1_OY" }
                                ) {
                                    Text(
                                        "1 Oylik",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (statsPeriod == "1_OY") Color.White else SlateGray
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (statsPeriod == "BUGUN") OrangeAmber else Color.Transparent,
                                    modifier = Modifier.clickable { statsPeriod = "BUGUN" }
                                ) {
                                    Text(
                                        "Bugun",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (statsPeriod == "BUGUN") Color.White else SlateGray
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (statsPeriod == "HAMMASI") OrangeAmber else Color.Transparent,
                                    modifier = Modifier.clickable { statsPeriod = "HAMMASI" }
                                ) {
                                    Text(
                                        "Hammasi",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (statsPeriod == "HAMMASI") Color.White else SlateGray
                                    )
                                }
                            }
                        }

                        // Hero Revenue Card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFFE65100), Color(0xFFF57C00), Color(0xFFFF9800))
                                    ),
                                    RoundedCornerShape(18.dp)
                                )
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        when (statsPeriod) {
                                            "1_OY" -> "Oxirgi 1 Oylik Savdo Tushumi"
                                            "BUGUN" -> "Bugungi Savdo Tushumi"
                                            else -> "Jami Muvaffaqiyatli Tushum"
                                        },
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Box(
                                        modifier = Modifier
                                            .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            when (statsPeriod) {
                                                "1_OY" -> "${stats.monthlyOrdersCount} ta yetkazildi (30 kun)"
                                                "BUGUN" -> "${stats.todayOrdersCount} ta buyurtma"
                                                else -> "${stats.completedOrders} ta yetkazildi"
                                            },
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                val displayRev = when (statsPeriod) {
                                    "1_OY" -> stats.monthlyRevenue
                                    "BUGUN" -> stats.todayRevenue
                                    else -> stats.totalRevenue
                                }
                                Text(
                                    "${formatter.format(displayRev.toLong())} so‘m",
                                    color = Color.White,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )

                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = Color.White.copy(alpha = 0.25f))
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("O‘rtacha chek", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                                        Text(
                                            "${formatter.format(stats.averageOrderValue.toLong())} so‘m",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Jami buyurtmalar qiymati", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                                        Text(
                                            "${formatter.format(stats.grossOrderValue.toLong())} so‘m",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Secondary Financial Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CardSurface),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.DeliveryDining, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Jarayonda / Kutilmoqda", fontSize = 11.sp, color = SlateGray, fontWeight = FontWeight.Medium)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "${formatter.format(stats.inProgressRevenue.toLong())} so‘m",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryBlue
                                    )
                                    Text(
                                        "${stats.inProgressOrders} ta faol buyurtma",
                                        fontSize = 11.sp,
                                        color = SlateGray
                                    )
                                }
                            }

                            Card(
                                colors = CardDefaults.cardColors(containerColor = CardSurface),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Cancel, contentDescription = null, tint = Color(0xFFD32F2F), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Bekor qilingan", fontSize = 11.sp, color = SlateGray, fontWeight = FontWeight.Medium)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val cancelledSum = orders.filter { it.status == "BEKOR_QILINDI" }.sumOf { it.totalPrice }
                                    Text(
                                        "${formatter.format(cancelledSum.toLong())} so‘m",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFD32F2F)
                                    )
                                    Text(
                                        "${stats.cancelledOrders} ta bekor qilingan",
                                        fontSize = 11.sp,
                                        color = SlateGray
                                    )
                                }
                            }
                        }

                        // Order Status Breakdown Matrix
                        Text("Buyurtmalar Holati Tahlili", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SecondaryNavy)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Yetkazilgan
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.1f)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("Yetkazildi", fontSize = 11.sp, color = SuccessGreen, fontWeight = FontWeight.Bold)
                                    Text("${stats.completedOrders}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = SuccessGreen)
                                    Text("Tushumga aylandi", fontSize = 9.sp, color = SlateGray)
                                }
                            }

                            // Jarayonda
                            Card(
                                colors = CardDefaults.cardColors(containerColor = OrangeAmber.copy(alpha = 0.1f)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("Jarayonda", fontSize = 11.sp, color = OrangeAmber, fontWeight = FontWeight.Bold)
                                    Text("${stats.inProgressOrders}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = OrangeAmber)
                                    Text("Oshxona / Kuryer", fontSize = 9.sp, color = SlateGray)
                                }
                            }

                            // Yangi
                            val newCount = orders.count { it.status == "YANGI" }
                            Card(
                                colors = CardDefaults.cardColors(containerColor = PrimaryBlue.copy(alpha = 0.1f)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("Yangi", fontSize = 11.sp, color = PrimaryBlue, fontWeight = FontWeight.Bold)
                                    Text("$newCount", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = PrimaryBlue)
                                    Text("Kutilmoqda", fontSize = 9.sp, color = SlateGray)
                                }
                            }

                            // Jami
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CardSurface),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("Jami", fontSize = 11.sp, color = SecondaryNavy, fontWeight = FontWeight.Bold)
                                    Text("${stats.totalOrders}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = SecondaryNavy)
                                    Text("Barcha buyurtma", fontSize = 9.sp, color = SlateGray)
                                }
                            }
                        }

                        // RESTAURANTS REVENUE LEADERBOARD
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Oshxonalar Savdo Reytingi (${restaurantSales.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = SecondaryNavy
                            )
                            Text(
                                "Tushum bo‘yicha",
                                fontSize = 11.sp,
                                color = OrangeAmber,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        if (restaurantSales.isEmpty()) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CardSurface),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                                    Text("Hozircha ro‘yxatdan o‘tgan oshxonalar yo‘q", color = SlateGray, fontSize = 13.sp)
                                }
                            }
                        } else {
                            val maxRevenue = restaurantSales.maxOfOrNull { it.totalRevenue }?.coerceAtLeast(1.0) ?: 1.0
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                restaurantSales.forEachIndexed { index, restStat ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = CardSurface),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(26.dp)
                                                            .background(
                                                                if (index == 0) Color(0xFFFFD700)
                                                                else if (index == 1) Color(0xFFC0C0C0)
                                                                else if (index == 2) Color(0xFFCD7F32)
                                                                else SurfaceSubtle,
                                                                CircleShape
                                                            ),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            "${index + 1}",
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 12.sp,
                                                            color = if (index < 3) Color.Black else SlateGray
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Column {
                                                        Text(
                                                            restStat.restaurantName,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 14.sp,
                                                            color = SecondaryNavy
                                                        )
                                                        if (restStat.category.isNotBlank()) {
                                                            Text(restStat.category, fontSize = 11.sp, color = SlateGray)
                                                        }
                                                    }
                                                }

                                                Column(horizontalAlignment = Alignment.End) {
                                                    Text(
                                                        "${formatter.format(restStat.monthlyRevenue.toLong())} so‘m",
                                                        fontWeight = FontWeight.ExtraBold,
                                                        fontSize = 14.sp,
                                                        color = SuccessGreen
                                                    )
                                                    Text(
                                                        "1 oylik: ${restStat.monthlyOrders} ta buyurtma",
                                                        fontSize = 10.5.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = OrangeAmber
                                                    )
                                                    Text(
                                                        "Jami: ${formatter.format(restStat.totalRevenue.toLong())} so‘m (${restStat.completedOrders} ta)",
                                                        fontSize = 10.sp,
                                                        color = SlateGray
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))

                                            // Progress bar showing percentage of total sales
                                            val progress = (restStat.totalRevenue / maxRevenue).toFloat().coerceIn(0f, 1f)
                                            LinearProgressIndicator(
                                                progress = { progress },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(6.dp)
                                                    .clip(RoundedCornerShape(3.dp)),
                                                color = OrangeAmber,
                                                trackColor = SurfaceSubtle
                                            )

                                            if (restStat.pendingOrders > 0) {
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(6.dp)
                                                            .background(OrangeAmber, CircleShape)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        "${restStat.pendingOrders} ta buyurtma hozir tayyorlanmoqda/yetkazilmoqda",
                                                        fontSize = 10.sp,
                                                        color = OrangeAmber,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // TOP SELLING DISHES
                        if (topDishes.isNotEmpty()) {
                            Text("Eng Ko‘p Buyurtma Qilingan Taomlar", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SecondaryNavy)
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CardSurface),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    topDishes.forEachIndexed { i, item ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                Text("🍔", fontSize = 16.sp)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(item.first, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = SecondaryNavy)
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("${item.second} marta", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OrangeAmber)
                                            }
                                        }
                                        if (i < topDishes.size - 1) {
                                            HorizontalDivider(color = BorderColor.copy(alpha = 0.5f))
                                        }
                                    }
                                }
                            }
                        }

                        // PLATFORM RESOURCES OVERVIEW
                        Text("Tizim Resurslari", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SecondaryNavy)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CardSurface),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Oshxonalar", fontSize = 11.sp, color = SlateGray)
                                    Text("${stats.activeRestaurants} faol", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SecondaryNavy)
                                    Text("${stats.pendingRestaurants} ta tasdiq kutilmoqda", fontSize = 10.sp, color = OrangeAmber)
                                }
                            }

                            Card(
                                colors = CardDefaults.cardColors(containerColor = CardSurface),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Taomlar Katalogi", fontSize = 11.sp, color = SlateGray)
                                    Text("${stats.totalFoodProducts} ta", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SecondaryNavy)
                                    Text("${stats.pendingProducts} ta tekshiruvda", fontSize = 10.sp, color = OrangeAmber)
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // RESTAURANTS
                    if (restaurants.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                            Text("Hozircha ro‘yxatdan o‘tgan oshxonalar yo‘q", color = SlateGray)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(restaurants, key = { it.id }) { rest ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = CardSurface),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(rest.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SecondaryNavy)
                                            val badgeColor = when (rest.status) {
                                                "TASDIQLANGAN" -> SuccessGreen
                                                "BLOKLANGAN" -> Color.Red
                                                else -> OrangeAmber
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .background(badgeColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(rest.status, color = badgeColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Rahbar: ${rest.ownerName} • ${rest.category}", fontSize = 13.sp, color = SlateGray)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Phone, contentDescription = null, tint = SlateGray, modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(rest.phone, fontSize = 12.sp, color = SlateGray)
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Place, contentDescription = null, tint = SlateGray, modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(rest.address, fontSize = 12.sp, color = SlateGray)
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            if (rest.status != "TASDIQLANGAN") {
                                                Button(
                                                    onClick = { foodAdminViewModel.approveRestaurant(rest.id) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                                    modifier = Modifier.weight(1f),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text("Tasdiqlash", fontSize = 11.sp)
                                                }
                                            } else {
                                                Button(
                                                    onClick = { foodAdminViewModel.blockRestaurant(rest.id) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                                    modifier = Modifier.weight(1f),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text("Bloklash", fontSize = 11.sp)
                                                }
                                            }
                                            OutlinedButton(
                                                onClick = { foodAdminViewModel.deleteRestaurant(rest.id) },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text("O‘chirish", color = Color.Red, fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // PRODUCTS
                    if (products.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                            Text("Hozircha taomlar mavjud emas", color = SlateGray)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(products, key = { it.id }) { product ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = CardSurface),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(product.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SecondaryNavy)
                                            Text("${formatter.format(product.price.toLong())} so‘m", fontWeight = FontWeight.Bold, color = OrangeAmber, fontSize = 14.sp)
                                        }
                                        Text("Oshxona: ${product.restaurantName} • ${product.categoryName}", fontSize = 12.sp, color = SlateGray)
                                        if (product.ingredients.isNotBlank()) {
                                            Text("Tarkibi: ${product.ingredients}", fontSize = 12.sp, color = SlateGray)
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            if (product.status != "APPROVED") {
                                                Button(
                                                    onClick = { foodAdminViewModel.approveProduct(product.id) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                                    modifier = Modifier.weight(1f),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text("Tasdiqlash", fontSize = 11.sp)
                                                }
                                                OutlinedButton(
                                                    onClick = {
                                                        rejectingProduct = product
                                                        rejectionReasonInput = ""
                                                    },
                                                    modifier = Modifier.weight(1f),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text("Rad etish", color = Color.Red, fontSize = 11.sp)
                                                }
                                            }
                                            OutlinedButton(
                                                onClick = { foodAdminViewModel.deleteProduct(product.id) },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text("O‘chirish", color = Color.Red, fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // CATEGORIES
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(categories, key = { it.id }) { cat ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CardSurface),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(cat.iconEmoji, fontSize = 24.sp)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(cat.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SecondaryNavy)
                                    }
                                    IconButton(onClick = { foodAdminViewModel.deleteCategory(cat.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "O‘chirish", tint = Color(0xFFD32F2F))
                                    }
                                }
                            }
                        }
                    }
                }

                4 -> {
                    // BANNERS
                    if (banners.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                            Text("Aktiv bannerlar yo‘q. '+' tugmasini bosib qo‘shing", color = SlateGray)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(banners, key = { it.id }) { banner ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = CardSurface),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(banner.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SecondaryNavy)
                                            if (banner.description.isNotBlank()) {
                                                Text(banner.description, fontSize = 12.sp, color = SlateGray)
                                            }
                                        }
                                        IconButton(onClick = { foodAdminViewModel.deleteBanner(banner.id) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "O‘chirish", tint = Color(0xFFD32F2F))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                5 -> {
                    // ORDERS & SALES JOURNAL
                    val filteredOrders = remember(orders, orderSearchQuery, selectedOrderFilter) {
                        orders.filter { order ->
                            val matchesSearch = orderSearchQuery.isBlank() ||
                                order.id.toString().contains(orderSearchQuery.trim()) ||
                                order.customerName.contains(orderSearchQuery.trim(), ignoreCase = true) ||
                                order.customerPhone.contains(orderSearchQuery.trim(), ignoreCase = true) ||
                                order.restaurantName.contains(orderSearchQuery.trim(), ignoreCase = true) ||
                                order.itemsSummary.contains(orderSearchQuery.trim(), ignoreCase = true) ||
                                order.deliveryAddress.contains(orderSearchQuery.trim(), ignoreCase = true)

                            val matchesFilter = when (selectedOrderFilter) {
                                "BARCHASI" -> true
                                "BUGUN" -> {
                                    val now = java.util.Calendar.getInstance()
                                    val orderCal = java.util.Calendar.getInstance().apply { timeInMillis = order.createdAt }
                                    now.get(java.util.Calendar.YEAR) == orderCal.get(java.util.Calendar.YEAR) &&
                                        now.get(java.util.Calendar.DAY_OF_YEAR) == orderCal.get(java.util.Calendar.DAY_OF_YEAR)
                                }
                                "YETKAZILDI" -> order.status == "YETKAZILDI"
                                "JARAYONDA" -> order.status in listOf("QABUL_QILINDI", "TAYYORLANMOQDA", "YOLDA")
                                "YANGI" -> order.status == "YANGI"
                                "BEKOR_QILINDI" -> order.status == "BEKOR_QILINDI"
                                else -> true
                            }
                            matchesSearch && matchesFilter
                        }
                    }

                    val filteredTotalSum = remember(filteredOrders) {
                        filteredOrders.filter { it.status != "BEKOR_QILINDI" }.sumOf { it.totalPrice }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Search Bar
                        OutlinedTextField(
                            value = orderSearchQuery,
                            onValueChange = { orderSearchQuery = it },
                            placeholder = { Text("Buyurtma ID, mijoz, telefon, oshxona...", fontSize = 13.sp, color = SlateGray) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SlateGray) },
                            trailingIcon = {
                                if (orderSearchQuery.isNotBlank()) {
                                    IconButton(onClick = { orderSearchQuery = "" }) {
                                        Icon(Icons.Default.Cancel, contentDescription = "Tozalash", tint = SlateGray)
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        // Filter Chips
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val filters = listOf(
                                "BARCHASI" to "Barchasi (${orders.size})",
                                "BUGUN" to "Bugungi",
                                "YETKAZILDI" to "Yetkazildi (Tushum)",
                                "JARAYONDA" to "Jarayonda",
                                "YANGI" to "Yangi",
                                "BEKOR_QILINDI" to "Bekor qilingan"
                            )
                            items(filters) { (key, label) ->
                                FilterChip(
                                    selected = selectedOrderFilter == key,
                                    onClick = { selectedOrderFilter = key },
                                    label = { Text(label, fontSize = 11.sp, fontWeight = if (selectedOrderFilter == key) FontWeight.Bold else FontWeight.Normal) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = OrangeAmber,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        // Filter Summary Banner
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CardSurface),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Topildi: ${filteredOrders.size} ta buyurtma",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = SlateGray
                                )
                                Text(
                                    "Jami: ${formatter.format(filteredTotalSum.toLong())} so‘m",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreen
                                )
                            }
                        }

                        // Orders List
                        if (filteredOrders.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Hech qanday buyurtma topilmadi", color = SlateGray, fontSize = 13.sp)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(filteredOrders, key = { it.id }) { order ->
                                    val statusColor = when (order.status) {
                                        "YETKAZILDI" -> SuccessGreen
                                        "BEKOR_QILINDI" -> Color(0xFFD32F2F)
                                        "YANGI" -> PrimaryBlue
                                        else -> OrangeAmber
                                    }
                                    val statusUz = when (order.status) {
                                        "YANGI" -> "Yangi"
                                        "QABUL_QILINDI" -> "Qabul qilindi"
                                        "TAYYORLANMOQDA" -> "Tayyorlanmoqda"
                                        "YOLDA" -> "Kuryer yo‘lda"
                                        "YETKAZILDI" -> "Yetkazildi"
                                        "BEKOR_QILINDI" -> "Bekor qilindi"
                                        else -> order.status
                                    }

                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = CardSurface),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            // Header: ID + Restaurant + Status
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            "Buyurtma #${order.id}",
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 14.sp,
                                                            color = SecondaryNavy
                                                        )
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Surface(
                                                            shape = RoundedCornerShape(6.dp),
                                                            color = OrangeAmber.copy(alpha = 0.12f)
                                                        ) {
                                                            Text(
                                                                order.restaurantName,
                                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = OrangeAmber
                                                            )
                                                        }
                                                    }
                                                    Text(
                                                        dateFormat.format(Date(order.createdAt)),
                                                        fontSize = 11.sp,
                                                        color = SlateGray
                                                    )
                                                }

                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = statusColor.copy(alpha = 0.15f)
                                                ) {
                                                    Text(
                                                        statusUz,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                        fontWeight = FontWeight.Bold,
                                                        color = statusColor,
                                                        fontSize = 11.sp
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))
                                            HorizontalDivider(color = BorderColor.copy(alpha = 0.5f))
                                            Spacer(modifier = Modifier.height(8.dp))

                                            // Order items & price
                                            Text(
                                                "Tarkibi: ${order.itemsSummary}",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = SecondaryNavy
                                            )

                                            Spacer(modifier = Modifier.height(6.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    "To‘lov: Naqd (yetkazilganda)",
                                                    fontSize = 11.sp,
                                                    color = SlateGray
                                                )
                                                Text(
                                                    "${formatter.format(order.totalPrice.toLong())} so‘m",
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 15.sp,
                                                    color = SuccessGreen
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))

                                            // Customer info with call action
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = LightBackground,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(10.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            "Mijoz: ${order.customerName}",
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = SecondaryNavy
                                                        )
                                                        Text(
                                                            order.customerPhone,
                                                            fontSize = 11.sp,
                                                            color = SlateGray
                                                        )
                                                        if (order.deliveryAddress.isNotBlank()) {
                                                            Text(
                                                                "Manzil: ${order.deliveryAddress}",
                                                                fontSize = 11.sp,
                                                                color = SlateGray
                                                            )
                                                        }
                                                    }

                                                    IconButton(
                                                        onClick = {
                                                            if (order.customerPhone.isNotBlank()) {
                                                                val callIntent = Intent(Intent.ACTION_DIAL).apply {
                                                                    data = Uri.parse("tel:${order.customerPhone}")
                                                                }
                                                                context.startActivity(callIntent)
                                                            }
                                                        },
                                                        modifier = Modifier.size(36.dp)
                                                    ) {
                                                        Icon(
                                                            Icons.Default.Phone,
                                                            contentDescription = "Qo‘ng‘iroq qilish",
                                                            tint = SuccessGreen
                                                        )
                                                    }
                                                }
                                            }

                                            // Admin Quick Status Controls
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                if (order.status != "YETKAZILDI") {
                                                    Button(
                                                        onClick = {
                                                            foodAdminViewModel.updateOrderStatus(order.id, "YETKAZILDI")
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                                        shape = RoundedCornerShape(8.dp),
                                                        modifier = Modifier.weight(1f),
                                                        contentPadding = PaddingValues(vertical = 6.dp)
                                                    ) {
                                                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Yetkazildi", fontSize = 11.sp)
                                                    }
                                                }

                                                if (order.status !in listOf("YETKAZILDI", "BEKOR_QILINDI")) {
                                                    OutlinedButton(
                                                        onClick = {
                                                            foodAdminViewModel.updateOrderStatus(order.id, "BEKOR_QILINDI")
                                                        },
                                                        shape = RoundedCornerShape(8.dp),
                                                        modifier = Modifier.weight(1f),
                                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F)),
                                                        border = BorderStroke(1.dp, Color(0xFFD32F2F)),
                                                        contentPadding = PaddingValues(vertical = 6.dp)
                                                    ) {
                                                        Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(14.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Bekor qilish", fontSize = 11.sp)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ADD CATEGORY DIALOG
    if (showAddCategoryDialog) {
        var catName by remember { mutableStateOf("") }
        var catEmoji by remember { mutableStateOf("🍽️") }
        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            title = { Text("Yangi Toifa Qo‘shish", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = catName,
                        onValueChange = { catName = it },
                        label = { Text("Toifa Nomi") },
                        placeholder = { Text("Masalan: Somsa & Pirojki") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = catEmoji,
                        onValueChange = { catEmoji = it },
                        label = { Text("Emoji Belgisi") },
                        placeholder = { Text("🥟, 🍕, 🍗...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (catName.isNotBlank()) {
                            foodAdminViewModel.createCategory(catName, catEmoji, categories.size + 1)
                            showAddCategoryDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAmber)
                ) {
                    Text("Qo‘shish")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategoryDialog = false }) {
                    Text("Bekor qilish")
                }
            }
        )
    }

    // ADD BANNER DIALOG
    if (showAddBannerDialog) {
        var bannerTitle by remember { mutableStateOf("") }
        var bannerDesc by remember { mutableStateOf("") }
        var imageSourceMode by remember { mutableStateOf("GALLERY") } // "GALLERY" or "URL"
        var urlImageInput by remember { mutableStateOf("") }
        var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
        var isSavingBanner by remember { mutableStateOf(false) }
        var imageError by remember { mutableStateOf<String?>(null) }
        val scope = rememberCoroutineScope()

        val foodBannerPhotoPicker = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            if (uri != null) {
                val validation = ImageStorageHelper.validateImageFile(context, uri)
                if (validation.isValid) {
                    selectedImageUri = uri
                    imageError = null
                } else {
                    imageError = validation.errorMessage ?: "Yaroqsiz rasm fayli"
                }
            }
        }

        AlertDialog(
            onDismissRequest = { if (!isSavingBanner) showAddBannerDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null, tint = OrangeAmber, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Yangi Reklama Banneri Qo‘shish", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Banner rasmi manbasi:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = SecondaryNavy)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = imageSourceMode == "GALLERY",
                            onClick = { imageSourceMode = "GALLERY" },
                            label = { Text("Galereyadan", fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = OrangeAmber,
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = imageSourceMode == "URL",
                            onClick = { imageSourceMode = "URL" },
                            label = { Text("Havola (URL)", fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(Icons.Filled.Link, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = OrangeAmber,
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    val previewUri = if (imageSourceMode == "URL") urlImageInput.trim() else selectedImageUri?.toString() ?: ""

                    if (previewUri.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(115.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                        ) {
                            SubcomposeAsyncImage(
                                model = previewUri,
                                contentDescription = "Banner preview",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                                loading = {
                                    Box(modifier = Modifier.fillMaxSize().background(OrangeAmber.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(color = OrangeAmber, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                                    }
                                },
                                error = {
                                    Box(modifier = Modifier.fillMaxSize().background(Color.Gray.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                                        Text("Rasmni yuklab bo‘lmadi", color = Color.Red, fontSize = 11.sp)
                                    }
                                }
                            )

                            IconButton(
                                onClick = {
                                    selectedImageUri = null
                                    urlImageInput = ""
                                },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(6.dp)
                                    .size(26.dp)
                                    .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                            ) {
                                Icon(Icons.Filled.Close, contentDescription = "Tozalash", tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }
                    }

                    if (imageSourceMode == "GALLERY") {
                        OutlinedButton(
                            onClick = { foodBannerPhotoPicker.launch("image/*") },
                            modifier = Modifier.fillMaxWidth().height(46.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null, tint = OrangeAmber, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (selectedImageUri != null) "Boshqa rasm tanlash" else "Galereyadan tanlash",
                                color = OrangeAmber,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        OutlinedTextField(
                            value = urlImageInput,
                            onValueChange = { 
                                urlImageInput = it
                                imageError = null
                            },
                            label = { Text("Rasm havolasi (URL)") },
                            placeholder = { Text("https://example.com/banner.jpg") },
                            leadingIcon = { Icon(Icons.Filled.Link, contentDescription = null, tint = OrangeAmber) },
                            trailingIcon = {
                                if (urlImageInput.isNotBlank()) {
                                    IconButton(onClick = { urlImageInput = "" }) {
                                        Icon(Icons.Filled.Close, contentDescription = "Tozalash", tint = SlateGray)
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (imageError != null) {
                        Text(text = imageError!!, color = Color.Red, fontSize = 11.sp)
                    }

                    HorizontalDivider(color = BorderColor.copy(alpha = 0.5f))

                    OutlinedTextField(
                        value = bannerTitle,
                        onValueChange = { bannerTitle = it },
                        label = { Text("Sarlavha *") },
                        placeholder = { Text("Masalan: 20% Chegirma!") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = bannerDesc,
                        onValueChange = { bannerDesc = it },
                        label = { Text("Tavsif") },
                        placeholder = { Text("Masalan: Hafta oxirigacha barcha oshxonalarda") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (bannerTitle.isNotBlank()) {
                            scope.launch {
                                isSavingBanner = true
                                var finalImage = if (imageSourceMode == "URL") urlImageInput.trim() else ""
                                if (imageSourceMode == "GALLERY" && selectedImageUri != null) {
                                    val saveResult = ImageStorageHelper.saveBannerImage(context, selectedImageUri!!)
                                    saveResult.onSuccess { savedPath ->
                                        finalImage = savedPath
                                    }.onFailure { err ->
                                        imageError = err.localizedMessage ?: "Rasmni saqlashda xatolik"
                                        isSavingBanner = false
                                        return@launch
                                    }
                                }
                                foodAdminViewModel.createBanner(bannerTitle.trim(), bannerDesc.trim(), finalImage)
                                isSavingBanner = false
                                showAddBannerDialog = false
                            }
                        }
                    },
                    enabled = bannerTitle.isNotBlank() && !isSavingBanner,
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAmber)
                ) {
                    if (isSavingBanner) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text("Saqlash")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddBannerDialog = false }, enabled = !isSavingBanner) {
                    Text("Bekor qilish")
                }
            }
        )
    }

    // REJECT PRODUCT DIALOG
    if (rejectingProduct != null) {
        val prod = rejectingProduct!!
        AlertDialog(
            onDismissRequest = { rejectingProduct = null },
            title = { Text("Taomni Rad Etish", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${prod.name} taomini rad etish sababini kiriting:")
                    OutlinedTextField(
                        value = rejectionReasonInput,
                        onValueChange = { rejectionReasonInput = it },
                        label = { Text("Sabab") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        foodAdminViewModel.rejectProduct(prod.id, rejectionReasonInput.ifBlank { "Standartlarga mos kelmadi" })
                        rejectingProduct = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Rad etish")
                }
            },
            dismissButton = {
                TextButton(onClick = { rejectingProduct = null }) {
                    Text("Yopish")
                }
            }
        )
    }

    // ADD RESTAURANT & SELLER DIALOG
    if (showAddRestaurantDialog) {
        var restName by remember { mutableStateOf("") }
        var restOwner by remember { mutableStateOf("") }
        var restPhone by remember { mutableStateOf("+998 ") }
        var restAddress by remember { mutableStateOf("") }
        var restCategory by remember { mutableStateOf("Milliy taomlar") }
        var restLogin by remember { mutableStateOf("") }
        var restPassword by remember { mutableStateOf("") }
        var restError by remember { mutableStateOf<String?>(null) }
        var isSubmitting by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        AlertDialog(
            onDismissRequest = { showAddRestaurantDialog = false },
            title = { Text("Yangi Oshxona & Sotuvchi Qo‘shish", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "Oshxona egasiga tizimga kirish uchun login va parol belgilang:",
                        fontSize = 12.sp,
                        color = SlateGray
                    )

                    OutlinedTextField(
                        value = restName,
                        onValueChange = { restName = it; restError = null },
                        label = { Text("Oshxona / Kafe Nomi") },
                        placeholder = { Text("Masalan: Rayhon Milliy Taomlar") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = restOwner,
                        onValueChange = { restOwner = it; restError = null },
                        label = { Text("Oshpaz / Rahbar Ismi") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = restPhone,
                        onValueChange = { restPhone = it; restError = null },
                        label = { Text("Telefon Raqami") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = restAddress,
                        onValueChange = { restAddress = it; restError = null },
                        label = { Text("Manzil") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = restCategory,
                        onValueChange = { restCategory = it; restError = null },
                        label = { Text("Toifasi (Milliy, Fast Food...)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = restLogin,
                        onValueChange = { restLogin = it; restError = null },
                        label = { Text("Oshxona Logini (3+ belgi)") },
                        placeholder = { Text("masalan: rayhon_food") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = restPassword,
                        onValueChange = { restPassword = it; restError = null },
                        label = { Text("Oshxona Paroli (6+ belgi)") },
                        placeholder = { Text("masalan: rayhon2026") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (restError != null) {
                        Text(
                            text = restError!!,
                            color = Color.Red,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (restName.isBlank() || restLogin.isBlank() || restPassword.isBlank()) {
                            restError = "Oshxona nomi, login va parol to‘ldirilishi shart"
                            return@Button
                        }
                        scope.launch {
                            isSubmitting = true
                            restError = null
                            val res = authViewModel.registerFoodsSeller(
                                login = restLogin.trim(),
                                password = restPassword.trim(),
                                storeName = restName.trim(),
                                ownerName = restOwner.trim().ifBlank { restName.trim() },
                                phone = restPhone.trim(),
                                address = restAddress.trim()
                            )
                            isSubmitting = false
                            res.fold(
                                onSuccess = {
                                    showAddRestaurantDialog = false
                                },
                                onFailure = {
                                    restError = it.message ?: "Xatolik yuz berdi"
                                }
                            )
                        }
                    },
                    enabled = !isSubmitting,
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAmber)
                ) {
                    Text(if (isSubmitting) "Saqlanmoqda..." else "Qo‘shish")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddRestaurantDialog = false }) {
                    Text("Bekor qilish")
                }
            }
        )
    }
}
