package com.example.ui.screens.customer

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.util.InAppNotification
import com.example.data.util.NotificationHelper
import com.example.ui.theme.BorderColor
import com.example.ui.theme.CardSurface
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DarkText
import com.example.ui.theme.LightBackground
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueLight
import com.example.ui.theme.PrimaryBurgundy
import com.example.ui.theme.SecondaryNavy
import com.example.ui.theme.SecondaryText
import com.example.ui.theme.SlateGray
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.PrimaryBlueLight
import com.example.ui.theme.SecondaryNavy
import com.example.ui.theme.SlateGray
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodels.AuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerAuthScreen(
    authViewModel: AuthViewModel,
    onAuthSuccess: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(1) } // Default 1: Ro'yxatdan o'tish
    val authError by authViewModel.authError.collectAsState()
    val authSuccess by authViewModel.authSuccessMessage.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // In-App SMS Notification State
    var activeNotification by remember { mutableStateOf<InAppNotification?>(null) }
    var autoFillCode by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        NotificationHelper.inAppNotifications.collectLatest { notif ->
            activeNotification = notif
            delay(8000) // Auto-hide notification after 8 seconds
            if (activeNotification?.id == notif.id) {
                activeNotification = null
            }
        }
    }

    LaunchedEffect(authError, authSuccess) {
        authError?.let {
            snackbarHostState.showSnackbar(it)
            authViewModel.clearMessages()
        }
        authSuccess?.let {
            snackbarHostState.showSnackbar(it)
            authViewModel.clearMessages()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = if (selectedTab == 0) "Mijoz sifatida kirish" else "Ro‘yxatdan o‘tish (Telefon orqali)",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = SecondaryNavy
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Orqaga",
                                tint = SecondaryNavy
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = CardSurface)
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LightBackground)
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = CardSurface,
                    contentColor = PrimaryBurgundy
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                text = "Kirish",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == 0) PrimaryBurgundy else SecondaryText,
                                fontSize = 14.sp
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                text = "Ro‘yxatdan o‘tish",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == 1) PrimaryBurgundy else SecondaryText,
                                fontSize = 14.sp
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedTab == 0) {
                    CustomerLoginContent(
                        isLoading = isLoading,
                        receivedCode = autoFillCode,
                        onRequestOtp = { phone, onSent ->
                            authViewModel.sendLoginVerificationCode(phone) { success, _ ->
                                onSent(success)
                            }
                        },
                        onLoginWithOtp = { phone, code ->
                            authViewModel.loginWithPhoneCode(phone, code) {
                                onAuthSuccess()
                            }
                        },
                        onLoginWithPassword = { phone, pass ->
                            authViewModel.loginCustomer(phone, pass) {
                                onAuthSuccess()
                            }
                        },
                        onSwitchToRegister = { selectedTab = 1 }
                    )
                } else {
                    CustomerPhoneRegisterContent(
                        isLoading = isLoading,
                        receivedCode = autoFillCode,
                        onRequestOtp = { phone, onSent ->
                            authViewModel.sendRegistrationVerificationCode(phone) { success, _ ->
                                onSent(success)
                            }
                        },
                        onRegisterWithOtp = { phone, code, fullName, password ->
                            authViewModel.registerWithPhoneCode(phone, code, fullName, password) {
                                onAuthSuccess()
                            }
                        },
                        onSwitchToLogin = { selectedTab = 0 }
                    )
                }
            }
        }

        // Live In-App Notification Dropdown Popup
        AnimatedVisibility(
            visible = activeNotification != null,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 12.dp, start = 12.dp, end = 12.dp)
        ) {
            activeNotification?.let { notif ->
                InAppSmsNotificationCard(
                    notification = notif,
                    onDismiss = { activeNotification = null },
                    onAutoFill = { code ->
                        autoFillCode = code
                        activeNotification = null
                    }
                )
            }
        }
    }
}

@Composable
fun InAppSmsNotificationCard(
    notification: InAppNotification,
    onDismiss: () -> Unit,
    onAutoFill: (String) -> Unit
) {
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(PrimaryBlue, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Sms,
                        contentDescription = "SMS",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = notification.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Hozirgina qabul qilindi",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Text("✕", color = Color(0xFF94A3B8), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = notification.message,
                color = Color(0xFFF1F5F9),
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            if (!notification.code.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color(0xFF334155),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "KOD: ${notification.code}",
                                color = Color(0xFF38BDF8),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                letterSpacing = 2.sp
                            )
                        }
                    }

                    Button(
                        onClick = { onAutoFill(notification.code) },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Kiritish", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                            val clip = ClipData.newPlainText("SMS Code", notification.code)
                            clipboard?.setPrimaryClip(clip)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF64748B)),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color(0xFFCBD5E1)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerPhoneRegisterContent(
    isLoading: Boolean,
    receivedCode: String?,
    onRequestOtp: (phone: String, onSent: (Boolean) -> Unit) -> Unit,
    onRegisterWithOtp: (phone: String, code: String, fullName: String, password: String) -> Unit,
    onSwitchToLogin: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("+998 ") }
    var verificationCode by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isCodeSent by remember { mutableStateOf(false) }
    var resendTimer by remember { mutableIntStateOf(0) }
    var localError by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(receivedCode) {
        if (!receivedCode.isNullOrBlank()) {
            verificationCode = receivedCode
            isCodeSent = true
        }
    }

    LaunchedEffect(resendTimer) {
        if (resendTimer > 0) {
            delay(1000)
            resendTimer -= 1
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(PrimaryBlueLight, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Phone,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Telefon orqali ro‘yxatdan o‘tish",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        ),
                        color = SecondaryNavy
                    )
                    Text(
                        text = "Elektron pochta shart emas! Kod SMS orqali yuboriladi.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SlateGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Full Name
            OutlinedTextField(
                value = fullName,
                onValueChange = {
                    fullName = it
                    localError = null
                },
                label = { Text("Ism-familiyangiz", color = SecondaryText) },
                placeholder = { Text("Ali Valiyev", color = SecondaryText) },
                leadingIcon = {
                    Icon(imageVector = Icons.Filled.Person, contentDescription = null, tint = PrimaryBurgundy)
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = DarkText,
                    unfocusedTextColor = DarkText,
                    focusedLabelColor = PrimaryBurgundy,
                    unfocusedLabelColor = SecondaryText,
                    focusedLeadingIconColor = PrimaryBurgundy,
                    unfocusedLeadingIconColor = SecondaryText,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = PrimaryBurgundy,
                    unfocusedBorderColor = BorderColor
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reg_name_input")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Phone
            OutlinedTextField(
                value = phone,
                onValueChange = {
                    phone = it
                    localError = null
                },
                label = { Text("Telefon raqamingiz *", color = SecondaryText) },
                placeholder = { Text("+998 90 123 45 67", color = SecondaryText) },
                leadingIcon = {
                    Icon(imageVector = Icons.Filled.Phone, contentDescription = null, tint = PrimaryBurgundy)
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = DarkText,
                    unfocusedTextColor = DarkText,
                    focusedLabelColor = PrimaryBurgundy,
                    unfocusedLabelColor = SecondaryText,
                    focusedLeadingIconColor = PrimaryBurgundy,
                    unfocusedLeadingIconColor = SecondaryText,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = PrimaryBurgundy,
                    unfocusedBorderColor = BorderColor
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reg_phone_input")
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (!isCodeSent) {
                // Button to request SMS OTP
                Button(
                    onClick = {
                        val digits = phone.replace(Regex("[^0-9]"), "")
                        if (digits.length < 9) {
                            localError = "Iltimos, to‘liq telefon raqamingizni kiriting"
                        } else {
                            localError = null
                            onRequestOtp(phone) { success ->
                                if (success) {
                                    isCodeSent = true
                                    resendTimer = 60
                                }
                            }
                        }
                    },
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBurgundy,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("request_sms_code_button")
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(imageVector = Icons.Filled.Sms, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SMS Tasdiqlash kodini olish",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                // SMS Code Input Box
                Surface(
                    color = Color(0xFFF8FAFC),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.NotificationsActive,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Tasdiqlash kodi bildirishnoma orqali yuborildi",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = SuccessGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = verificationCode,
                            onValueChange = {
                                if (it.length <= 6) {
                                    verificationCode = it
                                    localError = null
                                }
                            },
                            label = { Text("6 xonali SMS kod *", color = SecondaryText) },
                            placeholder = { Text("123456", color = SecondaryText) },
                            leadingIcon = {
                                Icon(imageVector = Icons.Filled.Security, contentDescription = null, tint = PrimaryBurgundy)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = DarkText,
                                unfocusedTextColor = DarkText,
                                focusedLabelColor = PrimaryBurgundy,
                                unfocusedLabelColor = SecondaryText,
                                focusedLeadingIconColor = PrimaryBurgundy,
                                unfocusedLeadingIconColor = SecondaryText,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = PrimaryBurgundy,
                                unfocusedBorderColor = BorderColor
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reg_otp_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (resendTimer > 0) {
                                Text(
                                    text = "Qayta yuborish: $resendTimer soniya",
                                    fontSize = 12.sp,
                                    color = SlateGray
                                )
                            } else {
                                Text(
                                    text = "Kodni qayta yuborish",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBurgundy,
                                    modifier = Modifier.clickable {
                                        onRequestOtp(phone) { success ->
                                            if (success) resendTimer = 60
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Optional Password
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        localError = null
                    },
                    label = { Text("Parol (ixtiyoriy)", color = SecondaryText) },
                    placeholder = { Text("Keyingi kirishlar uchun", color = SecondaryText) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Filled.Lock, contentDescription = null, tint = PrimaryBurgundy)
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = null,
                                tint = SlateGray
                            )
                        }
                    },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = DarkText,
                        unfocusedTextColor = DarkText,
                        focusedLabelColor = PrimaryBurgundy,
                        unfocusedLabelColor = SecondaryText,
                        focusedLeadingIconColor = PrimaryBurgundy,
                        unfocusedLeadingIconColor = SecondaryText,
                        focusedTrailingIconColor = PrimaryBurgundy,
                        unfocusedTrailingIconColor = SecondaryText,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = PrimaryBurgundy,
                        unfocusedBorderColor = BorderColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reg_password_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Final Submit Button
                Button(
                    onClick = {
                        if (verificationCode.trim().length < 4) {
                            localError = "Iltimos, tasdiqlash kodini to‘liq kiriting"
                        } else {
                            onRegisterWithOtp(phone.trim(), verificationCode.trim(), fullName.trim(), password)
                        }
                    },
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBurgundy,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("reg_submit_button")
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text(
                            text = "Tasdiqlash va Ro‘yxatdan o‘tish",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            if (localError != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = localError!!, color = DangerRed, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Allaqachon hisobingiz bormi?", fontSize = 13.sp, color = SlateGray)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Kirish",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue,
                    modifier = Modifier
                        .clickable { onSwitchToLogin() }
                        .testTag("switch_to_login_btn")
                )
            }
        }
    }
}

@Composable
fun CustomerLoginContent(
    isLoading: Boolean,
    receivedCode: String?,
    onRequestOtp: (phone: String, onSent: (Boolean) -> Unit) -> Unit,
    onLoginWithOtp: (phone: String, code: String) -> Unit,
    onLoginWithPassword: (phone: String, pass: String) -> Unit,
    onSwitchToRegister: () -> Unit
) {
    var phone by remember { mutableStateOf("+998 ") }
    var password by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isCodeSent by remember { mutableStateOf(false) }
    var usePasswordLogin by remember { mutableStateOf(false) } // Default: Fast SMS OTP Login
    var resendTimer by remember { mutableIntStateOf(0) }
    var localError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(receivedCode) {
        if (!receivedCode.isNullOrBlank()) {
            verificationCode = receivedCode
            isCodeSent = true
        }
    }

    LaunchedEffect(resendTimer) {
        if (resendTimer > 0) {
            delay(1000)
            resendTimer -= 1
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header with Official Burgundy G Emblem
            com.example.ui.components.GagarinGoLogo(
                size = 46.dp,
                tint = PrimaryBlue
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Gagarin Go tizimiga kirish",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                color = SecondaryNavy
            )
            Text(
                text = if (usePasswordLogin) "Telefon raqamingiz va parolingizni kiriting" else "Telefon raqamingizga SMS kod yuboriladi",
                style = MaterialTheme.typography.bodySmall,
                color = SlateGray
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Phone Field
            OutlinedTextField(
                value = phone,
                onValueChange = {
                    phone = it
                    localError = null
                },
                label = { Text("Telefon raqami", color = SecondaryText) },
                placeholder = { Text("+998 90 123 45 67", color = SecondaryText) },
                leadingIcon = {
                    Icon(imageVector = Icons.Filled.Phone, contentDescription = null, tint = PrimaryBurgundy)
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = DarkText,
                    unfocusedTextColor = DarkText,
                    focusedLabelColor = PrimaryBurgundy,
                    unfocusedLabelColor = SecondaryText,
                    focusedLeadingIconColor = PrimaryBurgundy,
                    unfocusedLeadingIconColor = SecondaryText,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = PrimaryBurgundy,
                    unfocusedBorderColor = BorderColor
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_phone_input")
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (usePasswordLogin) {
                // Password Login Field
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        localError = null
                    },
                    label = { Text("Parol", color = SecondaryText) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Filled.Lock, contentDescription = null, tint = PrimaryBurgundy)
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = null,
                                tint = SlateGray
                            )
                        }
                    },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = DarkText,
                        unfocusedTextColor = DarkText,
                        focusedLabelColor = PrimaryBurgundy,
                        unfocusedLabelColor = SecondaryText,
                        focusedLeadingIconColor = PrimaryBurgundy,
                        unfocusedLeadingIconColor = SecondaryText,
                        focusedTrailingIconColor = PrimaryBurgundy,
                        unfocusedTrailingIconColor = SecondaryText,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = PrimaryBurgundy,
                        unfocusedBorderColor = BorderColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_password_input")
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (phone.replace(Regex("[^0-9]"), "").length < 9) {
                            localError = "Iltimos, telefon raqamingizni kiriting"
                        } else if (password.trim().isEmpty()) {
                            localError = "Iltimos, parolingizni kiriting"
                        } else {
                            onLoginWithPassword(phone.trim(), password)
                        }
                    },
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBurgundy,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("login_submit_button")
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text(
                            text = "Parol orqali kirish",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            } else {
                // SMS OTP Flow
                if (!isCodeSent) {
                    Button(
                        onClick = {
                            val digits = phone.replace(Regex("[^0-9]"), "")
                            if (digits.length < 9) {
                                localError = "Iltimos, telefon raqamingizni kiriting"
                            } else {
                                localError = null
                                onRequestOtp(phone) { success ->
                                    if (success) {
                                        isCodeSent = true
                                        resendTimer = 60
                                    }
                                }
                            }
                        },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryBurgundy,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("login_get_sms_code_button")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(imageVector = Icons.Filled.Sms, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SMS Tasdiqlash kodini olish",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = verificationCode,
                        onValueChange = {
                            if (it.length <= 6) {
                                verificationCode = it
                                localError = null
                            }
                        },
                        label = { Text("6 xonali SMS kod", color = SecondaryText) },
                        placeholder = { Text("123456", color = SecondaryText) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Filled.Security, contentDescription = null, tint = PrimaryBurgundy)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = DarkText,
                            unfocusedTextColor = DarkText,
                            focusedLabelColor = PrimaryBurgundy,
                            unfocusedLabelColor = SecondaryText,
                            focusedLeadingIconColor = PrimaryBurgundy,
                            unfocusedLeadingIconColor = SecondaryText,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = PrimaryBurgundy,
                            unfocusedBorderColor = BorderColor
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_otp_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (resendTimer > 0) {
                            Text(text = "Qayta yuborish: $resendTimer soniya", fontSize = 12.sp, color = SlateGray)
                        } else {
                            Text(
                                text = "Kodni qayta yuborish",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBurgundy,
                                modifier = Modifier.clickable {
                                    onRequestOtp(phone) { success ->
                                        if (success) resendTimer = 60
                                    }
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (verificationCode.trim().length < 4) {
                                localError = "Iltimos, SMS kodni kiriting"
                            } else {
                                onLoginWithOtp(phone.trim(), verificationCode.trim())
                            }
                        },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryBurgundy,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("login_verify_otp_button")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text(
                                text = "Kirishni tasdiqlash",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }

            if (localError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = localError!!, color = DangerRed, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Switch between SMS and Password mode
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (usePasswordLogin) "⚡ SMS kod orqali tezkor kirish" else "🔑 Parol orqali kirish",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryBurgundy,
                    modifier = Modifier
                        .clickable {
                            usePasswordLogin = !usePasswordLogin
                            localError = null
                        }
                        .padding(4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Hisobingiz yo‘qmi?", fontSize = 13.sp, color = SlateGray)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Ro‘yxatdan o‘tish",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBurgundy,
                    modifier = Modifier
                        .clickable { onSwitchToRegister() }
                        .testTag("switch_to_register_btn")
                )
            }
        }
    }
}
