package com.example.ui.screens.food

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CardSurface
import com.example.ui.theme.LightBackground
import com.example.ui.theme.OrangeAmber
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SecondaryNavy
import com.example.ui.theme.SlateGray
import com.example.ui.viewmodels.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodAdminAuthScreen(
    authViewModel: AuthViewModel,
    onAuthSuccess: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gagarin Food Admin", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Orqaga")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CardSurface,
                    titleContentColor = SecondaryNavy
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LightBackground)
                .padding(innerPadding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color(0xFFFFF3E0), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🛡️", fontSize = 40.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Food Administrator Tizimi",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = SecondaryNavy
            )

            Text(
                text = "Gagarin Food restoran va taomlar boshqaruvi",
                fontSize = 13.sp,
                color = SlateGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = login,
                        onValueChange = { login = it; errorMessage = null },
                        label = { Text("Food Admin Logini") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = OrangeAmber) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("food_admin_login_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OrangeAmber,
                            focusedLabelColor = OrangeAmber
                        )
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; errorMessage = null },
                        label = { Text("Parol") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = OrangeAmber) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("food_admin_password_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OrangeAmber,
                            focusedLabelColor = OrangeAmber
                        )
                    )

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = Color(0xFFD32F2F),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                errorMessage = null
                                val result = authViewModel.loginFoodsAdmin(login, password)
                                isLoading = false
                                result.fold(
                                    onSuccess = { onAuthSuccess() },
                                    onFailure = { errorMessage = it.message ?: "Kirishda xatolik" }
                                )
                            }
                        },
                        enabled = !isLoading && login.isNotBlank() && password.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangeAmber),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("food_admin_submit_button")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                        } else {
                            Text("Food Admin sifatida kirish", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Standart hisob: foodadmin / foodadmin123",
                fontSize = 12.sp,
                color = SlateGray
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodSellerAuthScreen(
    authViewModel: AuthViewModel,
    onAuthSuccess: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Login fields
    var loginInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Oshxona & Kafe Kabineti", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Orqaga")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CardSurface,
                    titleContentColor = SecondaryNavy
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LightBackground)
                .padding(innerPadding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .background(Color(0xFFFFF3E0), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "👨‍🍳", fontSize = 40.sp)
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Gagarin Food Hamkori",
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                color = SecondaryNavy
            )

            Text(
                text = "Oshxona, kafe va restoranlar uchun maxsus boshqaruv kabineti",
                fontSize = 13.sp,
                color = SlateGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "🔐 Tizimga kirish",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryNavy
                    )

                    OutlinedTextField(
                        value = loginInput,
                        onValueChange = { loginInput = it; errorMessage = null },
                        label = { Text("Oshxona Logini") },
                        placeholder = { Text("Admin bergan login") },
                        leadingIcon = { Icon(Icons.Default.Store, contentDescription = null, tint = OrangeAmber) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("food_seller_login_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OrangeAmber,
                            focusedLabelColor = OrangeAmber
                        )
                    )

                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it; errorMessage = null },
                        label = { Text("Parol") },
                        placeholder = { Text("Admin bergan parol") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = OrangeAmber) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("food_seller_password_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OrangeAmber,
                            focusedLabelColor = OrangeAmber
                        )
                    )

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = Color(0xFFD32F2F),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                errorMessage = null
                                val result = authViewModel.loginFoodsSeller(loginInput, passwordInput)
                                isLoading = false
                                result.fold(
                                    onSuccess = { onAuthSuccess() },
                                    onFailure = { errorMessage = it.message ?: "Kirishda xatolik" }
                                )
                            }
                        },
                        enabled = !isLoading && loginInput.isNotBlank() && passwordInput.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangeAmber),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("food_seller_submit_button")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                        } else {
                            Text("Oshxona kabinetiga kirish", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Notice about Admin issuing login/passwords
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("ℹ️", fontSize = 18.sp)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = "Hisobingiz yo‘qmi?",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = SecondaryNavy
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Gagarin Food bo‘limida taom sotish yoki oshxona ochish uchun do‘kon egalari mustaqil ro‘yxatdan o‘tmaydi. Login va parolni Bosh Administrator shaxsan beradi.",
                        fontSize = 12.sp,
                        color = SecondaryNavy,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}
