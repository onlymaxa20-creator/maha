package com.example.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.AddShoppingCart
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.data.local.entity.ProductEntity
import com.example.ui.theme.BorderColor
import com.example.ui.theme.CardSurface
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DarkText
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueLight
import com.example.ui.theme.SecondaryNavy
import com.example.ui.theme.SlateGray
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SurfaceSubtle
import java.util.Locale

@Composable
fun ProductCard(
    product: ProductEntity,
    isFavorite: Boolean,
    onProductClick: () -> Unit,
    onAddToCart: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
    rating: Double? = null,
    reviewCount: Int? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onProductClick)
            .testTag("product_card_${product.id}"),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top Image Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(SurfaceSubtle)
            ) {
                AppProductImage(
                    imageUri = product.imageUri,
                    contentDescription = product.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentScale = ContentScale.Crop,
                    iconSize = 28.dp
                )

                // Favorite Button Overlay
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(36.dp)
                        .background(Color.White.copy(alpha = 0.9f), CircleShape)
                        .testTag("favorite_button_${product.id}")
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Sevimlilar",
                        tint = if (isFavorite) DangerRed else SlateGray,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Discount / Aksiya Badge on Top Left
                if (product.originalPrice > product.price && product.originalPrice > 0) {
                    val discount = (((product.originalPrice - product.price) / product.originalPrice) * 100).toInt()
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
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                } else if (product.isPromotional) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFF97316),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp)
                    ) {
                        Text(
                            text = "🔥 AKSIYA",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Category Tag
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = SecondaryNavy.copy(alpha = 0.75f),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                ) {
                    Text(
                        text = product.categoryName,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Info Content
            Column(modifier = Modifier.padding(12.dp)) {
                // Seller Name
                if (product.sellerName.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Storefront,
                            contentDescription = null,
                            tint = SlateGray,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = product.sellerName,
                            fontSize = 11.sp,
                            color = SlateGray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Product Title
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                    color = DarkText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.height(38.dp)
                )

                if (rating != null && rating > 0.0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp, bottom = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = StarGold,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = String.format(Locale.US, "%.1f", rating),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = DarkText
                        )
                        if (reviewCount != null && reviewCount > 0) {
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "($reviewCount)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SlateGray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Stock Info
                Text(
                    text = if (product.stock > 0) "Mavjud: ${product.stock} ${product.unit}" else "Mavjud emas",
                    fontSize = 11.5.sp,
                    color = if (product.stock > 0) SuccessGreen else DangerRed,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Price and Add button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        if (product.originalPrice > product.price && product.originalPrice > 0) {
                            Text(
                                text = Formatters.formatPrice(product.originalPrice),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = SlateGray,
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                            )
                        }
                        Text(
                            text = Formatters.formatPrice(product.price),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 15.5.sp
                            ),
                            color = if (product.originalPrice > product.price && product.originalPrice > 0) Color(0xFFDC2626) else PrimaryBlue
                        )
                    }

                    IconButton(
                        onClick = onAddToCart,
                        modifier = Modifier
                            .size(34.dp)
                            .background(PrimaryBlue, RoundedCornerShape(8.dp))
                            .testTag("add_to_cart_btn_${product.id}"),
                        enabled = product.stock > 0
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AddShoppingCart,
                            contentDescription = "Savatga qo‘shish",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Deal of the Day Card matching the modern e-commerce screenshot:
 * - Rounded white card with clean borders
 * - Top-right wishlist heart button on circular white background
 * - Top-left maroon discount badge (e.g. "40% OFF")
 * - Product title
 * - Current discounted price in bold maroon
 * - Original price with strikethrough
 */
@Composable
fun DealOfTheDayCard(
    product: ProductEntity,
    isFavorite: Boolean,
    onProductClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(160.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onProductClick)
            .testTag("deal_card_${product.id}"),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Product Image Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(SurfaceSubtle)
            ) {
                AppProductImage(
                    imageUri = product.imageUri,
                    contentDescription = product.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    contentScale = ContentScale.Fit,
                    iconSize = 28.dp
                )

                // Discount Badge (e.g., "40% OFF")
                val discountPercent = if (product.originalPrice > product.price && product.originalPrice > 0) {
                    (((product.originalPrice - product.price) / product.originalPrice) * 100).toInt()
                } else if (product.isPromotional) 40 else 0

                if (discountPercent > 0) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = PrimaryBlue,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "$discountPercent% OFF",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Heart Wishlist Button on Top-Right
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(32.dp)
                        .background(Color.White.copy(alpha = 0.95f), CircleShape)
                        .testTag("deal_fav_${product.id}")
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Wishlist",
                        tint = if (isFavorite) DangerRed else SlateGray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Info Area
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = product.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SecondaryNavy,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = Formatters.formatPrice(product.price),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )

                    if (product.originalPrice > product.price && product.originalPrice > 0) {
                        Text(
                            text = Formatters.formatPrice(product.originalPrice),
                            fontSize = 11.sp,
                            color = SlateGray,
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                        )
                    }
                }
            }
        }
    }
}
