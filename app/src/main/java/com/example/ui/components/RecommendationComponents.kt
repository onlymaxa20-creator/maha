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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.AddShoppingCart
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ProductEntity
import com.example.ui.theme.BorderColor
import com.example.ui.theme.CardSurface
import com.example.ui.theme.DangerRed
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBurgundy
import com.example.ui.theme.SecondaryNavy
import com.example.ui.theme.SlateGray
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SurfaceSubtle

/**
 * Modern recommendation card component for personalized carousels.
 */
@Composable
fun RecommendationProductCard(
    product: ProductEntity,
    isFavorite: Boolean,
    onProductClick: () -> Unit,
    onAddToCart: () -> Unit,
    onToggleFavorite: () -> Unit,
    badgeText: String? = "Tavsiya",
    badgeColor: Color = Color(0xFF6D28D9),
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(170.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onProductClick)
            .testTag("rec_product_card_${product.id}"),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Image Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(135.dp)
                    .background(SurfaceSubtle)
            ) {
                AppProductImage(
                    imageUri = product.imageUri,
                    contentDescription = product.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(135.dp),
                    contentScale = ContentScale.Crop,
                    iconSize = 28.dp
                )

                // Recommendation / Special Badge on Top Left
                if (!badgeText.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(topStart = 0.dp, bottomEnd = 10.dp),
                        color = badgeColor,
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Text(
                            text = badgeText,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                }

                // Wishlist Heart Button
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(5.dp)
                        .size(32.dp)
                        .background(Color.White.copy(alpha = 0.9f), CircleShape)
                        .testTag("rec_fav_${product.id}")
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Sevimlilar",
                        tint = if (isFavorite) DangerRed else SlateGray,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Discount Badge on bottom left of image if discount exists
                if (product.originalPrice > product.price && product.originalPrice > 0) {
                    val discount = (((product.originalPrice - product.price) / product.originalPrice) * 100).toInt()
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = DangerRed,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(6.dp)
                    ) {
                        Text(
                            text = "-$discount%",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Product Details
            Column(modifier = Modifier.padding(10.dp)) {
                // Store name
                if (product.sellerName.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Storefront,
                            contentDescription = null,
                            tint = SlateGray,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = product.sellerName,
                            fontSize = 10.sp,
                            color = SlateGray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                }

                // Product Name
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    ),
                    color = SecondaryNavy,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.height(34.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Price and Quick Add
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        if (product.originalPrice > product.price && product.originalPrice > 0) {
                            Text(
                                text = Formatters.formatPrice(product.originalPrice),
                                fontSize = 10.sp,
                                color = SlateGray,
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                            )
                        }
                        Text(
                            text = Formatters.formatPrice(product.price),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            ),
                            color = PrimaryBurgundy
                        )
                    }

                    IconButton(
                        onClick = onAddToCart,
                        modifier = Modifier
                            .size(30.dp)
                            .background(PrimaryBlue, RoundedCornerShape(8.dp))
                            .testTag("rec_add_cart_${product.id}"),
                        enabled = product.stock > 0
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AddShoppingCart,
                            contentDescription = "Savatga qo‘shish",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Universal Recommendation Carousel Section.
 */
@Composable
fun RecommendationSection(
    title: String,
    subtitle: String? = null,
    icon: ImageVector = Icons.Filled.AutoAwesome,
    iconColor: Color = Color(0xFF7C3AED),
    products: List<ProductEntity>,
    favoriteIds: Set<Long>,
    onProductClick: (ProductEntity) -> Unit,
    onAddToCart: (ProductEntity) -> Unit,
    onToggleFavorite: (ProductEntity) -> Unit,
    onItemImpression: ((Long) -> Unit)? = null,
    badgeText: String? = "Tavsiya",
    badgeColor: Color = Color(0xFF6D28D9),
    modifier: Modifier = Modifier
) {
    if (products.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(iconColor.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = SecondaryNavy
                    )
                    if (!subtitle.isNullOrBlank()) {
                        Text(
                            text = subtitle,
                            fontSize = 11.sp,
                            color = SlateGray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Horizontal Product List
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
        ) {
            items(products, key = { it.id }) { product ->
                LaunchedEffect(product.id) {
                    onItemImpression?.invoke(product.id)
                }

                RecommendationProductCard(
                    product = product,
                    isFavorite = favoriteIds.contains(product.id),
                    onProductClick = { onProductClick(product) },
                    onAddToCart = { onAddToCart(product) },
                    onToggleFavorite = { onToggleFavorite(product) },
                    badgeText = badgeText,
                    badgeColor = badgeColor
                )
            }
        }
    }
}
