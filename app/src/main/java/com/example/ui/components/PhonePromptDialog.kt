package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueLight
import com.example.ui.theme.SecondaryNavy
import com.example.ui.theme.SlateGray

/**
 * Requirement #5:
 * Prompt shown when customer tries to place an order without a phone number:
 * “Buyurtma berish uchun telefon raqamingizni kiriting.”
 * Button: “Telefon raqamini kiritish”
 */
@Composable
fun PhonePromptDialog(
    initialPhone: String = "+998 ",
    onDismiss: () -> Unit,
    onSavePhone: (String) -> Unit
) {
    var phoneInput by remember { mutableStateOf(if (initialPhone.isBlank()) "+998 " else initialPhone) }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = PrimaryBlueLight,
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Phone,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier
                            .padding(14.dp)
                            .size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Telefon raqami kerak",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = SecondaryNavy,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Buyurtma berish uchun telefon raqamingizni kiriting.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecondaryNavy,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = phoneInput,
                    onValueChange = {
                        phoneInput = it
                        errorText = null
                    },
                    label = { Text("Telefon raqamingiz") },
                    placeholder = { Text("+998 90 123 45 67") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    isError = errorText != null,
                    supportingText = {
                        if (errorText != null) {
                            Text(text = errorText!!, color = MaterialTheme.colorScheme.error)
                        } else {
                            Text(text = "Kuryer siz bilan bog‘lanishi uchun kerak bo‘ladi", fontSize = 11.sp, color = SlateGray)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_phone_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmed = phoneInput.trim()
                    if (trimmed.length < 9) {
                        errorText = "Iltimos, to‘g‘ri telefon raqamini kiriting"
                    } else {
                        onSavePhone(trimmed)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                modifier = Modifier.testTag("save_phone_dialog_btn")
            ) {
                Text("Telefon raqamini kiritish")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Bekor qilish")
            }
        },
        shape = RoundedCornerShape(18.dp)
    )
}
