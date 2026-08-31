package com.example.ui.screens.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.ReviewEntity
import com.example.ui.components.AddReviewDialog
import com.example.ui.components.AppProductImage
import com.example.ui.components.Formatters
import com.example.ui.components.ProductRatingSummaryView
import android.content.Intent
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Share
import com.example.ui.components.RecommendationSection
import com.example.ui.components.ReviewCardItem
import com.example.ui.components.StarGold
import com.example.ui.components.StarRatingBar
import com.example.ui.theme.BorderColor
import com.example.ui.theme.CardSurface
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DarkText
import com.example.ui.theme.LightBackground
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueLight
import com.example.ui.theme.SecondaryNavy
import com.example.ui.theme.SecondaryText
import com.example.ui.theme.SlateGray
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessGreenLight
import com.example.ui.theme.SurfaceSubtle
import com.example.ui.viewmodels.AuthViewModel
import com.example.ui.viewmodels.StoreViewModel
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    product: ProductEntity,
    storeViewModel: StoreViewModel,
    authViewModel: AuthViewModel,
    onBack: () -> Unit,
    onProductClick: ((ProductEntity) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val session by authViewModel.session.collectAsState()
    val favoriteProducts by storeViewModel.favoriteProducts.collectAsState()
    val isFavorite = favoriteProducts.any { it.id == product.id }
    val favIds = remember(favoriteProducts) { favoriteProducts.map { it.id }.toSet() }
    val reviews by storeViewModel.getReviewsForProduct(product.id).collectAsState(initial = emptyList())

    // Recommendation flows
    val similarProducts by storeViewModel.getSimilarProducts(product.id).collectAsState(initial = emptyList())
    val categoryTrending by storeViewModel.getCategoryTrendingProducts(product.categoryId, product.id).collectAsState(initial = emptyList())

    var quantity by remember { mutableIntStateOf(1) }
    var showAddReviewDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Track product view for personalization engine
    LaunchedEffect(product.id) {
        storeViewModel.trackProductView(product)
    }

    if (showAddReviewDialog) {
        AddReviewDialog(
            productName = product.name,
            initialUserName = session?.user?.fullName ?: "",
            onDismiss = { showAddReviewDialog = false },
            onSubmit = { rating, comment, reviewerName ->
                storeViewModel.addReview(
                    productId = product.id,
                    userId = session?.user?.id ?: 0L,
                    userName = reviewerName,
                    userPhone = session?.user?.phone ?: "",
                    rating = rating,
                    comment = comment
                ) { success, msg ->
                    showAddReviewDialog = false
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(msg)
                    }
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mahsulot haqida",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryNavy
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Orqaga",
                            tint = SecondaryNavy
                        )
                    }
                },
                actions = {
                    // Share button with event tracking
                    IconButton(
                        onClick = {
                            storeViewModel.trackProductShare(product.id, product.categoryId, product.sellerId)
                            try {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "GagarinGo: ${product.name} - ${Formatters.formatPrice(product.price)}\nBozor ilovasidan xarid qiling!")
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Mahsulotni ulashish"))
                            } catch (_: Exception) {}
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "Ulashish",
                            tint = SecondaryNavy
                        )
                    }

                    IconButton(
                        onClick = { storeViewModel.toggleFavorite(product.id) }
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Sevimlilar",
                            tint = if (isFavorite) DangerRed else SecondaryNavy
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CardSurface)
            )
        },
        bottomBar = {
            Surface(
                color = CardSurface,
                shadowElevation = 0.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Umumiy narx:",
                            fontSize = 12.sp,
                            color = SlateGray
                        )
                        Text(
                            text = Formatters.formatPrice(product.price * quantity),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                    }

                    Button(
                        onClick = {
                            storeViewModel.addToCart(product.id, quantity) { success, msg ->
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(msg)
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(12.dp),
                        enabled = product.stock > 0,
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("detail_add_to_cart_btn")
                    ) {
                        Text(
                            text = if (product.stock > 0) "Savatga qo‘shish" else "Mavjud emas",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
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
                .verticalScroll(rememberScrollState())
        ) {
            // Product Hero Image Area
            AppProductImage(
                imageUri = product.imageUri,
                contentDescription = product.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                contentScale = ContentScale.Crop,
                iconSize = 48.dp
            )

            // Info Card
            Column(modifier = Modifier.padding(16.dp)) {
                // Category & Seller Tags
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PrimaryBlueLight
                    ) {
                        Text(
                            text = product.categoryName,
                            color = PrimaryBlue,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    if (product.sellerName.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Storefront,
                                contentDescription = null,
                                tint = SecondaryText,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = product.sellerName,
                                fontSize = 13.sp,
                                color = SecondaryText,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Title
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp
                    ),
                    color = DarkText
                )

                // Rating & Reviews Count under Title
                val avgRating = if (reviews.isNotEmpty()) reviews.map { it.rating }.average() else 0.0
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 6.dp, bottom = 4.dp)
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Filled.Star,
                        contentDescription = null,
                        tint = StarGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (reviews.isNotEmpty()) String.format(Locale.US, "%.1f", avgRating) else "Yangi",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryNavy
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "(${reviews.size} ta sharh)",
                        fontSize = 12.sp,
                        color = SlateGray,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "•",
                        fontSize = 12.sp,
                        color = SlateGray
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Gagarin kafolati",
                        fontSize = 12.sp,
                        color = PrimaryBlue,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Price and stock status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = Formatters.formatPrice(product.price),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp
                        ),
                        color = PrimaryBlue
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (product.stock > 0) SuccessGreenLight else Color(0xFFFEE2E2)
                    ) {
                        Text(
                            text = if (product.stock > 0) "Mavjud: ${product.stock} ${product.unit}" else "Mavjud emas",
                            color = if (product.stock > 0) SuccessGreen else DangerRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quantity selector
                if (product.stock > 0) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Miqdor:",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = SecondaryNavy
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { if (quantity > 1) quantity-- },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFF1F5F9))
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Remove,
                                        contentDescription = "Kamaytirish",
                                        tint = SecondaryNavy,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Text(
                                    text = "$quantity ${product.unit}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SecondaryNavy,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )

                                IconButton(
                                    onClick = { if (quantity < product.stock) quantity++ },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFF1F5F9))
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Add,
                                        contentDescription = "Oshirish",
                                        tint = SecondaryNavy,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Delivery Perks in Gagarin
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryBlueLight.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.LocalShipping,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Bepul yetkazib berish",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = SecondaryNavy
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Gagarin shahri va Mirzacho‘l tumani bo‘ylab barcha buyurtmalar bepul yetkaziladi.",
                            fontSize = 12.sp,
                            color = SlateGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Description
                Text(
                    text = "Tavsif",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = DarkText
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (product.description.isNotBlank()) product.description else "Ushbu mahsulot uchun qo‘shimcha tavsif kiritilmagan.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = Color(0xFF334155),
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 1. SIMILAR PRODUCTS RECOMMENDATION SECTION
                if (similarProducts.isNotEmpty()) {
                    RecommendationSection(
                        title = "🔄 O‘xshash mahsulotlar",
                        subtitle = "Shu kabi xususiyatga ega boshqa tovarlar",
                        icon = Icons.Filled.AutoAwesome,
                        iconColor = Color(0xFF6D28D9),
                        products = similarProducts,
                        favoriteIds = favIds,
                        onProductClick = { recProduct ->
                            storeViewModel.trackRecommendationClick(recProduct.id)
                            onProductClick?.invoke(recProduct)
                        },
                        onAddToCart = { recProduct ->
                            storeViewModel.addToCart(recProduct.id, 1)
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("${recProduct.name} savatga qo‘shildi")
                            }
                        },
                        onToggleFavorite = { recProduct ->
                            storeViewModel.toggleFavorite(recProduct.id)
                        },
                        onItemImpression = { recId ->
                            storeViewModel.trackRecommendationImpression(recId)
                        },
                        badgeText = "O‘xshash",
                        badgeColor = Color(0xFF6D28D9),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // 2. CATEGORY TRENDING RECOMMENDATION SECTION
                if (categoryTrending.isNotEmpty()) {
                    RecommendationSection(
                        title = "🔥 Shu toifadagi xaridorgir tovarlar",
                        subtitle = "Mijozlar tomonidan ko‘p tanlangan mahsulotlar",
                        icon = Icons.Filled.LocalFireDepartment,
                        iconColor = Color(0xFFEA580C),
                        products = categoryTrending,
                        favoriteIds = favIds,
                        onProductClick = { recProduct ->
                            storeViewModel.trackRecommendationClick(recProduct.id)
                            onProductClick?.invoke(recProduct)
                        },
                        onAddToCart = { recProduct ->
                            storeViewModel.addToCart(recProduct.id, 1)
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("${recProduct.name} savatga qo‘shildi")
                            }
                        },
                        onToggleFavorite = { recProduct ->
                            storeViewModel.toggleFavorite(recProduct.id)
                        },
                        onItemImpression = { recId ->
                            storeViewModel.trackRecommendationImpression(recId)
                        },
                        badgeText = "Trend",
                        badgeColor = Color(0xFFEA580C),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Reviews & 5-Star Ratings Section
                ProductRatingSummaryView(
                    reviews = reviews,
                    onWriteReviewClick = { showAddReviewDialog = true }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // List of Reviews
                if (reviews.isEmpty()) {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = PrimaryBlueLight.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            StarRatingBar(
                                rating = 5,
                                starSize = 26.dp,
                                starSpacing = 4.dp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Birinchi bo‘lib sharh qoldiring!",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = SecondaryNavy
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Mahsulot haqida o‘z fikringizni bildiring va 5 yulduzli tizimda baholang.",
                                fontSize = 12.sp,
                                color = SlateGray,
                                lineHeight = 16.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { showAddReviewDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Sharh yozish", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        reviews.forEach { review ->
                            ReviewCardItem(review = review)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
