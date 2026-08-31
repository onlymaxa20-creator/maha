package com.example.ui.screens.customer

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.FoodOrderEntity
import com.example.data.local.entity.SupportInfoEntity
import com.example.data.local.entity.UserEntity
import com.example.data.model.OrderDetail
import com.example.ui.components.EmptyStateView
import com.example.ui.components.Formatters
import com.example.ui.components.OrderStatusBadge
import com.example.ui.components.map.OrderLocationSection
import com.example.ui.theme.BorderColor
import com.example.ui.theme.CardSurface
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DangerRedLight
import com.example.ui.theme.LightBackground
import com.example.ui.theme.OrangeAmber
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueLight
import com.example.ui.theme.PrimaryBurgundy
import com.example.ui.theme.SecondaryNavy
import com.example.ui.theme.SlateGray
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SurfaceSubtle
import com.example.ui.viewmodels.AuthViewModel
import com.example.ui.viewmodels.FoodStoreViewModel
import com.example.ui.viewmodels.StoreViewModel
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    storeViewModel: StoreViewModel,
    foodStoreViewModel: FoodStoreViewModel? = null,
    onNavigateToAuth: () -> Unit,
    onNavigateToSeller: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val session by authViewModel.session.collectAsState()
    val orders by storeViewModel.customerOrders.collectAsState()
    val foodOrders by (foodStoreViewModel?.customerFoodOrders?.collectAsState() ?: remember { mutableStateOf(emptyList()) })
    val supportInfo by storeViewModel.supportInfo.collectAsState()
    val authError by authViewModel.authError.collectAsState()
    val authSuccess by authViewModel.authSuccessMessage.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedOrderCategoryTab by remember { mutableIntStateOf(0) } // 0: Bozor, 1: Gagarin Taomlar
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showManualUpdateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(session.user?.id) {
        storeViewModel.setCustomerId(session.user?.id)
        foodStoreViewModel?.setCurrentCustomerId(session.user?.id)
    }

    if (showManualUpdateDialog) {
        com.example.ui.components.AppUpdateDialog(
            supportInfo = supportInfo,
            onDismiss = { showManualUpdateDialog = false }
        )
    }

    LaunchedEffect(authError, authSuccess) {
        authError?.let {
            snackbarHostState.showSnackbar(it)
            authViewModel.clearMessages()
        }
        authSuccess?.let {
            snackbarHostState.showSnackbar(it)
            authViewModel.clearMessages()
        }
    }

    if (!session.isLoggedIn || session.user == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Profil va boshqaruv",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = SecondaryNavy
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = CardSurface)
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LightBackground)
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = PrimaryBlueLight,
                            modifier = Modifier.size(60.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Xarid qilish hisobi",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = SecondaryNavy
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Buyurtmalaringizni ko‘rish va xarid qilish uchun tizimga kiring.",
                            fontSize = 13.sp,
                            color = SlateGray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = onNavigateToAuth,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryBlue,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("profile_login_btn")
                        ) {
                            Text(
                                text = "Mijoz sifatida kirish / Ro‘yxatdan o‘tish",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Business & Admin Portals Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Biznes va Boshqaruv Panellari",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = SecondaryNavy
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Do‘koningizni boshqarish yoki tizim ma’murligi uchun:",
                            fontSize = 12.sp,
                            color = SlateGray
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = onNavigateToSeller,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryBurgundy,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("profile_seller_portal_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Storefront,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sotuvchi Kabineti (Bozor & Mahsulotlar)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = onNavigateToAdmin,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF6D28D9),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("profile_admin_portal_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AdminPanelSettings,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Admin Boshqaruv Paneli",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Support Section for Guest
                SupportInfoCard(supportInfo = supportInfo)

                Spacer(modifier = Modifier.height(16.dp))

                // App Version & Update Status for Guest
                AppVersionInfoCard(
                    supportInfo = supportInfo,
                    onCheckUpdate = { showManualUpdateDialog = true }
                )

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
        return
    }

    val user = session.user!!
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var selectedOrderDetail by remember { mutableStateOf<OrderDetail?>(null) }

    if (showEditProfileDialog) {
        EditProfileDialog(
            user = user,
            onDismiss = { showEditProfileDialog = false },
            onSave = { name, phone, address ->
                authViewModel.updateProfile(name, phone, address) {
                    showEditProfileDialog = false
                }
            }
        )
    }

    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            onDismiss = { showChangePasswordDialog = false },
            onConfirm = { oldP, newP, confP ->
                authViewModel.changePassword(oldP, newP, confP) {
                    showChangePasswordDialog = false
                }
            }
        )
    }

    if (selectedOrderDetail != null) {
        CustomerOrderDetailDialog(
            orderDetail = selectedOrderDetail!!,
            onDismiss = { selectedOrderDetail = null }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profil va sozlamalar",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryNavy
                    )
                },
                actions = {
                    IconButton(
                        onClick = { authViewModel.logout() },
                        modifier = Modifier.testTag("logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ExitToApp,
                            contentDescription = "Chiqish",
                            tint = DangerRed
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CardSurface)
            )
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
                    text = { Text("Mening hisobim", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Buyurtmalar tarixi (${orders.size})", fontWeight = FontWeight.SemiBold) }
                )
            }

            if (selectedTab == 0) {
                // Customer Profile & Settings
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Profile Header Card
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = PrimaryBlueLight,
                                    modifier = Modifier.size(56.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Filled.Person,
                                            contentDescription = null,
                                            tint = PrimaryBlue,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = user.fullName.ifBlank { "Xaridor" },
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 17.sp
                                        ),
                                        color = SecondaryNavy
                                    )
                                    Text(
                                        text = user.email.ifBlank { user.login },
                                        fontSize = 13.sp,
                                        color = SlateGray
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = PrimaryBlueLight,
                                        modifier = Modifier.padding(top = 4.dp)
                                    ) {
                                        Text(
                                            text = "Mijoz (Customer)",
                                            color = PrimaryBlue,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { showEditProfileDialog = true },
                                    modifier = Modifier.testTag("edit_profile_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Edit,
                                        contentDescription = "Tahrirlash",
                                        tint = PrimaryBlue
                                    )
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = BorderColor)

                            // Contact info rows
                            ProfileInfoRow(
                                icon = Icons.Outlined.Call,
                                title = "Telefon raqami:",
                                value = user.phone.ifBlank { "Kiritilmagan" }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            ProfileInfoRow(
                                icon = Icons.Outlined.Home,
                                title = "Saqlangan yetkazib berish manzili:",
                                value = user.savedAddress.ifBlank { "Gagarin shahri, Mirzacho‘l tumani" }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            ProfileInfoRow(
                                icon = Icons.Outlined.Email,
                                title = "Elektron pochta:",
                                value = user.email.ifBlank { "-" }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Account Settings Buttons
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Xavfsizlik va sozlamalar",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = SecondaryNavy
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedButton(
                                onClick = { showChangePasswordDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("change_password_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Lock,
                                    contentDescription = null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Parolni o‘zgartirish", color = SecondaryNavy)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Quick access to Seller / Admin Login
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Boshqa panellarga o‘tish",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = SecondaryNavy
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Sotuvchi yoki Administrator sifatida kirish uchun:",
                                fontSize = 12.sp,
                                color = SlateGray
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(modifier = Modifier.fillMaxWidth()) {
                                Button(
                                    onClick = onNavigateToSeller,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = PrimaryBlue,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Storefront,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Sotuvchi paneli",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Button(
                                    onClick = onNavigateToAdmin,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF6D28D9),
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.AdminPanelSettings,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Admin paneli",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Support Section (Editable by Admin)
                    SupportInfoCard(supportInfo = supportInfo)

                    Spacer(modifier = Modifier.height(16.dp))

                    // App Version & Update Status Card
                    AppVersionInfoCard(
                        supportInfo = supportInfo,
                        onCheckUpdate = { showManualUpdateDialog = true }
                    )

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
            } else {
                // Order History List (Market Orders and Gagarin Taomlar Orders)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Category selector for orders
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedOrderCategoryTab == 0,
                            onClick = { selectedOrderCategoryTab = 0 },
                            label = { Text("🛒 Bozor buyurtmalari (${orders.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryBlue,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                        FilterChip(
                            selected = selectedOrderCategoryTab == 1,
                            onClick = { selectedOrderCategoryTab = 1 },
                            label = { Text("🍔 Gagarin Taomlar (${foodOrders.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = OrangeAmber,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    if (selectedOrderCategoryTab == 0) {
                        if (orders.isEmpty()) {
                            EmptyStateView(
                                title = "Bozor xaridlari yo‘q",
                                description = "Siz hali bozor mahsulotlaridan buyurtma bermadingiz.",
                                icon = Icons.Outlined.ReceiptLong,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                orders.forEach { orderDetail ->
                                    CustomerOrderCard(
                                        orderDetail = orderDetail,
                                        onClick = { selectedOrderDetail = orderDetail }
                                    )
                                }
                            }
                        }
                    } else {
                        if (foodOrders.isEmpty()) {
                            EmptyStateView(
                                title = "Taom buyurtmalari yo‘q",
                                description = "Gagarin Taomlar bo‘limidan sevimli taomlaringizga buyurtma bering.",
                                icon = Icons.Outlined.Restaurant,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                foodOrders.forEach { foodOrder ->
                                    CustomerFoodOrderCard(foodOrder = foodOrder)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String
) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PrimaryBlue,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = title, fontSize = 12.sp, color = SlateGray)
            Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = SecondaryNavy)
        }
    }
}

@Composable
fun CustomerOrderCard(
    orderDetail: OrderDetail,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val order = orderDetail.order
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("order_card_${order.id}")
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
                text = "Sana: ${Formatters.formatDate(order.createdAt)}",
                fontSize = 12.sp,
                color = SlateGray
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Manzil: ${order.deliveryAddress}",
                fontSize = 12.sp,
                color = SecondaryNavy
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = BorderColor)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${orderDetail.items.size} ta mahsulot",
                    fontSize = 13.sp,
                    color = SlateGray
                )
                Text(
                    text = Formatters.formatPrice(order.totalAmount),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue
                )
            }
        }
    }
}

private data class FoodStatusInfo(
    val label: String,
    val color: Color,
    val background: Color,
    val desc: String,
    val step: Int
)

@Composable
fun FoodOrderStatusStepper(currentStep: Int) {
    val steps = listOf("Qabul", "Tayyorlanmoqda", "Yetkazilmoqda", "Yetkazildi")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, name ->
            val isPassedOrCurrent = (currentStep >= index + 1) || (index == 0 && currentStep >= 0)
            val isCurrent = (currentStep == index + 1) || (index == 0 && currentStep == 0)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(
                            if (isPassedOrCurrent) OrangeAmber else Color(0xFFE0E0E0),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isPassedOrCurrent) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = name,
                    fontSize = 9.sp,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                    color = if (isPassedOrCurrent) OrangeAmber else SlateGray,
                    maxLines = 1
                )
            }
            if (index < steps.lastIndex) {
                Box(
                    modifier = Modifier
                        .weight(0.5f)
                        .height(2.dp)
                        .background(if (currentStep > index + 1) OrangeAmber else Color(0xFFE0E0E0))
                )
            }
        }
    }
}

@Composable
fun CustomerFoodOrderCard(
    foodOrder: FoodOrderEntity,
    modifier: Modifier = Modifier
) {
    val formatter = remember { NumberFormat.getNumberInstance(Locale("uz", "UZ")) }

    val statusInfo = when (foodOrder.status) {
        "YANGI" -> FoodStatusInfo(
            label = "Yangi (Oshxonaga yuborildi)",
            color = Color(0xFFE65100),
            background = Color(0xFFFFF3E0),
            desc = "⏳ Buyurtmangiz oshxonaga yuborildi. Oshpaz tez orada qabul qiladi.",
            step = 0
        )
        "QABUL_QILINDI" -> FoodStatusInfo(
            label = "Qabul qilindi",
            color = Color(0xFF1565C0),
            background = Color(0xFFE3F2FD),
            desc = "✅ Oshxona buyurtmangizni qabul qildi va tayyorlashga kirishmoqda.",
            step = 1
        )
        "TAYYORLANMOQDA" -> FoodStatusInfo(
            label = "Tayyorlanmoqda",
            color = Color(0xFF6A1B9A),
            background = Color(0xFFF3E5F5),
            desc = "🍳 Oshpaz taomingizni mehr bilan tayyorlamoqda.",
            step = 2
        )
        "TAYYOR" -> FoodStatusInfo(
            label = "Tayyor",
            color = Color(0xFF00838F),
            background = Color(0xFFE0F7FA),
            desc = "🍱 Taomingiz tayyor bo‘ldi va kuryerga topshirilmoqda.",
            step = 2
        )
        "YETKAZILMOQDA" -> FoodStatusInfo(
            label = "Yetkazilmoqda",
            color = Color(0xFF2E7D32),
            background = Color(0xFFE8F5E9),
            desc = "🛵 Kuryer taomingizni olib sizning manzilingizga yo‘lga chiqdi!",
            step = 3
        )
        "YETKAZILDI" -> FoodStatusInfo(
            label = "Yetkazildi",
            color = Color(0xFF1B5E20),
            background = Color(0xFFE8F5E9),
            desc = "🎉 Taomingiz yetkazib berildi. Yoqimli ishtaha!",
            step = 4
        )
        "BEKOR_QILINDI" -> FoodStatusInfo(
            label = "Bekor qilindi",
            color = DangerRed,
            background = DangerRedLight,
            desc = "❌ Buyurtma bekor qilindi. ${foodOrder.rejectionReason}",
            step = -1
        )
        else -> FoodStatusInfo(
            label = foodOrder.status.replace("_", " "),
            color = OrangeAmber,
            background = Color(0xFFFFF8E1),
            desc = "Buyurtmangiz holati yangilandi: ${foodOrder.status}",
            step = 0
        )
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
        modifier = modifier
            .fillMaxWidth()
            .testTag("food_order_card_${foodOrder.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🍔", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = foodOrder.restaurantName.ifBlank { "Gagarin Taomlar" },
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = SecondaryNavy
                    )
                }
                Text(
                    text = "Buyurtma #${foodOrder.id}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = PrimaryBlue
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Status Badge
            Surface(
                color = statusInfo.background,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, statusInfo.color.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(statusInfo.color, shape = CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = statusInfo.label,
                        color = statusInfo.color,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            // Real-time Visual Progress Stepper (if not cancelled)
            if (statusInfo.step >= 0) {
                Spacer(modifier = Modifier.height(10.dp))
                FoodOrderStatusStepper(currentStep = statusInfo.step)
            }

            // Descriptive Message
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = LightBackground,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = statusInfo.desc,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = SecondaryNavy,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Taomlar: ${foodOrder.itemsSummary}",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = SecondaryNavy
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Manzil: ${foodOrder.deliveryAddress}",
                fontSize = 12.sp,
                color = SlateGray
            )

            if (foodOrder.customerNote.isNotBlank()) {
                Text(
                    text = "Mo‘ljal / Izoh: ${foodOrder.customerNote}",
                    fontSize = 11.sp,
                    color = SlateGray
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = BorderColor)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "100% BEPUL yetkazish • Naqd pul",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Text(
                    text = "${formatter.format(foodOrder.totalPrice.toLong())} so‘m",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = OrangeAmber
                )
            }
        }
    }
}

@Composable
fun CustomerOrderDetailDialog(
    orderDetail: OrderDetail,
    onDismiss: () -> Unit
) {
    val order = orderDetail.order
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Buyurtma: ${order.orderNumber}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = SecondaryNavy
                )
                Text(
                    text = Formatters.formatDate(order.createdAt),
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Holati:", fontSize = 13.sp, color = SlateGray)
                    OrderStatusBadge(status = order.status)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Yetkazib berish:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SecondaryNavy
                )
                Text(
                    text = "${order.deliveryCity}, ${order.deliveryDistrict}\n${order.deliveryAddress}",
                    fontSize = 13.sp,
                    color = SecondaryNavy
                )
                if (order.deliveryNotes.isNotBlank()) {
                    Text(
                        text = "Izoh: ${order.deliveryNotes}",
                        fontSize = 12.sp,
                        color = SlateGray
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Telefon: ${order.customerPhone}",
                    fontSize = 13.sp,
                    color = SecondaryNavy
                )

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

                Text(
                    text = "Mahsulotlar ro‘yxati:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = SecondaryNavy
                )
                Spacer(modifier = Modifier.height(6.dp))

                orderDetail.items.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.productName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = SecondaryNavy
                            )
                            Text(
                                text = "${item.quantity} ${item.unit} × ${Formatters.formatPrice(item.unitPrice)}",
                                fontSize = 11.sp,
                                color = SlateGray
                            )
                        }
                        Text(
                            text = Formatters.formatPrice(item.itemTotal),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryBlue
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = BorderColor)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Yetkazib berish:", fontSize = 13.sp, color = SlateGray)
                    Text(text = "BEPUL", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
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
fun EditProfileDialog(
    user: UserEntity,
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, address: String) -> Unit
) {
    var name by remember { mutableStateOf(user.fullName) }
    var phone by remember { mutableStateOf(user.phone) }
    var address by remember { mutableStateOf(user.savedAddress) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Profilni tahrirlash",
                fontWeight = FontWeight.Bold,
                color = SecondaryNavy
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Ism-familiya") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Telefon raqami") },
                    placeholder = { Text("+998 90 123 45 67") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Yetkazib berish manzili") },
                    placeholder = { Text("Gagarin shahri, ko‘cha, uy") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, phone, address) },
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
fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (oldPass: String, newPass: String, confirmPass: String) -> Unit
) {
    var oldPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Parolni o‘zgartirish",
                fontWeight = FontWeight.Bold,
                color = SecondaryNavy
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = oldPass,
                    onValueChange = { oldPass = it },
                    label = { Text("Eski parol") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = newPass,
                    onValueChange = { newPass = it },
                    label = { Text("Yangi parol (kamida 6 ta)") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = confirmPass,
                    onValueChange = { confirmPass = it },
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
                    if (newPass.length < 6) {
                        errorText = "Yangi parol kamida 6 ta belgidan iborat bo‘lishi kerak"
                    } else if (newPass != confirmPass) {
                        errorText = "Yangi parollar mos kelmadi"
                    } else {
                        onConfirm(oldPass, newPass, confirmPass)
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
fun SupportInfoCard(
    supportInfo: SupportInfoEntity,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color(0xFFEFF6FF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Headphones,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Mijozlarni qo‘llab-quvvatlash",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = SecondaryNavy
                    )
                    Text(
                        text = "Savol va yordam uchun aloqa markazi",
                        fontSize = 12.sp,
                        color = SlateGray
                    )
                }
            }

            if (supportInfo.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = supportInfo.description,
                    fontSize = 12.sp,
                    color = SecondaryNavy.copy(alpha = 0.85f),
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = BorderColor.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(12.dp))

            // Phone Contact 1
            if (supportInfo.phone.isNotBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${supportInfo.phone.replace(" ", "")}"))
                            try {
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        }
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Phone,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Asosiy telefon raqam",
                            fontSize = 11.sp,
                            color = SlateGray
                        )
                        Text(
                            text = supportInfo.phone,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SecondaryNavy
                        )
                    }
                    Text(
                        text = "Qo‘ng‘iroq",
                        fontSize = 12.sp,
                        color = PrimaryBlue,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Phone Contact 2 (Secondary)
            if (supportInfo.secondaryPhone.isNotBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${supportInfo.secondaryPhone.replace(" ", "")}"))
                            try {
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        }
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Call,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Qo‘shimcha raqam",
                            fontSize = 11.sp,
                            color = SlateGray
                        )
                        Text(
                            text = supportInfo.secondaryPhone,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SecondaryNavy
                        )
                    }
                    Text(
                        text = "Qo‘ng‘iroq",
                        fontSize = 12.sp,
                        color = SuccessGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Telegram Support
            if (supportInfo.telegramUsername.isNotBlank()) {
                val cleanTg = supportInfo.telegramUsername.removePrefix("@").trim()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/$cleanTg"))
                            try {
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        }
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = null,
                        tint = Color(0xFF0284C7),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Telegram orqali yordam",
                            fontSize = 11.sp,
                            color = SlateGray
                        )
                        Text(
                            text = "@$cleanTg",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF0284C7)
                        )
                    }
                    Text(
                        text = "Yozish",
                        fontSize = 12.sp,
                        color = Color(0xFF0284C7),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Telegram Channel
            if (supportInfo.telegramChannel.isNotBlank()) {
                val cleanChannel = supportInfo.telegramChannel.removePrefix("@").trim()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/$cleanChannel"))
                            try {
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        }
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Campaign,
                        contentDescription = null,
                        tint = Color(0xFF7C3AED),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Rasmiy kanalimiz",
                            fontSize = 11.sp,
                            color = SlateGray
                        )
                        Text(
                            text = "@$cleanChannel",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF7C3AED)
                        )
                    }
                    Text(
                        text = "Kanalga o‘tish",
                        fontSize = 12.sp,
                        color = Color(0xFF7C3AED),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Working hours & Address
            if (supportInfo.workingHours.isNotBlank() || supportInfo.address.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider(color = BorderColor.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(8.dp))

                if (supportInfo.workingHours.isNotBlank()) {
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Schedule,
                            contentDescription = null,
                            tint = SlateGray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = supportInfo.workingHours,
                            fontSize = 12.sp,
                            color = SlateGray
                        )
                    }
                }

                if (supportInfo.address.isNotBlank()) {
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = DangerRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = supportInfo.address,
                            fontSize = 12.sp,
                            color = SlateGray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppVersionInfoCard(
    supportInfo: SupportInfoEntity,
    onCheckUpdate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentVersionCode = com.example.BuildConfig.VERSION_CODE
    val currentVersionName = com.example.BuildConfig.VERSION_NAME
    val hasUpdate = supportInfo.latestVersionCode > currentVersionCode
    val isMandatory = currentVersionCode < supportInfo.minRequiredVersionCode || (supportInfo.isForceUpdate && hasUpdate)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                color = if (hasUpdate) (if (isMandatory) DangerRedLight else PrimaryBlueLight) else Color(0xFFE6F4EA),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (hasUpdate) Icons.Outlined.SystemUpdate else Icons.Outlined.Verified,
                            contentDescription = null,
                            tint = if (hasUpdate) (if (isMandatory) DangerRed else PrimaryBlue) else Color(0xFF137333),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "Gagarin Go",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = SecondaryNavy
                        )
                        Text(
                            text = "Joriy versiya: v$currentVersionName (kod: $currentVersionCode)",
                            fontSize = 12.sp,
                            color = SlateGray
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (hasUpdate) (if (isMandatory) DangerRedLight else PrimaryBlueLight) else Color(0xFFE6F4EA)
                ) {
                    Text(
                        text = if (hasUpdate) (if (isMandatory) "Majburiy yangilanish!" else "Yangi v${supportInfo.latestVersionName}") else "Eng so‘nggi",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (hasUpdate) (if (isMandatory) DangerRed else PrimaryBlue) else Color(0xFF137333),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (hasUpdate) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isMandatory) DangerRedLight else PrimaryBlueLight,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Yangi imkoniyatlar bilan Gagarin Go v${supportInfo.latestVersionName} tayyor!",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isMandatory) DangerRed else PrimaryBlue
                        )
                        if (supportInfo.releaseNotes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = supportInfo.releaseNotes,
                                fontSize = 11.sp,
                                color = SecondaryNavy,
                                maxLines = 3,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onCheckUpdate,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isMandatory) DangerRed else PrimaryBlue
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("check_update_btn")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.SystemUpdate,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Ilovani yangilash (Play Market / APK)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                OutlinedButton(
                    onClick = onCheckUpdate,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .testTag("check_update_btn")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.SystemUpdate,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Yangilanishlarni tekshirish",
                        fontSize = 13.sp,
                        color = SecondaryNavy
                    )
                }
            }
        }
    }
}
