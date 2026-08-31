package com.example.ui.screens.customer

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.DeliveryDining
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import com.example.data.local.entity.FoodOrderEntity
import com.example.data.local.entity.FoodProductEntity
import com.example.data.local.entity.FoodRestaurantEntity
import com.example.ui.components.FoodCategoryVisualCard
import com.example.data.util.ImageStorageHelper
import com.example.data.util.LocationStorageHelper
import com.example.ui.components.map.BurgundyRed
import com.example.ui.components.map.DeliveryMapPickerSheet
import com.example.ui.components.map.OrderLocationSection
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Place
import com.example.ui.theme.CardSurface
import com.example.ui.theme.LightBackground
import com.example.ui.theme.OrangeAmber
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SecondaryNavy
import com.example.ui.theme.SlateGray
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodels.AuthViewModel
import com.example.ui.viewmodels.FoodStoreViewModel
import java.text.NumberFormat
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GagarinFoodScreen(
    foodStoreViewModel: FoodStoreViewModel,
    authViewModel: AuthViewModel,
    onNavigateToFoodSellerAuth: () -> Unit,
    onNavigateToFoodAdminAuth: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentSession by authViewModel.session.collectAsState()
    val restaurants by foodStoreViewModel.restaurants.collectAsState()
    val categories by foodStoreViewModel.categories.collectAsState()
    val products by foodStoreViewModel.displayedProducts.collectAsState()
    val banners by foodStoreViewModel.banners.collectAsState()
    val selectedCategory by foodStoreViewModel.selectedCategory.collectAsState()
    val selectedRestaurantId by foodStoreViewModel.selectedRestaurantId.collectAsState()
    val searchQuery by foodStoreViewModel.searchQuery.collectAsState()
    val cartItems by foodStoreViewModel.foodCart.collectAsState()
    val cartTotal by foodStoreViewModel.foodCartTotal.collectAsState()
    val cartWarning by foodStoreViewModel.cartWarning.collectAsState()

    var showCartSheet by remember { mutableStateOf(false) }
    var showCheckoutDialog by remember { mutableStateOf(false) }
    var showOrdersHistoryDialog by remember { mutableStateOf(false) }
    var selectedProductDetail by remember { mutableStateOf<FoodProductEntity?>(null) }
    var pendingSwitchProduct by remember { mutableStateOf<FoodProductEntity?>(null) }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val formatter = remember { NumberFormat.getNumberInstance(Locale("uz", "UZ")) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🍔", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Gagarin Taomlar",
                                fontWeight = FontWeight.Black,
                                fontSize = 19.sp,
                                color = SecondaryNavy
                            )
                        }
                        Text(
                            text = "100% Bepul Yetkazib Berish • Naqd Pul",
                            fontSize = 11.sp,
                            color = SuccessGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showOrdersHistoryDialog = true },
                        modifier = Modifier.testTag("food_orders_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Buyurtmalar tarixi",
                            tint = SecondaryNavy
                        )
                    }
                    IconButton(
                        onClick = onNavigateToFoodSellerAuth,
                        modifier = Modifier.testTag("food_seller_login_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Store,
                            contentDescription = "Oshxona kabineti",
                            tint = OrangeAmber
                        )
                    }
                    IconButton(
                        onClick = onNavigateToFoodAdminAuth,
                        modifier = Modifier.testTag("food_admin_login_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Food Admin",
                            tint = SecondaryNavy
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CardSurface,
                    titleContentColor = SecondaryNavy
                )
            )
        },
        floatingActionButton = {
            if (cartItems.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { showCartSheet = true },
                    containerColor = OrangeAmber,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("food_cart_fab")
                ) {
                    BadgedBox(
                        badge = {
                            Badge(containerColor = Color.White, contentColor = OrangeAmber) {
                                Text("${cartItems.sumOf { it.quantity }}")
                            }
                        }
                    ) {
                        Icon(Icons.Default.ShoppingBag, contentDescription = "Savat")
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LightBackground)
                .padding(innerPadding)
        ) {
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { foodStoreViewModel.setSearchQuery(it) },
                placeholder = { Text("Taom yoki oshxona nomini qidiring...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SlateGray) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { foodStoreViewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Tozalash", tint = SlateGray)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = CardSurface,
                    unfocusedContainerColor = CardSurface,
                    focusedBorderColor = OrangeAmber,
                    unfocusedBorderColor = Color(0xFFE0E0E0)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("food_search_input")
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Promotional / Delivery Banner
                item(span = { GridItemSpan(2) }) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = OrangeAmber),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Surface(
                                    color = Color.White.copy(alpha = 0.25f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "100% BEPUL YETKAZISH",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Gagarin bo‘ylab eng mazali taomlar!",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "To‘lov faqat naqd pul orqali qabul qilinadi",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 12.sp
                                )
                            }
                            Text(text = "🍔", fontSize = 42.sp)
                        }
                    }
                }

                // Custom Banners from Admin if any
                if (banners.isNotEmpty()) {
                    item(span = { GridItemSpan(2) }) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            items(banners, key = { it.id }) { banner ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SecondaryNavy),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.width(280.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(banner.badgeText, color = OrangeAmber, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            Text(banner.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            if (banner.description.isNotBlank()) {
                                                Text(banner.description, color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                                            }
                                        }
                                        Text("🎁", fontSize = 32.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // Category Chips
                item(span = { GridItemSpan(2) }) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Text("🍽️", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Taom Toifalari",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = SecondaryNavy
                            )
                        }
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 2.dp)
                        ) {
                            item {
                                val isSelected = selectedCategory == "Barchasi"
                                FoodCategoryVisualCard(
                                    name = "Barchasi",
                                    iconEmoji = "🍽️",
                                    isSelected = isSelected,
                                    onClick = { foodStoreViewModel.selectCategory("Barchasi") }
                                )
                            }
                            items(categories, key = { it.id }) { cat ->
                                val isSelected = selectedCategory == cat.name
                                FoodCategoryVisualCard(
                                    name = cat.name,
                                    iconEmoji = cat.iconEmoji,
                                    isSelected = isSelected,
                                    onClick = { foodStoreViewModel.selectCategory(cat.name) }
                                )
                            }
                        }
                    }
                }

                // Restaurants Row
                if (restaurants.isNotEmpty()) {
                    item(span = { GridItemSpan(2) }) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🏪", fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Oshxona va Restoranlar",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = SecondaryNavy
                                    )
                                }
                                if (selectedRestaurantId != null) {
                                    TextButton(onClick = { foodStoreViewModel.selectRestaurant(null) }) {
                                        Text("Filtrni tozalash", fontSize = 12.sp, color = OrangeAmber)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(restaurants, key = { it.id }) { rest ->
                                    val isSelected = selectedRestaurantId == rest.id
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) Color(0xFFFFF3E0) else CardSurface
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, OrangeAmber) else null,
                                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                        modifier = Modifier
                                            .width(170.dp)
                                            .clickable {
                                                if (isSelected) foodStoreViewModel.selectRestaurant(null)
                                                else foodStoreViewModel.selectRestaurant(rest.id)
                                            }
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("🏪", fontSize = 20.sp)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = rest.name,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    color = SecondaryNavy
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = rest.category,
                                                fontSize = 11.sp,
                                                color = SlateGray,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = if (rest.isOpen) "🟢 Ochiq" else "🔴 Yopiq",
                                                fontSize = 10.sp,
                                                color = if (rest.isOpen) SuccessGreen else Color.Red,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Section header
                item(span = { GridItemSpan(2) }) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                    ) {
                        Text("🍔", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Mazali Taomlar Menyusi",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = SecondaryNavy
                        )
                    }
                }

                // Products Empty State
                if (products.isEmpty()) {
                    item(span = { GridItemSpan(2) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🍲", fontSize = 48.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Hozircha taomlar mavjud emas",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SecondaryNavy
                                )
                                Text(
                                    "Oshxona egasi sifatida kirib menyuni to‘ldiring",
                                    fontSize = 12.sp,
                                    color = SlateGray,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = onNavigateToFoodSellerAuth,
                                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAmber),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Oshxona qo‘shish / Kirish")
                                }
                            }
                        }
                    }
                } else {
                    // Products Grid Items
                    items(products, key = { it.id }) { product ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CardSurface),
                            shape = RoundedCornerShape(14.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedProductDetail = product }
                                .testTag("food_item_${product.id}")
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(115.dp)
                                        .background(Color(0xFFF5F5F5)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (product.imageUri.isNotBlank()) {
                                        val resolvedModel = ImageStorageHelper.resolveImageModel(product.imageUri, context) ?: product.imageUri
                                        SubcomposeAsyncImage(
                                            model = resolvedModel,
                                            contentDescription = product.name,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize(),
                                            loading = {
                                                Box(
                                                    modifier = Modifier.fillMaxSize(),
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
                                                Text("🍲", fontSize = 36.sp)
                                            }
                                        )
                                    } else {
                                        Text("🍲", fontSize = 40.sp)
                                    }
                                    Surface(
                                        color = SuccessGreen,
                                        shape = RoundedCornerShape(topStart = 0.dp, bottomStart = 8.dp, topEnd = 0.dp, bottomEnd = 0.dp),
                                        modifier = Modifier.align(Alignment.TopEnd)
                                    ) {
                                        Text(
                                            text = "BEPUL",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = product.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = SecondaryNavy,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = product.restaurantName,
                                        fontSize = 11.sp,
                                        color = SlateGray,
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${formatter.format(product.price.toLong())} so‘m",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = OrangeAmber
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Button(
                                        onClick = {
                                            foodStoreViewModel.addToCart(product, 1)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = OrangeAmber),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(34.dp)
                                            .testTag("add_to_cart_${product.id}")
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Savatga", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // CART WARNING (DIFFERENT RESTAURANTS ALERT)
    if (cartWarning != null) {
        AlertDialog(
            onDismissRequest = { foodStoreViewModel.dismissCartWarning() },
            title = { Text("Oshxonani almashtirish", fontWeight = FontWeight.Bold) },
            text = { Text(cartWarning!!) },
            confirmButton = {
                Button(
                    onClick = {
                        val firstProduct = products.firstOrNull { it.restaurantId != (cartItems.firstOrNull()?.product?.restaurantId ?: -1L) }
                        if (firstProduct != null) {
                            foodStoreViewModel.forceAddToCartWithClear(firstProduct, 1)
                        } else {
                            foodStoreViewModel.clearCart()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAmber)
                ) {
                    Text("Savatni tozalab qo‘shish")
                }
            },
            dismissButton = {
                TextButton(onClick = { foodStoreViewModel.dismissCartWarning() }) {
                    Text("Bekor qilish")
                }
            }
        )
    }

    // PRODUCT DETAIL DIALOG
    if (selectedProductDetail != null) {
        val prod = selectedProductDetail!!
        var quantity by remember { mutableIntStateOf(1) }

        AlertDialog(
            onDismissRequest = { selectedProductDetail = null },
            title = {
                Text(prod.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = SecondaryNavy)
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (prod.imageUri.isNotBlank()) {
                        val resolvedDetailModel = ImageStorageHelper.resolveImageModel(prod.imageUri, context) ?: prod.imageUri
                        SubcomposeAsyncImage(
                            model = resolvedDetailModel,
                            contentDescription = prod.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            loading = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = OrangeAmber,
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp
                                    )
                                }
                            },
                            error = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp)
                                        .background(Color(0xFFF5F5F5)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🍲", fontSize = 48.sp)
                                }
                            }
                        )
                    }
                    Text("Oshxona: ${prod.restaurantName}", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = SecondaryNavy)
                    Text("Kategoriya: ${prod.categoryName}", fontSize = 13.sp, color = SlateGray)
                    Text("Tayyorlash vaqti: ${prod.preparationTime}", fontSize = 13.sp, color = SlateGray)
                    if (prod.ingredients.isNotBlank()) {
                        Text("Tarkibi: ${prod.ingredients}", fontSize = 13.sp, color = SecondaryNavy)
                    }
                    if (prod.description.isNotBlank()) {
                        Text(prod.description, fontSize = 12.sp, color = SlateGray)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Narxi: ${formatter.format(prod.price.toLong())} so‘m",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = OrangeAmber
                    )

                    Surface(
                        color = SuccessGreen.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "🛵 Yetkazib berish: 100% BEPUL (Gagarin bo‘ylab)",
                            color = SuccessGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { if (quantity > 1) quantity-- }) {
                            Icon(Icons.Default.Remove, contentDescription = "Kamaytirish")
                        }
                        Text("$quantity", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(horizontal = 16.dp))
                        IconButton(onClick = { quantity++ }) {
                            Icon(Icons.Default.Add, contentDescription = "Ko‘paytirish")
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        foodStoreViewModel.addToCart(prod, quantity)
                        selectedProductDetail = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAmber)
                ) {
                    Text("Savatga qo‘shish (${formatter.format((prod.price * quantity).toLong())} so‘m)")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedProductDetail = null }) {
                    Text("Yopish")
                }
            }
        )
    }

    // CART BOTTOM SHEET
    if (showCartSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showCartSheet = false },
            sheetState = sheetState,
            containerColor = CardSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Taom Savati", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = SecondaryNavy)
                    TextButton(onClick = { foodStoreViewModel.clearCart() }) {
                        Text("Tozalash", color = Color.Red, fontSize = 13.sp)
                    }
                }

                if (cartItems.isNotEmpty()) {
                    Text(
                        text = "Oshxona: ${cartItems.first().product.restaurantName}",
                        fontWeight = FontWeight.SemiBold,
                        color = OrangeAmber,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    cartItems.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF9F9F9), RoundedCornerShape(10.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.product.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SecondaryNavy)
                                Text("${formatter.format(item.product.price.toLong())} so‘m", fontSize = 12.sp, color = SlateGray)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { foodStoreViewModel.decreaseQuantity(item.product.id) }) {
                                    Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(18.dp))
                                }
                                Text("${item.quantity}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                IconButton(onClick = { foodStoreViewModel.increaseQuantity(item.product.id) }) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(colors = CardDefaults.cardColors(containerColor = LightBackground), shape = RoundedCornerShape(10.dp)) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Taomlar qiymati:", fontSize = 13.sp, color = SlateGray)
                            Text("${formatter.format(cartTotal.toLong())} so‘m", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Yetkazib berish:", fontSize = 13.sp, color = SlateGray)
                            Text("0 so‘m (BEPUL)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SuccessGreen)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("To‘lov turi:", fontSize = 13.sp, color = SlateGray)
                            Text("NAQD PUL", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SecondaryNavy)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        showCartSheet = false
                        showCheckoutDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAmber),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("proceed_to_food_checkout")
                ) {
                    Text(
                        "Buyurtmani rasmiylashtirish (${formatter.format(cartTotal.toLong())} so‘m)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }

    // CHECKOUT DIALOG
    if (showCheckoutDialog) {
        val savedLocation = remember { LocationStorageHelper.getSavedLocation(context) }
        var selectedLocation by remember { mutableStateOf(savedLocation) }
        var showMapPickerInFood by remember { mutableStateOf(false) }

        var customerName by remember { mutableStateOf(currentSession.user?.fullName ?: "") }
        var customerPhone by remember { mutableStateOf(currentSession.user?.phone ?: "+998 ") }
        var deliveryAddress by remember {
            mutableStateOf(
                if (!currentSession.user?.savedAddress.isNullOrBlank()) currentSession.user!!.savedAddress
                else savedLocation.getFullFormattedAddress()
            )
        }
        var customerNote by remember { mutableStateOf(savedLocation.landmark) }
        var isPlacing by remember { mutableStateOf(false) }

        if (showMapPickerInFood) {
            DeliveryMapPickerSheet(
                initialLocation = selectedLocation,
                onDismiss = { showMapPickerInFood = false },
                onLocationConfirmed = { newLocation ->
                    selectedLocation = newLocation
                    deliveryAddress = newLocation.getFullFormattedAddress()
                    if (newLocation.landmark.isNotBlank() && customerNote.isBlank()) {
                        customerNote = newLocation.landmark
                    }
                }
            )
        }

        AlertDialog(
            onDismissRequest = { if (!isPlacing) showCheckoutDialog = false },
            title = { Text("Buyurtmani Tasdiqlash", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Ismingiz") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = customerPhone,
                        onValueChange = { customerPhone = it },
                        label = { Text("Telefon raqamingiz") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Real Interactive Map Picker Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD54F)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showMapPickerInFood = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Surface(
                                    color = BurgundyRed,
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.Place,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Xaritada manzilni belgilash",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = SecondaryNavy
                                    )
                                    Text(
                                        text = String.format(
                                            Locale.US,
                                            "Lat: %.4f, Lng: %.4f",
                                            selectedLocation.latitude,
                                            selectedLocation.longitude
                                        ),
                                        fontSize = 10.sp,
                                        color = SlateGray
                                    )
                                }
                            }

                            OutlinedButton(
                                onClick = { showMapPickerInFood = true },
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Xarita", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = deliveryAddress,
                        onValueChange = { deliveryAddress = it },
                        label = { Text("Yetkazib berish manzili") },
                        placeholder = { Text("Gagarin shahri, ko‘cha, uy...") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = customerNote,
                        onValueChange = { customerNote = it },
                        label = { Text("Oshpaz / Kuryer uchun izoh / mo‘ljal") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Surface(
                        color = Color(0xFFFFF3E0),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("To‘lov: ${formatter.format(cartTotal.toLong())} so‘m", fontWeight = FontWeight.Bold, color = OrangeAmber, fontSize = 14.sp)
                            Text("Yetkazib berish: 100% BEPUL", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("To‘lov usuli: Taom yetib borganda NAQD PUL", fontSize = 12.sp, color = SecondaryNavy)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            isPlacing = true
                            val userId = currentSession.user?.id ?: 1L
                            val result = foodStoreViewModel.placeOrder(
                                userId = userId,
                                customerName = customerName,
                                customerPhone = customerPhone,
                                deliveryAddress = deliveryAddress,
                                customerNote = customerNote,
                                deliveryLatitude = selectedLocation.latitude,
                                deliveryLongitude = selectedLocation.longitude,
                                deliveryStreet = selectedLocation.street,
                                deliveryHouseNumber = selectedLocation.houseNumber,
                                deliveryLandmark = if (customerNote.isNotBlank()) customerNote else selectedLocation.landmark
                            )
                            isPlacing = false
                            if (result.isSuccess) {
                                showCheckoutDialog = false
                                snackbarHostState.showSnackbar("✅ Buyurtmangiz qabul qilindi! Oshpaz tayyorlashni boshladi.")
                            } else {
                                snackbarHostState.showSnackbar("❌ Xatolik: ${result.exceptionOrNull()?.message}")
                            }
                        }
                    },
                    enabled = !isPlacing && customerPhone.length >= 9 && deliveryAddress.length >= 3,
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAmber)
                ) {
                    if (isPlacing) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    } else {
                        Text("Buyurtma Berish (Naqd Pul)")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showCheckoutDialog = false }, enabled = !isPlacing) {
                    Text("Bekor qilish")
                }
            }
        )
    }

    // ORDERS HISTORY DIALOG
    if (showOrdersHistoryDialog) {
        val userId = currentSession.user?.id ?: 1L
        val userOrdersFlow = remember(userId) { foodStoreViewModel.getUserOrders(userId) }
        val userOrders by userOrdersFlow.collectAsState(initial = emptyList())

        AlertDialog(
            onDismissRequest = { showOrdersHistoryDialog = false },
            title = { Text("Mening Buyurtmalarim", fontWeight = FontWeight.Bold) },
            text = {
                if (userOrders.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                        Text("Sizda hali taom buyurtmalari yo‘q", color = SlateGray)
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        userOrders.forEach { ord ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Buyurtma #${ord.id}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(ord.status.replace("_", " "), color = OrangeAmber, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                    Text("Oshxona: ${ord.restaurantName}", fontSize = 12.sp, color = SecondaryNavy)
                                    Text(ord.itemsSummary, fontSize = 12.sp, color = SlateGray)
                                    Text("Manzil: ${ord.deliveryAddress}", fontSize = 11.sp, color = SecondaryNavy)
                                    Text("Jami: ${formatter.format(ord.totalPrice.toLong())} so‘m (Naqd)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = OrangeAmber)

                                    if (ord.deliveryLatitude != 0.0 && ord.deliveryLongitude != 0.0) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        OrderLocationSection(
                                            customerName = ord.customerName,
                                            customerPhone = ord.customerPhone,
                                            deliveryAddress = ord.deliveryAddress,
                                            latitude = ord.deliveryLatitude,
                                            longitude = ord.deliveryLongitude,
                                            street = ord.deliveryStreet,
                                            houseNumber = ord.deliveryHouseNumber,
                                            landmark = ord.deliveryLandmark
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showOrdersHistoryDialog = false }) {
                    Text("Yopish")
                }
            }
        )
    }
}
