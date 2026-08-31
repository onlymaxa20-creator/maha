package com.example.ui.screens.admin

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.BuildConfig
import com.example.ui.components.AppUpdateDialog
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SwitchAccessShortcut
import androidx.compose.material.icons.filled.ToggleOff
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.data.local.entity.AdminAuditLogEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.PromoBannerEntity
import com.example.data.local.entity.SupportInfoEntity
import com.example.data.local.entity.UserEntity
import com.example.data.model.AdminDashboardStats
import com.example.data.model.OrderDetail
import com.example.data.model.SellerRevenueStats
import com.example.data.util.ImageStorageHelper
import com.example.data.util.ImageValidationResult
import com.example.ui.components.AppProductImage
import com.example.ui.components.EmptyStateView
import com.example.ui.components.Formatters
import com.example.ui.components.GagarinGoLogoCompact
import com.example.ui.components.map.OrderLocationSection
import com.example.ui.components.OrderStatusBadge
import com.example.ui.components.getBannerBrush
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
import com.example.ui.viewmodels.AdminViewModel
import com.example.ui.viewmodels.AuthViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    adminUser: UserEntity,
    adminViewModel: AdminViewModel,
    authViewModel: AuthViewModel,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val stats by adminViewModel.dashboardStats.collectAsState()
    val sellersRevenueStats by adminViewModel.sellersRevenueStats.collectAsState()
    val auditLogs by adminViewModel.auditLogs.collectAsState()
    val customers by adminViewModel.customers.collectAsState()
    val sellers by adminViewModel.sellers.collectAsState()
    val allProducts by adminViewModel.allProducts.collectAsState()
    val allOrders by adminViewModel.allOrders.collectAsState()
    val categories by adminViewModel.categories.collectAsState()
    val allBanners by adminViewModel.allBanners.collectAsState()
    val supportInfo by adminViewModel.supportInfo.collectAsState()

    val adminError by adminViewModel.adminError.collectAsState()
    val adminSuccess by adminViewModel.adminSuccess.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(adminError, adminSuccess) {
        adminError?.let {
            snackbarHostState.showSnackbar(it)
            adminViewModel.clearMessages()
        }
        adminSuccess?.let {
            snackbarHostState.showSnackbar(it)
            adminViewModel.clearMessages()
        }
    }

    var showCreateSellerDialog by remember { mutableStateOf(false) }
    var sellerForPasswordReset by remember { mutableStateOf<UserEntity?>(null) }
    var selectedOrderDetail by remember { mutableStateOf<OrderDetail?>(null) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showChangeAdminPassDialog by remember { mutableStateOf(false) }
    var selectedCustomerForDetail by remember { mutableStateOf<UserEntity?>(null) }
    var selectedSellerForRevenueDetail by remember { mutableStateOf<SellerRevenueStats?>(null) }
    var showAddBannerDialog by remember { mutableStateOf(false) }
    var bannerToEdit by remember { mutableStateOf<PromoBannerEntity?>(null) }
    var sellerToDelete by remember { mutableStateOf<UserEntity?>(null) }

    // Intercept back button to navigate tabs, close dialogs, or exit admin dashboard smoothly
    BackHandler {
        if (sellerToDelete != null) {
            sellerToDelete = null
        } else if (selectedOrderDetail != null) {
            selectedOrderDetail = null
        } else if (selectedCustomerForDetail != null) {
            selectedCustomerForDetail = null
        } else if (selectedSellerForRevenueDetail != null) {
            selectedSellerForRevenueDetail = null
        } else if (sellerForPasswordReset != null) {
            sellerForPasswordReset = null
        } else if (bannerToEdit != null) {
            bannerToEdit = null
        } else if (showCreateSellerDialog) {
            showCreateSellerDialog = false
        } else if (showAddCategoryDialog) {
            showAddCategoryDialog = false
        } else if (showAddBannerDialog) {
            showAddBannerDialog = false
        } else if (showChangeAdminPassDialog) {
            showChangeAdminPassDialog = false
        } else if (selectedTab != 0) {
            selectedTab = 0
        } else {
            onLogout()
        }
    }

    if (sellerToDelete != null) {
        val targetSeller = sellerToDelete!!
        AlertDialog(
            onDismissRequest = { sellerToDelete = null },
            icon = {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = null,
                    tint = DangerRed,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Sotuvchi va mahsulotlarini o‘chirish",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = SecondaryNavy
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Haqiqatan ham «${targetSeller.storeName}» (${targetSeller.fullName}) sotuvchisini o‘chirmoqchimisiz?",
                        fontSize = 14.sp,
                        color = SecondaryNavy,
                        fontWeight = FontWeight.Medium
                    )
                    Surface(
                        color = DangerRedLight,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "⚠️ Diqqat: Ushbu sotuvchiga tegishli bo‘lgan barcha mahsulotlar va ma'lumotlar ham tizimdan butunlay o‘chiriladi!",
                            fontSize = 12.sp,
                            color = DangerRed,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        adminViewModel.deleteSeller(targetSeller.id)
                        sellerToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("Ha, to‘liq o‘chirish", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { sellerToDelete = null }) {
                    Text("Bekor qilish", color = SlateGray)
                }
            }
        )
    }

    // Dialogs
    if (showAddBannerDialog || bannerToEdit != null) {
        AddEditPromoBannerDialog(
            banner = bannerToEdit,
            onDismiss = {
                showAddBannerDialog = false
                bannerToEdit = null
            },
            onSave = { name, title, description, imageUrl, targetLink, startDate, endDate, badgeText, gradientType, isActive ->
                if (bannerToEdit == null) {
                    adminViewModel.addBanner(
                        name = name,
                        title = title,
                        description = description,
                        imageUrl = imageUrl,
                        targetLink = targetLink,
                        startDate = startDate,
                        endDate = endDate,
                        badgeText = badgeText,
                        gradientType = gradientType,
                        isActive = isActive
                    ) {
                        showAddBannerDialog = false
                    }
                } else {
                    adminViewModel.updateBanner(
                        bannerToEdit!!.copy(
                            name = name,
                            title = title,
                            description = description,
                            imageUrl = imageUrl,
                            targetLink = targetLink,
                            startDate = startDate,
                            endDate = endDate,
                            badgeText = badgeText,
                            gradientType = gradientType,
                            isActive = isActive
                        )
                    ) {
                        bannerToEdit = null
                    }
                }
            }
        )
    }

    if (showCreateSellerDialog) {
        CreateSellerDialog(
            onDismiss = { showCreateSellerDialog = false },
            onCreate = { login, pass, storeName, owner, phone, email, role, address ->
                adminViewModel.createSeller(login, pass, storeName, owner, phone, email, role, address) {
                    showCreateSellerDialog = false
                }
            }
        )
    }

    if (sellerForPasswordReset != null) {
        ResetSellerPasswordDialog(
            seller = sellerForPasswordReset!!,
            onDismiss = { sellerForPasswordReset = null },
            onSave = { newPass ->
                adminViewModel.updateSellerPassword(sellerForPasswordReset!!.id, newPass) {
                    sellerForPasswordReset = null
                }
            }
        )
    }

    if (selectedOrderDetail != null) {
        AdminOrderDetailDialog(
            orderDetail = selectedOrderDetail!!,
            onDismiss = { selectedOrderDetail = null },
            onUpdateStatus = { newStatus ->
                adminViewModel.updateOrderStatus(selectedOrderDetail!!.order.id, newStatus)
                selectedOrderDetail = null
            }
        )
    }

    if (showAddCategoryDialog) {
        AddCategoryDialog(
            onDismiss = { showAddCategoryDialog = false },
            onAdd = { catName ->
                adminViewModel.addCategory(catName) {
                    showAddCategoryDialog = false
                }
            }
        )
    }

    if (showChangeAdminPassDialog) {
        AdminChangePasswordDialog(
            onDismiss = { showChangeAdminPassDialog = false },
            onSave = { oldP, newP, confP ->
                adminViewModel.changeAdminPassword(oldP, newP, confP) {
                    showChangeAdminPassDialog = false
                }
            }
        )
    }

    if (selectedCustomerForDetail != null) {
        val customerOrders = allOrders.filter { it.order.customerId == selectedCustomerForDetail!!.id }
        CustomerDetailAdminDialog(
            customer = selectedCustomerForDetail!!,
            orders = customerOrders,
            onDismiss = { selectedCustomerForDetail = null }
        )
    }

    if (selectedSellerForRevenueDetail != null) {
        val sellerProducts = allProducts.filter { it.sellerId == selectedSellerForRevenueDetail!!.sellerId }
        val sellerOrders = allOrders.filter { orderDetail ->
            orderDetail.items.any { it.sellerId == selectedSellerForRevenueDetail!!.sellerId }
        }
        SellerRevenueDetailDialog(
            sellerStats = selectedSellerForRevenueDetail!!,
            allSellerOrders = sellerOrders,
            sellerProducts = sellerProducts,
            onDismiss = { selectedSellerForRevenueDetail = null }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        GagarinGoLogoCompact(size = 36.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Admin Boshqaruvi",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SecondaryNavy
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = PrimaryBlueLight
                                ) {
                                    Text(
                                        text = "Gagarin Go",
                                        color = PrimaryBlue,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Admin: ${adminUser.login}",
                                fontSize = 11.sp,
                                color = SlateGray
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            authViewModel.logout()
                            onLogout()
                        },
                        modifier = Modifier.testTag("admin_logout_btn")
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
            if (selectedTab == 4) {
                FloatingActionButton(
                    onClick = { showCreateSellerDialog = true },
                    containerColor = PrimaryBlue,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("admin_fab_add_seller")
                ) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = "Sotuvchi qo‘shish")
                }
            } else if (selectedTab == 6) {
                FloatingActionButton(
                    onClick = { showAddCategoryDialog = true },
                    containerColor = PrimaryBlue,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("admin_fab_add_category")
                ) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = "Kategoriya qo‘shish")
                }
            } else if (selectedTab == 7) {
                FloatingActionButton(
                    onClick = { showAddBannerDialog = true },
                    containerColor = PrimaryBlue,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("admin_fab_add_banner")
                ) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = "Reklama qo‘shish")
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
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = CardSurface,
                contentColor = PrimaryBlue,
                edgePadding = 12.dp
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("📊 Statistika", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("💰 Sotuvchilar daromadi (${sellersRevenueStats.size})", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("📦 Buyurtmalar (${allOrders.size})", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("👥 Foydalanuvchilar (${customers.size})", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    text = { Text("🏪 Sotuvchilar (${sellers.size})", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 5,
                    onClick = { selectedTab = 5 },
                    text = { Text("🛍️ Mahsulotlar (${allProducts.size})", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 6,
                    onClick = { selectedTab = 6 },
                    text = { Text("🏷️ Kategoriyalar (${categories.size})", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 7,
                    onClick = { selectedTab = 7 },
                    text = { Text("📢 Reklamalar (${allBanners.size})", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 8,
                    onClick = { selectedTab = 8 },
                    text = { Text("🎧 Qo‘llab-quvvatlash", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 9,
                    onClick = { selectedTab = 9 },
                    text = { Text("📜 Audit jurnali (${auditLogs.size})", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 10,
                    onClick = { selectedTab = 10 },
                    text = { Text("⚙️ Admin sozlamalari", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 11,
                    onClick = { selectedTab = 11 },
                    text = { Text("📲 Ilova yangilanishi", fontWeight = FontWeight.SemiBold) }
                )
            }

            when (selectedTab) {
                0 -> AdminOverviewTab(
                    stats = stats,
                    onGoToSellersRevenue = { selectedTab = 1 },
                    onGoToOrders = { selectedTab = 2 },
                    onGoToCustomers = { selectedTab = 3 },
                    onGoToSellers = { selectedTab = 4 },
                    onGoToProducts = { selectedTab = 5 }
                )
                1 -> AdminSellersRevenueTab(
                    sellersRevenueStats = sellersRevenueStats,
                    onSellerClick = { seller ->
                        adminViewModel.selectSellerForRevenue(seller)
                        selectedSellerForRevenueDetail = seller
                    }
                )
                2 -> AdminOrdersTab(
                    orders = allOrders,
                    onOrderClick = { selectedOrderDetail = it }
                )
                3 -> AdminCustomersTab(
                    customers = customers,
                    orders = allOrders,
                    onCustomerClick = { selectedCustomerForDetail = it }
                )
                4 -> AdminSellersTab(
                    sellers = sellers,
                    onToggleActive = { seller -> adminViewModel.toggleSellerActive(seller.id, seller.isActive) },
                    onResetPassword = { seller -> sellerForPasswordReset = seller },
                    onDelete = { seller -> sellerToDelete = seller },
                    onCreateSellerClick = { showCreateSellerDialog = true }
                )
                5 -> AdminProductsTab(
                    products = allProducts,
                    categories = categories,
                    onDeleteProduct = { adminViewModel.deleteProduct(it) },
                    onAddCategoryClick = { showAddCategoryDialog = true },
                    onDeleteCategory = { adminViewModel.deleteCategory(it) }
                )
                6 -> AdminCategoriesTab(
                    categories = categories,
                    onAddCategoryClick = { showAddCategoryDialog = true },
                    onDeleteCategory = { adminViewModel.deleteCategory(it) }
                )
                7 -> AdminPromoBannersTab(
                    banners = allBanners,
                    onAddNewClick = { showAddBannerDialog = true },
                    onEditBanner = { bannerToEdit = it },
                    onToggleActive = { b -> adminViewModel.toggleBannerActive(b) },
                    onDeleteBanner = { b -> adminViewModel.deleteBanner(b) }
                )
                8 -> AdminSupportTab(
                    supportInfo = supportInfo,
                    onSaveSupportInfo = { phone, secPhone, tg, ch, hours, addr, desc ->
                        adminViewModel.updateSupportInfo(phone, secPhone, tg, ch, hours, addr, desc)
                    }
                )
                9 -> AdminAuditLogsTab(
                    auditLogs = auditLogs
                )
                10 -> AdminSettingsTab(
                    adminUser = adminUser,
                    onChangePasswordClick = { showChangeAdminPassDialog = true }
                )
                11 -> AdminAppVersionTab(
                    supportInfo = supportInfo,
                    onSaveVersionConfig = { code, name, minCode, force, title, msg, notes, play, tg ->
                        adminViewModel.updateAppVersionConfig(code, name, minCode, force, title, msg, notes, play, tg)
                    }
                )
            }
        }
    }
}

// ----------------------------------------------------
// TAB 0: UMUMIY STATISTIKA (REAL DASHBOARD METRICS)
// ----------------------------------------------------
@Composable
fun AdminOverviewTab(
    stats: AdminDashboardStats,
    onGoToSellersRevenue: () -> Unit,
    onGoToOrders: () -> Unit,
    onGoToCustomers: () -> Unit,
    onGoToSellers: () -> Unit,
    onGoToProducts: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Gagarin Go — Boshqaruv Paneli",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = SecondaryNavy
        )
        Text(
            text = "Barcha ko‘rsatkichlar real ma’lumotlar bazasidan hisoblab chiqarilmoqda",
            fontSize = 12.sp,
            color = SlateGray
        )

        Spacer(modifier = Modifier.height(16.dp))

        // REAL REVENUE HIGHLIGHT CARD (TOTAL COMPLETED SALES)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PrimaryBlue),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Umumiy yakunlangan savdo",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "Gagarin Go",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = Formatters.formatPrice(stats.totalRevenue),
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = Color.White.copy(alpha = 0.2f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Platforma real daromadi (5%):", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                        Text(
                            text = Formatters.formatPrice(stats.platformRevenue),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFDE68A)
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Yakunlangan buyurtmalar:", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                        Text(
                            text = "${stats.completedOrders} ta",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Time-based Real Revenue Grid (Today, Weekly, Monthly)
        Text(
            text = "Davriy savdo tushumlari",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = SecondaryNavy
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            AdminTimeRevenueCard(
                periodTitle = "Bugungi savdo",
                amount = stats.todayRevenue,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            AdminTimeRevenueCard(
                periodTitle = "Haftalik savdo",
                amount = stats.weeklyRevenue,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            AdminTimeRevenueCard(
                periodTitle = "Oylik savdo",
                amount = stats.monthlyRevenue,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Order & User Counts
        Text(
            text = "Platforma ko‘rsatkichlari",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = SecondaryNavy
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            AdminMetricCard(
                title = "Jami foydalanuvchilar",
                value = "${stats.totalCustomers}",
                icon = Icons.Filled.Person,
                color = PrimaryBlue,
                bgColor = PrimaryBlueLight,
                modifier = Modifier.weight(1f).clickable(onClick = onGoToCustomers)
            )
            Spacer(modifier = Modifier.width(10.dp))
            AdminMetricCard(
                title = "Jami sotuvchilar",
                value = "${stats.totalSellers} (${stats.activeSellers} faol)",
                icon = Icons.Filled.Storefront,
                color = Color(0xFF721430),
                bgColor = PrimaryBlueLight,
                modifier = Modifier.weight(1f).clickable(onClick = onGoToSellers)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            AdminMetricCard(
                title = "Jami mahsulotlar",
                value = "${stats.totalProducts}",
                icon = Icons.Filled.ShoppingCart,
                color = Color(0xFF0284C7),
                bgColor = Color(0xFFE0F2FE),
                modifier = Modifier.weight(1f).clickable(onClick = onGoToProducts)
            )
            Spacer(modifier = Modifier.width(10.dp))
            AdminMetricCard(
                title = "Jami buyurtmalar",
                value = "${stats.totalOrders} (${stats.completedOrders} yetkazildi)",
                icon = Icons.AutoMirrored.Filled.List,
                color = WarningAmber,
                bgColor = WarningAmberLight,
                modifier = Modifier.weight(1f).clickable(onClick = onGoToOrders)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            AdminMetricCard(
                title = "Yakunlangan buyurtmalar",
                value = "${stats.completedOrders}",
                icon = Icons.Filled.CheckCircle,
                color = SuccessGreen,
                bgColor = SuccessGreenLight,
                modifier = Modifier.weight(1f).clickable(onClick = onGoToOrders)
            )
            Spacer(modifier = Modifier.width(10.dp))
            AdminMetricCard(
                title = "Bekor qilingan buyurtmalar",
                value = "${stats.cancelledOrders}",
                icon = Icons.Filled.Block,
                color = DangerRed,
                bgColor = DangerRedLight,
                modifier = Modifier.weight(1f).clickable(onClick = onGoToOrders)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Quick Management Shortcuts
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            border = BorderStroke(1.dp, BorderColor),
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
                    onClick = onGoToSellersRevenue,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Filled.Payments, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sotuvchilar daromadini ko‘rish")
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onGoToOrders,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.List, contentDescription = null, tint = SecondaryNavy, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Buyurtmalarni nazorat qilish", color = SecondaryNavy)
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
                text = "Gagarin Go © Boshqaruv Tizimi",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = SlateGray
            )
        }
    }
}

@Composable
fun AdminTimeRevenueCard(
    periodTitle: String,
    amount: Double,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(1.dp, BorderColor),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = periodTitle, fontSize = 11.sp, color = SlateGray, maxLines = 1)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = Formatters.formatPrice(amount),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun AdminMetricCard(
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
        border = BorderStroke(1.dp, BorderColor),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(bgColor, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = title, fontSize = 11.sp, color = SlateGray, maxLines = 1)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SecondaryNavy, maxLines = 1)
        }
    }
}

// ----------------------------------------------------
// TAB 1: SOTUVCHILAR DAROMADI (EACH SELLER REVENUE)
// ----------------------------------------------------
@Composable
fun AdminSellersRevenueTab(
    sellersRevenueStats: List<SellerRevenueStats>,
    onSellerClick: (SellerRevenueStats) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTimePeriod by remember { mutableStateOf("1_OYLIK") } // "1_OYLIK", "BARCHASI", "BUGUN"

    val filtered = sellersRevenueStats.filter {
        searchQuery.isBlank() ||
                it.storeName.contains(searchQuery, ignoreCase = true) ||
                it.sellerName.contains(searchQuery, ignoreCase = true) ||
                it.phone.contains(searchQuery, ignoreCase = true)
    }

    val totalCompletedSum = sellersRevenueStats.sumOf { it.totalSales }
    val totalCommission = sellersRevenueStats.sumOf { it.platformCommission }
    val totalMonthlySales = sellersRevenueStats.sumOf { it.monthlySales }
    val totalMonthlyCommission = sellersRevenueStats.sumOf { it.monthlyCommission }
    val totalMonthlyNet = sellersRevenueStats.sumOf { it.monthlyNetRevenue }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Hero 1-Month Highlight Summary Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = PrimaryBlue),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Do‘konlarning 1 oylik savdosi",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = Formatters.formatPrice(totalMonthlySales),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "Oxirgi 30 kun",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color.White.copy(alpha = 0.2f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "1 oylik platforma tushumi (5%):", fontSize = 10.5.sp, color = Color.White.copy(alpha = 0.8f))
                        Text(
                            text = Formatters.formatPrice(totalMonthlyCommission),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFDE68A)
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Do‘konlar sof daromadi (95%):", fontSize = 10.5.sp, color = Color.White.copy(alpha = 0.8f))
                        Text(
                            text = Formatters.formatPrice(totalMonthlyNet),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Time Filter Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedTimePeriod == "1_OYLIK",
                onClick = { selectedTimePeriod = "1_OYLIK" },
                label = { Text("1 Oylik sotuv", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryBlue,
                    selectedLabelColor = Color.White
                )
            )
            FilterChip(
                selected = selectedTimePeriod == "BARCHASI",
                onClick = { selectedTimePeriod = "BARCHASI" },
                label = { Text("Jami (Barchasi)", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryBlue,
                    selectedLabelColor = Color.White
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Sotuvchi yoki do‘kon nomi bo‘yicha qidirish...", fontSize = 13.sp) },
            leadingIcon = { Icon(imageVector = Icons.Filled.Search, contentDescription = null, tint = PrimaryBlue) },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (filtered.isEmpty()) {
            EmptyStateView(
                title = "Sotuvchilar topilmadi",
                description = if (searchQuery.isNotBlank()) "Qidiruv bo‘yicha sotuvchi topilmadi" else "Tizimda hozircha sotuvchilar mavjud emas.",
                icon = Icons.Filled.Storefront,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filtered) { seller ->
                    SellerRevenueCard(
                        seller = seller,
                        showMonthlyHighlight = selectedTimePeriod == "1_OYLIK",
                        onClick = { onSellerClick(seller) }
                    )
                }
            }
        }
    }
}

@Composable
fun SellerRevenueCard(
    seller: SellerRevenueStats,
    showMonthlyHighlight: Boolean = true,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(1.dp, BorderColor),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = seller.storeName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = SecondaryNavy
                    )
                    Text(
                        text = "Egasi: ${seller.sellerName} | Tel: ${seller.phone}",
                        fontSize = 12.sp,
                        color = SlateGray
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (seller.isActive) SuccessGreenLight else DangerRedLight
                ) {
                    Text(
                        text = if (seller.isActive) "Faol" else "Faolsiz",
                        color = if (seller.isActive) SuccessGreen else DangerRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // 1-Month Performance Card for this Store
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = SuccessGreen.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🗓️ 1 Oylik Savdo (30 kun):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen
                        )
                        Text(
                            text = "${seller.monthlyOrdersCount} ta buyurtma",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SecondaryNavy
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "1 oylik sotuv summasi:", fontSize = 10.sp, color = SlateGray)
                            Text(
                                text = Formatters.formatPrice(seller.monthlySales),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = PrimaryBlue
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Do‘kon daromadi (95%):", fontSize = 10.sp, color = SlateGray)
                            Text(
                                text = Formatters.formatPrice(seller.monthlyNetRevenue),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SuccessGreen
                            )
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = BorderColor)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Jami buyurtmalar:", fontSize = 11.sp, color = SlateGray)
                    Text(
                        text = "${seller.totalOrdersCount} ta (${seller.completedOrdersCount} yakunlangan)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = SecondaryNavy
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Jami barcha davrdagi savdo:", fontSize = 11.sp, color = SlateGray)
                    Text(
                        text = Formatters.formatPrice(seller.totalSales),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryNavy
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Jami komissiya (5%): ${Formatters.formatPrice(seller.platformCommission)}",
                    fontSize = 11.sp,
                    color = Color(0xFF065F46),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Jami sof: ${Formatters.formatPrice(seller.sellerNetRevenue)}",
                    fontSize = 11.sp,
                    color = SecondaryNavy,
                    fontWeight = FontWeight.Bold
                )
            }

            if (seller.lastOrderDate != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Oxirgi buyurtma: ${Formatters.formatDate(seller.lastOrderDate)}",
                    fontSize = 10.sp,
                    color = SlateGray
                )
            }
        }
    }
}

// ----------------------------------------------------
// SELLER REVENUE DETAIL DIALOG WITH TIMEFRAME FILTERS
// ----------------------------------------------------
@Composable
fun SellerRevenueDetailDialog(
    sellerStats: SellerRevenueStats,
    allSellerOrders: List<OrderDetail>,
    sellerProducts: List<ProductEntity>,
    onDismiss: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf("Barchasi") } // "Barchasi", "Bugun", "Shu hafta", "Shu oy"

    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val startOfToday = calendar.timeInMillis
    val startOfWeek = startOfToday - (6L * 24 * 60 * 60 * 1000)
    val startOfMonth = startOfToday - (29L * 24 * 60 * 60 * 1000)

    val filteredOrders = remember(selectedFilter, allSellerOrders) {
        when (selectedFilter) {
            "Bugun" -> allSellerOrders.filter { it.order.createdAt >= startOfToday }
            "Shu hafta" -> allSellerOrders.filter { it.order.createdAt >= startOfWeek }
            "Shu oy" -> allSellerOrders.filter { it.order.createdAt >= startOfMonth }
            else -> allSellerOrders
        }
    }

    val completedOrders = filteredOrders.filter { it.order.status == "Yetkazildi" }
    val cancelledOrders = filteredOrders.filter { it.order.status == "Bekor qilindi" }

    // Dynamic sales calculation for this seller
    val filteredSales = completedOrders.sumOf { orderDetail ->
        orderDetail.items.filter { it.sellerId == sellerStats.sellerId }.sumOf { it.itemTotal }
    }
    val filteredCommission = filteredSales * 0.05
    val filteredNet = filteredSales - filteredCommission

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "${sellerStats.storeName} — Moliyaviy tahlil",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = SecondaryNavy
                )
                Text(
                    text = "Sotuvchi: ${sellerStats.sellerName} | Tel: ${sellerStats.phone}",
                    fontSize = 12.sp,
                    color = SlateGray
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Filter Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Barchasi", "Bugun", "Shu hafta", "Shu oy").forEach { filter ->
                        val isSelected = selectedFilter == filter
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) PrimaryBlue else CardSurface,
                            border = BorderStroke(1.dp, if (isSelected) PrimaryBlue else BorderColor),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedFilter = filter }
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = filter,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else SecondaryNavy
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 4 Real Financial Metric Cards
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryBlueLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "Tanlangan davr savdosi:", fontSize = 11.sp, color = SlateGray)
                        Text(
                            text = Formatters.formatPrice(filteredSales),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PrimaryBlue
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = BorderColor)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "Platforma ulushi (5%):", fontSize = 10.sp, color = SlateGray)
                                Text(text = Formatters.formatPrice(filteredCommission), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF065F46))
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "Sotuvchi sof tushumi:", fontSize = 10.sp, color = SlateGray)
                                Text(text = Formatters.formatPrice(filteredNet), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SecondaryNavy)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = SuccessGreenLight),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(text = "Yakunlangan", fontSize = 10.sp, color = Color(0xFF065F46))
                            Text(text = "${completedOrders.size} ta", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF065F46))
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = DangerRedLight),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(text = "Bekor qilingan", fontSize = 10.sp, color = DangerRed)
                            Text(text = "${cancelledOrders.size} ta", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DangerRed)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Orders breakdown for this seller
                Text(
                    text = "Buyurtmalar ro‘yxati (${filteredOrders.size} ta):",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = SecondaryNavy
                )
                Spacer(modifier = Modifier.height(6.dp))

                if (filteredOrders.isEmpty()) {
                    Text(text = "Bu davrda buyurtmalar mavjud emas.", fontSize = 12.sp, color = SlateGray)
                } else {
                    filteredOrders.forEach { orderDetail ->
                        val sellerItems = orderDetail.items.filter { it.sellerId == sellerStats.sellerId }
                        val sellerOrderSum = sellerItems.sumOf { it.itemTotal }
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = CardSurface),
                            border = BorderStroke(1.dp, BorderColor),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = orderDetail.order.orderNumber, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                                    OrderStatusBadge(status = orderDetail.order.status)
                                }
                                Text(text = "Sana: ${Formatters.formatDate(orderDetail.order.createdAt)}", fontSize = 10.sp, color = SlateGray)
                                Spacer(modifier = Modifier.height(4.dp))
                                sellerItems.forEach { item ->
                                    Text(text = "• ${item.productName} (${item.quantity} ${item.unit} × ${Formatters.formatPrice(item.unitPrice)})", fontSize = 11.sp, color = SecondaryNavy)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Sotuvchi summasi: ${Formatters.formatPrice(sellerOrderSum)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlue
                                )
                            }
                        }
                    }
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

// ----------------------------------------------------
// TAB 2: BUYURTMALAR (ORDERS MANAGEMENT)
// ----------------------------------------------------
@Composable
fun AdminOrdersTab(
    orders: List<OrderDetail>,
    onOrderClick: (OrderDetail) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("Barchasi") }

    val filtered = orders.filter { orderDetail ->
        val matchesSearch = searchQuery.isBlank() ||
                orderDetail.order.orderNumber.contains(searchQuery, ignoreCase = true) ||
                orderDetail.order.customerName.contains(searchQuery, ignoreCase = true) ||
                orderDetail.order.customerPhone.contains(searchQuery, ignoreCase = true)
        val matchesStatus = selectedStatusFilter == "Barchasi" || orderDetail.order.status == selectedStatusFilter
        matchesSearch && matchesStatus
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Buyurtma raqami, xaridor yoki telefon...", fontSize = 13.sp) },
            leadingIcon = { Icon(imageVector = Icons.Filled.Search, contentDescription = null, tint = PrimaryBlue) },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Status filter chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("Barchasi", "Yangi", "Tayyorlanmoqda", "Yetkazilmoqda", "Yetkazildi", "Bekor qilindi").forEach { st ->
                val isSelected = selectedStatusFilter == st
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) PrimaryBlue else CardSurface,
                    border = BorderStroke(1.dp, if (isSelected) PrimaryBlue else BorderColor),
                    modifier = Modifier.clickable { selectedStatusFilter = st }
                ) {
                    Text(
                        text = st,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else SecondaryNavy,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filtered.isEmpty()) {
            EmptyStateView(
                title = "Buyurtmalar topilmadi",
                description = if (searchQuery.isNotBlank()) "Qidiruv bo‘yicha buyurtma topilmadi" else "Tizimda hozircha buyurtmalar mavjud emas.",
                icon = Icons.AutoMirrored.Filled.List,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filtered) { orderDetail ->
                    AdminOrderCard(
                        orderDetail = orderDetail,
                        onClick = { onOrderClick(orderDetail) }
                    )
                }
            }
        }
    }
}

@Composable
fun AdminOrderCard(
    orderDetail: OrderDetail,
    onClick: () -> Unit
) {
    val order = orderDetail.order
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(1.dp, BorderColor),
        modifier = Modifier
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

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = BorderColor)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${orderDetail.items.size} ta mahsulot | ${Formatters.formatDate(order.createdAt)}",
                    fontSize = 12.sp,
                    color = SlateGray
                )
                Text(
                    text = Formatters.formatPrice(order.totalAmount),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue
                )
            }
        }
    }
}

// ----------------------------------------------------
// TAB 3: FOYDALANUVCHILAR (CUSTOMERS MANAGEMENT)
// ----------------------------------------------------
@Composable
fun AdminCustomersTab(
    customers: List<UserEntity>,
    orders: List<OrderDetail>,
    onCustomerClick: (UserEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = customers.filter {
        searchQuery.isBlank() ||
                it.fullName.contains(searchQuery, ignoreCase = true) ||
                it.email.contains(searchQuery, ignoreCase = true) ||
                it.phone.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Mijozlarni qidirish (ism, telefon, email)...", fontSize = 13.sp) },
            leadingIcon = { Icon(imageVector = Icons.Filled.Search, contentDescription = null, tint = PrimaryBlue) },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (filtered.isEmpty()) {
            EmptyStateView(
                title = "Xaridorlar topilmadi",
                description = if (searchQuery.isNotBlank()) "Qidiruv bo‘yicha xaridor topilmadi" else "Tizimda hozircha ro‘yxatdan o‘tgan mijozlar yo‘q.",
                icon = Icons.Filled.Person,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filtered) { customer ->
                    val customerOrders = orders.filter { it.order.customerId == customer.id }
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = CardSurface),
                        border = BorderStroke(1.dp, BorderColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCustomerClick(customer) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(PrimaryBlueLight, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = customer.fullName.ifBlank { "Mijoz #${customer.id}" },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = SecondaryNavy
                                )
                                Text(
                                    text = "Tel: ${customer.phone.ifBlank { "Kiritilmagan" }}",
                                    fontSize = 12.sp,
                                    color = SlateGray
                                )
                                Text(
                                    text = "Buyurtmalar soni: ${customerOrders.size} ta",
                                    fontSize = 11.sp,
                                    color = PrimaryBlue,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// TAB 4: SOTUVCHILAR (SELLERS MANAGEMENT)
// ----------------------------------------------------
@Composable
fun AdminSellersTab(
    sellers: List<UserEntity>,
    onToggleActive: (UserEntity) -> Unit,
    onResetPassword: (UserEntity) -> Unit,
    onDelete: (UserEntity) -> Unit,
    onCreateSellerClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = sellers.filter {
        searchQuery.isBlank() ||
                it.storeName.contains(searchQuery, ignoreCase = true) ||
                it.fullName.contains(searchQuery, ignoreCase = true) ||
                it.login.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Sotuvchilarni qidirish...", fontSize = 13.sp) },
            leadingIcon = { Icon(imageVector = Icons.Filled.Search, contentDescription = null, tint = PrimaryBlue) },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (filtered.isEmpty()) {
            EmptyStateView(
                title = "Sotuvchilar mavjud emas",
                description = "Yangi sotuvchi qo‘shish uchun pastdagi '+' tugmasini bosing.",
                icon = Icons.Filled.Storefront,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filtered) { seller ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = CardSurface),
                        border = BorderStroke(1.dp, BorderColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = seller.storeName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = SecondaryNavy
                                    )
                                    Text(
                                        text = "Login: ${seller.login} | Egasi: ${seller.fullName}",
                                        fontSize = 12.sp,
                                        color = SlateGray
                                    )
                                    Text(
                                        text = "Tel: ${seller.phone}",
                                        fontSize = 12.sp,
                                        color = PrimaryBlue,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (seller.isActive) SuccessGreenLight else DangerRedLight
                                ) {
                                    Text(
                                        text = if (seller.isActive) "Faol" else "Faolsiz",
                                        color = if (seller.isActive) SuccessGreen else DangerRed,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = BorderColor)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = { onToggleActive(seller) },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = if (seller.isActive) "Faolsizlantirish" else "Faollashtirish",
                                        fontSize = 11.sp,
                                        color = if (seller.isActive) DangerRed else SuccessGreen
                                    )
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                OutlinedButton(
                                    onClick = { onResetPassword(seller) },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(imageVector = Icons.Filled.Key, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "Parol", fontSize = 11.sp)
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                IconButton(onClick = { onDelete(seller) }) {
                                    Icon(imageVector = Icons.Filled.Delete, contentDescription = "O‘chirish", tint = DangerRed)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// TAB 5: MAHSULOTLAR (PRODUCTS MANAGEMENT)
// ----------------------------------------------------
@Composable
fun AdminProductsTab(
    products: List<ProductEntity>,
    categories: List<CategoryEntity>,
    onDeleteProduct: (ProductEntity) -> Unit,
    onAddCategoryClick: () -> Unit,
    onDeleteCategory: (CategoryEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = products.filter {
        searchQuery.isBlank() ||
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.sellerName.contains(searchQuery, ignoreCase = true) ||
                it.categoryName.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Mahsulot nomi yoki sotuvchi bo‘yicha qidirish...", fontSize = 13.sp) },
            leadingIcon = { Icon(imageVector = Icons.Filled.Search, contentDescription = null, tint = PrimaryBlue) },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (filtered.isEmpty()) {
            EmptyStateView(
                title = "Mahsulotlar topilmadi",
                description = if (searchQuery.isNotBlank()) "Qidiruv bo‘yicha mahsulot topilmadi" else "Tizimda hozircha mahsulotlar mavjud emas.",
                icon = Icons.Filled.ShoppingCart,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filtered) { product ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardSurface),
                        border = BorderStroke(1.dp, BorderColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AppProductImage(
                                imageUri = product.imageUri,
                                contentDescription = product.name,
                                modifier = Modifier.size(48.dp),
                                shape = RoundedCornerShape(8.dp),
                                contentScale = ContentScale.Crop,
                                iconSize = 20.dp
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = product.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SecondaryNavy, maxLines = 1)
                                Text(text = "Sotuvchi: ${product.sellerName} | Qoldiq: ${product.stock} ${product.unit}", fontSize = 11.sp, color = SlateGray)
                                Text(text = Formatters.formatPrice(product.price), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                            }

                            IconButton(onClick = { onDeleteProduct(product) }) {
                                Icon(imageVector = Icons.Filled.Delete, contentDescription = "O‘chirish", tint = DangerRed)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// TAB 6: KATEGORIYALAR (CATEGORIES MANAGEMENT)
// ----------------------------------------------------
@Composable
fun AdminCategoriesTab(
    categories: List<CategoryEntity>,
    onAddCategoryClick: () -> Unit,
    onDeleteCategory: (CategoryEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Mahsulot kategoriyalari (${categories.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = SecondaryNavy
            )

            Button(
                onClick = onAddCategoryClick,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Qo‘shish", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (categories.isEmpty()) {
            EmptyStateView(
                title = "Kategoriyalar mavjud emas",
                description = "Yangi kategoriya qo‘shish uchun yuqoridagi tugmani bosing.",
                icon = Icons.Filled.Category,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(categories) { cat ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = CardSurface),
                        border = BorderStroke(1.dp, BorderColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Filled.Category, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(text = cat.name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = SecondaryNavy)
                            }

                            IconButton(onClick = { onDeleteCategory(cat) }) {
                                Icon(imageVector = Icons.Filled.Delete, contentDescription = "O‘chirish", tint = DangerRed)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// TAB 7: REKLAMALAR (PROMO BANNERS)
// ----------------------------------------------------
@Composable
fun AdminPromoBannersTab(
    banners: List<PromoBannerEntity>,
    onAddNewClick: () -> Unit,
    onEditBanner: (PromoBannerEntity) -> Unit,
    onToggleActive: (PromoBannerEntity) -> Unit,
    onDeleteBanner: (PromoBannerEntity) -> Unit
) {
    var bannerToDelete by remember { mutableStateOf<PromoBannerEntity?>(null) }

    if (bannerToDelete != null) {
        AlertDialog(
            onDismissRequest = { bannerToDelete = null },
            title = {
                Text(
                    text = "Reklamani o‘chirish",
                    fontWeight = FontWeight.Bold,
                    color = DangerRed
                )
            },
            text = {
                Text(
                    text = "Haqiqatan ham \"${bannerToDelete!!.name.ifBlank { bannerToDelete!!.title }}\" reklamasini o‘chirmoqchimisiz? Bu reklama butun platformadan o‘chiriladi.",
                    fontSize = 13.sp,
                    color = SlateGray
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val toDelete = bannerToDelete
                        bannerToDelete = null
                        if (toDelete != null) {
                            onDeleteBanner(toDelete)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("O‘chirish")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { bannerToDelete = null }) {
                    Text("Bekor qilish")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Bosh sahifa reklamalari",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = SecondaryNavy
                )
                Text(
                    text = "Jami: ${banners.size} ta reklama",
                    fontSize = 12.sp,
                    color = SlateGray
                )
            }

            Button(
                onClick = onAddNewClick,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Reklama qo‘shish", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (banners.isEmpty()) {
            EmptyStateView(
                title = "Hozircha reklamalar mavjud emas.",
                description = "Bosh sahifada ko‘rinadigan reklama yoki banner yaratish uchun '+' tugmasini bosing.",
                icon = Icons.Filled.Campaign,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(banners) { banner ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardSurface),
                        border = BorderStroke(1.dp, BorderColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Banner Preview Area
                            val hasImage = banner.imageUrl.isNotBlank()
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(95.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            ) {
                                if (hasImage) {
                                    SubcomposeAsyncImage(
                                        model = banner.imageUrl,
                                        contentDescription = banner.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize(),
                                        loading = {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(getBannerBrush(banner.gradientType)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                            }
                                        },
                                        error = {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(getBannerBrush(banner.gradientType))
                                            )
                                        }
                                    )
                                    // Gradient Scrim Overlay
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                androidx.compose.ui.graphics.Brush.verticalGradient(
                                                    colors = listOf(
                                                        Color.Black.copy(alpha = 0.2f),
                                                        Color.Black.copy(alpha = 0.75f)
                                                    )
                                                )
                                            )
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(getBannerBrush(banner.gradientType))
                                    )
                                }

                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(10.dp),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color.White.copy(alpha = 0.3f)
                                        ) {
                                            Text(
                                                text = banner.badgeText.ifBlank { "REKLAMA" },
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }

                                        if (hasImage) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = Color.Black.copy(alpha = 0.5f)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                ) {
                                                    Icon(imageVector = Icons.Filled.Image, contentDescription = null, tint = Color.White, modifier = Modifier.size(11.dp))
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text(text = "Rasm yuklangan", color = Color.White, fontSize = 9.sp)
                                                }
                                            }
                                        }
                                    }

                                    Column {
                                        Text(
                                            text = banner.title,
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        val sub = banner.description.ifBlank { banner.name }
                                        if (sub.isNotBlank()) {
                                            Text(
                                                text = sub,
                                                color = Color.White.copy(alpha = 0.88f),
                                                fontSize = 11.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Details
                            if (banner.name.isNotBlank() && banner.name != banner.title) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "Kampaniya nomi: ", fontSize = 12.sp, color = SlateGray)
                                    Text(text = banner.name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SecondaryNavy)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }

                            if (banner.targetLink.isNotBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Filled.Link, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "Havola: ", fontSize = 11.sp, color = SlateGray)
                                    Text(text = banner.targetLink, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = PrimaryBlue)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Filled.CalendarMonth, contentDescription = null, tint = SlateGray, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Boshlanish: ${Formatters.formatDate(banner.startDate)} — Tugash: ${Formatters.formatDate(banner.endDate)}",
                                    fontSize = 11.sp,
                                    color = SlateGray
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            HorizontalDivider(color = BorderColor.copy(alpha = 0.6f))

                            Spacer(modifier = Modifier.height(8.dp))

                            // Status & Actions
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Switch(
                                        checked = banner.isActive,
                                        onCheckedChange = { onToggleActive(banner) },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = SuccessGreen
                                        ),
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (banner.isActive) SuccessGreenLight else DangerRedLight
                                    ) {
                                        Text(
                                            text = if (banner.isActive) "Faol (Mijozlarga ko‘rinmoqda)" else "To‘xtatilgan",
                                            color = if (banner.isActive) SuccessGreen else DangerRed,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Row {
                                    IconButton(
                                        onClick = { onEditBanner(banner) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(imageVector = Icons.Filled.Edit, contentDescription = "Tahrirlash", tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(
                                        onClick = { bannerToDelete = banner },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(imageVector = Icons.Filled.Delete, contentDescription = "O‘chirish", tint = DangerRed, modifier = Modifier.size(18.dp))
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

// ----------------------------------------------------
// TAB 8: QO‘LLAB-QUVVATLASH (SUPPORT REAL-TIME SYNC)
// ----------------------------------------------------
@Composable
fun AdminSupportTab(
    supportInfo: SupportInfoEntity,
    onSaveSupportInfo: (phone: String, secondaryPhone: String, tg: String, channel: String, hours: String, addr: String, desc: String) -> Unit
) {
    var phone by remember(supportInfo) { mutableStateOf(supportInfo.phone) }
    var secondaryPhone by remember(supportInfo) { mutableStateOf(supportInfo.secondaryPhone) }
    var telegramUsername by remember(supportInfo) { mutableStateOf(supportInfo.telegramUsername) }
    var telegramChannel by remember(supportInfo) { mutableStateOf(supportInfo.telegramChannel) }
    var workingHours by remember(supportInfo) { mutableStateOf(supportInfo.workingHours) }
    var address by remember(supportInfo) { mutableStateOf(supportInfo.address) }
    var description by remember(supportInfo) { mutableStateOf(supportInfo.description) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            border = BorderStroke(1.dp, BorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(PrimaryBlueLight, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Filled.Headphones, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Mijozlarni qo‘llab-quvvatlash ma’lumotlari",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = SecondaryNavy
                        )
                        Text(
                            text = "Xaridorlar profilidagi yordam markaziga darhol sinxronlashadi",
                            fontSize = 12.sp,
                            color = SlateGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Asosiy qo‘llab-quvvatlash telefoni") },
                    leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null, tint = PrimaryBlue) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = secondaryPhone,
                    onValueChange = { secondaryPhone = it },
                    label = { Text("Qo‘shimcha telefon raqami") },
                    leadingIcon = { Icon(Icons.Filled.PhoneAndroid, contentDescription = null, tint = PrimaryBlue) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = telegramUsername,
                    onValueChange = { telegramUsername = it },
                    label = { Text("Telegram qo‘llab-quvvatlash (@username)") },
                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color(0xFF0284C7)) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = telegramChannel,
                    onValueChange = { telegramChannel = it },
                    label = { Text("Rasmiy Telegram kanal (@kanal)") },
                    leadingIcon = { Icon(Icons.Filled.Campaign, contentDescription = null, tint = Color(0xFF0284C7)) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = workingHours,
                    onValueChange = { workingHours = it },
                    label = { Text("Ish vaqti") },
                    leadingIcon = { Icon(Icons.Filled.Schedule, contentDescription = null, tint = SlateGray) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Manzil / Ofis joylashuvi") },
                    leadingIcon = { Icon(Icons.Filled.LocationOn, contentDescription = null, tint = DangerRed) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Qo‘shimcha tavsif / xabar") },
                    maxLines = 3,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        onSaveSupportInfo(
                            phone.trim(),
                            secondaryPhone.trim(),
                            telegramUsername.trim(),
                            telegramChannel.trim(),
                            workingHours.trim(),
                            address.trim(),
                            description.trim()
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ma’lumotlarni saqlash va sinxronlash")
                }
            }
        }
    }
}

// ----------------------------------------------------
// TAB 9: AUDIT JURNALI (AUDIT LOGS)
// ----------------------------------------------------
@Composable
fun AdminAuditLogsTab(
    auditLogs: List<AdminAuditLogEntity>
) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = auditLogs.filter {
        searchQuery.isBlank() ||
                it.action.contains(searchQuery, ignoreCase = true) ||
                it.adminLogin.contains(searchQuery, ignoreCase = true) ||
                it.details.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Xavfsizlik va Audit Jurnali",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = SecondaryNavy
        )
        Text(
            text = "Admin tomonidan bajarilgan barcha muhim amallar qayd etiladi",
            fontSize = 12.sp,
            color = SlateGray
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Audit jurnali bo‘yicha qidirish...", fontSize = 13.sp) },
            leadingIcon = { Icon(imageVector = Icons.Filled.Search, contentDescription = null, tint = PrimaryBlue) },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (filtered.isEmpty()) {
            EmptyStateView(
                title = "Audit yozuvlari mavjud emas",
                description = "Admin amallari amalga oshirilganda bu yerda ko‘rinadi.",
                icon = Icons.Filled.History,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filtered) { log ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = CardSurface),
                        border = BorderStroke(1.dp, BorderColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = PrimaryBlueLight
                                ) {
                                    Text(
                                        text = log.adminLogin,
                                        color = PrimaryBlue,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                Text(
                                    text = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp)),
                                    fontSize = 11.sp,
                                    color = SlateGray
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = log.action,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = SecondaryNavy
                            )

                            if (log.details.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = log.details,
                                    fontSize = 12.sp,
                                    color = SlateGray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// TAB 10: ADMIN SOZLAMALARI (SETTINGS & PASSWORD CHANGE)
// ----------------------------------------------------
@Composable
fun AdminSettingsTab(
    adminUser: UserEntity,
    onChangePasswordClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            border = BorderStroke(1.dp, BorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(PrimaryBlueLight, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Filled.Security, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = "Admin xavfsizlik sozlamalari", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SecondaryNavy)
                        Text(text = "Tizim ma’muri hisobini himoyalash", fontSize = 12.sp, color = SlateGray)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(text = "Joriy login: ${adminUser.login}", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = SecondaryNavy)
                Text(text = "Roli: Super Administrator", fontSize = 12.sp, color = SlateGray)
                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onChangePasswordClick,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Admin parolini o‘zgartirish")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            border = BorderStroke(1.dp, BorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Platforma ma’lumotlari", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SecondaryNavy)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Ilova: Gagarin Go", fontSize = 13.sp, color = SecondaryNavy)
                Text(text = "Hudud: Gagarin shahri, Mirzacho‘l tumani", fontSize = 13.sp, color = SecondaryNavy)
                Text(text = "Yetkazib berish: Gagarin shahri bo‘ylab BEPUL", fontSize = 13.sp, color = SuccessGreen, fontWeight = FontWeight.Bold)
                Text(text = "Komissiya tizimi: 5% platforma daromadi", fontSize = 13.sp, color = PrimaryBlue, fontWeight = FontWeight.Medium)
            }
        }
    }
}

// ----------------------------------------------------
// TAB 11: ILOVA VERSIYALARI VA YANGILANISHLAR (IN-APP UPDATE CONTROLLER)
// ----------------------------------------------------
@Composable
fun AdminAppVersionTab(
    supportInfo: SupportInfoEntity,
    onSaveVersionConfig: (
        latestVersionCode: Int,
        latestVersionName: String,
        minRequiredVersionCode: Int,
        isForceUpdate: Boolean,
        updateTitle: String,
        updateMessage: String,
        releaseNotes: String,
        playStoreUrl: String,
        telegramApkUrl: String
    ) -> Unit
) {
    var latestVersionCodeText by remember { mutableStateOf(supportInfo.latestVersionCode.toString()) }
    var latestVersionName by remember { mutableStateOf(supportInfo.latestVersionName) }
    var minRequiredVersionCodeText by remember { mutableStateOf(supportInfo.minRequiredVersionCode.toString()) }
    var isForceUpdate by remember { mutableStateOf(supportInfo.isForceUpdate) }
    var updateTitle by remember { mutableStateOf(supportInfo.updateTitle) }
    var updateMessage by remember { mutableStateOf(supportInfo.updateMessage) }
    var releaseNotes by remember { mutableStateOf(supportInfo.releaseNotes) }
    var playStoreUrl by remember { mutableStateOf(supportInfo.playStoreUrl) }
    var telegramApkUrl by remember { mutableStateOf(supportInfo.telegramApkUrl) }
    var showPreviewDialog by remember { mutableStateOf(false) }

    LaunchedEffect(supportInfo) {
        latestVersionCodeText = supportInfo.latestVersionCode.toString()
        latestVersionName = supportInfo.latestVersionName
        minRequiredVersionCodeText = supportInfo.minRequiredVersionCode.toString()
        isForceUpdate = supportInfo.isForceUpdate
        updateTitle = supportInfo.updateTitle
        updateMessage = supportInfo.updateMessage
        releaseNotes = supportInfo.releaseNotes
        playStoreUrl = supportInfo.playStoreUrl
        telegramApkUrl = supportInfo.telegramApkUrl
    }

    val currentInstalledCode = BuildConfig.VERSION_CODE
    val currentInstalledName = BuildConfig.VERSION_NAME

    if (showPreviewDialog) {
        val parsedLatestCode = latestVersionCodeText.toIntOrNull() ?: supportInfo.latestVersionCode
        val parsedMinCode = minRequiredVersionCodeText.toIntOrNull() ?: supportInfo.minRequiredVersionCode
        val previewEntity = supportInfo.copy(
            latestVersionCode = parsedLatestCode,
            latestVersionName = latestVersionName,
            minRequiredVersionCode = parsedMinCode,
            isForceUpdate = isForceUpdate,
            updateTitle = updateTitle,
            updateMessage = updateMessage,
            releaseNotes = releaseNotes,
            playStoreUrl = playStoreUrl,
            telegramApkUrl = telegramApkUrl
        )
        AppUpdateDialog(
            supportInfo = previewEntity,
            onDismiss = { showPreviewDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Ilova versiyalari va Yangilanishlar boshqaruvi",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = SecondaryNavy
        )
        Text(
            text = "Play Market yoki Telegram orqali yangilanish chiqarganingizda barcha mijozlarga avtomatik xabar va majburiy yangilanish talabini yuboring.",
            fontSize = 12.sp,
            color = SlateGray,
            lineHeight = 16.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 1. Current App Status Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            border = BorderStroke(1.dp, BorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Joriy qurilmadagi versiya:",
                            fontSize = 12.sp,
                            color = SlateGray
                        )
                        Text(
                            text = "v$currentInstalledName (Build $currentInstalledCode)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (supportInfo.isForceUpdate || supportInfo.latestVersionCode > currentInstalledCode) WarningAmberLight else Color(0xFFE6F4EA)
                    ) {
                        Text(
                            text = if (supportInfo.isForceUpdate) "Majburiy rejim faol"
                            else if (supportInfo.latestVersionCode > currentInstalledCode) "Yangilanish tarqatilmoqda"
                            else "Normal holat",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (supportInfo.isForceUpdate) WarningAmber
                            else if (supportInfo.latestVersionCode > currentInstalledCode) PrimaryBlue
                            else Color(0xFF137333),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = BorderColor.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Serverdagi eng so‘nggi versiya kodi:", fontSize = 12.sp, color = SecondaryNavy)
                    Text(text = "${supportInfo.latestVersionCode} (v${supportInfo.latestVersionName})", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SecondaryNavy)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Minimal talab etilgan versiya kodi:", fontSize = 12.sp, color = SecondaryNavy)
                    Text(text = "${supportInfo.minRequiredVersionCode}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DangerRed)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Quick Action Presets
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = PrimaryBlueLight.copy(alpha = 0.4f)),
            border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "⚡ Tezkor amallar (1 tugma bilan sozlash)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = SecondaryNavy
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = {
                            val nextCode = (latestVersionCodeText.toIntOrNull() ?: currentInstalledCode) + 1
                            latestVersionCodeText = nextCode.toString()
                            minRequiredVersionCodeText = nextCode.toString()
                            isForceUpdate = true
                            latestVersionName = "1.$nextCode"
                            updateTitle = "Ilovaning yangi versiyasi chiqdi! 🚀"
                            updateMessage = "Gagarin Go ilovasida yangi imkoniyatlar va xavfsizlik yangilanishlari qo‘shildi. Davom etish uchun yangilang."
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Yangi versiyani majburiy qilish", fontSize = 11.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedButton(
                        onClick = {
                            isForceUpdate = false
                            minRequiredVersionCodeText = "1"
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Majburiylikni o‘chirish", fontSize = 11.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Edit Form
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            border = BorderStroke(1.dp, BorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Versiya sozlamalarini tahrirlash",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = SecondaryNavy
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = latestVersionCodeText,
                        onValueChange = { latestVersionCodeText = it },
                        label = { Text("Eng yangi Version Code *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = latestVersionName,
                        onValueChange = { latestVersionName = it },
                        label = { Text("Version Nomi *") },
                        placeholder = { Text("1.1") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = minRequiredVersionCodeText,
                    onValueChange = { minRequiredVersionCodeText = it },
                    label = { Text("Minimal talab etilgan Version Code (Eskilarni to‘xtatish)") },
                    supportingText = { Text("Agar foydalanuvchi versiyasi bundan past bo‘lsa, ilova majburiy yangilanish talab qiladi") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Force Update Switch
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isForceUpdate) DangerRedLight else SurfaceSubtle,
                    border = BorderStroke(1.dp, if (isForceUpdate) DangerRed.copy(alpha = 0.4f) else BorderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Barcha eski versiyalarni darhol to‘xtatish (Majburiy)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isForceUpdate) DangerRed else SecondaryNavy
                            )
                            Text(
                                text = if (isForceUpdate) "Foydalanuvchilar ilovani yangilamasdan davom eta olishmaydi" else "Foydalanuvchilar 'Keyinroq' tugmasi orqali ilovadan foydalanishi mumkin",
                                fontSize = 11.sp,
                                color = SlateGray
                            )
                        }

                        Switch(
                            checked = isForceUpdate,
                            onCheckedChange = { isForceUpdate = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = DangerRed
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = updateTitle,
                    onValueChange = { updateTitle = it },
                    label = { Text("Oyna sarlavhasi (Title)") },
                    placeholder = { Text("Ilovaning yangi versiyasi chiqdi! 🚀") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = updateMessage,
                    onValueChange = { updateMessage = it },
                    label = { Text("Foydalanuvchiga xabar matni") },
                    maxLines = 3,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = releaseNotes,
                    onValueChange = { releaseNotes = it },
                    label = { Text("Yangi versiyadagi o‘zgarishlar (Changelog)") },
                    placeholder = { Text("• Yangi imkoniyatlar...\n• Xatoliklar to‘g‘rilandi...") },
                    maxLines = 4,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = playStoreUrl,
                    onValueChange = { playStoreUrl = it },
                    label = { Text("Google Play Store havolasi") },
                    placeholder = { Text("https://play.google.com/store/apps/details?id=...") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = telegramApkUrl,
                    onValueChange = { telegramApkUrl = it },
                    label = { Text("Telegram kanal / APK yuklab olish havolasi") },
                    placeholder = { Text("https://t.me/gagarin_go_app yoki @gagarin_go_app") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { showPreviewDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("📲 Oynani ko‘rish (Test)", fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            val parsedLatestCode = latestVersionCodeText.toIntOrNull() ?: supportInfo.latestVersionCode
                            val parsedMinCode = minRequiredVersionCodeText.toIntOrNull() ?: supportInfo.minRequiredVersionCode
                            onSaveVersionConfig(
                                parsedLatestCode,
                                latestVersionName.trim().ifEmpty { "1.0" },
                                parsedMinCode,
                                isForceUpdate,
                                updateTitle.trim(),
                                updateMessage.trim(),
                                releaseNotes.trim(),
                                playStoreUrl.trim(),
                                telegramApkUrl.trim()
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(48.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Saqlash va Tarqatish", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// DIALOGS
// ----------------------------------------------------
@Composable
fun AdminChangePasswordDialog(
    onDismiss: () -> Unit,
    onSave: (currentPass: String, newPass: String, confirmPass: String) -> Unit
) {
    var currentPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }
    var passwordVisible by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Admin parolini o‘zgartirish",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = SecondaryNavy
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = currentPass,
                    onValueChange = { currentPass = it; errorText = null },
                    label = { Text("Joriy parol") },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = newPass,
                    onValueChange = { newPass = it; errorText = null },
                    label = { Text("Yangi parol (kamida 6 ta belgi)") },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = confirmPass,
                    onValueChange = { confirmPass = it; errorText = null },
                    label = { Text("Yangi parolni tasdiqlang") },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { passwordVisible = !passwordVisible }
                ) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = null,
                        tint = SlateGray,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (passwordVisible) "Parolni yashirish" else "Parolni ko‘rsatish",
                        fontSize = 12.sp,
                        color = SlateGray
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
                    if (currentPass.isEmpty()) {
                        errorText = "Joriy parolni kiriting"
                    } else if (newPass.length < 6) {
                        errorText = "Yangi parol kamida 6 ta belgidan iborat bo‘lishi kerak"
                    } else if (newPass != confirmPass) {
                        errorText = "Yangi parollar mos kelmadi"
                    } else {
                        onSave(currentPass, newPass, confirmPass)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("O‘zgartirish")
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

@Composable
fun CreateSellerDialog(
    onDismiss: () -> Unit,
    onCreate: (login: String, pass: String, storeName: String, owner: String, phone: String, email: String, role: String, address: String) -> Unit
) {
    var selectedRole by remember { mutableStateOf("SELLER") }
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var storeName by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("+998 ") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("Gagarin shahri") }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (selectedRole == "FOODS_SELLER") "Yangi Food Oshxona hisobini yaratish" else "Yangi Bozor Sotuvchisi yaratish",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = SecondaryNavy
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (selectedRole == "SELLER") PrimaryBlue else CardSurface,
                        border = BorderStroke(1.dp, if (selectedRole == "SELLER") PrimaryBlue else BorderColor),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedRole = "SELLER" }
                    ) {
                        Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                            Text(text = "🛒 Bozor Do‘koni", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (selectedRole == "SELLER") Color.White else SecondaryNavy)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (selectedRole == "FOODS_SELLER") Color(0xFFEA580C) else CardSurface,
                        border = BorderStroke(1.dp, if (selectedRole == "FOODS_SELLER") Color(0xFFEA580C) else BorderColor),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedRole = "FOODS_SELLER" }
                    ) {
                        Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                            Text(text = "🍔 Gagarin Food", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (selectedRole == "FOODS_SELLER") Color.White else SecondaryNavy)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = storeName,
                    onValueChange = { storeName = it; errorText = null },
                    label = { Text(if (selectedRole == "FOODS_SELLER") "Oshxona / Kafe nomi *" else "Do‘kon nomi *") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = ownerName,
                    onValueChange = { ownerName = it },
                    label = { Text("Egasi F.I.SH.") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = login,
                    onValueChange = { login = it; errorText = null },
                    label = { Text("Kirish logini *") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; errorText = null },
                    label = { Text("Parol (kamida 6 ta belgi) *") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Telefon raqami *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorText != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = errorText!!, color = DangerRed, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (storeName.isBlank()) {
                        errorText = "Do‘kon nomini kiriting"
                    } else if (login.trim().length < 3) {
                        errorText = "Login kamida 3 ta belgidan iborat bo‘lishi kerak"
                    } else if (password.length < 6) {
                        errorText = "Parol kamida 6 ta belgidan iborat bo‘lishi kerak"
                    } else {
                        onCreate(login, password, storeName, ownerName, phone, email, selectedRole, address)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Yaratish")
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

@Composable
fun ResetSellerPasswordDialog(
    seller: UserEntity,
    onDismiss: () -> Unit,
    onSave: (newPass: String) -> Unit
) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "${seller.storeName} parolini yangilash",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = SecondaryNavy
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Login: ${seller.login}", fontSize = 13.sp, color = SlateGray)
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it; errorText = null },
                    label = { Text("Yangi parol") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it; errorText = null },
                    label = { Text("Yangi parolni tasdiqlang") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorText != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = errorText!!, color = DangerRed, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (newPassword.length < 6) {
                        errorText = "Parol kamida 6 ta belgidan iborat bo‘lishi kerak"
                    } else if (newPassword != confirmPassword) {
                        errorText = "Parollar mos kelmadi"
                    } else {
                        onSave(newPassword)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Saqlash")
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

@Composable
fun AdminOrderDetailDialog(
    orderDetail: OrderDetail,
    onDismiss: () -> Unit,
    onUpdateStatus: (newStatus: String) -> Unit
) {
    val order = orderDetail.order
    var showStatusDropdown by remember { mutableStateOf(false) }
    val statuses = listOf("Yangi", "Tayyorlanmoqda", "Yetkazilmoqda", "Yetkazildi", "Bekor qilindi")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(text = "Buyurtma: ${order.orderNumber}", fontWeight = FontWeight.Bold, color = SecondaryNavy)
                Text(text = "Sana: ${Formatters.formatDate(order.createdAt)}", fontSize = 12.sp, color = SlateGray)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(text = "Buyurtma holatini boshqarish:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SecondaryNavy)
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
                        statuses.forEach { st ->
                            DropdownMenuItem(
                                text = { Text(st) },
                                onClick = {
                                    showStatusDropdown = false
                                    onUpdateStatus(st)
                                }
                            )
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = BorderColor)

                Text(text = "Xaridor ma’lumotlari:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SecondaryNavy)
                Text(text = "Ismi: ${order.customerName}", fontSize = 12.sp, color = SecondaryNavy)
                Text(text = "Telefon: ${order.customerPhone}", fontSize = 12.sp, color = PrimaryBlue, fontWeight = FontWeight.Bold)
                Text(text = "Manzil: ${order.deliveryAddress}", fontSize = 12.sp, color = SecondaryNavy)
                if (order.deliveryNotes.isNotBlank()) {
                    Text(text = "Izoh: ${order.deliveryNotes}", fontSize = 12.sp, color = SlateGray)
                }

                if (order.deliveryLatitude != 0.0 && order.deliveryLongitude != 0.0) {
                    Spacer(modifier = Modifier.height(8.dp))
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
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = BorderColor)

                Text(text = "Mahsulotlar:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SecondaryNavy)
                Spacer(modifier = Modifier.height(4.dp))

                orderDetail.items.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = item.productName, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = SecondaryNavy)
                            Text(text = "Sotuvchi: ${item.sellerName} | ${item.quantity} ${item.unit} × ${Formatters.formatPrice(item.unitPrice)}", fontSize = 11.sp, color = SlateGray)
                        }
                        Text(text = Formatters.formatPrice(item.itemTotal), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = BorderColor)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Yetkazib berish:", fontSize = 13.sp, color = SlateGray)
                    Text(text = "BEPUL (0 so‘m)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Jami:", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SecondaryNavy)
                    Text(text = Formatters.formatPrice(order.totalAmount), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
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
fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Yangi kategoriya qo‘shish", fontWeight = FontWeight.Bold, color = SecondaryNavy) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Kategoriya nomi") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onAdd(name.trim()) },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Qo‘shish")
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

@Composable
fun CustomerDetailAdminDialog(
    customer: UserEntity,
    orders: List<OrderDetail>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(text = customer.fullName.ifBlank { "Mijoz #${customer.id}" }, fontWeight = FontWeight.Bold, color = SecondaryNavy)
                Text(text = "Telefon: ${customer.phone}", fontSize = 12.sp, color = PrimaryBlue)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(text = "Saqlangan manzil: ${customer.savedAddress.ifBlank { "Gagarin shahri" }}", fontSize = 12.sp, color = SlateGray)
                Text(text = "Email: ${customer.email.ifBlank { "-" }}", fontSize = 12.sp, color = SlateGray)

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = BorderColor)

                Text(text = "Buyurtmalar tarixi (${orders.size} ta):", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SecondaryNavy)
                Spacer(modifier = Modifier.height(6.dp))

                if (orders.isEmpty()) {
                    Text(text = "Mijoz hali buyurtma bermagan.", fontSize = 12.sp, color = SlateGray)
                } else {
                    orders.forEach { orderDetail ->
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = CardSurface),
                            border = BorderStroke(1.dp, BorderColor),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = orderDetail.order.orderNumber, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                                    OrderStatusBadge(status = orderDetail.order.status)
                                }
                                Text(text = "Sana: ${Formatters.formatDate(orderDetail.order.createdAt)}", fontSize = 10.sp, color = SlateGray)
                                Text(text = "Summa: ${Formatters.formatPrice(orderDetail.order.totalAmount)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SecondaryNavy)
                            }
                        }
                    }
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
fun AddEditPromoBannerDialog(
    banner: PromoBannerEntity?,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        title: String,
        description: String,
        imageUrl: String,
        targetLink: String,
        startDate: Long,
        endDate: Long,
        badgeText: String,
        gradientType: String,
        isActive: Boolean
    ) -> Unit
) {
    val context = LocalContext.current

    var name by remember(banner) { mutableStateOf(banner?.name ?: "") }
    var title by remember(banner) { mutableStateOf(banner?.title ?: "") }
    var description by remember(banner) { mutableStateOf(banner?.description ?: "") }
    var currentImageUrl by remember(banner) { mutableStateOf(banner?.imageUrl ?: "") }
    var urlImageInput by remember(banner) { 
        mutableStateOf(if (banner?.imageUrl?.startsWith("http") == true) banner.imageUrl else "") 
    }
    var imageSourceMode by remember(banner) { 
        mutableStateOf(if (banner?.imageUrl?.startsWith("http") == true) "URL" else "GALLERY") 
    }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var imageValidationInfo by remember { mutableStateOf<ImageValidationResult?>(null) }
    var imageErrorMessage by remember { mutableStateOf<String?>(null) }

    var targetLink by remember(banner) { mutableStateOf(banner?.targetLink ?: "") }
    var durationDays by remember(banner) { mutableIntStateOf(30) }
    var badgeText by remember(banner) { mutableStateOf(banner?.badgeText ?: "AKSIYA") }
    var gradientType by remember(banner) { mutableStateOf(banner?.gradientType ?: "burgundy") }
    var isActive by remember(banner) { mutableStateOf(banner?.isActive ?: true) }
    var isSaving by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val validation = ImageStorageHelper.validateImageFile(context, uri)
            if (validation.isValid) {
                selectedImageUri = uri
                imageValidationInfo = validation
                imageErrorMessage = null
            } else {
                imageErrorMessage = validation.errorMessage ?: "Yaroqsiz rasm fayli"
            }
        }
    }

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (banner == null) Icons.Filled.AddPhotoAlternate else Icons.Filled.Edit,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (banner == null) "Yangi reklama / banner" else "Reklamani tahrirlash",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = SecondaryNavy
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // 1. RASM YUKLASH VA PREVIEW (GALEREYA YOKI HAVOLA ORQALI)
                Text(
                    text = "Banner rasmi:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = SecondaryNavy
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Manba tanlash (Galereya yoki Havola)
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
                            selectedContainerColor = PrimaryBlue,
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
                            selectedContainerColor = PrimaryBlue,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                val normalizedBannerUrl = if (urlImageInput.isNotBlank()) ImageStorageHelper.normalizeImageUrl(urlImageInput) else ""
                val previewUri = if (imageSourceMode == "URL") {
                    normalizedBannerUrl.ifBlank { if (selectedImageUri != null) selectedImageUri.toString() else currentImageUrl }
                } else {
                    selectedImageUri?.toString() ?: (if (normalizedBannerUrl.isNotBlank()) normalizedBannerUrl else currentImageUrl)
                }

                if (previewUri.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(115.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                    ) {
                        val resolvedBannerPreview = ImageStorageHelper.resolveImageModel(previewUri, context) ?: previewUri
                        SubcomposeAsyncImage(
                            model = resolvedBannerPreview,
                            contentDescription = "Banner preview",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                            loading = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(getBannerBrush(gradientType)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                                }
                            },
                            error = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(getBannerBrush(gradientType)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Rasmni yuklashda xatolik (URL yoki fayl xato)", color = Color.White, fontSize = 11.sp)
                                }
                            }
                        )

                        // Top indicator & actions
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color.Black.copy(alpha = 0.65f)
                            ) {
                                Text(
                                    text = if (imageSourceMode == "URL" && urlImageInput.isNotBlank()) "URL havola"
                                           else if (selectedImageUri != null) "Galereyadan tanlangan" 
                                           else "Mavjud rasm",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    selectedImageUri = null
                                    urlImageInput = ""
                                    currentImageUrl = ""
                                    imageValidationInfo = null
                                    imageErrorMessage = null
                                },
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                            ) {
                                Icon(imageVector = Icons.Filled.Close, contentDescription = "Rasmni tozalash", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    if (imageSourceMode == "GALLERY" && imageValidationInfo != null) {
                        imageValidationInfo?.let { info ->
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Filled.Check, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "O‘lcham: ${info.width}x${info.height} px (${String.format("%.1f", info.sizeBytes / 1024.0)} KB)",
                                    fontSize = 10.sp,
                                    color = SuccessGreen
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Tanlangan rejimga qarab kirish formasi
                if (imageSourceMode == "GALLERY") {
                    OutlinedButton(
                        onClick = { photoPickerLauncher.launch("image/*") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = CardSurface)
                    ) {
                        Icon(imageVector = Icons.Filled.AddPhotoAlternate, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (selectedImageUri != null || currentImageUrl.isNotBlank()) "Boshqa rasm tanlash (Galereya)" else "Galereyadan rasm tanlash",
                            color = PrimaryBlue,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    OutlinedTextField(
                        value = urlImageInput,
                        onValueChange = { 
                            urlImageInput = it 
                            imageErrorMessage = null
                        },
                        label = { Text("Google yoki internet rasm havolasi (URL)") },
                        placeholder = { Text("https://... yoki Googledan olingan havola") },
                        leadingIcon = {
                            Icon(Icons.Filled.Link, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                        },
                        trailingIcon = {
                            if (urlImageInput.isNotBlank()) {
                                IconButton(onClick = { urlImageInput = "" }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Tozalash", tint = SlateGray, modifier = Modifier.size(18.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (imageErrorMessage != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = imageErrorMessage!!,
                        color = DangerRed,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = BorderColor.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(12.dp))

                // 2. ASOSIY MAYDONLAR
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Reklama nomi / Kampaniya nomi") },
                    placeholder = { Text("Masalan: Bahorgi Chegirmalar") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Sarlavha (Katta matn) *") },
                    placeholder = { Text("Masalan: Barcha mahsulotlarga -30%!") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Tavsif (Qo‘shimcha ma’lumot)") },
                    placeholder = { Text("Masalan: Mirzacho‘l bozorida yangi hosil mevalari") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Havola yoki bo'lim
                OutlinedTextField(
                    value = targetLink,
                    onValueChange = { targetLink = it },
                    label = { Text("Havola yoki bog‘lanadigan sahifa") },
                    placeholder = { Text("bozor, foods, ads, jobs, services yoki https://...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(text = "Tezkor sahifa tanlash:", fontSize = 11.sp, color = SlateGray)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("bozor" to "Bozor", "foods" to "Foods", "ads" to "E'lonlar", "services" to "Ustalar", "jobs" to "Ishlar").forEach { (slug, label) ->
                        FilterChip(
                            selected = targetLink == slug,
                            onClick = { targetLink = if (targetLink == slug) "" else slug },
                            label = { Text(label, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryBlueLight,
                                selectedLabelColor = PrimaryBlue
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Yorliq (Badge)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = badgeText,
                        onValueChange = { badgeText = it },
                        label = { Text("Yorliq matni") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Amal qilish muddati:", fontSize = 11.sp, color = SlateGray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(7 to "7 kun", 30 to "30 kun", 90 to "90 kun").forEach { (days, lbl) ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (durationDays == days) PrimaryBlue else CardSurface,
                                    border = BorderStroke(1.dp, if (durationDays == days) PrimaryBlue else BorderColor),
                                    modifier = Modifier.clickable { durationDays = days }
                                ) {
                                    Text(
                                        text = lbl,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (durationDays == days) Color.White else SecondaryNavy,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Fon gradienti
                Text(text = "Fon rangi / Gradient turi:", fontSize = 11.sp, color = SlateGray)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "burgundy" to "Burgundy (Asosiy)",
                        "blue" to "Moviy",
                        "orange" to "To‘q sariq",
                        "emerald" to "Yashil",
                        "rose" to "Qizil"
                    ).forEach { (type, label) ->
                        Surface(
                            shape = CircleShape,
                            color = when (type) {
                                "orange" -> Color(0xFFEA580C)
                                "blue" -> Color(0xFF2563EB)
                                "emerald" -> Color(0xFF059669)
                                "rose" -> Color(0xFFE11D48)
                                else -> Color(0xFF8B1E3F)
                            },
                            border = if (gradientType == type) BorderStroke(3.dp, PrimaryBlue) else BorderStroke(1.dp, Color.White),
                            modifier = Modifier
                                .size(30.dp)
                                .clickable { gradientType = type }
                        ) {}
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Faol / Faol emas Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Reklama holati:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SecondaryNavy)
                        Text(
                            text = if (isActive) "Faol — mijozlarga darhol ko‘rinadi" else "To‘xtatilgan — ko‘rinmaydi",
                            fontSize = 11.sp,
                            color = if (isActive) SuccessGreen else DangerRed
                        )
                    }

                    Switch(
                        checked = isActive,
                        onCheckedChange = { isActive = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = SuccessGreen
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmedTitle = title.trim()
                    if (trimmedTitle.isEmpty()) {
                        return@Button
                    }
                    isSaving = true
                    var finalImageUrl = if (imageSourceMode == "URL") {
                        ImageStorageHelper.normalizeImageUrl(urlImageInput)
                    } else {
                        currentImageUrl
                    }

                    if (imageSourceMode == "GALLERY" && selectedImageUri != null) {
                        val base64 = ImageStorageHelper.compressAndEncodeImageToBase64(context, selectedImageUri!!, maxDimension = 900, quality = 80)
                        if (base64 != null) {
                            finalImageUrl = base64
                        } else {
                            val saveResult = ImageStorageHelper.saveBannerImage(context, selectedImageUri!!)
                            saveResult.onSuccess { savedPath ->
                                finalImageUrl = savedPath
                            }.onFailure { err ->
                                imageErrorMessage = err.localizedMessage ?: "Rasmni saqlashda xatolik yuz berdi"
                                isSaving = false
                                return@Button
                            }
                        }
                    } else if (imageSourceMode == "URL" && urlImageInput.isNotBlank()) {
                        finalImageUrl = ImageStorageHelper.normalizeImageUrl(urlImageInput)
                    }

                    val computedStartDate = banner?.startDate ?: System.currentTimeMillis()
                    val computedEndDate = computedStartDate + (durationDays.toLong() * 24 * 60 * 60 * 1000)

                    onSave(
                        name.trim().ifBlank { trimmedTitle },
                        trimmedTitle,
                        description.trim(),
                        finalImageUrl,
                        targetLink.trim(),
                        computedStartDate,
                        computedEndDate,
                        badgeText.trim().ifBlank { "REKLAMA" },
                        gradientType,
                        isActive
                    )
                },
                enabled = title.isNotBlank() && !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text("Saqlash va Tasdiqlash")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, enabled = !isSaving) {
                Text("Bekor qilish")
            }
        },
        shape = RoundedCornerShape(18.dp)
    )
}
