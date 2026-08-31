package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DangerRedLight
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueLight
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessGreenLight
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmberLight

@Composable
fun OrderStatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (status) {
        "Yangi" -> Pair(PrimaryBlueLight, PrimaryBlue)
        "Tayyorlanmoqda" -> Pair(WarningAmberLight, Color(0xFFB45309))
        "Yetkazilmoqda" -> Pair(Color(0xFFE0E7FF), Color(0xFF4338CA))
        "Yetkazildi" -> Pair(SuccessGreenLight, Color(0xFF065F46))
        "Bekor qilindi" -> Pair(DangerRedLight, DangerRed)
        else -> Pair(Color(0xFFF1F5F9), Color(0xFF475569))
    }

    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = status,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
