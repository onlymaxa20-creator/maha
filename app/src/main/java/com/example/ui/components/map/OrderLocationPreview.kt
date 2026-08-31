package com.example.ui.components.map

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SecondaryNavy
import com.example.ui.theme.SlateGray
import com.example.ui.theme.SuccessGreen
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.util.Locale

@Composable
fun OrderLocationSection(
    customerName: String,
    customerPhone: String,
    deliveryAddress: String,
    latitude: Double,
    longitude: Double,
    street: String = "",
    houseNumber: String = "",
    landmark: String = "",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showFullMapModal by remember { mutableStateOf(false) }

    val safeLat = if (latitude != 0.0) latitude else 40.6622
    val safeLng = if (longitude != 0.0) longitude else 68.1672

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = BurgundyRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Yetkazib berish manzili:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = SecondaryNavy
                    )
                }

                Surface(
                    color = BurgundyRed.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = String.format(Locale.US, "%.4f, %.4f", safeLat, safeLng),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = BurgundyRed,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = deliveryAddress.ifBlank { "Gagarin shahri, Mirzacho‘l tumani" },
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = SecondaryNavy
            )

            if (street.isNotBlank() || houseNumber.isNotBlank()) {
                val detailStr = buildString {
                    if (street.isNotBlank()) append("Ko‘cha: $street")
                    if (houseNumber.isNotBlank()) {
                        if (isNotEmpty()) append(", ")
                        append("Uy: $houseNumber")
                    }
                }
                Text(
                    text = detailStr,
                    fontSize = 12.sp,
                    color = SlateGray
                )
            }

            if (landmark.isNotBlank()) {
                Text(
                    text = "Mo‘ljal: $landmark",
                    fontSize = 12.sp,
                    color = BurgundyRed,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Mini Map Preview (Interactive on click)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(10.dp))
                    .clickable { showFullMapModal = true }
            ) {
                AndroidView(
                    factory = { ctx ->
                        try {
                            Configuration.getInstance().load(ctx, ctx.getSharedPreferences("osmdroid_gagarin", Context.MODE_PRIVATE))
                            Configuration.getInstance().userAgentValue = ctx.packageName
                        } catch (_: Exception) {}

                        MapView(ctx).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(false) // fixed preview
                            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
                            controller.setZoom(15.5)
                            val geoPoint = GeoPoint(safeLat, safeLng)
                            controller.setCenter(geoPoint)

                            val marker = Marker(this).apply {
                                position = geoPoint
                                title = customerName.ifBlank { "Mijoz manzili" }
                                snippet = deliveryAddress
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            }
                            overlays.add(marker)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Overlay Click hint badge
                Surface(
                    color = Color.Black.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Icon(Icons.Default.Fullscreen, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Kattalashtirish", color = Color.White, fontSize = 10.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons: "Xaritada ko‘rish" & "Navigatsiyani boshlash"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { showFullMapModal = true },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("view_on_map_btn")
                ) {
                    Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Xaritada ko‘rish", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = {
                        startNavigation(context, safeLat, safeLng, customerName)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BurgundyRed),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1.2f)
                        .testTag("start_navigation_btn")
                ) {
                    Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Navigatsiyani boshlash", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showFullMapModal) {
        FullOrderMapModal(
            customerName = customerName,
            customerPhone = customerPhone,
            deliveryAddress = deliveryAddress,
            latitude = safeLat,
            longitude = safeLng,
            street = street,
            houseNumber = houseNumber,
            landmark = landmark,
            onDismiss = { showFullMapModal = false },
            onNavigate = { startNavigation(context, safeLat, safeLng, customerName) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullOrderMapModal(
    customerName: String,
    customerPhone: String,
    deliveryAddress: String,
    latitude: Double,
    longitude: Double,
    street: String = "",
    houseNumber: String = "",
    landmark: String = "",
    onDismiss: () -> Unit,
    onNavigate: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        modifier = Modifier.fillMaxHeight(0.92f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = BurgundyRed.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Navigation, contentDescription = null, tint = BurgundyRed, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Mijoz yetkazish koordinatalari",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = SecondaryNavy
                        )
                        Text(
                            text = String.format(Locale.US, "Lat: %.6f, Lng: %.6f", latitude, longitude),
                            fontSize = 11.sp,
                            color = SlateGray
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Yopish")
                }
            }

            // Interactive Map with Pin Marker
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(14.dp))
            ) {
                AndroidView(
                    factory = { ctx ->
                        try {
                            Configuration.getInstance().load(ctx, ctx.getSharedPreferences("osmdroid_gagarin", Context.MODE_PRIVATE))
                            Configuration.getInstance().userAgentValue = ctx.packageName
                        } catch (_: Exception) {}

                        MapView(ctx).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)
                            zoomController.setVisibility(CustomZoomButtonsController.Visibility.ALWAYS)
                            controller.setZoom(16.5)
                            val geoPoint = GeoPoint(latitude, longitude)
                            controller.setCenter(geoPoint)

                            val marker = Marker(this).apply {
                                position = geoPoint
                                title = customerName.ifBlank { "Mijoz" }
                                snippet = deliveryAddress
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                showInfoWindow()
                            }
                            overlays.add(marker)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Details card & Navigation button
            Card(
                colors = CardDefaults.cardColors(containerColor = BurgundyLight),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Mijoz: $customerName",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = SecondaryNavy
                    )
                    if (customerPhone.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                try {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${customerPhone.replace(" ", "")}"))
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            }
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Tel: $customerPhone",
                                fontSize = 13.sp,
                                color = PrimaryBlue,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Text(
                        text = "Manzil: $deliveryAddress",
                        fontSize = 13.sp,
                        color = SecondaryNavy
                    )
                    if (street.isNotBlank() || houseNumber.isNotBlank()) {
                        Text(
                            text = "Ko‘cha / Uy: $street ${if (houseNumber.isNotBlank()) "$houseNumber-uy" else ""}",
                            fontSize = 12.sp,
                            color = SlateGray
                        )
                    }
                    if (landmark.isNotBlank()) {
                        Text(
                            text = "Mo‘ljal: $landmark",
                            fontSize = 12.sp,
                            color = BurgundyRed,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onNavigate,
                colors = ButtonDefaults.buttonColors(containerColor = BurgundyRed),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(horizontal = 16.dp)
                    .testTag("modal_start_navigation_btn")
            ) {
                Icon(Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Navigatsiyani boshlash (Google / Yandex Maps)", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

fun startNavigation(context: Context, latitude: Double, longitude: Double, label: String = "Mijoz") {
    try {
        // Priority 1: Google Navigation Intent
        val navUri = Uri.parse("google.navigation:q=$latitude,$longitude&mode=d")
        val mapIntent = Intent(Intent.ACTION_VIEW, navUri).apply {
            setPackage("com.google.android.apps.maps")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        if (mapIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(mapIntent)
            return
        }

        // Priority 2: Generic Geo Intent with coordinates and label
        val geoUri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude($label)")
        val genericIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        if (genericIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(genericIntent)
            return
        }

        // Priority 3: Browser Fallback (Google Maps web URL)
        val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$latitude,$longitude")
        val webIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(webIntent)
    } catch (e: Exception) {
        Toast.makeText(context, "Navigatsiyani ochishda xatolik: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
