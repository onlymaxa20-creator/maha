package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import com.example.data.local.entity.PromoBannerEntity
import com.example.data.util.ImageStorageHelper
import kotlinx.coroutines.delay

fun getBannerBrush(gradientType: String): Brush {
    return when (gradientType.lowercase()) {
        "burgundy" -> Brush.linearGradient(listOf(Color(0xFF8B1E3F), Color(0xFF5A0E24)))
        "orange" -> Brush.linearGradient(listOf(Color(0xFFEA580C), Color(0xFFC2410C)))
        "blue" -> Brush.linearGradient(listOf(Color(0xFF2563EB), Color(0xFF1E40AF)))
        "emerald" -> Brush.linearGradient(listOf(Color(0xFF059669), Color(0xFF065F46)))
        "rose" -> Brush.linearGradient(listOf(Color(0xFFE11D48), Color(0xFF9F1239)))
        else -> Brush.linearGradient(listOf(Color(0xFF8B1E3F), Color(0xFF6B112B))) // Gagarin Burgundy
    }
}

/**
 * Real promo banner carousel placed directly ABOVE categories.
 * Supports both real uploaded image banners and styled gradient banners.
 */
@Composable
fun PromoBannerSection(
    banners: List<PromoBannerEntity>,
    modifier: Modifier = Modifier,
    onBannerClick: ((PromoBannerEntity) -> Unit)? = null
) {
    if (banners.isEmpty()) return

    val context = LocalContext.current
    var currentBannerIndex by remember { mutableIntStateOf(0) }

    // Auto rotate banners if more than 1
    LaunchedEffect(banners.size) {
        if (banners.size > 1) {
            while (true) {
                delay(5000)
                currentBannerIndex = (currentBannerIndex + 1) % banners.size
            }
        }
    }

    val activeBanner = banners.getOrNull(currentBannerIndex % banners.size) ?: banners.first()

    val handleBannerAction: () -> Unit = {
        if (onBannerClick != null) {
            onBannerClick(activeBanner)
        } else if (activeBanner.targetLink.isNotBlank()) {
            val link = activeBanner.targetLink.trim()
            try {
                if (link.startsWith("http://") || link.startsWith("https://")) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                    context.startActivity(intent)
                } else if (link.startsWith("tel:")) {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse(link))
                    context.startActivity(intent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = handleBannerAction)
    ) {
        val hasRealImage = activeBanner.imageUrl.isNotBlank()

        if (hasRealImage) {
            val resolvedBannerImage = ImageStorageHelper.resolveImageModel(activeBanner.imageUrl, context) ?: activeBanner.imageUrl
            // Full-bleed Real Image Banner with elegant bottom gradient scrim
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
            ) {
                SubcomposeAsyncImage(
                    model = resolvedBannerImage,
                    contentDescription = activeBanner.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    loading = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(getBannerBrush(activeBanner.gradientType)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    },
                    error = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(getBannerBrush(activeBanner.gradientType))
                        )
                    }
                )

                // Dark gradient scrim overlay for maximum text readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.2f),
                                    Color.Black.copy(alpha = 0.75f)
                                )
                            )
                        )
                )

                // Banner Details
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top row: Badge and dots
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF8B1E3F).copy(alpha = 0.9f)
                        ) {
                            Text(
                                text = activeBanner.badgeText.ifBlank { "REKLAMA" },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp),
                                letterSpacing = 0.5.sp
                            )
                        }

                        if (banners.size > 1) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                banners.indices.forEach { index ->
                                    Box(
                                        modifier = Modifier
                                            .size(if (index == currentBannerIndex % banners.size) 7.dp else 5.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (index == currentBannerIndex % banners.size) Color.White else Color.White.copy(alpha = 0.5f)
                                            )
                                    )
                                }
                            }
                        }
                    }

                    // Bottom: Title, description, and link icon
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = activeBanner.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            val sub = activeBanner.description.ifBlank { activeBanner.name }
                            if (sub.isNotBlank() && sub != activeBanner.title) {
                                Text(
                                    text = sub,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = Color.White.copy(alpha = 0.92f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        if (activeBanner.targetLink.isNotBlank()) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.25f),
                                modifier = Modifier.size(26.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.ChevronRight,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Gradient Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(getBannerBrush(activeBanner.gradientType))
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left Icon + Text Content
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.22f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (activeBanner.badgeText.contains("AKSIYA")) Icons.Filled.LocalFireDepartment else Icons.Filled.Campaign,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color.White.copy(alpha = 0.28f)
                                ) {
                                    Text(
                                        text = activeBanner.badgeText.ifBlank { "REKLAMA" },
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        letterSpacing = 0.4.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = activeBanner.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            val sub = activeBanner.description.ifBlank { activeBanner.name }
                            if (sub.isNotBlank() && sub != activeBanner.title) {
                                Text(
                                    text = sub,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = Color.White.copy(alpha = 0.9f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // Dot Indicators if multiple banners
                    if (banners.size > 1) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            banners.indices.forEach { index ->
                                Box(
                                    modifier = Modifier
                                        .size(if (index == currentBannerIndex % banners.size) 6.dp else 4.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (index == currentBannerIndex % banners.size) Color.White else Color.White.copy(alpha = 0.4f)
                                        )
                                    )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Modern Hero Banner Card matching the screenshot:
 * - Deep maroon/burgundy card with rounded corners
 * - Left: "New Collection" label, "Elevate Your Everyday Style" bold headline,
 *         subtitle, and "Shop Now" pill button
 * - Right: Lifestyle fashion photoshoot image (handbag, white sneakers, small vase)
 * - Carousel dots indicator below
 */
@Composable
fun ModernHeroBannerCard(
    onShopNowClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(175.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF6B1832), // Deep Maroon
                                Color(0xFF7A1F3D), // Burgundy
                                Color(0xFF531024)  // Dark Maroon
                            )
                        )
                    )
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Text Column
                    Column(
                        modifier = Modifier
                            .weight(1.15f)
                            .padding(start = 18.dp, top = 14.dp, bottom = 14.dp, end = 8.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "New Collection",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFFFD4E2),
                            letterSpacing = 0.5.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Elevate Your\nEveryday Style",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Trendy picks, top brands &\nexclusive deals.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.White.copy(alpha = 0.88f),
                            lineHeight = 14.sp,
                            maxLines = 2
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFF330915),
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable(onClick = onShopNowClick)
                        ) {
                            Text(
                                text = "Shop Now",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }

                    // Right Image Area
                    Box(
                        modifier = Modifier
                            .weight(0.85f)
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp))
                    ) {
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.res.painterResource(
                                id = com.example.R.drawable.img_hero_fashion_1787475164388
                            ),
                            contentDescription = "New Collection Lifestyle",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        // Carousel Dots Indicator below the banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(width = 18.dp, height = 6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFF7A1F3D))
            )
            Spacer(modifier = Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFD1D5DB))
            )
            Spacer(modifier = Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFD1D5DB))
            )
        }
    }
}

