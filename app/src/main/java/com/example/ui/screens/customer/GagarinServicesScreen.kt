package com.example.ui.screens.customer

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ServiceMasterEntity
import com.example.data.local.entity.PromoBannerEntity
import com.example.ui.components.PromoBannerSection
import com.example.data.model.UserRole
import com.example.data.util.AdOwnershipHelper
import com.example.ui.components.StarGold
import com.example.ui.theme.BorderColor
import com.example.ui.theme.CardSurface
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DarkText
import com.example.ui.theme.LightBackground
import com.example.ui.theme.PrimaryBurgundy
import com.example.ui.theme.SecondaryNavy
import com.example.ui.theme.SecondaryText
import com.example.ui.theme.SlateGray
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SurfaceSubtle
import com.example.ui.viewmodels.AuthViewModel
import com.example.ui.viewmodels.EcosystemViewModel
import java.util.Locale
import kotlinx.coroutines.launch

val serviceCategories = listOf(
    "Barchasi",
    "Santexnik",
    "Elektrik",
    "Maishiy texnika",
    "Usta / Remont",
    "Konditsioner",
    "Mebel ustasi",
    "Tozalash",
    "Avto usta",
    "Repetitor",
    "Boshqa"
)

private fun normalizePhoneNumber(phone: String?): String {
    return phone?.replace("[^0-9]".toRegex(), "") ?: ""
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GagarinServicesScreen(
    ecosystemViewModel: EcosystemViewModel,
    authViewModel: AuthViewModel,
    promoBanners: List<PromoBannerEntity> = emptyList(),
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val session by authViewModel.session.collectAsState()
    val selectedCategory by ecosystemViewModel.selectedServiceCategory.collectAsState()
    val mastersList by ecosystemViewModel.serviceMasters.collectAsState()

    var showRegisterMasterDialog by remember { mutableStateOf(false) }
    var masterToEdit by remember { mutableStateOf<ServiceMasterEntity?>(null) }
    var masterToDelete by remember { mutableStateOf<ServiceMasterEntity?>(null) }
    var showAuthPromptDialog by remember { mutableStateOf(false) }
    var showAuthModal by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    if (showAuthPromptDialog) {
        AlertDialog(
            onDismissRequest = { showAuthPromptDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = null,
                    tint = Color(0xFF0891B2),
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Tizimga kirish talab qilinadi",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = SecondaryNavy
                )
            },
            text = {
                Text(
                    text = "Usta sifatida anketangizni joylashtirish uchun avval o‘z akkauntingizga kiring yoki ro‘yxatdan o‘ting.",
                    fontSize = 13.sp,
                    color = SecondaryNavy
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showAuthPromptDialog = false
                        showAuthModal = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0891B2))
                ) {
                    Text("Tizimga kirish", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAuthPromptDialog = false }) {
                    Text("Bekor qilish", color = SlateGray)
                }
            }
        )
    }

    if (showAuthModal) {
        ModalBottomSheet(
            onDismissRequest = { showAuthModal = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            CustomerAuthScreen(
                authViewModel = authViewModel,
                onAuthSuccess = {
                    showAuthModal = false
                    showRegisterMasterDialog = true
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Tizimga muvaffaqiyatli kirdingiz!")
                    }
                },
                onBack = { showAuthModal = false }
            )
        }
    }

    if (showRegisterMasterDialog) {
        RegisterMasterDialog(
            masterToEdit = null,
            initialPhone = session.user?.phone ?: "+998 ",
            initialName = session.user?.fullName ?: "",
            onDismiss = { showRegisterMasterDialog = false },
            onSubmit = { name, category, phone, exp, price, desc ->
                ecosystemViewModel.registerMaster(
                    userId = session.user?.id ?: 0L,
                    masterName = name,
                    category = category,
                    phone = phone,
                    experienceYears = exp,
                    priceRange = price,
                    description = desc
                ) { success, msg, newMasterId ->
                    showRegisterMasterDialog = false
                    if (success && newMasterId != null) {
                        AdOwnershipHelper.registerMyMaster(context, newMasterId)
                    }
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(msg)
                    }
                }
            }
        )
    }

    masterToEdit?.let { targetMaster ->
        RegisterMasterDialog(
            masterToEdit = targetMaster,
            initialPhone = targetMaster.phone,
            initialName = targetMaster.masterName,
            onDismiss = { masterToEdit = null },
            onSubmit = { name, category, phone, exp, price, desc ->
                val updated = targetMaster.copy(
                    masterName = name,
                    category = category,
                    phone = phone,
                    experienceYears = exp,
                    priceRange = price,
                    description = desc
                )
                ecosystemViewModel.updateServiceMaster(updated) { success, msg ->
                    masterToEdit = null
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(msg)
                    }
                }
            }
        )
    }

    masterToDelete?.let { master ->
        AlertDialog(
            onDismissRequest = { masterToDelete = null },
            title = { Text("Usta anketasini o‘chirish", fontWeight = FontWeight.Bold, color = SecondaryNavy) },
            text = { Text("\"${master.masterName}\" anketasini o‘chirishni tasdiqlaysizmi?") },
            confirmButton = {
                Button(
                    onClick = {
                        val id = master.id
                        masterToDelete = null
                        ecosystemViewModel.deleteServiceMaster(id) { success, msg ->
                            if (success) {
                                AdOwnershipHelper.removeMyMaster(context, id)
                            }
                            coroutineScope.launch { snackbarHostState.showSnackbar(msg) }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("O‘chirish", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { masterToDelete = null }) {
                    Text("Bekor qilish", color = SlateGray)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            Surface(
                color = CardSurface,
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Orqaga",
                            tint = DarkText
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Gagarin Ustalari",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkText
                        )
                        Text(
                            text = "Malakali ustalar va xizmatlar",
                            fontSize = 12.sp,
                            color = SecondaryText
                        )
                    }
                    Button(
                        onClick = {
                            if (!session.isLoggedIn) {
                                showAuthPromptDialog = true
                            } else {
                                showRegisterMasterDialog = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBurgundy),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Usta bo‘lish", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (!session.isLoggedIn) {
                        showAuthPromptDialog = true
                    } else {
                        showRegisterMasterDialog = true
                    }
                },
                containerColor = PrimaryBurgundy,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("register_master_fab")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Usta bo‘lib qo‘shilish", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        val servicesBanners = remember(promoBanners) {
            promoBanners.filter {
                it.actionTag.equals("SERVICES", ignoreCase = true) ||
                it.actionTag.equals("ALL", ignoreCase = true)
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = innerPadding.calculateBottomPadding() + 80.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize().background(LightBackground)
        ) {
            // Reklama bannerlari (Admin boshqaruvidagi Ustalar/Xizmatlar bo'limi reklamalari)
            if (servicesBanners.isNotEmpty()) {
                item {
                    PromoBannerSection(
                        banners = servicesBanners,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
            }

            // Categories
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(serviceCategories) { cat ->
                        val isSelected = cat == selectedCategory
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) PrimaryBurgundy else CardSurface,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) PrimaryBurgundy else BorderColor
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { ecosystemViewModel.selectServiceCategory(cat) }
                                .testTag("service_cat_${cat.lowercase()}")
                        ) {
                            Text(
                                text = cat,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else DarkText,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // Filtered masters / couriers list
            val displayList = if (selectedCategory == "🚴‍♂️ Kuryerlar") {
                mastersList.filter { it.category == "Kuryerlik" || it.masterName.contains("kuryer", ignoreCase = true) }
            } else {
                mastersList
            }

            items(displayList) { master ->
                val canEdit = AdOwnershipHelper.canManageMaster(context, master, session)
                val canDelete = AdOwnershipHelper.canDeleteMaster(context, master, session)

                ServiceMasterCard(
                    master = master,
                    canEdit = canEdit,
                    canDelete = canDelete,
                    onEdit = { masterToEdit = master },
                    onDelete = { masterToDelete = master },
                    onCall = { phone ->
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                        context.startActivity(intent)
                    }
                )
            }

            if (displayList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Ushbu toifada mutaxassislar anketasi hozircha topilmadi",
                            color = SlateGray,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun ServiceMasterCard(
    master: ServiceMasterEntity,
    canEdit: Boolean = false,
    canDelete: Boolean = false,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
    onCall: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth().testTag("master_item_${master.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(PrimaryBurgundy.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Person, contentDescription = null, tint = PrimaryBurgundy, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = master.masterName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = DarkText
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Outlined.Verified, contentDescription = "Tasdiqlangan", tint = PrimaryBurgundy, modifier = Modifier.size(16.dp))
                        }
                        Text(
                            text = "${master.category} • ${master.experienceYears} yillik tajriba",
                            fontSize = 11.sp,
                            color = SlateGray
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (canEdit) {
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier.size(32.dp).testTag("edit_master_btn_${master.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Tahrirlash",
                                tint = PrimaryBurgundy,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    if (canDelete) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(32.dp).testTag("delete_master_btn_${master.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "O‘chirish",
                                tint = DangerRed,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Rating
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = StarGold.copy(alpha = 0.15f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Icon(Icons.Filled.Star, contentDescription = null, tint = StarGold, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = String.format(Locale.US, "%.1f", master.rating),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color(0xFFB45309)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (master.description.isNotBlank()) {
                Text(
                    text = master.description,
                    fontSize = 12.sp,
                    color = DarkText.copy(alpha = 0.85f),
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Xizmat narxi: ${master.priceRange}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBurgundy
                )

                Button(
                    onClick = { onCall(master.phone) },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp).testTag("call_master_btn_${master.id}")
                ) {
                    Icon(Icons.Filled.Call, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Qo‘ng‘iroq qilish", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun RegisterMasterDialog(
    masterToEdit: ServiceMasterEntity? = null,
    initialPhone: String = "+998 ",
    initialName: String = "",
    onDismiss: () -> Unit,
    onSubmit: (name: String, category: String, phone: String, exp: Int, price: String, desc: String) -> Unit
) {
    var name by remember { mutableStateOf(masterToEdit?.masterName ?: initialName) }
    var selectedCat by remember { mutableStateOf(masterToEdit?.category ?: "Santexnik") }
    var phone by remember { mutableStateOf(masterToEdit?.phone ?: initialPhone) }
    var expText by remember { mutableStateOf((masterToEdit?.experienceYears ?: 5).toString()) }
    var priceRange by remember { mutableStateOf(masterToEdit?.priceRange ?: "Kelishilgan holda") }
    var description by remember { mutableStateOf(masterToEdit?.description ?: "") }

    val selectableCats = serviceCategories.filter { it != "Barchasi" }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (masterToEdit != null) "✏️ Usta anketasini tahrirlash" else "🏠 Usta sifatida ro‘yxatdan o‘tish",
                fontWeight = FontWeight.Bold,
                color = SecondaryNavy
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Ism-familiyangiz *") },
                    placeholder = { Text("Masalan: Rustam Usta") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Text(
                    text = "Xizmat toifasini tanlang *",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SecondaryNavy
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(selectableCats) { cat ->
                        val isSel = cat == selectedCat
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSel) Color(0xFF0891B2) else SurfaceSubtle,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSel) Color(0xFF0891B2) else BorderColor
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { selectedCat = cat }
                        ) {
                            Text(
                                text = cat,
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) Color.White else SecondaryNavy,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Telefon raqamingiz *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
                OutlinedTextField(
                    value = priceRange,
                    onValueChange = { priceRange = it },
                    label = { Text("Narxlar diapazoni") },
                    placeholder = { Text("Masalan: 50 000 so'mdan boshlab") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Bajaradigan xizmatlaringiz haqida") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val exp = expText.toIntOrNull() ?: 1
                    onSubmit(name, selectedCat, phone, exp, priceRange, description)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBurgundy),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("submit_master_btn")
            ) {
                Text(if (masterToEdit != null) "Saqlash" else "Ro‘yxatdan o‘tish", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Bekor qilish", color = SlateGray)
            }
        }
    )
}

