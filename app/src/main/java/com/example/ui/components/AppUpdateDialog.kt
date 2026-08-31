package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.BuildConfig
import com.example.data.local.entity.SupportInfoEntity
import com.example.ui.theme.BorderColor
import com.example.ui.theme.CardSurface
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DangerRedLight
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueLight
import com.example.ui.theme.SecondaryNavy
import com.example.ui.theme.SlateGray
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SurfaceSubtle
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmberLight

/**
 * Universal App Update Dialog & Force-Update Controller.
 * Triggers when a newer version of Gagarin Go is available.
 * Supports updating directly via Google Play Store or downloading APK via Telegram.
 */
@Composable
fun AppUpdateDialog(
    supportInfo: SupportInfoEntity,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentVersionCode = BuildConfig.VERSION_CODE
    val currentVersionName = BuildConfig.VERSION_NAME

    val isMandatory = currentVersionCode < supportInfo.minRequiredVersionCode || supportInfo.isForceUpdate

    // Disable system back press if update is strictly mandatory
    BackHandler(enabled = isMandatory) {
        Toast.makeText(
            context,
            "Ilovadan foydalanish uchun uni yangilashingiz shart!",
            Toast.LENGTH_SHORT
        ).show()
    }

    AlertDialog(
        onDismissRequest = {
            if (!isMandatory) {
                onDismiss()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = !isMandatory,
            dismissOnClickOutside = !isMandatory
        ),
        modifier = modifier
            .fillMaxWidth(0.95f)
            .testTag("app_update_dialog"),
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Icon / Banner
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = if (isMandatory) listOf(DangerRed, Color(0xFFE11D48))
                                else listOf(PrimaryBlue, Color(0xFF4F46E5))
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isMandatory) Icons.Filled.Warning else Icons.Filled.SystemUpdate,
                        contentDescription = "Yangilanish",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Title
                Text(
                    text = if (isMandatory) "⚠️ Majburiy Yangilanish!" else supportInfo.updateTitle,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SecondaryNavy,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Version Badge Comparison
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SurfaceSubtle,
                        border = BorderStroke(1.dp, BorderColor)
                    ) {
                        Text(
                            text = "Sizdagi: v$currentVersionName",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SlateGray,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "➔", fontSize = 14.sp, color = PrimaryBlue, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isMandatory) DangerRedLight else PrimaryBlueLight
                    ) {
                        Text(
                            text = "Yangi: v${supportInfo.latestVersionName}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isMandatory) DangerRed else PrimaryBlue,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Update Message
                Text(
                    text = supportInfo.updateMessage,
                    fontSize = 13.sp,
                    color = SecondaryNavy,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                // Mandatory notice badge
                if (isMandatory) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = DangerRedLight,
                        border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Warning, contentDescription = null, tint = DangerRed, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Eski versiyada ishlash to‘xtatildi. Ilovani davom ettirish uchun uni yangilashingiz shart.",
                                fontSize = 11.sp,
                                color = DangerRed,
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }

                // Release Notes (Changelog)
                if (supportInfo.releaseNotes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceSubtle),
                        border = BorderStroke(1.dp, BorderColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.NewReleases, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Yangi versiyadagi o‘zgarishlar:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SecondaryNavy
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = supportInfo.releaseNotes,
                                fontSize = 12.sp,
                                color = SecondaryNavy,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
                HorizontalDivider(color = BorderColor)
                Spacer(modifier = Modifier.height(14.dp))

                // Primary Update Button: Google Play Store
                Button(
                    onClick = {
                        openGooglePlay(context, supportInfo.playStoreUrl)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F9D58)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("update_google_play_btn")
                ) {
                    Icon(Icons.Filled.ShoppingBag, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Google Play orqali yangilash",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Secondary Update Button: Telegram Channel / APK Download
                Button(
                    onClick = {
                        openTelegramApk(context, supportInfo.telegramApkUrl)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF229ED9)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("update_telegram_apk_btn")
                ) {
                    Icon(Icons.Outlined.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Telegramdan APK yuklab olish",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Optional Dismiss Button (Only if update is NOT mandatory)
                if (!isMandatory) {
                    Spacer(modifier = Modifier.height(6.dp))
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("update_remind_later_btn")
                    ) {
                        Text("Keyinroq eslatish", color = SlateGray, fontSize = 13.sp)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}

/**
 * Opens Google Play Store app page or browser fallback
 */
private fun openGooglePlay(context: Context, playStoreUrl: String) {
    try {
        val packageName = context.packageName
        val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(marketIntent)
    } catch (e: Exception) {
        // Fallback to web link
        try {
            val url = if (playStoreUrl.isNotBlank()) playStoreUrl else "https://play.google.com/store/apps/details?id=${context.packageName}"
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
        } catch (e2: Exception) {
            Toast.makeText(context, "Google Play havolasi ochilmadi", Toast.LENGTH_SHORT).show()
        }
    }
}

/**
 * Opens Telegram channel or direct APK download link
 */
private fun openTelegramApk(context: Context, telegramUrl: String) {
    try {
        val trimmed = telegramUrl.trim()
        val targetUrl = when {
            trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
            trimmed.startsWith("@") -> "https://t.me/${trimmed.removePrefix("@")}"
            trimmed.startsWith("t.me/") -> "https://$trimmed"
            trimmed.isNotBlank() -> "https://t.me/$trimmed"
            else -> "https://t.me/gagarin_go_app"
        }

        val telegramIntent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(telegramIntent)
    } catch (e: Exception) {
        Toast.makeText(context, "Telegram ochilmadi", Toast.LENGTH_SHORT).show()
    }
}
