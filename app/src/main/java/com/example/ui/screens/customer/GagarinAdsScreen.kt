package com.example.ui.screens.customer

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.data.local.entity.ClassifiedAdEntity
import com.example.data.local.entity.PromoBannerEntity
import com.example.ui.components.PromoBannerSection
import com.example.data.model.UserRole
import com.example.data.util.AdOwnershipHelper
import com.example.data.util.ImageStorageHelper
import com.example.data.util.ImageValidationResult
import com.example.ui.components.EmptyStateView
import com.example.ui.components.Formatters
import com.example.ui.theme.BorderColor
import com.example.ui.theme.CardSurface
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DarkText
import com.example.ui.theme.LightBackground
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBurgundy
import com.example.ui.theme.SecondaryNavy
import com.example.ui.theme.SecondaryText
import com.example.ui.theme.SlateGray
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SurfaceSubtle
import com.example.ui.viewmodels.AuthViewModel
import com.example.ui.viewmodels.EcosystemViewModel
import kotlinx.coroutines.launch

val adCategories = listOf(
    "Barchasi",
    "Avtomobil",
    "Ko'chmas mulk",
    "Elektronika & Telefon",
    "Kiyim & Poyabzal",
    "Chorva & Qishloq xo'jaligi",
    "Mebel & Jihozlar",
    "Xizmatlar",
    "Boshqa"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GagarinAdsScreen(
    ecosystemViewModel: EcosystemViewModel,
    authViewModel: AuthViewModel,
    promoBanners: List<PromoBannerEntity> = emptyList(),
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val session by authViewModel.session.collectAsState()
    val selectedCategory by ecosystemViewModel.selectedAdCategory.collectAsState()
    val adsList by ecosystemViewModel.ads.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showPostAdDialog by remember { mutableStateOf(false) }
    var adToEdit by remember { mutableStateOf<ClassifiedAdEntity?>(null) }
    var selectedAdDetail by remember { mutableStateOf<ClassifiedAdEntity?>(null) }
    var adToDelete by remember { mutableStateOf<ClassifiedAdEntity?>(null) }
    var showAuthPromptDialog by remember { mutableStateOf(false) }
    var showAuthModal by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Filter ads by search query and category
    val filteredAds = remember(adsList, searchQuery) {
        if (searchQuery.isBlank()) adsList
        else adsList.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.description.contains(searchQuery, ignoreCase = true) ||
            it.location.contains(searchQuery, ignoreCase = true) ||
            it.authorName.contains(searchQuery, ignoreCase = true)
        }
    }

    // Require Login Dialog
    if (showAuthPromptDialog) {
        AlertDialog(
            onDismissRequest = { showAuthPromptDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = null,
                    tint = Color(0xFFDC2626),
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
                    text = "Gagarin bepul e'lonlar bo‘limiga e'lon joylashtirish uchun avval o‘z akkauntingizga kiring yoki ro‘yxatdan o‘ting.",
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    modifier = Modifier.testTag("auth_prompt_login_btn")
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

    // Bottom Sheet for Customer Login / Register
    if (showAuthModal) {
        ModalBottomSheet(
            onDismissRequest = { showAuthModal = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            CustomerAuthScreen(
                authViewModel = authViewModel,
                onAuthSuccess = {
                    showAuthModal = false
                    showPostAdDialog = true
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Tizimga muvaffaqiyatli kirdingiz! Endi e'lon berishingiz mumkin.")
                    }
                },
                onBack = { showAuthModal = false }
            )
        }
    }

    // Post Ad Dialog
    if (showPostAdDialog) {
        PostClassifiedAdDialog(
            initialAuthor = session.user?.fullName ?: "",
            initialPhone = session.user?.phone ?: "+998 90 ",
            adToEdit = null,
            onDismiss = { showPostAdDialog = false },
            onSubmit = { title, cat, price, isNeg, loc, desc, imgUri, phone, author ->
                ecosystemViewModel.postAd(
                    userId = session.user?.id ?: 0L,
                    authorName = author,
                    authorPhone = phone,
                    title = title,
                    category = cat,
                    price = price,
                    isNegotiable = isNeg,
                    location = loc,
                    description = desc,
                    imageUri = imgUri
                ) { success, msg, newAdId ->
                    showPostAdDialog = false
                    if (success && newAdId != null) {
                        AdOwnershipHelper.registerMyAd(context, newAdId)
                    }
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(msg)
                    }
                }
            }
        )
    }

    // Edit Ad Dialog (Strictly for Ad Author or Admin)
    if (adToEdit != null) {
        val targetAd = adToEdit!!
        PostClassifiedAdDialog(
            initialAuthor = targetAd.authorName,
            initialPhone = targetAd.authorPhone,
            adToEdit = targetAd,
            onDismiss = { adToEdit = null },
            onSubmit = { title, cat, price, isNeg, loc, desc, imgUri, phone, author ->
                val updatedAd = targetAd.copy(
                    title = title,
                    category = cat,
                    price = price,
                    isNegotiable = isNeg,
                    location = loc,
                    description = desc,
                    imageUri = imgUri,
                    authorName = author,
                    authorPhone = phone
                )
                ecosystemViewModel.updateAd(updatedAd) { success, msg ->
                    adToEdit = null
                    if (selectedAdDetail?.id == targetAd.id) {
                        selectedAdDetail = updatedAd
                    }
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(msg)
                    }
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (adToDelete != null) {
        val ad = adToDelete!!
        AlertDialog(
            onDismissRequest = { adToDelete = null },
            title = { Text("E'lonni o‘chirish", fontWeight = FontWeight.Bold) },
            text = { Text("Haqiqatan ham \"${ad.title}\" nomli e'lonni o‘chirmoqchimisiz?") },
            confirmButton = {
                Button(
                    onClick = {
                        val targetAdId = ad.id
                        val targetImage = ad.imageUri
                        ecosystemViewModel.deleteAd(targetAdId) { success, msg ->
                            if (success) {
                                AdOwnershipHelper.removeMyAd(context, targetAdId)
                            }
                            if (targetImage.isNotBlank()) {
                                ImageStorageHelper.deleteImageFile(targetImage)
                            }
                            adToDelete = null
                            if (selectedAdDetail?.id == targetAdId) {
                                selectedAdDetail = null
                            }
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(msg)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    modifier = Modifier.testTag("confirm_delete_ad_btn")
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("O‘chirish", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { adToDelete = null }) {
                    Text("Bekor qilish", color = SlateGray)
                }
            }
        )
    }

    // Detailed Ad View Dialog
    if (selectedAdDetail != null) {
        val ad = selectedAdDetail!!
        val canEdit = AdOwnershipHelper.canManageAd(context, ad, session)
        val canDelete = AdOwnershipHelper.canDeleteAd(context, ad, session)

        AlertDialog(
            onDismissRequest = { selectedAdDetail = null },
            modifier = Modifier.fillMaxWidth(0.95f),
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = ad.title,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryNavy,
                        fontSize = 17.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { selectedAdDetail = null }) {
                        Icon(Icons.Filled.Close, contentDescription = "Yopish", tint = SlateGray)
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Image Banner
                    if (ad.imageUri.isNotBlank()) {
                        val resolvedImage = ImageStorageHelper.resolveImageModel(ad.imageUri, context) ?: ad.imageUri
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceSubtle)
                        ) {
                            SubcomposeAsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(resolvedImage)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = ad.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                loading = {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(modifier = Modifier.size(28.dp), color = Color(0xFFDC2626))
                                    }
                                },
                                error = {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Filled.Image, contentDescription = null, tint = SlateGray, modifier = Modifier.size(48.dp))
                                    }
                                }
                            )
                        }
                    }

                    // Price and Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${Formatters.formatPrice(ad.price)} ${if (ad.isNegotiable) "(Kelishiladi)" else ""}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = Color(0xFFDC2626)
                        )

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SurfaceSubtle
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.Filled.Visibility, contentDescription = null, tint = SlateGray, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${ad.viewsCount} marta ko‘rildi", fontSize = 11.sp, color = SlateGray)
                            }
                        }
                    }

                    // Metadata Badges
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFDC2626).copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "Toifa: ${ad.category}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFDC2626),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SurfaceSubtle
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Filled.LocationOn, contentDescription = null, tint = SlateGray, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = ad.location, fontSize = 11.sp, color = SecondaryNavy)
                            }
                        }
                    }

                    // Author Card
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, BorderColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(PrimaryBlue.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = ad.authorName.take(1).uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlue,
                                    fontSize = 16.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = ad.authorName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = SecondaryNavy
                                )
                                Text(
                                    text = ad.authorPhone,
                                    fontSize = 11.sp,
                                    color = SlateGray
                                )
                            }
                        }
                    }

                    // Description
                    if (ad.description.isNotBlank()) {
                        Column(modifier = Modifier.padding(top = 4.dp)) {
                            Text("Batafsil ma’lumot:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SecondaryNavy)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = ad.description,
                                fontSize = 12.sp,
                                color = SecondaryNavy,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${ad.authorPhone}"))
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("call_author_btn")
                        ) {
                            Icon(Icons.Filled.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Qo‘ng‘iroq qilish", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "📢 Gagarin E'lon:\n${ad.title}\nNarxi: ${Formatters.formatPrice(ad.price)}\nTel: ${ad.authorPhone}\nJoylashuv: ${ad.location}\nGagarin Go ilovasida ko‘rish"
                                    )
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "E'lonni ulashish"))
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp), tint = SlateGray)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ulashish", color = SecondaryNavy, fontSize = 12.sp)
                        }
                    }

                    if (canEdit || canDelete) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (canEdit) {
                                OutlinedButton(
                                    onClick = {
                                        adToEdit = ad
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).testTag("edit_ad_btn")
                                ) {
                                    Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp), tint = PrimaryBlue)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Tahrirlash", color = PrimaryBlue, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }

                            if (canDelete) {
                                Button(
                                    onClick = {
                                        adToDelete = ad
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).testTag("delete_ad_btn")
                                ) {
                                    Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("O‘chirish", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            },
            dismissButton = {}
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
                            text = "Gagarin E'lonlar",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkText
                        )
                        Text(
                            text = "Mirzacho‘l va Gagarin bepul e'lonlari",
                            fontSize = 12.sp,
                            color = SecondaryText
                        )
                    }
                    Button(
                        onClick = {
                            if (!session.isLoggedIn) {
                                showAuthPromptDialog = true
                            } else {
                                showPostAdDialog = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBurgundy),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("E'lon berish", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                        showPostAdDialog = true
                    }
                },
                containerColor = PrimaryBurgundy,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("post_ad_fab")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("E'lon berish", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        val adsBanners = remember(promoBanners) {
            promoBanners.filter {
                it.actionTag.equals("ADS", ignoreCase = true) ||
                it.actionTag.equals("ALL", ignoreCase = true)
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(
                start = 14.dp,
                end = 14.dp,
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = innerPadding.calculateBottomPadding() + 80.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize().background(LightBackground)
        ) {
            // Search Bar for Ads
            item(span = { GridItemSpan(2) }) {
                if (adsBanners.isNotEmpty()) {
                    PromoBannerSection(
                        banners = adsBanners,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("E'lonlar ichidan qidirish (avto, uy, tel...)", color = SlateGray, fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Qidirish",
                            tint = PrimaryBurgundy
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Filled.Close, contentDescription = "Tozalash", tint = SlateGray)
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CardSurface,
                        unfocusedContainerColor = CardSurface,
                        focusedBorderColor = PrimaryBurgundy,
                        unfocusedBorderColor = BorderColor
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("ad_search_input")
                )
            }

            // Categories
            item(span = { GridItemSpan(2) }) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    items(adCategories) { cat ->
                        val isSelected = cat == selectedCategory
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) PrimaryBurgundy else CardSurface,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) PrimaryBurgundy else BorderColor
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { ecosystemViewModel.selectAdCategory(cat) }
                                .testTag("ad_cat_${cat.lowercase()}")
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

            // Ads List Title & Count
            item(span = { GridItemSpan(2) }) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedCategory != "Barchasi") "$selectedCategory (${filteredAds.size})"
                        else "Barcha E'lonlar (${filteredAds.size})",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = SecondaryNavy
                    )
                }
            }

            // Ad Items
            items(filteredAds, key = { it.id }) { ad ->
                val canEdit = AdOwnershipHelper.canManageAd(context, ad, session)
                val canDelete = AdOwnershipHelper.canDeleteAd(context, ad, session)
                val isOwner = AdOwnershipHelper.isAdCreator(context, ad, session)
                val isAdminRole = session.role == UserRole.ADMIN || session.role == UserRole.SUPER_ADMIN

                ClassifiedAdCard(
                    ad = ad,
                    isOwner = isOwner,
                    isAdminRole = isAdminRole,
                    canEdit = canEdit,
                    canDelete = canDelete,
                    onClick = {
                        ecosystemViewModel.viewAd(ad.id)
                        selectedAdDetail = ad
                    },
                    onCall = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${ad.authorPhone}"))
                        context.startActivity(intent)
                    },
                    onEdit = { adToEdit = ad },
                    onDelete = { adToDelete = ad }
                )
            }

            if (filteredAds.isEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    EmptyStateView(
                        title = "E'lonlar topilmadi",
                        description = if (searchQuery.isNotBlank()) "Qidiruv bo‘yicha e'lon topilmadi. Qidiruv so‘zini o‘zgartirib ko‘ring."
                        else "Ushbu toifada hali e'lonlar mavjud emas. Birinchi bo‘lib e'lon bering!",
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }

            item(span = { GridItemSpan(2) }) {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun ClassifiedAdCard(
    ad: ClassifiedAdEntity,
    isOwner: Boolean = false,
    isAdminRole: Boolean = false,
    canEdit: Boolean = false,
    canDelete: Boolean = false,
    onClick: () -> Unit,
    onCall: () -> Unit,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(
            1.dp,
            if (isOwner) PrimaryBurgundy.copy(alpha = 0.5f) else BorderColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("ad_item_${ad.id}")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(SurfaceSubtle)
            ) {
                if (ad.imageUri.isNotBlank()) {
                    val resolvedCardImage = ImageStorageHelper.resolveImageModel(ad.imageUri, LocalContext.current) ?: ad.imageUri
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(resolvedCardImage)
                            .crossfade(true)
                            .build(),
                        contentDescription = ad.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        loading = {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = PrimaryBurgundy)
                            }
                        },
                        error = {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Image, contentDescription = null, tint = SlateGray, modifier = Modifier.size(32.dp))
                            }
                        }
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFFF1F5F9), Color(0xFFE2E8F0))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Campaign, contentDescription = null, tint = SlateGray, modifier = Modifier.size(36.dp))
                    }
                }

                // Category Tag
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color.Black.copy(alpha = 0.65f),
                    modifier = Modifier.align(Alignment.TopStart).padding(6.dp)
                ) {
                    Text(
                        text = ad.category,
                        fontSize = 9.5.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }

                // Ownership / Admin Badge
                if (isOwner || isAdminRole) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (isOwner) PrimaryBurgundy.copy(alpha = 0.95f) else Color(0xFF6D28D9).copy(alpha = 0.95f),
                        modifier = Modifier.align(Alignment.BottomStart).padding(6.dp)
                    ) {
                        Text(
                            text = if (isOwner) "Sizning e'loningiz" else "Admin",
                            fontSize = 8.5.sp,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Views count badge
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color.Black.copy(alpha = 0.65f),
                    modifier = Modifier.align(Alignment.TopEnd).padding(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Filled.Visibility, contentDescription = null, tint = Color.White, modifier = Modifier.size(11.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(text = "${ad.viewsCount}", fontSize = 9.5.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = ad.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = DarkText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = Formatters.formatPrice(ad.price),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.5.sp,
                        color = PrimaryBurgundy
                    )

                    if (ad.isNegotiable) {
                        Text(text = "Kelishiladi", fontSize = 9.5.sp, color = SuccessGreen, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.LocationOn, contentDescription = null, tint = SlateGray, modifier = Modifier.size(11.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(text = ad.location, fontSize = 10.sp, color = SlateGray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Action Buttons Bar (Clean, highly visible buttons)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Call Button (Always visible and prominent)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SuccessGreen,
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .clickable { onCall() }
                            .testTag("call_ad_card_btn_${ad.id}")
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(Icons.Filled.Call, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Qo‘ng‘iroq", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Edit Button (If authorized)
                    if (canEdit) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PrimaryBlue.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .size(34.dp)
                                .clickable { onEdit() }
                                .testTag("edit_ad_card_btn_${ad.id}")
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Icon(Icons.Filled.Edit, contentDescription = "Tahrirlash", tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    // Delete Button (ONLY FOR CREATOR)
                    if (canDelete) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = DangerRed.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .size(34.dp)
                                .clickable { onDelete() }
                                .testTag("delete_ad_card_btn_${ad.id}")
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Icon(Icons.Filled.Delete, contentDescription = "O‘chirish", tint = DangerRed, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PostClassifiedAdDialog(
    initialAuthor: String,
    initialPhone: String,
    adToEdit: ClassifiedAdEntity? = null,
    onDismiss: () -> Unit,
    onSubmit: (
        title: String,
        category: String,
        price: Double,
        isNegotiable: Boolean,
        location: String,
        description: String,
        imageUri: String,
        phone: String,
        author: String
    ) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(adToEdit?.title ?: "") }
    var selectedCat by remember { mutableStateOf(adToEdit?.category ?: adCategories.getOrElse(1) { "Avto" }) }
    var priceText by remember { mutableStateOf(if (adToEdit != null) "${adToEdit.price.toLong()}" else "") }
    var isNegotiable by remember { mutableStateOf(adToEdit?.isNegotiable ?: true) }
    var location by remember { mutableStateOf(adToEdit?.location ?: "Gagarin shahri") }
    var description by remember { mutableStateOf(adToEdit?.description ?: "") }
    var phone by remember { mutableStateOf(adToEdit?.authorPhone ?: initialPhone) }
    var author by remember { mutableStateOf(adToEdit?.authorName ?: initialAuthor) }

    // Real Image picking, URL and validation state
    var imageSourceMode by remember { 
        mutableStateOf(if (adToEdit?.imageUri?.startsWith("http") == true) "URL" else "GALLERY") 
    }
    var urlImageInput by remember { 
        mutableStateOf(if (adToEdit?.imageUri?.startsWith("http") == true) adToEdit.imageUri else "") 
    }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var persistentSavedUri by remember { mutableStateOf(adToEdit?.imageUri ?: "") }
    var base64EncodedImage by remember { mutableStateOf<String?>(null) }
    var imageValidationInfo by remember { mutableStateOf<ImageValidationResult?>(null) }
    var imageValidationError by remember { mutableStateOf<String?>(null) }
    var isSavingImage by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val validation = ImageStorageHelper.validateImageFile(context, uri)
            if (validation.isValid) {
                selectedImageUri = uri
                imageValidationInfo = validation
                imageValidationError = null

                // Encode to Base64 for Firestore real-time sync across all devices
                val base64 = ImageStorageHelper.compressAndEncodeImageToBase64(context, uri, maxDimension = 900, quality = 80)
                if (base64 != null) {
                    base64EncodedImage = base64
                }

                // Also save to persistent internal storage for fast local caching
                val saveResult = ImageStorageHelper.saveAdImage(context, uri)
                saveResult.onSuccess { savedPath ->
                    persistentSavedUri = savedPath
                }.onFailure { ex ->
                    imageValidationError = "Rasmni saqlashda xatolik: ${ex.localizedMessage}"
                }
            } else {
                imageValidationError = validation.errorMessage ?: "Noto‘g‘ri rasm formati"
                selectedImageUri = null
                imageValidationInfo = null
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth(0.95f),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (adToEdit != null) Icons.Filled.Edit else Icons.Filled.Campaign,
                    contentDescription = null,
                    tint = Color(0xFFDC2626)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (adToEdit != null) "✏️ E'lonni tahrirlash" else "📢 Yangi e'lon berish",
                    fontWeight = FontWeight.Bold,
                    color = SecondaryNavy,
                    fontSize = 17.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Image Picker / URL Section
                Text("E'lon rasmi:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SecondaryNavy)

                // Mode Selector: Galereyadan yoki Google / Internet havolasi
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
                            selectedContainerColor = Color(0xFFDC2626),
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = imageSourceMode == "URL",
                        onClick = { imageSourceMode = "URL" },
                        label = { Text("Google / URL", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(Icons.Filled.Link, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFDC2626),
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                val normalizedUrl = if (urlImageInput.isNotBlank()) ImageStorageHelper.normalizeImageUrl(urlImageInput) else ""
                val currentImageModel: Any? = when {
                    imageSourceMode == "URL" && normalizedUrl.isNotBlank() -> normalizedUrl
                    imageSourceMode == "GALLERY" && selectedImageUri != null -> selectedImageUri
                    base64EncodedImage != null -> base64EncodedImage
                    persistentSavedUri.isNotBlank() -> ImageStorageHelper.resolveImageModel(persistentSavedUri, context) ?: persistentSavedUri
                    else -> null
                }

                // Image Preview Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SurfaceSubtle,
                    border = BorderStroke(1.dp, if (currentImageModel != null) SuccessGreen else BorderColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(enabled = imageSourceMode == "GALLERY") { photoPickerLauncher.launch("image/*") }
                        .testTag("ad_image_picker")
                ) {
                    if (currentImageModel != null) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            SubcomposeAsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(currentImageModel)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Tanlangan rasm",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                loading = {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color(0xFFDC2626), strokeWidth = 2.dp)
                                    }
                                },
                                error = {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(Icons.Filled.Image, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(28.dp))
                                            Text("Rasmni yuklashda xatolik (URL yoki fayl yaroqsiz)", fontSize = 10.sp, color = Color(0xFFDC2626))
                                        }
                                    }
                                }
                            )
                            IconButton(
                                onClick = {
                                    if (persistentSavedUri.isNotBlank() && persistentSavedUri != adToEdit?.imageUri) {
                                        ImageStorageHelper.deleteImageFile(persistentSavedUri)
                                    }
                                    selectedImageUri = null
                                    persistentSavedUri = ""
                                    base64EncodedImage = null
                                    urlImageInput = ""
                                    imageValidationInfo = null
                                    imageValidationError = null
                                },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(28.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            ) {
                                Icon(Icons.Filled.Close, contentDescription = "O‘chirish", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = if (imageSourceMode == "URL") Icons.Filled.Link else Icons.Filled.AddPhotoAlternate,
                                contentDescription = null,
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (imageSourceMode == "URL") "Google yoki veb rasm havolasini kiriting" else "Galereyadan rasm tanlash",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SecondaryNavy
                            )
                            Text(
                                text = if (imageSourceMode == "URL") "Google Images, Drive, Unsplash, JPG, PNG" else "JPG, PNG, WEBP (Maks 15 MB)",
                                fontSize = 10.sp,
                                color = SlateGray
                            )
                        }
                    }
                }

                // Controls based on selected mode
                if (imageSourceMode == "URL") {
                    OutlinedTextField(
                        value = urlImageInput,
                        onValueChange = { 
                            urlImageInput = it
                            imageValidationError = null
                        },
                        label = { Text("Google yoki internet rasm havolasi (URL)") },
                        placeholder = { Text("https://... yoki Googledan olingan havola") },
                        leadingIcon = {
                            Icon(Icons.Filled.Link, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
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
                } else {
                    OutlinedButton(
                        onClick = { photoPickerLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (selectedImageUri != null || persistentSavedUri.isNotBlank()) "Boshqa rasm tanlash" else "Galereyadan rasm yuklash",
                            fontSize = 12.sp,
                            color = Color(0xFFDC2626),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (imageValidationInfo != null && imageSourceMode == "GALLERY") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Check, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Rasm saqlandi: ${imageValidationInfo!!.width}x${imageValidationInfo!!.height} px (${String.format("%.1f", imageValidationInfo!!.sizeBytes / 1024.0)} KB)",
                            fontSize = 10.sp,
                            color = SuccessGreen
                        )
                    }
                }

                if (imageValidationError != null) {
                    Text(
                        text = imageValidationError!!,
                        fontSize = 11.sp,
                        color = Color(0xFFDC2626)
                    )
                }

                // Category Selection
                Text("Kategoriya:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SecondaryNavy)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    items(adCategories.filter { it != "Barchasi" }) { cat ->
                        val isSelected = cat == selectedCat
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Color(0xFFDC2626) else SurfaceSubtle,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedCat = cat }
                        ) {
                            Text(
                                text = cat,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else SecondaryNavy,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("E'lon sarlavhasi *") },
                    placeholder = { Text("Masalan: Cobalt 2022 oq rang, ideal holatda") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Price
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Narxi (so‘mda) *") },
                    placeholder = { Text("Masalan: 135000000") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Negotiable switch
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text("Kelishiladimi?", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = SecondaryNavy)
                        Text("Xaridor bilan narxni kelishish imkoniyati", fontSize = 10.sp, color = SlateGray)
                    }
                    Switch(
                        checked = isNegotiable,
                        onCheckedChange = { isNegotiable = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = SuccessGreen)
                    )
                }

                // Author Name
                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("Ismingiz *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Phone
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Telefon raqamingiz *") },
                    placeholder = { Text("+998 90 123 45 67") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Location
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Manzil / Joylashuv") },
                    placeholder = { Text("Gagarin shahri, Dehqon bozori") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Tavsif / Holati haqida") },
                    placeholder = { Text("Mahsulot yoki buyum haqida batafsil ma’lumot bering...") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = priceText.filter { it.isDigit() }.toDoubleOrNull() ?: 0.0
                    val finalImage = when {
                        imageSourceMode == "URL" && urlImageInput.isNotBlank() -> ImageStorageHelper.normalizeImageUrl(urlImageInput)
                        base64EncodedImage != null -> base64EncodedImage!!
                        persistentSavedUri.isNotBlank() -> persistentSavedUri
                        selectedImageUri != null -> selectedImageUri.toString()
                        else -> ""
                    }
                    onSubmit(title, selectedCat, price, isNegotiable, location, description, finalImage, phone, author)
                },
                enabled = title.isNotBlank() && priceText.isNotBlank() && phone.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("submit_ad_btn")
            ) {
                Text(
                    text = if (adToEdit != null) "O‘zgarishlarni saqlash" else "E'lonni joylash",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Bekor qilish", color = SlateGray)
            }
        }
    )
}
