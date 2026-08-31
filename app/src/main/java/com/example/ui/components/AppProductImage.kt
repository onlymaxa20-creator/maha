package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.data.util.ImageStorageHelper
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueLight
import com.example.ui.theme.SurfaceSubtle

/**
 * Universal, robust image display component for Gagarin Go ecosystem.
 * Automatically resolves Base64 images, remote HTTPS URLs, local file paths,
 * and Content URIs seamlessly across all Android devices.
 */
@Composable
fun AppProductImage(
    imageUri: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    shape: Shape = RectangleShape,
    iconSize: Dp = 48.dp,
    placeholderIcon: ImageVector = Icons.Filled.ShoppingBag,
    backgroundColor: Color = SurfaceSubtle
) {
    val context = LocalContext.current
    val imageModel = remember(imageUri) {
        ImageStorageHelper.resolveImageModel(imageUri, context)
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        when (imageModel) {
            is Bitmap -> {
                Image(
                    bitmap = imageModel.asImageBitmap(),
                    contentDescription = contentDescription,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = contentScale
                )
            }
            null -> {
                PlaceholderView(
                    icon = placeholderIcon,
                    iconSize = iconSize
                )
            }
            else -> {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imageModel)
                        .crossfade(true)
                        .build(),
                    contentDescription = contentDescription,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = contentScale,
                    loading = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = PrimaryBlue
                            )
                        }
                    },
                    error = {
                        PlaceholderView(
                            icon = placeholderIcon,
                            iconSize = iconSize
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun PlaceholderView(
    icon: ImageVector,
    iconSize: Dp
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = CircleShape,
            color = PrimaryBlueLight,
            modifier = Modifier.size(iconSize * 1.6f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(iconSize)
                )
            }
        }
    }
}
