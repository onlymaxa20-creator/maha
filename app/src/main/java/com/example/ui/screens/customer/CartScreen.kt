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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.ui.components.AppProductImage
import com.example.data.local.entity.OrderEntity
import com.example.data.model.CartItemDetail
import com.example.ui.components.EmptyStateView
import com.example.ui.components.Formatters
import com.example.ui.components.PhonePromptDialog
import com.example.ui.theme.BorderColor
import com.example.ui.theme.CardSurface
import com.example.ui.theme.DangerRed
import com.example.ui.theme.LightBackground
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueLight
import com.example.ui.theme.SecondaryNavy
import com.example.ui.theme.SlateGray
import com.example.ui.theme.SurfaceSubtle
import com.example.ui.viewmodels.AuthViewModel
import com.example.ui.viewmodels.StoreViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    storeViewModel: StoreViewModel,
    authViewModel: AuthViewModel,
    onNavigateToAuth: () -> Unit,
    onOrderPlaced: (OrderEntity) -> Unit,
    onExploreProducts: () -> Unit,
    modifier: Modifier = Modifier
) {
    val session by authViewModel.session.collectAsState()
    val cartItems by storeViewModel.cartItems.collectAsState()
    val totalSum by storeViewModel.cartTotalSum.collectAsState()

    var showCheckoutSheet by remember { mutableStateOf(false) }
    var showPhonePrompt by remember { mutableStateOf(false) }

    if (showPhonePrompt) {
        PhonePromptDialog(
            initialPhone = session.user?.phone ?: "+998 ",
            onDismiss = { showPhonePrompt = false },
            onSavePhone = { newPhone ->
                authViewModel.updatePhone(newPhone) {
                    showPhonePrompt = false
                    showCheckoutSheet = true
                }
            }
        )
    }

    if (showCheckoutSheet && session.user != null) {
        CheckoutSheet(
            customer = session.user!!,
            cartItems = cartItems,
            totalSum = totalSum,
            storeViewModel = storeViewModel,
            authViewModel = authViewModel,
            onDismiss = { showCheckoutSheet = false },
            onOrderSuccess = { order ->
                showCheckoutSheet = false
                onOrderPlaced(order)
            },
            onEditPhoneRequested = {
                showPhonePrompt = true
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Savat",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryNavy
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CardSurface)
            )
        },
        bottomBar = {
            if (cartItems.isNotEmpty()) {
                Surface(
                    color = CardSurface,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Jami hisob:",
                                    fontSize = 13.sp,
                                    color = SlateGray
                                )
                                Text(
                                    text = Formatters.formatPrice(totalSum),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PrimaryBlue
                                )
                            }

                            Button(
                                onClick = {
                                    if (!session.isLoggedIn || session.user == null) {
                                        onNavigateToAuth()
                                    } else {
                                        // Requirement #5 phone check
                                        val phone = session.user?.phone?.trim()
                                        if (phone.isNullOrEmpty() || phone.length < 9) {
                                            showPhonePrompt = true
                                        } else {
                                            showCheckoutSheet = true
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("checkout_button")
                            ) {
                                Text(
                                    text = "Buyurtma berish",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        if (cartItems.isEmpty()) {
            EmptyStateView(
                title = "Savatingiz hozircha bo‘sh.",
                description = "O‘zingizga ma’qul mahsulotlarni tanlab, savatga qo‘shishingiz mumkin.",
                icon = Icons.Outlined.ShoppingCart,
                actionButtonText = "Mahsulotlarni ko‘rish",
                onActionClick = onExploreProducts,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LightBackground)
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Free Delivery Tag
                item {
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
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Gagarin shahri bo‘ylab yetkazib berish BEPUL",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PrimaryBlue
                            )
                        }
                    }
                }

                items(cartItems, key = { it.cartItem.id }) { detail ->
                    CartItemRow(
                        detail = detail,
                        onIncrease = {
                            storeViewModel.updateCartQuantity(detail, detail.cartItem.quantity + 1)
                        },
                        onDecrease = {
                            storeViewModel.updateCartQuantity(detail, detail.cartItem.quantity - 1)
                        },
                        onRemove = {
                            storeViewModel.removeCartItem(detail)
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun CartItemRow(
    detail: CartItemDetail,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = CardSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product Image
            AppProductImage(
                imageUri = detail.product.imageUri,
                contentDescription = detail.product.name,
                modifier = Modifier.size(72.dp),
                shape = RoundedCornerShape(10.dp),
                contentScale = ContentScale.Crop,
                iconSize = 24.dp
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = detail.product.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = SecondaryNavy,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = Formatters.formatPrice(detail.product.price),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue
                )
                Text(
                    text = "Sotuvchi: ${detail.product.sellerName.ifBlank { "Do‘kon" }}",
                    fontSize = 11.sp,
                    color = SlateGray
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Quantity Controls
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onDecrease,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFF1F5F9))
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Remove,
                            contentDescription = "Kamaytirish",
                            tint = SecondaryNavy,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Text(
                        text = "${detail.cartItem.quantity}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryNavy,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    IconButton(
                        onClick = onIncrease,
                        enabled = detail.cartItem.quantity < detail.product.stock,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFF1F5F9))
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Oshirish",
                            tint = SecondaryNavy,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Remove Button & Total
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.DeleteOutline,
                        contentDescription = "O‘chirish",
                        tint = DangerRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = Formatters.formatPrice(detail.totalPrice),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SecondaryNavy
                )
            }
        }
    }
}
