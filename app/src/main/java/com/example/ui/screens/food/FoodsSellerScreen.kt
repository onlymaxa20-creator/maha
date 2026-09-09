package com.example.ui.screens.food

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.data.local.entity.FoodCategoryEntity
import com.example.data.local.entity.FoodOrderEntity
import com.example.data.local.entity.FoodProductEntity
import com.example.data.local.entity.UserEntity
import com.example.data.util.ImageStorageHelper
import com.example.ui.components.map.OrderLocationSection
import com.example.ui.theme.CardSurface
import com.example.ui.theme.LightBackground
import com.example.ui.theme.OrangeAmber
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SecondaryNavy
import com.example.ui.theme.SlateGray
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodels.AuthViewModel
import com.example.ui.viewmodels.FoodSellerViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodsSellerScreen(
    sellerUser: UserEntity,
    foodSellerViewModel: FoodSellerViewModel,
    authViewModel: AuthViewModel,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(sellerUser) {
        foodSellerViewModel.setSellerUser(sellerUser)
    }

    val restaurant by foodSellerViewModel.myRestaurant.collectAsState()
    val products by foodSellerViewModel.myProducts.collectAsState()
    val orders by foodSellerViewModel.myOrders.collectAsState()
    val stats by foodSellerViewModel.dashboardStats.collectAsState()
    val categories by foodSellerViewModel.categories.collectAsState()
    val isSubmitting by foodSellerViewModel.isSubmitting.collectAsState()
    val actionSuccess by foodSellerViewModel.actionSuccess.collectAsState()
    val actionError by foodSellerViewModel.actionError.collectAsState()

    LaunchedEffect(actionSuccess) {
        actionSuccess?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            foodSellerViewModel.clearMessages()
        }
    }

    LaunchedEffect(actionError) {
        actionError?.let { err ->
            snackbarHostState.showSnackbar(err)
            foodSellerViewModel.clearMessages()
        }
    }

    var currentTab by remember { mutableIntStateOf(0) } // 0: Stats, 1: Menu, 2: Orders, 3: Settings
    var showAddProductDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<FoodProductEntity?>(null) }
    var productToDelete by remember { mutableStateOf<FoodProductEntity?>(null) }
    var rejectingOrder by remember { mutableStateOf<FoodOrderEntity?>(null) }
    var rejectionReasonInput by remember { mutableStateOf("") }

    BackHandler {
        if (rejectingOrder != null) {
            rejectingOrder = null
        } else if (productToDelete != null) {
            productToDelete = null
        } else if (editingProduct != null) {
            editingProduct = null
        } else if (showAddProductDialog) {
            showAddProductDialog = false
        } else if (currentTab != 0) {
            currentTab = 0
        } else {
            onLogout()
        }
    }

    val formatter = remember { NumberFormat.getNumberInstance(Locale("uz", "UZ")) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = restaurant?.name ?: sellerUser.storeName.ifBlank { "Fast Food Oshxonasi" },
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            maxLines = 1
                        )
                        Text(
                            text = if (restaurant?.isOpen == true) "🟢 Oshxona Ochiq" else "🔴 Oshxona Yopiq",
                            fontSize = 12.sp,
                            color = if (restaurant?.isOpen == true) SuccessGreen else Color.Red
                        )
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
                    label = { Text("Ko‘rsatkichlar", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = OrangeAmber,
                        selectedTextColor = OrangeAmber,
                        indicatorColor = Color(0xFFFFF3E0)
                    )
                )
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    icon = { Icon(Icons.Default.Fastfood, contentDescription = "Menyu") },
                    label = { Text("Menyu (${products.size})", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = OrangeAmber,
                        selectedTextColor = OrangeAmber,
                        indicatorColor = Color(0xFFFFF3E0)
                    )
                )
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 },
                    icon = { Icon(Icons.Default.ListAlt, contentDescription = "Buyurtmalar") },
                    label = { Text("Buyurtmalar (${orders.size})", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = OrangeAmber,
                        selectedTextColor = OrangeAmber,
                        indicatorColor = Color(0xFFFFF3E0)
                    )
                )
                NavigationBarItem(
                    selected = currentTab == 3,
                    onClick = { currentTab = 3 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Sozlamalar") },
                    label = { Text("Sozlamalar", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = OrangeAmber,
                        selectedTextColor = OrangeAmber,
                        indicatorColor = Color(0xFFFFF3E0)
                    )
                )
            }
        },
        floatingActionButton = {
            if (currentTab == 1) {
                FloatingActionButton(
                    onClick = { showAddProductDialog = true },
                    containerColor = OrangeAmber,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Yangi taom qo‘shish")
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
                    // STATS TAB
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = OrangeAmber),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text("Jami Tushum (Yetkazilgan)", color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp)
                                Text(
                                    "${formatter.format(stats.totalRevenue.toLong())} so‘m",
                                    color = Color.White,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("To‘lov turi: 100% Naqd Pul (Yetkazilganda)", color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp)
                            }
                        }

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
                                    Text("Taomlar", fontSize = 12.sp, color = SlateGray)
                                    Text("${products.size}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = SecondaryNavy)
                                    Text("Faol: ${stats.activeProductsCount}", fontSize = 11.sp, color = SuccessGreen)
                                }
                            }
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CardSurface),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Kutilmoqda", fontSize = 12.sp, color = SlateGray)
                                    Text("${stats.pendingOrdersCount}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = OrangeAmber)
                                    Text("Jarayonda", fontSize = 11.sp, color = OrangeAmber)
                                }
                            }
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = CardSurface),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Yetkazilgan Buyurtmalar", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SecondaryNavy)
                                    Text("Muvaffaqiyatli yetkazilgan jami taomlar", fontSize = 12.sp, color = SlateGray)
                                }
                                Text("${stats.completedOrdersCount}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                            }
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = CardSurface),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Oshxona Holati", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SecondaryNavy)
                                    Text("Mijozlar buyurtma berishi uchun ochiq qoldiring", fontSize = 12.sp, color = SlateGray)
                                }
                                Switch(
                                    checked = restaurant?.isOpen ?: true,
                                    onCheckedChange = { foodSellerViewModel.toggleStoreOpen(it) },
                                    colors = SwitchDefaults.colors(checkedThumbColor = OrangeAmber, checkedTrackColor = Color(0xFFFFF3E0))
                                )
                            }
                        }
                    }
                }

                1 -> {
                    // MENU MANAGEMENT TAB
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Header with summary and add button
                        Surface(
                            color = CardSurface,
                            tonalElevation = 1.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Oshxona Menyusi",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = SecondaryNavy
                                    )
                                    Text(
                                        text = "${products.size} ta taom kiritilgan",
                                        fontSize = 12.sp,
                                        color = SlateGray
                                    )
                                }

                                Button(
                                    onClick = { showAddProductDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAmber),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Taom Qo‘shish", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        if (products.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("🍲", fontSize = 52.sp)
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text(
                                        "Menyuda hali taomlar yo‘q",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SecondaryNavy
                                    )
                                    Text(
                                        "Bozor sotuvchisidek tez va oson yangi taom qo‘shing",
                                        fontSize = 13.sp,
                                        color = SlateGray,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = { showAddProductDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = OrangeAmber),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("+ Yangi taom qo‘shish", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(products, key = { it.id }) { product ->
                                    val resolvedModel = ImageStorageHelper.resolveImageModel(product.imageUri, context) ?: product.imageUri

                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = CardSurface),
                                        shape = RoundedCornerShape(14.dp),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (product.imageUri.isNotBlank()) {
                                                SubcomposeAsyncImage(
                                                    model = resolvedModel,
                                                    contentDescription = product.name,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier
                                                        .size(76.dp)
                                                        .clip(RoundedCornerShape(10.dp)),
                                                    loading = {
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxSize()
                                                                .background(Color(0xFFFFF3E0)),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            CircularProgressIndicator(
                                                                color = OrangeAmber,
                                                                modifier = Modifier.size(20.dp),
                                                                strokeWidth = 2.dp
                                                            )
                                                        }
                                                    },
                                                    error = {
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxSize()
                                                                .background(Color(0xFFFFF3E0)),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text("🍲", fontSize = 28.sp)
                                                        }
                                                    }
                                                )
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .size(76.dp)
                                                        .background(Color(0xFFFFF3E0), RoundedCornerShape(10.dp)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text("🍲", fontSize = 32.sp)
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(12.dp))

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = product.name,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp,
                                                    color = SecondaryNavy
                                                )
                                                Text(
                                                    text = "${formatter.format(product.price.toLong())} so‘m",
                                                    color = OrangeAmber,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                )
                                                Text(
                                                    text = "${product.categoryName} • ⏱️ ${product.preparationTime}",
                                                    fontSize = 11.sp,
                                                    color = SlateGray
                                                )
                                                if (product.ingredients.isNotBlank()) {
                                                    Text(
                                                        text = "Tarkibi: ${product.ingredients}",
                                                        fontSize = 11.sp,
                                                        color = SlateGray,
                                                        maxLines = 1
                                                    )
                                                }
                                                if (product.status == "REJECTED") {
                                                    Text(
                                                        text = "Rad etilgan: ${product.rejectionReason}",
                                                        color = Color.Red,
                                                        fontSize = 11.sp
                                                    )
                                                }
                                            }

                                            Column(horizontalAlignment = Alignment.End) {
                                                Switch(
                                                    checked = product.isAvailable,
                                                    onCheckedChange = { foodSellerViewModel.toggleProductAvailability(product.id, it) },
                                                    colors = SwitchDefaults.colors(checkedThumbColor = OrangeAmber)
                                                )
                                                Row {
                                                    IconButton(onClick = { editingProduct = product }) {
                                                        Icon(Icons.Default.Edit, contentDescription = "Tahrirlash", tint = SlateGray, modifier = Modifier.size(18.dp))
                                                    }
                                                    IconButton(onClick = { productToDelete = product }) {
                                                        Icon(Icons.Default.Delete, contentDescription = "O‘chirish", tint = Color(0xFFD32F2F), modifier = Modifier.size(18.dp))
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

                2 -> {
                    // ORDERS TAB
                    if (orders.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("📦", fontSize = 48.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    "Hozircha buyurtmalar yo‘q",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SecondaryNavy
                                )
                                Text(
                                    "Mijozlar buyurtma berganda bu yerda real vaqtda paydo bo‘ladi",
                                    fontSize = 13.sp,
                                    color = SlateGray,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(orders, key = { it.id }) { order ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = CardSurface),
                                    shape = RoundedCornerShape(14.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Buyurtma #${order.id}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = SecondaryNavy
                                            )
                                            val statusColor = when (order.status) {
                                                "YANGI" -> OrangeAmber
                                                "QABUL_QILINDI" -> PrimaryBlue
                                                "TAYYORLANMOQDA" -> Color(0xFF9C27B0)
                                                "YETKAZILMOQDA" -> Color(0xFF009688)
                                                "YETKAZILDI" -> SuccessGreen
                                                else -> Color.Red
                                            }
                                            Surface(
                                                color = statusColor.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = order.status,
                                                    color = statusColor,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = order.itemsSummary,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp,
                                            color = SecondaryNavy
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Mijoz: ${order.customerName} (${order.customerPhone})",
                                            fontSize = 12.sp,
                                            color = SlateGray
                                        )
                                        Text(
                                            text = "Manzil: ${order.deliveryAddress}",
                                            fontSize = 12.sp,
                                            color = SlateGray
                                        )
                                        if (order.customerNote.isNotBlank()) {
                                            Text(
                                                text = "Izoh: ${order.customerNote}",
                                                fontSize = 12.sp,
                                                color = OrangeAmber
                                            )
                                        }

                                        if (order.deliveryLatitude != 0.0 && order.deliveryLongitude != 0.0) {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            OrderLocationSection(
                                                customerName = order.customerName,
                                                customerPhone = order.customerPhone,
                                                deliveryAddress = order.deliveryAddress,
                                                latitude = order.deliveryLatitude,
                                                longitude = order.deliveryLongitude,
                                                street = order.deliveryStreet,
                                                houseNumber = order.deliveryHouseNumber,
                                                landmark = if (order.deliveryLandmark.isNotBlank()) order.deliveryLandmark else order.customerNote
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Jami: ${formatter.format(order.totalPrice.toLong())} so‘m",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = OrangeAmber
                                            )
                                            Text(
                                                text = "Yetkazish: BEPUL",
                                                fontSize = 11.sp,
                                                color = SuccessGreen,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            when (order.status) {
                                                "YANGI" -> {
                                                    Button(
                                                        onClick = { foodSellerViewModel.updateOrderStatus(order.id, "QABUL_QILINDI") },
                                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                                        shape = RoundedCornerShape(8.dp),
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Text("✅ Qabul qilish", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                    OutlinedButton(
                                                        onClick = { rejectingOrder = order },
                                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                                                        shape = RoundedCornerShape(8.dp)
                                                    ) {
                                                        Text("❌ Rad etish", fontSize = 12.sp)
                                                    }
                                                }
                                                "QABUL_QILINDI" -> {
                                                    Button(
                                                        onClick = { foodSellerViewModel.updateOrderStatus(order.id, "TAYYORLANMOQDA") },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0)),
                                                        shape = RoundedCornerShape(8.dp),
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Text("🍳 Tayyorlanmoqda", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                                "TAYYORLANMOQDA" -> {
                                                    Button(
                                                        onClick = { foodSellerViewModel.updateOrderStatus(order.id, "YETKAZILMOQDA") },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009688)),
                                                        shape = RoundedCornerShape(8.dp),
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Text("🛵 Yetkazilmoqda (Kuryerga berildi)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                                "YETKAZILMOQDA" -> {
                                                    Button(
                                                        onClick = { foodSellerViewModel.updateOrderStatus(order.id, "YETKAZILDI") },
                                                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                                        shape = RoundedCornerShape(8.dp),
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Text("🎉 Yetkazildi (Yakunlash)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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

                3 -> {
                    // SETTINGS TAB
                    var storeName by remember(restaurant) { mutableStateOf(restaurant?.name ?: sellerUser.storeName) }
                    var ownerName by remember(restaurant) { mutableStateOf(restaurant?.ownerName ?: sellerUser.fullName) }
                    var phone by remember(restaurant) { mutableStateOf(restaurant?.phone ?: sellerUser.phone) }
                    var address by remember(restaurant) { mutableStateOf(restaurant?.address ?: sellerUser.savedAddress) }
                    var workingHours by remember(restaurant) { mutableStateOf(restaurant?.workingHours ?: "09:00 - 23:00") }
                    var category by remember(restaurant) { mutableStateOf(restaurant?.category ?: "Fast Food & Milliy") }
                    var description by remember(restaurant) { mutableStateOf(restaurant?.description ?: "") }
                    var coverUri by remember(restaurant) { mutableStateOf(restaurant?.coverUri ?: "") }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Oshxona Sozlamalari",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = SecondaryNavy
                        )

                        OutlinedTextField(
                            value = storeName,
                            onValueChange = { storeName = it },
                            label = { Text("Oshxona Nomi") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = ownerName,
                            onValueChange = { ownerName = it },
                            label = { Text("Egasi Ismi") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Telefon Raqami") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("Manzil") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = workingHours,
                            onValueChange = { workingHours = it },
                            label = { Text("Ish Vaqti") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Qisqacha Tavsif") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = coverUri,
                            onValueChange = { coverUri = it },
                            label = { Text("Muqova Rasm Havolasi (URL)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                foodSellerViewModel.saveRestaurantProfile(
                                    name = storeName,
                                    ownerName = ownerName,
                                    phone = phone,
                                    address = address,
                                    workingHours = workingHours,
                                    category = category,
                                    description = description,
                                    coverUri = coverUri
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OrangeAmber),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text("Ma'lumotlarni Saqlash", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // ADD PRODUCT DIALOG
    if (showAddProductDialog) {
        AddEditFoodProductDialog(
            product = null,
            categories = categories,
            isSubmitting = isSubmitting,
            onDismiss = { showAddProductDialog = false },
            onSave = { name, categoryName, price, desc, img, prepTime, ingredients, isAvail ->
                foodSellerViewModel.addProduct(
                    name = name,
                    categoryName = categoryName,
                    price = price,
                    description = desc,
                    imageUri = img,
                    preparationTime = prepTime,
                    ingredients = ingredients,
                    isAvailable = isAvail,
                    onSuccess = {
                        showAddProductDialog = false
                    }
                )
            }
        )
    }

    // EDIT PRODUCT DIALOG
    if (editingProduct != null) {
        AddEditFoodProductDialog(
            product = editingProduct,
            categories = categories,
            isSubmitting = isSubmitting,
            onDismiss = { editingProduct = null },
            onSave = { name, categoryName, price, desc, img, prepTime, ingredients, isAvail ->
                val updated = editingProduct!!.copy(
                    name = name,
                    categoryName = categoryName,
                    price = price,
                    description = desc,
                    imageUri = img,
                    preparationTime = prepTime,
                    ingredients = ingredients,
                    isAvailable = isAvail
                )
                foodSellerViewModel.updateProduct(
                    product = updated,
                    onSuccess = {
                        editingProduct = null
                    }
                )
            }
        )
    }

    // DELETE CONFIRMATION DIALOG
    if (productToDelete != null) {
        val prod = productToDelete!!
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text("Taomni o‘chirish", fontWeight = FontWeight.Bold) },
            text = { Text("Haqiqatan ham \"${prod.name}\" taomini menyudan o‘chirmoqchimisiz?") },
            confirmButton = {
                Button(
                    onClick = {
                        foodSellerViewModel.deleteProduct(prod.id)
                        productToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("O‘chirish")
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) {
                    Text("Bekor qilish")
                }
            }
        )
    }

    // REJECT ORDER DIALOG
    if (rejectingOrder != null) {
        val order = rejectingOrder!!
        AlertDialog(
            onDismissRequest = { rejectingOrder = null },
            title = { Text("Buyurtmani Bekor Qilish", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Buyurtma #${order.id} ni bekor qilish sababini kiriting:")
                    OutlinedTextField(
                        value = rejectionReasonInput,
                        onValueChange = { rejectionReasonInput = it },
                        label = { Text("Bekor qilish sababi") },
                        placeholder = { Text("Masalan: Masalliq tugagan yoki yetkazish imkonsiz") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        foodSellerViewModel.rejectOrder(order.id, rejectionReasonInput.ifBlank { "Oshxona tomonidan bekor qilindi" })
                        rejectingOrder = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Bekor qilish")
                }
            },
            dismissButton = {
                TextButton(onClick = { rejectingOrder = null }) {
                    Text("Yopish")
                }
            }
        )
    }
}

/**
 * Bozor sotuvchisidek qulay, to'liq funksiyali va Google/Galereya rasmlarini qo'llab-quvvatlovchi dialog
 */
@Composable
fun AddEditFoodProductDialog(
    product: FoodProductEntity?,
    categories: List<FoodCategoryEntity>,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        categoryName: String,
        price: Double,
        description: String,
        imageUri: String,
        preparationTime: String,
        ingredients: String,
        isAvailable: Boolean
    ) -> Unit
) {
    val context = LocalContext.current

    var name by remember { mutableStateOf(product?.name ?: "") }
    var priceText by remember { mutableStateOf(if (product != null) "${product.price.toLong()}" else "") }
    var prepTime by remember { mutableStateOf(product?.preparationTime ?: "15-20 daqiqa") }
    var ingredients by remember { mutableStateOf(product?.ingredients ?: "") }
    var description by remember { mutableStateOf(product?.description ?: "") }
    var imageUri by remember { mutableStateOf(product?.imageUri ?: "") }
    var isAvailable by remember { mutableStateOf(product?.isAvailable ?: true) }

    val defaultFastFoodCategories = listOf(
        "Burger & Lavash",
        "Pitsa & Pishiriqlar",
        "Milliy Taomlar",
        "Shashlik & Gril",
        "Sho‘rvalar",
        "Salatlar",
        "Ichimliklar",
        "Desert & Shirinliklar",
        "Fast Food"
    )

    val availableCategoryNames = remember(categories) {
        val dbCats = categories.map { it.name }
        (dbCats + defaultFastFoodCategories).distinct()
    }

    var selectedCategoryName by remember {
        mutableStateOf(product?.categoryName ?: availableCategoryNames.firstOrNull() ?: "Fast Food")
    }

    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showUrlInput by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val savedPath = ImageStorageHelper.compressAndEncodeImageToBase64(context, uri) ?: ImageStorageHelper.saveImageFromUri(context, uri)
            if (savedPath != null) {
                imageUri = savedPath
                errorText = null
            }
        }
    }

    val foodPresets = listOf(
        "Burger" to "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=600&auto=format&fit=crop",
        "Lavash" to "https://images.unsplash.com/photo-1626700051175-6818013e1d4f?w=600&auto=format&fit=crop",
        "Pitsa" to "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=600&auto=format&fit=crop",
        "Shashlik" to "https://images.unsplash.com/photo-1555939594-58d7cb561ad1?w=600&auto=format&fit=crop",
        "Palov / Osh" to "https://images.unsplash.com/photo-1633964913295-ceb43826e7c9?w=600&auto=format&fit=crop",
        "Somsa" to "https://images.unsplash.com/photo-1601050690597-df0568f70950?w=600&auto=format&fit=crop",
        "Gril tovuq" to "https://images.unsplash.com/photo-1626082927389-6cd097cdc6ec?w=600&auto=format&fit=crop",
        "Lag‘mon" to "https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=600&auto=format&fit=crop",
        "Sho‘rva" to "https://images.unsplash.com/photo-1547592166-23ac45744acd?w=600&auto=format&fit=crop",
        "Manti" to "https://images.unsplash.com/photo-1541696432-82c6da8ce7bf?w=600&auto=format&fit=crop",
        "Qozon kabob" to "https://images.unsplash.com/photo-1544025162-d76694265947?w=600&auto=format&fit=crop",
        "Salat" to "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=600&auto=format&fit=crop",
        "Ichimlik" to "https://images.unsplash.com/photo-1581009146145-b5ef050c2e1e?w=600&auto=format&fit=crop",
        "Shirinlik" to "https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=600&auto=format&fit=crop",
        "Tandir non" to "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=600&auto=format&fit=crop"
    )

    val prepTimePresets = listOf("10-15 daqiqa", "15-20 daqiqa", "20-30 daqiqa", "30-45 daqiqa")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (product == null) "Yangi Taom Qo‘shish" else "Taomni Tahrirlash",
                fontWeight = FontWeight.Bold,
                color = SecondaryNavy
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Taom nomi
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorText = null
                    },
                    label = { Text("Taom nomi *") },
                    placeholder = { Text("Masalan: Lavash pishloqli, Double Burger") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Kategoriya
                Text(text = "Kategoriya *", fontSize = 12.sp, color = SlateGray)
                Spacer(modifier = Modifier.height(4.dp))
                Box {
                    OutlinedButton(
                        onClick = { showCategoryDropdown = true },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = selectedCategoryName, color = SecondaryNavy, fontWeight = FontWeight.SemiBold)
                            Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = null)
                        }
                    }

                    DropdownMenu(
                        expanded = showCategoryDropdown,
                        onDismissRequest = { showCategoryDropdown = false }
                    ) {
                        availableCategoryNames.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    selectedCategoryName = cat
                                    showCategoryDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Narxi
                OutlinedTextField(
                    value = priceText,
                    onValueChange = {
                        priceText = it.filter { char -> char.isDigit() }
                        errorText = null
                    },
                    label = { Text("Narxi (so‘m) *") },
                    placeholder = { Text("Masalan: 35000") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Tayyorlash vaqti
                Text(text = "Tayyorlash vaqti", fontSize = 12.sp, color = SlateGray)
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(prepTimePresets) { preset ->
                        val isSelected = prepTime == preset
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) OrangeAmber else CardSurface,
                            border = BorderStroke(1.dp, if (isSelected) OrangeAmber else Color.LightGray),
                            modifier = Modifier.clickable { prepTime = preset }
                        ) {
                            Text(
                                text = preset,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else SecondaryNavy,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = prepTime,
                    onValueChange = { prepTime = it },
                    placeholder = { Text("Masalan: 15-20 daqiqa") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Tarkibi (Masalliqlar)
                OutlinedTextField(
                    value = ingredients,
                    onValueChange = { ingredients = it },
                    label = { Text("Tarkibi (Masalliqlar)") },
                    placeholder = { Text("Mol go‘shti, pishloq, bodring, maxsus sous...") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Rasm qismi
                Text(
                    text = "Taom Rasmi",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SecondaryNavy
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Image Preview Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF5F5F5))
                        .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri.isNotBlank()) {
                        val resolvedImg = ImageStorageHelper.resolveImageModel(imageUri, context) ?: imageUri
                        SubcomposeAsyncImage(
                            model = resolvedImg,
                            contentDescription = "Taom rasmi",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                            loading = {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = OrangeAmber, modifier = Modifier.size(24.dp))
                                }
                            },
                            error = {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Filled.Image, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(32.dp))
                                    Text("Rasm ochilmadi", fontSize = 11.sp, color = Color.Gray)
                                }
                            }
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PhotoLibrary,
                                contentDescription = null,
                                tint = SlateGray,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Rasm tanlanmagan",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = SlateGray
                            )
                            Text(
                                text = "Galereyadan yoki Google / Web havolasidan yuklang",
                                fontSize = 11.sp,
                                color = SlateGray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Galereya & Delete tugmalari
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = OrangeAmber),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Filled.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (imageUri.isBlank()) "Galereyadan tanlash" else "Boshqa rasm tanlash", fontSize = 12.sp)
                    }

                    if (imageUri.isNotBlank()) {
                        OutlinedButton(
                            onClick = { imageUri = "" },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                if (imageUri.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (imageUri.startsWith("data:image")) "✅ Rasm bulutga (barcha mijozlarga) sinxronlanadi"
                        else if (imageUri.startsWith("http")) "🔗 Google / Web rasm havolasi biriktirildi"
                        else "✅ Qurilma xotirasidan rasm biriktirildi",
                        fontSize = 11.sp,
                        color = SuccessGreen,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Google & Web URL / Namunalar paneli
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showUrlInput = !showUrlInput }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (showUrlInput) "▼ Namuna rasmlar va Google URL ni yopish" else "▶ Google / Web URL yoki tayyor taom rasmlari",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = SlateGray
                    )
                }

                if (showUrlInput) {
                    Spacer(modifier = Modifier.height(4.dp))

                    // Preset Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(foodPresets) { (presetName, url) ->
                            val isCurrent = imageUri == url
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isCurrent) OrangeAmber else CardSurface,
                                border = BorderStroke(1.dp, if (isCurrent) OrangeAmber else Color.LightGray),
                                modifier = Modifier.clickable { imageUri = url }
                            ) {
                                Text(
                                    text = presetName,
                                    fontSize = 11.sp,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isCurrent) Color.White else SecondaryNavy,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Google / Web URL Input
                    OutlinedTextField(
                        value = imageUri,
                        onValueChange = {
                            imageUri = ImageStorageHelper.normalizeImageUrl(it.trim())
                        },
                        label = { Text("Google yoki Web rasm havolasi (URL)") },
                        placeholder = { Text("https://...") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "💡 Maslahat: Google Rasmlar yoki saytlardan «Rasm manzilidan nusxa olish» (Copy image address) orqali havolani qo‘yishingiz mumkin.",
                        fontSize = 10.sp,
                        color = SlateGray,
                        lineHeight = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tavsif
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Taom tavsifi (ixtiyoriy)") },
                    placeholder = { Text("Mazzali va issiq holatda yetkaziladi...") },
                    shape = RoundedCornerShape(10.dp),
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Sotuvda faollik switchi
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Sotuvda mavjud:", fontSize = 13.sp, color = SecondaryNavy, fontWeight = FontWeight.Medium)
                    Switch(
                        checked = isAvailable,
                        onCheckedChange = { isAvailable = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = OrangeAmber)
                    )
                }

                if (errorText != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = errorText!!, color = Color(0xFFD32F2F), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val priceVal = priceText.toDoubleOrNull() ?: 0.0
                    if (name.trim().isEmpty()) {
                        errorText = "Taom nomini kiriting"
                    } else if (priceVal <= 0) {
                        errorText = "Taom narxi 0 dan katta bo‘lishi kerak"
                    } else {
                        onSave(
                            name.trim(),
                            selectedCategoryName.trim().ifBlank { "Fast Food" },
                            priceVal,
                            description.trim(),
                            imageUri.trim(),
                            prepTime.trim().ifBlank { "15-20 daqiqa" },
                            ingredients.trim(),
                            isAvailable
                        )
                    }
                },
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = OrangeAmber)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Saqlanmoqda...")
                } else {
                    Text(if (product == null) "Qo‘shish" else "Saqlash", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Bekor qilish")
            }
        }
    )
}
