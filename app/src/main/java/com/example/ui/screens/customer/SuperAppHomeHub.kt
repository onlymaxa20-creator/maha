package com.example.ui.screens.customer

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.R
import com.example.data.local.entity.FoodProductEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.PromoBannerEntity
import com.example.ui.components.Formatters
import com.example.ui.components.ProductCard
import com.example.ui.components.PromoBannerSection
import com.example.ui.theme.BorderColor
import com.example.ui.theme.CardSurface
import com.example.ui.theme.DarkBurgundy
import com.example.ui.theme.DarkText
import com.example.ui.theme.LightBackground
import com.example.ui.theme.PrimaryBurgundy
import com.example.ui.theme.SecondaryText
import com.example.ui.theme.SurfaceSubtle
import com.example.ui.viewmodels.EcosystemTab

data class HubServiceCardItem(
    val id: String,
    val title: String,
    val titleHighlight: String? = null,
    val badge: String? = null,
    val imageRes: Int,
    val tab: EcosystemTab
)

@Composable
fun SuperAppHomeHub(
    products: List<ProductEntity>,
    foodProducts: List<FoodProductEntity>,
    favoriteProductIds: Set<Long>,
    promoBanners: List<PromoBannerEntity> = emptyList(),
    onProductClick: (ProductEntity) -> Unit,
    onAddToCart: (ProductEntity) -> Unit,
    onToggleFavorite: (ProductEntity) -> Unit,
    onAddFoodToCart: (FoodProductEntity) -> Unit,
    onNavigateToTab: (EcosystemTab) -> Unit,
    onPlusClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val hubServices = remember {
        listOf(
            HubServiceCardItem(
                id = "market",
                title = "Market",
                badge = "SALOM",
                imageRes = R.drawable.hub_market_icon_1788619858751,
                tab = EcosystemTab.BOZOR
            ),
            HubServiceCardItem(
                id = "food",
                title = "Taomlar",
                badge = "-40% gacha",
                imageRes = R.drawable.hub_food_icon_1788619877706,
                tab = EcosystemTab.FOOD
            ),
            HubServiceCardItem(
                id = "ads",
                title = "E'lonlar",
                imageRes = R.drawable.hub_ads_icon_1788619897635,
                tab = EcosystemTab.ADS
            ),
            HubServiceCardItem(
                id = "services",
                title = "Xizmatlar",
                imageRes = R.drawable.hub_services_icon_1788619939599,
                tab = EcosystemTab.SERVICES
            ),
            HubServiceCardItem(
                id = "jobs",
                title = "Vakansiyalar",
                imageRes = R.drawable.hub_jobs_icon_1788619959740,
                tab = EcosystemTab.JOBS
            ),
            HubServiceCardItem(
                id = "more_services",
                title = "Yangi xizmat",
                titleHighlight = "Tez kunda",
                badge = "+",
                imageRes = 0,
                tab = EcosystemTab.HOME_HUB
            )
        )
    }

    val homeBanners = remember(promoBanners) {
        promoBanners.filter { 
            it.actionTag.isBlank() || 
            it.actionTag.equals("HOME", ignoreCase = true) || 
            it.actionTag.equals("ALL", ignoreCase = true) 
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
            .fillMaxSize()
            .testTag("super_app_home_hub")
    ) {
        // 1. KATTA 3D XIZMATLAR KARTOCHKASI (2 USTUNLI TO'R) VA PLUS TUGMASI
        items(hubServices, key = { it.id }) { service ->
            HubServiceGridCard(
                item = service,
                onClick = { 
                    if (service.id == "more_services") {
                        onPlusClick()
                    } else {
                        onNavigateToTab(service.tab)
                    }
                }
            )
        }

        // 2. ADMIN REKLAMA BANNERI YOKI GAGARIN GO BANNERI
        item(span = { GridItemSpan(2) }) {
            if (homeBanners.isNotEmpty()) {
                PromoBannerSection(
                    banners = homeBanners,
                    modifier = Modifier.padding(top = 4.dp)
                )
            } else {
                SuperAppPromoBanner(
                    onExplore = { onNavigateToTab(EcosystemTab.BOZOR) }
                )
            }
        }

        // 3. MAHSULOT VA TAOMLAR CHIQUVCHI BO'LIMLAR
        // A) BOZOR MAHSULOTLARI SARLAVHASI
        item(span = { GridItemSpan(2) }) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color(0xFFFDF2F4), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ShoppingBag,
                            contentDescription = null,
                            tint = PrimaryBurgundy,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Bozor Mahsulotlari",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkText
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onNavigateToTab(EcosystemTab.BOZOR) }
                        .padding(4.dp)
                ) {
                    Text(
                        text = "Barchasi",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBurgundy
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = PrimaryBurgundy,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // B) BOZOR MAHSULOTLARI TO'RI
        if (products.isEmpty()) {
            item(span = { GridItemSpan(2) }) {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurface),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Hozirda sotuvda mahsulotlar yangilanmoqda",
                            fontSize = 13.sp,
                            color = SecondaryText
                        )
                    }
                }
            }
        } else {
            items(products.take(6), key = { "hub_prod_${it.id}" }) { product ->
                ProductCard(
                    product = product,
                    isFavorite = favoriteProductIds.contains(product.id),
                    onProductClick = { onProductClick(product) },
                    onAddToCart = { onAddToCart(product) },
                    onToggleFavorite = { onToggleFavorite(product) }
                )
            }
        }

        // C) GAGARIN TAOMLAR SARLAVHASI
        item(span = { GridItemSpan(2) }) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color(0xFFFFEDD5), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Restaurant,
                            contentDescription = null,
                            tint = Color(0xFFEA580C),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Mazali Taomlar",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkText
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onNavigateToTab(EcosystemTab.FOOD) }
                        .padding(4.dp)
                ) {
                    Text(
                        text = "Barchasi",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEA580C)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = Color(0xFFEA580C),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // D) TAOMLAR TO'RI
        if (foodProducts.isEmpty()) {
            item(span = { GridItemSpan(2) }) {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurface),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Restoranlar menyusi yangilanmoqda",
                            fontSize = 13.sp,
                            color = SecondaryText
                        )
                    }
                }
            }
        } else {
            items(foodProducts.take(6), key = { "hub_food_${it.id}" }) { food ->
                HubFoodProductCard(
                    food = food,
                    onAddToCart = { onAddFoodToCart(food) },
                    onClick = { onNavigateToTab(EcosystemTab.FOOD) }
                )
            }
        }

        // Quyi mualliflik belgisi
        item(span = { GridItemSpan(2) }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp, bottom = 28.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Gagarin Go • Bir ilova – barcha xizmatlar",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = SecondaryText
                )
            }
        }
    }
}

@Composable
fun HubServiceGridCard(
    item: HubServiceCardItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F1F4)),
        modifier = modifier
            .fillMaxWidth()
            .height(152.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .testTag("hub_service_${item.id}")
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // O'ng burchakdagi Badge
            if (item.badge != null) {
                Surface(
                    shape = RoundedCornerShape(topStart = 0.dp, bottomStart = 10.dp, topEnd = 20.dp, bottomEnd = 0.dp),
                    color = PrimaryBurgundy,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Text(
                        text = item.badge,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.5.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Markazdagi 3D Render Tasvir yoki Plus ikonkasi
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (item.imageRes != 0) {
                        Image(
                            painter = painterResource(id = item.imageRes),
                            contentDescription = item.title,
                            modifier = Modifier
                                .size(92.dp)
                                .padding(top = if (item.badge != null) 4.dp else 0.dp),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(
                                    brush = Brush.linearGradient(
                                        listOf(PrimaryBurgundy, Color(0xFF5A0E24))
                                    ),
                                    shape = CircleShape
                                )
                                .shadow(4.dp, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Yangi xizmat",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }

                // Sarlavha
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(bottom = 2.dp)
                ) {
                    Text(
                        text = item.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp,
                        color = DarkText
                    )
                    if (item.titleHighlight != null) {
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = item.titleHighlight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = PrimaryBurgundy
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SuperAppPromoBanner(
    onExplore: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F1F4)),
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Gagarin",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = DarkText
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Go",
                            fontWeight = FontWeight.Black,
                            fontStyle = FontStyle.Italic,
                            fontSize = 18.sp,
                            color = PrimaryBurgundy
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Bir ilova — barcha xizmatlar!",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkText
                    )
                    Text(
                        text = "Tez, qulay va ishonchli",
                        fontSize = 11.5.sp,
                        color = SecondaryText
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onExplore,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBurgundy),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Batafsil",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Image(
                    painter = painterResource(id = R.drawable.hub_banner_art_1788619983779),
                    contentDescription = null,
                    modifier = Modifier
                        .size(110.dp)
                        .clip(RoundedCornerShape(14.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Indicator dots
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 16.dp, height = 5.dp)
                        .background(PrimaryBurgundy, CircleShape)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Box(
                    modifier = Modifier
                        .size(width = 5.dp, height = 5.dp)
                        .background(Color(0xFFE5E7EB), CircleShape)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Box(
                    modifier = Modifier
                        .size(width = 5.dp, height = 5.dp)
                        .background(Color(0xFFE5E7EB), CircleShape)
                )
            }
        }
    }
}

@Composable
fun HubFoodProductCard(
    food: FoodProductEntity,
    onAddToCart: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F1F4)),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
                    .background(Color(0xFFF3F4F6))
            ) {
                SubcomposeAsyncImage(
                    model = food.imageUri.ifBlank { "https://images.unsplash.com/photo-1546069901-ba9599a7e63c" },
                    contentDescription = food.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Restoran nomi chip
                Surface(
                    shape = RoundedCornerShape(bottomEnd = 8.dp),
                    color = Color.Black.copy(alpha = 0.65f),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Text(
                        text = food.restaurantName,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = food.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = food.preparationTime,
                    fontSize = 10.5.sp,
                    color = SecondaryText
                )

                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${Formatters.formatPrice(food.price)} so'm",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFEA580C)
                    )

                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFEA580C),
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .clickable { onAddToCart() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Savatga qo'shish",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
