package com.example.ui.components

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.data.local.entity.ProductEntity
import com.example.ui.theme.BorderColor
import com.example.ui.theme.CardSurface
import com.example.ui.theme.DangerRed
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueLight
import com.example.ui.theme.SecondaryNavy
import com.example.ui.theme.SlateGray

@Composable
fun FlashSaleSection(
    promotionalProducts: List<ProductEntity>,
    onProductClick: (ProductEntity) -> Unit,
    onAddToCart: (ProductEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    if (promotionalProducts.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFFFEDD5),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.LocalFireDepartment,
                            contentDescription = null,
                            tint = Color(0xFFEA580C),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Aksiyalar va Chegirmalar",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp
                        ),
                        color = SecondaryNavy
                    )
                    Text(
                        text = "Faqat Gagarin Go da super narxlar",
                        fontSize = 11.sp,
                        color = SlateGray
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFEE2E2),
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Text(
                    text = "🔥 SUPER NARX",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFDC2626),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Horizontal Carousel of Promo Products
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(promotionalProducts, key = { it.id }) { product ->
                AksiyaProductCard(
                    product = product,
                    onClick = { onProductClick(product) },
                    onAddToCart = { onAddToCart(product) }
                )
            }
        }
    }
}

@Composable
fun AksiyaProductCard(
    product: ProductEntity,
    onClick: () -> Unit,
    onAddToCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    val discount = if (product.originalPrice > product.price && product.originalPrice > 0) {
        (((product.originalPrice - product.price) / product.originalPrice) * 100).toInt()
    } else {
        15 // Default promo visual tag if marked as promotional
    }

    Card(
        modifier = modifier
            .width(150.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("promo_product_card_${product.id}"),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Image Area with Discount Badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
                    .background(Color(0xFFF8FAFC))
            ) {
                if (product.imageUri.isNotBlank()) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(product.imageUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        loading = {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = PrimaryBlue
                                )
                            }
                        },
                        error = {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.ShoppingBag,
                                    contentDescription = null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Surface(
                            shape = CircleShape,
                            color = PrimaryBlueLight,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.ShoppingBag,
                                    contentDescription = null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }

                // Discount Pill
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFEF4444),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                ) {
                    Text(
                        text = "-$discount%",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                    )
                }
            }

            // Body Info
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = product.name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SecondaryNavy,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(3.dp))

                // Original Price Strikethrough
                if (product.originalPrice > product.price && product.originalPrice > 0) {
                    Text(
                        text = Formatters.formatPrice(product.originalPrice),
                        fontSize = 10.sp,
                        color = SlateGray,
                        textDecoration = TextDecoration.LineThrough
                    )
                } else {
                    Text(
                        text = Formatters.formatPrice(product.price * 1.25),
                        fontSize = 10.sp,
                        color = SlateGray,
                        textDecoration = TextDecoration.LineThrough
                    )
                }

                // Final Discounted Price + Add Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = Formatters.formatPrice(product.price),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFDC2626)
                    )

                    IconButton(
                        onClick = onAddToCart,
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color(0xFFEF4444), RoundedCornerShape(7.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AddShoppingCart,
                            contentDescription = "Savatga",
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}
