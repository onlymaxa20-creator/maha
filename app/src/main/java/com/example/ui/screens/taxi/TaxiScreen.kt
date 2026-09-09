package com.example.ui.screens.taxi

import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalTaxi
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderColor
import com.example.ui.theme.CardSurface
import com.example.ui.theme.DarkText
import com.example.ui.theme.LightBackground
import com.example.ui.theme.PrimaryBurgundy
import com.example.ui.theme.SecondaryText
import com.example.data.local.entity.PromoBannerEntity
import com.example.ui.components.PromoBannerSection
import com.example.ui.viewmodels.EcosystemViewModel

@Composable
fun TaxiScreen(
    ecosystemViewModel: EcosystemViewModel,
    userLocation: String,
    onBack: () -> Unit,
    promoBanners: List<PromoBannerEntity> = emptyList(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activeRide by ecosystemViewModel.activeRide.collectAsState()
    val selectedTariff by ecosystemViewModel.selectedTaxiTariff.collectAsState()

    var pickupAddress by remember(userLocation) { mutableStateOf(userLocation) }
    var destinationAddress by remember { mutableStateOf("") }
    var passengerPhone by remember { mutableStateOf("") }

    val tariffs = listOf(
        Triple("Ekonom", "10 000 so'm", "4 daq"),
        Triple("Komfort", "15 000 so'm", "6 daq"),
        Triple("Yuk Taksi", "35 000 so'm", "10 daq"),
        Triple("Shaharlararo", "50 000 so'm", "15 daq")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LightBackground)
    ) {
        // Header
        Surface(
            color = CardSurface,
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Orqaga",
                        tint = DarkText
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "Gagarin Taksi",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkText
                    )
                    Text(
                        text = "Tez va qulay yetkazish • 4+ daqiqa",
                        fontSize = 12.sp,
                        color = SecondaryText
                    )
                }
            }
        }

        val taxiBanners = remember(promoBanners) {
            promoBanners.filter {
                it.actionTag.equals("TAXI", ignoreCase = true) ||
                it.actionTag.equals("ALL", ignoreCase = true)
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Reklama bannerlari (Admin boshqaruvidagi Taksi bo'limi reklamalari)
            if (taxiBanners.isNotEmpty()) {
                item {
                    PromoBannerSection(
                        banners = taxiBanners,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
            }

            // Active Ride Status Card if existing
            if (activeRide != null && activeRide!!.status != "COMPLETED" && activeRide!!.status != "CANCELLED") {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFDF2F4)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECDD3)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(PrimaryBurgundy, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.DirectionsCar,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Haydovchi qidirilmoqda...",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = PrimaryBurgundy
                                    )
                                    Text(
                                        text = "${activeRide!!.tariff} • ${activeRide!!.estimatedPrice} so'm",
                                        fontSize = 12.sp,
                                        color = DarkText
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { ecosystemViewModel.cancelTaxi(activeRide!!.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBurgundy),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Safarni bekor qilish", color = PrimaryBurgundy)
                            }
                        }
                    }
                }
            }

            // Route Input Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Marshrutni belgilang",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkText
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = pickupAddress,
                            onValueChange = { pickupAddress = it },
                            label = { Text("Qayerdan (Sizning manzilingiz)") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.LocationOn,
                                    contentDescription = null,
                                    tint = PrimaryBurgundy
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBurgundy,
                                unfocusedBorderColor = BorderColor
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = destinationAddress,
                            onValueChange = { destinationAddress = it },
                            placeholder = { Text("Qayerga borasiz?") },
                            label = { Text("Boradigan manzil") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Navigation,
                                    contentDescription = null,
                                    tint = Color(0xFF059669)
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBurgundy,
                                unfocusedBorderColor = BorderColor
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = passengerPhone,
                            onValueChange = { passengerPhone = it },
                            label = { Text("Telefon raqamingiz") },
                            placeholder = { Text("+998 90 123 45 67") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBurgundy,
                                unfocusedBorderColor = BorderColor
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Tarifni tanlang",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Tariffs
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tariffs.forEach { (tariffName, price, time) ->
                        val isSelected = selectedTariff == tariffName
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFFFDF2F4) else CardSurface
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (isSelected) PrimaryBurgundy else BorderColor
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { ecosystemViewModel.setTaxiTariff(tariffName) }
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.LocalTaxi,
                                    contentDescription = null,
                                    tint = if (isSelected) PrimaryBurgundy else SecondaryText,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = tariffName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) PrimaryBurgundy else DarkText
                                )
                                Text(
                                    text = price,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = DarkText
                                )
                                Text(
                                    text = time,
                                    fontSize = 10.sp,
                                    color = if (isSelected) PrimaryBurgundy else SecondaryText
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        if (destinationAddress.isBlank()) {
                            Toast.makeText(context, "Iltimos, boradigan manzilni kiriting", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val phone = if (passengerPhone.isNotBlank()) passengerPhone else "+998 90 000 00 00"
                        val price = when (selectedTariff) {
                            "Komfort" -> 15000.0
                            "Yuk Taksi" -> 35000.0
                            "Shaharlararo" -> 50000.0
                            else -> 10000.0
                        }
                        ecosystemViewModel.requestTaxi(
                            userId = 1L,
                            passengerName = "Mijoz",
                            passengerPhone = phone,
                            pickup = pickupAddress,
                            destination = destinationAddress,
                            tariff = selectedTariff,
                            price = price,
                            note = "",
                            onComplete = { _, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBurgundy),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("request_taxi_btn")
                ) {
                    Text(
                        text = "Taksi chaqirish • $selectedTariff",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
