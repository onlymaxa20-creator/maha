package com.example.ui.components.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Signpost
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.data.model.DeliveryLocationData
import com.example.data.util.LocationStorageHelper
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SecondaryNavy
import com.example.ui.theme.SlateGray
import com.example.ui.theme.SuccessGreen
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import java.util.Locale

// Burgundy & Red palette accents matching GagarinGo style
val BurgundyRed = Color(0xFF8B0000)
val BurgundyDeep = Color(0xFF880E4F)
val BurgundyLight = Color(0xFFFFEBEE)

data class GagarinPresetArea(
    val name: String,
    val description: String,
    val lat: Double,
    val lng: Double
)

val GAGARIN_PRESET_AREAS = listOf(
    GagarinPresetArea("Markaz", "Gagarin shahar markazi", 40.6622, 68.1672),
    GagarinPresetArea("Mustaqillik shoh ko‘chasi", "Mustaqillik ko‘chasi", 40.6650, 68.1635),
    GagarinPresetArea("Dehqon Bozor", "Gagarin markaziy bozori", 40.6610, 68.1690),
    GagarinPresetArea("Do‘stlik MFY", "Do‘stlik mahallasi", 40.6585, 68.1720),
    GagarinPresetArea("Bo‘ston MFY", "Bo‘ston mahallasi", 40.6680, 68.1610),
    GagarinPresetArea("Markaziy Shifoxona", "Mirzacho‘l markaziy shifoxonasi", 40.6570, 68.1640),
    GagarinPresetArea("Navbahor MFY", "Navbahor mahallasi", 40.6720, 68.1710),
    GagarinPresetArea("Guliston yo‘li", "Gagarin-Guliston trassasi", 40.6540, 68.1800)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryMapPickerSheet(
    initialLocation: DeliveryLocationData? = null,
    onDismiss: () -> Unit,
    onLocationConfirmed: (DeliveryLocationData) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Retrieve last saved location or default Gagarin
    val saved = remember { initialLocation ?: LocationStorageHelper.getSavedLocation(context) }

    var currentLat by remember { mutableDoubleStateOf(saved.latitude) }
    var currentLng by remember { mutableDoubleStateOf(saved.longitude) }
    var readableAddress by remember { mutableStateOf(saved.readableAddress) }
    var streetInput by remember { mutableStateOf(saved.street) }
    var houseInput by remember { mutableStateOf(saved.houseNumber) }
    var landmarkInput by remember { mutableStateOf(saved.landmark) }

    var isGeocoding by remember { mutableStateOf(false) }
    var isLocatingGps by remember { mutableStateOf(false) }
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }

    // Geocoding helper debounce
    LaunchedEffect(currentLat, currentLng) {
        delay(400) // debounce map moves
        isGeocoding = true
        withContext(Dispatchers.IO) {
            try {
                var foundAddress: String? = null
                if (Geocoder.isPresent()) {
                    val geocoder = Geocoder(context, Locale("uz", "UZ"))
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(currentLat, currentLng, 1)
                    if (!addresses.isNullOrEmpty()) {
                        val addr: Address = addresses[0]
                        val thoroughfare = addr.thoroughfare ?: addr.subLocality ?: ""
                        val feature = addr.featureName ?: ""
                        val subAdmin = addr.subAdminArea ?: "Mirzacho‘l tumani"
                        val locality = addr.locality ?: "Gagarin shahri"

                        val line = buildString {
                            append(locality)
                            if (subAdmin.isNotBlank() && !subAdmin.equals(locality, ignoreCase = true)) {
                                append(", $subAdmin")
                            }
                            if (thoroughfare.isNotBlank()) {
                                append(", $thoroughfare")
                            } else if (feature.isNotBlank() && feature != thoroughfare) {
                                append(", $feature")
                            }
                        }
                        if (line.isNotBlank()) {
                            foundAddress = line
                            if (streetInput.isBlank() && thoroughfare.isNotBlank()) {
                                streetInput = thoroughfare
                            }
                        }
                    }
                }

                if (foundAddress == null) {
                    // Fallback local smart geocoder for Gagarin & Mirzacho'l
                    foundAddress = getGagarinSmartAddress(currentLat, currentLng)
                }

                withContext(Dispatchers.Main) {
                    readableAddress = foundAddress
                    isGeocoding = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    readableAddress = getGagarinSmartAddress(currentLat, currentLng)
                    isGeocoding = false
                }
            }
        }
    }

    // GPS location launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            fetchDeviceLocation(context) { lat, lng ->
                currentLat = lat
                currentLng = lng
                mapViewRef?.controller?.animateTo(GeoPoint(lat, lng), 17.0, 800L)
            }
        }
        isLocatingGps = false
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        modifier = Modifier.fillMaxHeight(0.95f)
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
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = BurgundyRed.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Place,
                                contentDescription = null,
                                tint = BurgundyRed,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Yetkazib berish manzili",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = SecondaryNavy
                        )
                        Text(
                            text = "Xaritani harakatlantirib manzilni tanlang",
                            fontSize = 11.sp,
                            color = SlateGray
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Yopish", tint = SlateGray)
                }
            }

            // Quick preset area chips for Gagarin
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GAGARIN_PRESET_AREAS.forEach { preset ->
                    Surface(
                        color = if (Math.abs(currentLat - preset.lat) < 0.002 && Math.abs(currentLng - preset.lng) < 0.002) {
                            BurgundyRed
                        } else {
                            Color(0xFFF3F4F6)
                        },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.clickable {
                            currentLat = preset.lat
                            currentLng = preset.lng
                            streetInput = preset.description
                            mapViewRef?.controller?.animateTo(GeoPoint(preset.lat, preset.lng), 16.5, 600L)
                        }
                    ) {
                        Text(
                            text = preset.name,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (Math.abs(currentLat - preset.lat) < 0.002 && Math.abs(currentLng - preset.lng) < 0.002) {
                                Color.White
                            } else {
                                SecondaryNavy
                            },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Interactive Map Section with Center Pin
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
            ) {
                // Real OSMDroid MapView
                AndroidView(
                    factory = { ctx ->
                        try {
                            Configuration.getInstance().load(ctx, ctx.getSharedPreferences("osmdroid_gagarin", Context.MODE_PRIVATE))
                            Configuration.getInstance().userAgentValue = ctx.packageName
                        } catch (_: Exception) {}

                        MapView(ctx).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)
                            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
                            controller.setZoom(16.0)
                            controller.setCenter(GeoPoint(currentLat, currentLng))

                            addMapListener(object : MapListener {
                                override fun onScroll(event: ScrollEvent?): Boolean {
                                    val center = mapCenter
                                    currentLat = center.latitude
                                    currentLng = center.longitude
                                    return true
                                }

                                override fun onZoom(event: ZoomEvent?): Boolean {
                                    val center = mapCenter
                                    currentLat = center.latitude
                                    currentLng = center.longitude
                                    return true
                                }
                            })
                            mapViewRef = this
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Fixed Center Pin Overlay
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        color = SecondaryNavy,
                        shape = RoundedCornerShape(8.dp),
                        shadowElevation = 4.dp
                    ) {
                        Text(
                            text = "Yetkazish joyi",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Markaziy belgi",
                        tint = BurgundyRed,
                        modifier = Modifier
                            .size(42.dp)
                            .shadow(0.dp)
                    )
                    // Pin shadow dot on map
                    Box(
                        modifier = Modifier
                            .size(8.dp, 4.dp)
                            .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                    )
                }

                // Coordinates pill
                Surface(
                    color = Color.White.copy(alpha = 0.92f),
                    shape = RoundedCornerShape(8.dp),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = String.format(Locale.US, "%.5f, %.5f", currentLat, currentLng),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SecondaryNavy
                        )
                        if (isGeocoding) {
                            Spacer(modifier = Modifier.width(6.dp))
                            CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp, color = BurgundyRed)
                        }
                    }
                }

                // Floating Map Controls (Zoom In, Zoom Out, GPS)
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledIconButton(
                        onClick = {
                            isLocatingGps = true
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                fetchDeviceLocation(context) { lat, lng ->
                                    currentLat = lat
                                    currentLng = lng
                                    mapViewRef?.controller?.animateTo(GeoPoint(lat, lng), 17.0, 800L)
                                    isLocatingGps = false
                                }
                            } else {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.White),
                        shape = CircleShape,
                        modifier = Modifier
                            .size(42.dp)
                            .shadow(3.dp, CircleShape)
                            .testTag("gps_my_location_btn")
                    ) {
                        if (isLocatingGps) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = BurgundyRed)
                        } else {
                            Icon(Icons.Default.MyLocation, contentDescription = "Mening joylashuvim", tint = BurgundyRed)
                        }
                    }

                    FilledIconButton(
                        onClick = {
                            mapViewRef?.controller?.zoomIn()
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.White),
                        shape = CircleShape,
                        modifier = Modifier
                            .size(38.dp)
                            .shadow(3.dp, CircleShape)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Kattalashtirish", tint = SecondaryNavy)
                    }

                    FilledIconButton(
                        onClick = {
                            mapViewRef?.controller?.zoomOut()
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.White),
                        shape = CircleShape,
                        modifier = Modifier
                            .size(38.dp)
                            .shadow(3.dp, CircleShape)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Kichiklashtirish", tint = SecondaryNavy)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Address Details Form & Confirmation
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Readable Address Preview Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = BurgundyLight),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Place,
                            contentDescription = null,
                            tint = BurgundyRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Aniqlangan manzil:",
                                fontSize = 10.sp,
                                color = BurgundyRed,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = readableAddress,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SecondaryNavy,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Street & House inputs in a row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = streetInput,
                        onValueChange = { streetInput = it },
                        label = { Text("Ko‘cha / Mahalla") },
                        placeholder = { Text("Mustaqillik ko‘chasi") },
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("map_street_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BurgundyRed,
                            focusedLabelColor = BurgundyRed
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = houseInput,
                        onValueChange = { houseInput = it },
                        label = { Text("Uy / Xonadon") },
                        placeholder = { Text("12-uy") },
                        modifier = Modifier
                            .weight(0.7f)
                            .testTag("map_house_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BurgundyRed,
                            focusedLabelColor = BurgundyRed
                        ),
                        singleLine = true
                    )
                }

                // Landmark input
                OutlinedTextField(
                    value = landmarkInput,
                    onValueChange = { landmarkInput = it },
                    label = { Text("Mo‘ljal (kuryer uchun)") },
                    placeholder = { Text("Masalan: 3-maktab yonida, qizil darvoza") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("map_landmark_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BurgundyRed,
                        focusedLabelColor = BurgundyRed
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Confirm Button
                Button(
                    onClick = {
                        val finalLocation = DeliveryLocationData(
                            latitude = currentLat,
                            longitude = currentLng,
                            readableAddress = readableAddress.ifBlank { "Gagarin shahri, Mirzacho‘l tumani" },
                            street = streetInput.trim(),
                            houseNumber = houseInput.trim(),
                            landmark = landmarkInput.trim()
                        )
                        // Persist immediately in local storage so it survives app restarts
                        LocationStorageHelper.saveLocation(context, finalLocation)
                        onLocationConfirmed(finalLocation)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BurgundyRed),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("confirm_delivery_location_btn")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Manzilni tasdiqlash",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun fetchDeviceLocation(context: Context, onLocation: (Double, Double) -> Unit) {
    try {
        val fusedClient = LocationServices.getFusedLocationProviderClient(context)
        fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
            .addOnSuccessListener { loc: Location? ->
                if (loc != null) {
                    onLocation(loc.latitude, loc.longitude)
                } else {
                    fusedClient.lastLocation.addOnSuccessListener { lastLoc: Location? ->
                        if (lastLoc != null) {
                            onLocation(lastLoc.latitude, lastLoc.longitude)
                        }
                    }
                }
            }
    } catch (_: Exception) {}
}

private fun getGagarinSmartAddress(lat: Double, lng: Double): String {
    // Find closest preset area or district boundary
    var closestPreset = GAGARIN_PRESET_AREAS[0]
    var minDistance = Double.MAX_VALUE

    for (preset in GAGARIN_PRESET_AREAS) {
        val dist = Math.hypot(lat - preset.lat, lng - preset.lng)
        if (dist < minDistance) {
            minDistance = dist
            closestPreset = preset
        }
    }

    return if (minDistance < 0.008) {
        "Gagarin shahri, ${closestPreset.description}"
    } else {
        "Gagarin shahri, Mirzacho‘l tumani"
    }
}
