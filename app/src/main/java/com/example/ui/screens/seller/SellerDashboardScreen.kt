package com.example.ui.screens.seller

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.Check
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import com.example.ui.components.map.OrderLocationSection
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.UserEntity
import com.example.data.model.SellerOrderDetail
import com.example.data.util.ImageStorageHelper
import com.example.ui.components.AppProductImage
import com.example.ui.components.EmptyStateView
import com.example.ui.components.Formatters
import com.example.ui.components.OrderStatusBadge
import com.example.ui.theme.BorderColor
import com.example.ui.theme.CardSurface
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DangerRedLight
import com.example.ui.theme.LightBackground
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueLight
import com.example.ui.theme.SecondaryNavy
import com.example.ui.theme.SlateGray
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessGreenLight
import com.example.ui.theme.SurfaceSubtle
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmberLight
import com.example.ui.viewmodels.AuthViewModel
import com.example.ui.viewmodels.SellerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerDashboardScreen(
    sellerUser: UserEntity,
    sellerViewModel: SellerViewModel,
    authViewModel: AuthViewModel,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(sellerUser) {
        sellerViewModel.setSeller(sellerUser.id, sellerUser.storeName.ifBlank { sellerUser.login })
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    val stats by sellerViewModel.dashboardStats.collectAsState()
    val products by sellerViewModel.myProducts.collectAsState()
    val orders by sellerViewModel.myOrders.collectAsState()
    val categories by sellerViewModel.categories.collectAsState()

    val actionError by sellerViewModel.actionError.collectAsState()
    val actionSuccess by sellerViewModel.actionSuccess.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(actionError, actionSuccess) {
        actionError?.let {
            snackbarHostState.showSnackbar(it)
            sellerViewModel.clearMessages()
        }
        actionSuccess?.let {
            snackbarHostState.showSnackbar(it)
            sellerViewModel.clearMessages()
        }
    }

    var showAddProductDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<ProductEntity?>(null) }
    var selectedOrderForDetail by remember { mutableStateOf<SellerOrderDetail?>(null) }

    BackHandler {
        if (selectedOrderForDetail != null) {
            selectedOrderForDetail = null
        } else if (productToEdit != null) {
            productToEdit = null
        } else if (showAddProductDialog) {
            showAddProductDialog = false
        } else if (selectedTab != 0) {
            selectedTab = 0
        } else {
            onLogout()
        }
    }

    if (showAddProductDialog) {
        AddEditProductDialog(
            product = null,
            categories = categories,
            onDismiss = { showAddProductDialog = false },
            onSave = { name, desc, catId, catName, price, stock, unit, img, avail ->
                sellerViewModel.addProduct(name, desc, catId, catName, price, stock, unit, img, avail) {
                    showAddProductDialog = false
                }
            }
        )
    }

    if (productToEdit != null) {
        AddEditProductDialog(
            product = productToEdit,
            categories = categories,
            onDismiss = { productToEdit = null },
            onSave = { name, desc, catId, catName, price, stock, unit, img, avail ->
                val updated = productToEdit!!.copy(
                    name = name,
                    description = desc,
                    categoryId = catId,
                    categoryName = catName,
                    price = price,
                    stock = stock,
                    unit = unit,
                    imageUri = img,
                    isAvailable = avail
                )
                sellerViewModel.updateProduct(updated) {
                    productToEdit = null
                }
            }
        )
    }

    if (selectedOrderForDetail != null) {
        SellerOrderDetailDialog(
            detail = selectedOrderForDetail!!,
            onDismiss = { selectedOrderForDetail = null },
            onUpdateStatus = { newStatus ->
                sellerViewModel.updateOrderStatus(selectedOrderForDetail!!.order.id, newStatus) {
                    selectedOrderForDetail = null
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = sellerUser.storeName.ifBlank { "Sotuvchi paneli" },
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = SecondaryNavy
                        )
                        Text(
                            text = "Login: ${sellerUser.login} | Gagarin",
                            fontSize = 11.sp,
                            color = SlateGray
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            authViewModel.logout()
                            onLogout()
                        },
                        modifier = Modifier.testTag("seller_logout_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Chiqish",
                            tint = DangerRed
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CardSurface)
            )
        },
        floatingActionButton = {
            if (selectedTab == 1) {
                FloatingActionButton(
                    onClick = { showAddProductDialog = true },
                    containerColor = PrimaryBlue,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("fab_add_product")
                ) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = "Mahsulot qo‘shish")
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LightBackground)
                .padding(innerPadding)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = CardSurface,
                contentColor = PrimaryBlue
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Boshqaruv", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Mahsulotlar (${products.size})", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Buyurtmalar (${orders.size})", fontWeight = FontWeight.SemiBold) }
                )
            }

            when (selectedTab) {
                0 -> SellerOverviewTab(stats = stats, onGoToProducts = { selectedTab = 1 }, onGoToOrders = { selectedTab = 2 })
                1 -> SellerProductsTab(
                    products = products,
                    onEdit = { productToEdit = it },
                    onDelete = { sellerViewModel.deleteProduct(it) },
                    onToggleAvailability = { sellerViewModel.toggleAvailability(it) },
                    onAddProductClick = { showAddProductDialog = true }
                )
                2 -> SellerOrdersTab(
                    orders = orders,
                    onOrderClick = { selectedOrderForDetail = it }
                )
            }
        }
    }
}

@Composable
fun SellerOverviewTab(
    stats: com.example.data.model.SellerDashboardStats,
    onGoToProducts: () -> Unit,
    onGoToOrders: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Do‘kon statistikasi",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = SecondaryNavy
        )
        Text(
            text = "Barcha ma’lumotlar bazadagi real holatga asoslangan",
            fontSize = 12.sp,
            color = SlateGray
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 4 real metric cards
        Row(modifier = Modifier.fillMaxWidth()) {
            SellerStatCard(
                title = "Mahsulotlarim",
                value = "${stats.myProductsCount}",
                icon = Icons.Filled.ShoppingCart,
                color = PrimaryBlue,
                bgColor = PrimaryBlueLight,
                modifier = Modifier.weight(1f).clickable(onClick = onGoToProducts)
            )
            Spacer(modifier = Modifier.width(12.dp))
            SellerStatCard(
                title = "Jami buyurtmalar",
                value = "${stats.myOrdersCount}",
                icon = Icons.AutoMirrored.Filled.List,
                color = Color(0xFF6D28D9),
                bgColor = Color(0xFFEDE9FE),
                modifier = Modifier.weight(1f).clickable(onClick = onGoToOrders)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            SellerStatCard(
                title = "Faol buyurtmalar",
                value = "${stats.myActiveOrdersCount}",
                icon = Icons.Filled.Info,
                color = WarningAmber,
                bgColor = WarningAmberLight,
                modifier = Modifier.weight(1f).clickable(onClick = onGoToOrders)
            )
            Spacer(modifier = Modifier.width(12.dp))
            SellerStatCard(
                title = "Tugallangan daromad",
                value = Formatters.formatPrice(stats.myRevenue),
                icon = Icons.Filled.ShoppingCart,
                color = SuccessGreen,
                bgColor = SuccessGreenLight,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Quick Actions
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Tezkor amallar",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = SecondaryNavy
                )
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onGoToProducts,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Yangi mahsulot joylashtirish")
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onGoToOrders,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.List, contentDescription = null, tint = SecondaryNavy, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Kelib tushgan buyurtmalarni tekshirish", color = SecondaryNavy)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
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

@Composable
fun SellerStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(bgColor, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = title, fontSize = 11.sp, color = SlateGray)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = SecondaryNavy,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun SellerProductsTab(
    products: List<ProductEntity>,
    onEdit: (ProductEntity) -> Unit,
    onDelete: (ProductEntity) -> Unit,
    onToggleAvailability: (ProductEntity) -> Unit,
    onAddProductClick: () -> Unit
) {
    if (products.isEmpty()) {
        EmptyStateView(
            title = "Mahsulotlar hozircha mavjud emas.",
            description = "Siz hali birorta ham mahsulot qo‘shmadingiz. Mahsulot qo‘shish uchun pastdagi tugmani bosing.",
            icon = Icons.Filled.ShoppingCart,
            actionButtonText = "Birinchi mahsulotni qo‘shish",
            onActionClick = onAddProductClick,
            modifier = Modifier.fillMaxSize()
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(products, key = { it.id }) { product ->
                SellerProductItemCard(
                    product = product,
                    onEdit = { onEdit(product) },
                    onDelete = { onDelete(product) },
                    onToggleAvailability = { onToggleAvailability(product) }
                )
            }
            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }
}

@Composable
fun SellerProductItemCard(
    product: ProductEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleAvailability: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Image
                AppProductImage(
                    imageUri = product.imageUri,
                    contentDescription = product.name,
                    modifier = Modifier.size(64.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentScale = ContentScale.Crop,
                    iconSize = 24.dp
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = SecondaryNavy,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Kategoriya: ${product.categoryName}",
                        fontSize = 11.sp,
                        color = SlateGray
                    )
                    Text(
                        text = Formatters.formatPrice(product.price),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                    Text(
                        text = "Qoldiq: ${product.stock} ${product.unit}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (product.stock > 0) SuccessGreen else DangerRed
                    )
                }

                // Edit & Delete
                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Filled.Edit, contentDescription = "Tahrirlash", tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Filled.Delete, contentDescription = "O‘chirish", tint = DangerRed, modifier = Modifier.size(18.dp))
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = BorderColor)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (product.isAvailable) "Sotuvda mavjud" else "Sotuvdan olingan",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (product.isAvailable) SuccessGreen else SlateGray
                )
                Switch(
                    checked = product.isAvailable,
                    onCheckedChange = { onToggleAvailability() },
                    colors = SwitchDefaults.colors(checkedThumbColor = PrimaryBlue)
                )
            }
        }
    }
}

@Composable
fun SellerOrdersTab(
    orders: List<SellerOrderDetail>,
    onOrderClick: (SellerOrderDetail) -> Unit
) {
    if (orders.isEmpty()) {
        EmptyStateView(
            title = "Buyurtmalar hozircha mavjud emas.",
            description = "Xaridorlar sizning mahsulotlaringizni xarid qilganda buyurtmalar bu yerda paydo bo‘ladi.",
            icon = Icons.AutoMirrored.Filled.List,
            modifier = Modifier.fillMaxSize()
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(orders, key = { it.order.id }) { detail ->
                SellerOrderCard(
                    detail = detail,
                    onClick = { onOrderClick(detail) }
                )
            }
        }
    }
}

@Composable
fun SellerOrderCard(
    detail: SellerOrderDetail,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val order = detail.order
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.orderNumber,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = PrimaryBlue
                )
                OrderStatusBadge(status = order.status)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Xaridor: ${order.customerName} (${order.customerPhone})",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = SecondaryNavy
            )

            Text(
                text = "Manzil: ${order.deliveryAddress}",
                fontSize = 12.sp,
                color = SlateGray
            )

            Text(
                text = "Sana: ${Formatters.formatDate(order.createdAt)}",
                fontSize = 11.sp,
                color = SlateGray
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = BorderColor)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${detail.sellerItems.size} turdagi mahsulotingiz",
                    fontSize = 12.sp,
                    color = SlateGray
                )
                Text(
                    text = Formatters.formatPrice(detail.sellerTotal),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue
                )
            }
        }
    }
}

@Composable
fun SellerOrderDetailDialog(
    detail: SellerOrderDetail,
    onDismiss: () -> Unit,
    onUpdateStatus: (newStatus: String) -> Unit
) {
    val order = detail.order
    var showStatusDropdown by remember { mutableStateOf(false) }
    val statuses = listOf("Yangi", "Tayyorlanmoqda", "Yetkazilmoqda", "Yetkazildi", "Bekor qilindi")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(text = "Buyurtma: ${order.orderNumber}", fontWeight = FontWeight.Bold, color = SecondaryNavy)
                Text(text = Formatters.formatDate(order.createdAt), fontSize = 12.sp, color = SlateGray)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Current status and updater
                Text(text = "Buyurtma holatini yangilash:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SecondaryNavy)
                Spacer(modifier = Modifier.height(4.dp))
                Box {
                    OutlinedButton(
                        onClick = { showStatusDropdown = true },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OrderStatusBadge(status = order.status)
                            Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = null)
                        }
                    }

                    DropdownMenu(
                        expanded = showStatusDropdown,
                        onDismissRequest = { showStatusDropdown = false }
                    ) {
                        statuses.forEach { statusOption ->
                            DropdownMenuItem(
                                text = { Text(statusOption) },
                                onClick = {
                                    showStatusDropdown = false
                                    onUpdateStatus(statusOption)
                                }
                            )
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = BorderColor)

                Text(text = "Xaridor va yetkazish ma’lumotlari:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SecondaryNavy)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Ismi: ${order.customerName}", fontSize = 12.sp, color = SecondaryNavy)
                Text(text = "Telefon: ${order.customerPhone}", fontSize = 12.sp, color = PrimaryBlue, fontWeight = FontWeight.SemiBold)
                if (order.deliveryAddress.isNotBlank()) {
                    Text(text = "Manzil: ${order.deliveryAddress}", fontSize = 12.sp, color = SlateGray)
                }
                if (order.deliveryNotes.isNotBlank()) {
                    Text(text = "Xaridor izohi: ${order.deliveryNotes}", fontSize = 12.sp, color = SlateGray)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Real Interactive Map and Turn-by-Turn Navigation Section
                OrderLocationSection(
                    customerName = order.customerName,
                    customerPhone = order.customerPhone,
                    deliveryAddress = order.deliveryAddress,
                    latitude = order.deliveryLatitude,
                    longitude = order.deliveryLongitude,
                    street = order.deliveryStreet,
                    houseNumber = order.deliveryHouseNumber,
                    landmark = if (order.deliveryLandmark.isNotBlank()) order.deliveryLandmark else order.deliveryNotes
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = BorderColor)

                Text(text = "Sizning mahsulotlaringiz:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SecondaryNavy)
                Spacer(modifier = Modifier.height(6.dp))

                detail.sellerItems.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = item.productName, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = SecondaryNavy)
                            Text(text = "${item.quantity} ${item.unit} × ${Formatters.formatPrice(item.unitPrice)}", fontSize = 11.sp, color = SlateGray)
                        }
                        Text(text = Formatters.formatPrice(item.itemTotal), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = PrimaryBlue)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = BorderColor)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Sizning ulushingiz jami:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SecondaryNavy)
                    Text(text = Formatters.formatPrice(detail.sellerTotal), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Yopish")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun AddEditProductDialog(
    product: ProductEntity?,
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        desc: String,
        categoryId: Long,
        categoryName: String,
        price: Double,
        stock: Int,
        unit: String,
        imageUri: String,
        isAvailable: Boolean
    ) -> Unit
) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var desc by remember { mutableStateOf(product?.description ?: "") }
    var priceText by remember { mutableStateOf(if (product != null) "${product.price.toLong()}" else "") }
    var stockText by remember { mutableStateOf(if (product != null) "${product.stock}" else "10") }
    var unit by remember { mutableStateOf(product?.unit ?: "dona") }
    var imageUri by remember { mutableStateOf(product?.imageUri ?: "") }
    var isAvailable by remember { mutableStateOf(product?.isAvailable ?: true) }

    var selectedCategoryId by remember {
        mutableStateOf(product?.categoryId ?: categories.firstOrNull()?.id ?: 1L)
    }
    var selectedCategoryName by remember {
        mutableStateOf(product?.categoryName ?: categories.firstOrNull()?.name ?: "Boshqa")
    }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showUrlInput by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (product == null) "Yangi mahsulot qo‘shish" else "Mahsulotni tahrirlash",
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
                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorText = null
                    },
                    label = { Text("Mahsulot nomi *") },
                    placeholder = { Text("Masalan: Samarqand noni") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Category selector
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
                            Text(text = selectedCategoryName, color = SecondaryNavy)
                            Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = null)
                        }
                    }

                    DropdownMenu(
                        expanded = showCategoryDropdown,
                        onDismissRequest = { showCategoryDropdown = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name) },
                                onClick = {
                                    selectedCategoryId = cat.id
                                    selectedCategoryName = cat.name
                                    showCategoryDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Price and Unit
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = {
                            priceText = it.filter { char -> char.isDigit() }
                            errorText = null
                        },
                        label = { Text("Narxi (so‘m) *") },
                        placeholder = { Text("50000") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.3f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Birligi") },
                        placeholder = { Text("dona / kg / litr") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Stock
                OutlinedTextField(
                    value = stockText,
                    onValueChange = {
                        stockText = it.filter { char -> char.isDigit() }
                        errorText = null
                    },
                    label = { Text("Mavjud soni (ombor qoldig‘i) *") },
                    placeholder = { Text("10") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Image URI and Local File Picker
                Text(
                    text = "Mahsulot rasmi",
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
                        .background(SurfaceSubtle)
                        .border(1.dp, BorderColor, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri.isNotBlank()) {
                        AppProductImage(
                            imageUri = imageUri,
                            contentDescription = "Rasm ko‘rinishi",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            iconSize = 32.dp
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
                                text = "Galereyadan yoki fayllardan rasm yuklang",
                                fontSize = 11.sp,
                                color = SlateGray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // File Picker Primary Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
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
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                if (imageUri.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (imageUri.startsWith("file://")) "✅ Qurilma xotirasidan rasm biriktirildi (Oflayn ishlaydi)" else "🔗 Rasm havolasi biriktirilgan",
                        fontSize = 11.sp,
                        color = if (imageUri.startsWith("file://")) SuccessGreen else PrimaryBlue,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Toggle for URL or preset images
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showUrlInput = !showUrlInput }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (showUrlInput) "▼ Namuna rasmlar va URL havolasini yopish" else "▶ Yoki tayyor namunalar / URL havolasi",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = SlateGray
                    )
                }

                if (showUrlInput) {
                    Spacer(modifier = Modifier.height(4.dp))

                    // Preset Image Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val presets = listOf(
                            "Non / Somsa" to "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=600&auto=format&fit=crop&q=80",
                            "Mevalar" to "https://images.unsplash.com/photo-1619566636858-adf3ef46400b?w=600&auto=format&fit=crop&q=80",
                            "Sabzavotlar" to "https://images.unsplash.com/photo-1540420773420-3366772f4999?w=600&auto=format&fit=crop&q=80",
                            "Go‘sht" to "https://images.unsplash.com/photo-1607623814075-e51df1bdc82f?w=600&auto=format&fit=crop&q=80",
                            "Sut / Qatiq" to "https://images.unsplash.com/photo-1550583724-b2692b85b150?w=600&auto=format&fit=crop&q=80",
                            "Ichimliklar" to "https://images.unsplash.com/photo-1551024709-8f23befc6f87?w=600&auto=format&fit=crop&q=80",
                            "Choy / Qahva" to "https://images.unsplash.com/photo-1576092768241-dec231879fc3?w=600&auto=format&fit=crop&q=80",
                            "Shirinliklar" to "https://images.unsplash.com/photo-1587314168485-3236d6710814?w=600&auto=format&fit=crop&q=80",
                            "Guruch / Don" to "https://images.unsplash.com/photo-1586201375761-83865001e31c?w=600&auto=format&fit=crop&q=80",
                            "Kiyim-kechak" to "https://images.unsplash.com/photo-1523381210434-271e8be1f52b?w=600&auto=format&fit=crop&q=80",
                            "Telefon / Gadjet" to "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=600&auto=format&fit=crop&q=80",
                            "Tozalik vositasi" to "https://images.unsplash.com/photo-1583947215259-38e31be8751f?w=600&auto=format&fit=crop&q=80"
                        )
                        items(presets) { (title, url) ->
                            val isCurrent = imageUri == url
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isCurrent) PrimaryBlue else CardSurface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isCurrent) PrimaryBlue else BorderColor),
                                modifier = Modifier
                                    .clickable { imageUri = url }
                            ) {
                                Text(
                                    text = title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isCurrent) Color.White else SecondaryNavy,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Image URI Input
                    OutlinedTextField(
                        value = imageUri,
                        onValueChange = { imageUri = it },
                        label = { Text("Rasm havolasi (URL)") },
                        placeholder = { Text("https://...") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Description
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Mahsulot tavsifi") },
                    placeholder = { Text("Sifatli, yangi...") },
                    shape = RoundedCornerShape(10.dp),
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Availability switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Sotuvda faol:", fontSize = 13.sp, color = SecondaryNavy)
                    Switch(
                        checked = isAvailable,
                        onCheckedChange = { isAvailable = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = PrimaryBlue)
                    )
                }

                if (errorText != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = errorText!!, color = DangerRed, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val priceVal = priceText.toDoubleOrNull() ?: 0.0
                    val stockVal = stockText.toIntOrNull() ?: 0

                    if (name.trim().isEmpty()) {
                        errorText = "Mahsulot nomini kiriting"
                    } else if (priceVal <= 0) {
                        errorText = "Mahsulot narxi 0 dan katta bo‘lishi kerak"
                    } else if (stockVal < 0) {
                        errorText = "Mavjud soni manfiy bo‘lishi mumkin emas"
                    } else {
                        onSave(
                            name.trim(),
                            desc.trim(),
                            selectedCategoryId,
                            selectedCategoryName,
                            priceVal,
                            stockVal,
                            unit.trim().ifBlank { "dona" },
                            imageUri.trim(),
                            isAvailable
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text(if (product == null) "Qo‘shish" else "Saqlash")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Bekor qilish")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
