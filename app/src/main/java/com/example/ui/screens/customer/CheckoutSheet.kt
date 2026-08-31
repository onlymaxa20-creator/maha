package com.example.ui.screens.customer

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.OrderEntity
import com.example.data.local.entity.UserEntity
import com.example.data.model.CartItemDetail
import com.example.data.model.DeliveryLocationData
import com.example.data.util.LocationStorageHelper
import com.example.ui.components.Formatters
import com.example.ui.components.map.BurgundyRed
import com.example.ui.components.map.DeliveryMapPickerSheet
import com.example.ui.theme.BorderColor
import com.example.ui.theme.CardSurface
import com.example.ui.theme.DangerRed
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueLight
import com.example.ui.theme.SecondaryNavy
import com.example.ui.theme.SlateGray
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodels.AuthViewModel
import com.example.ui.viewmodels.StoreViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutSheet(
    customer: UserEntity,
    cartItems: List<CartItemDetail>,
    totalSum: Double,
    storeViewModel: StoreViewModel,
    authViewModel: AuthViewModel,
    onDismiss: () -> Unit,
    onOrderSuccess: (OrderEntity) -> Unit,
    onEditPhoneRequested: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isPlacingOrder by storeViewModel.isPlacingOrder.collectAsState()
    val orderError by storeViewModel.orderPlacementError.collectAsState()
    val context = LocalContext.current

    // Initialize with saved location
    val savedLocation = remember { LocationStorageHelper.getSavedLocation(context) }
    var selectedLocation by remember { mutableStateOf(savedLocation) }
    var showMapPicker by remember { mutableStateOf(false) }

    var deliveryAddress by remember {
        mutableStateOf(
            if (customer.savedAddress.isNotBlank()) customer.savedAddress
            else savedLocation.getFullFormattedAddress()
        )
    }
    var deliveryNotes by remember { mutableStateOf(savedLocation.landmark) }
    var addressError by remember { mutableStateOf<String?>(null) }

    if (showMapPicker) {
        DeliveryMapPickerSheet(
            initialLocation = selectedLocation,
            onDismiss = { showMapPicker = false },
            onLocationConfirmed = { newLocation ->
                selectedLocation = newLocation
                deliveryAddress = newLocation.getFullFormattedAddress()
                if (newLocation.landmark.isNotBlank() && deliveryNotes.isBlank()) {
                    deliveryNotes = newLocation.landmark
                }
                addressError = null
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CardSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Text(
                text = "Buyurtmani rasmiylashtirish",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = SecondaryNavy
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Free Delivery Badge in Gagarin
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryBlueLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocalShipping,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Bepul yetkazib berish: 0 so‘m",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = PrimaryBlue
                        )
                        Text(
                            text = "Yetkazib berish xizmati: Gagarin shahri, Mirzacho‘l tumani",
                            fontSize = 11.sp,
                            color = SecondaryNavy
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Customer Phone Check Section
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = PrimaryBlueLight,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.Phone,
                                    contentDescription = null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Bog‘lanish uchun telefon",
                                fontSize = 12.sp,
                                color = SlateGray
                            )
                            Text(
                                text = customer.phone,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = SecondaryNavy
                            )
                        }
                    }

                    IconButton(onClick = onEditPhoneRequested) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "O‘zgartirish",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Delivery Map Picker Trigger Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD54F)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showMapPicker = true }
                    .testTag("open_delivery_map_btn")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Surface(
                            color = BurgundyRed,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Place,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Xaritada yetkazish joyini tanlang",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = SecondaryNavy
                            )
                            Text(
                                text = String.format(
                                    Locale.US,
                                    "Koordinata: %.4f, %.4f",
                                    selectedLocation.latitude,
                                    selectedLocation.longitude
                                ),
                                fontSize = 11.sp,
                                color = SlateGray
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { showMapPicker = true },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Xarita", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Delivery Address Input
            Text(
                text = "Yetkazib berish manzili *",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = SecondaryNavy
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = deliveryAddress,
                onValueChange = {
                    deliveryAddress = it
                    addressError = null
                },
                placeholder = { Text("Gagarin shahri, ko‘cha, uy raqami, xonadon") },
                isError = addressError != null,
                supportingText = {
                    if (addressError != null) {
                        Text(text = addressError!!, color = MaterialTheme.colorScheme.error)
                    } else {
                        Text(text = "Yetkazib berish manzilini to‘liq kiriting", fontSize = 11.sp, color = SlateGray)
                    }
                },
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = BorderColor
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("checkout_address_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Delivery Notes
            Text(
                text = "Kuryer uchun qo‘shimcha izoh / mo‘ljal (ixtiyoriy)",
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = SlateGray
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = deliveryNotes,
                onValueChange = { deliveryNotes = it },
                placeholder = { Text("Masalan: 2-podyezd, mo‘ljal: maktab yonida") },
                shape = RoundedCornerShape(10.dp),
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = BorderColor
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("checkout_notes_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Items Summary
            Text(
                text = "Buyurtma tarkibi (${cartItems.size} ta mahsulot):",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = SecondaryNavy
            )
            Spacer(modifier = Modifier.height(6.dp))
            cartItems.forEach { detail ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${detail.product.name} × ${detail.cartItem.quantity} ${detail.product.unit}",
                        fontSize = 13.sp,
                        color = SecondaryNavy,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = Formatters.formatPrice(detail.totalPrice),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Total summary
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF1F5F9), RoundedCornerShape(10.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Jami to‘lov:",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = SecondaryNavy
                )
                Text(
                    text = Formatters.formatPrice(totalSum),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryBlue
                )
            }

            if (orderError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = orderError!!,
                    color = DangerRed,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Submit Button
            Button(
                onClick = {
                    if (deliveryAddress.trim().isEmpty() || deliveryAddress.trim().length < 5) {
                        addressError = "Iltimos, to‘liq yetkazib berish manzilini kiriting"
                        return@Button
                    }
                    storeViewModel.placeOrder(
                        customerName = customer.fullName.ifBlank { customer.email },
                        customerPhone = customer.phone,
                        customerEmail = customer.email,
                        deliveryAddress = deliveryAddress.trim(),
                        deliveryNotes = deliveryNotes.trim(),
                        deliveryLatitude = selectedLocation.latitude,
                        deliveryLongitude = selectedLocation.longitude,
                        deliveryStreet = selectedLocation.street,
                        deliveryHouseNumber = selectedLocation.houseNumber,
                        deliveryLandmark = if (deliveryNotes.isNotBlank()) deliveryNotes.trim() else selectedLocation.landmark,
                        onSuccess = { order ->
                            onOrderSuccess(order)
                        }
                    )
                },
                enabled = !isPlacingOrder,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("submit_order_btn")
            ) {
                if (isPlacingOrder) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Text(
                        text = "Buyurtmani tasdiqlash",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

