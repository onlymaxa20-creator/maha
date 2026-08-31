package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ReviewEntity
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
import com.example.ui.theme.SecondaryNavy
import com.example.ui.theme.SlateGray
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SurfaceSubtle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val StarGold = Color(0xFFF59E0B) // Uzum / Material gold
val StarEmpty = Color(0xFFCBD5E1)

/**
 * Interactive or Read-only 5-Star Rating component
 */
@Composable
fun StarRatingBar(
    rating: Int,
    modifier: Modifier = Modifier,
    maxRating: Int = 5,
    starSize: Dp = 20.dp,
    starSpacing: Dp = 4.dp,
    isInteractive: Boolean = false,
    onRatingChanged: ((Int) -> Unit)? = null
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(starSpacing)
    ) {
        for (i in 1..maxRating) {
            val isSelected = i <= rating
            val icon = if (isSelected) Icons.Filled.Star else Icons.Outlined.StarBorder
            val tint = if (isSelected) StarGold else StarEmpty

            Box(
                modifier = Modifier
                    .size(starSize)
                    .then(
                        if (isInteractive && onRatingChanged != null) {
                            Modifier
                                .clip(CircleShape)
                                .clickable { onRatingChanged(i) }
                                .padding(1.dp)
                        } else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = "$i yulduz",
                    tint = tint,
                    modifier = Modifier.size(starSize)
                )
            }
        }
    }
}

/**
 * Summary Section of Product Ratings & Review distribution
 */
@Composable
fun ProductRatingSummaryView(
    reviews: List<ReviewEntity>,
    onWriteReviewClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalReviews = reviews.size
    val averageRating = if (totalReviews > 0) {
        reviews.map { it.rating }.average()
    } else 0.0

    // Rating distribution
    val count5 = reviews.count { it.rating == 5 }
    val count4 = reviews.count { it.rating == 4 }
    val count3 = reviews.count { it.rating == 3 }
    val count2 = reviews.count { it.rating == 2 }
    val count1 = reviews.count { it.rating == 1 }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Sharhlar va Baholar",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryNavy
                    )
                    Text(
                        text = if (totalReviews > 0) "$totalReviews ta xaridor baholagan" else "Hozircha sharhlar yo‘q",
                        fontSize = 12.sp,
                        color = SlateGray
                    )
                }

                Button(
                    onClick = onWriteReviewClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    modifier = Modifier.testTag("write_review_btn")
                ) {
                    Icon(
                        imageVector = Icons.Filled.EditNote,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Sharh yozish", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Score and Progress bars Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Large Score
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(end = 20.dp)
                ) {
                    Text(
                        text = if (totalReviews > 0) String.format(Locale.US, "%.1f", averageRating) else "0.0",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SecondaryNavy
                    )
                    StarRatingBar(
                        rating = if (totalReviews > 0) averageRating.toInt().coerceIn(1, 5) else 0,
                        starSize = 16.dp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "5 balldan",
                        fontSize = 11.sp,
                        color = SlateGray
                    )
                }

                // Progress bars for 5, 4, 3, 2, 1 stars
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    RatingProgressRow(stars = 5, count = count5, total = totalReviews)
                    RatingProgressRow(stars = 4, count = count4, total = totalReviews)
                    RatingProgressRow(stars = 3, count = count3, total = totalReviews)
                    RatingProgressRow(stars = 2, count = count2, total = totalReviews)
                    RatingProgressRow(stars = 1, count = count1, total = totalReviews)
                }
            }
        }
    }
}

@Composable
private fun RatingProgressRow(stars: Int, count: Int, total: Int) {
    val progress = if (total > 0) count.toFloat() / total.toFloat() else 0f
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "$stars",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = SlateGray,
            modifier = Modifier.width(10.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = null,
            tint = StarGold,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = StarGold,
            trackColor = SurfaceSubtle
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$count",
            fontSize = 11.sp,
            color = SlateGray,
            modifier = Modifier.width(20.dp)
        )
    }
}

/**
 * Individual Review item card
 */
@Composable
fun ReviewCardItem(
    review: ReviewEntity,
    modifier: Modifier = Modifier
) {
    val dateFormatter = remember { SimpleDateFormat("dd.MM.yyyy, HH:mm", Locale.getDefault()) }
    val formattedDate = remember(review.createdAt) {
        dateFormatter.format(Date(review.createdAt))
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, BorderColor, RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Avatar circle with first letter
                    Surface(
                        shape = CircleShape,
                        color = PrimaryBlueLight,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = review.userName.take(1).uppercase(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = PrimaryBlue
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = review.userName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SecondaryNavy
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Tasdiqlangan xarid",
                                tint = SuccessGreen,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Gagarin Go xaridori",
                                fontSize = 11.sp,
                                color = SuccessGreen,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Text(
                    text = formattedDate,
                    fontSize = 11.sp,
                    color = SlateGray
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Rating Stars
            Row(verticalAlignment = Alignment.CenterVertically) {
                StarRatingBar(
                    rating = review.rating,
                    starSize = 16.dp,
                    starSpacing = 3.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${review.rating} / 5",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SecondaryNavy
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Review comment body
            Text(
                text = review.comment,
                fontSize = 13.sp,
                color = SecondaryNavy,
                lineHeight = 19.sp
            )
        }
    }
}

/**
 * Dialog to add a new review with interactive 5-star rating selector
 */
@Composable
fun AddReviewDialog(
    productName: String,
    initialUserName: String = "",
    onDismiss: () -> Unit,
    onSubmit: (rating: Int, comment: String, reviewerName: String) -> Unit
) {
    var selectedRating by remember { mutableIntStateOf(5) }
    var reviewerName by remember { mutableStateOf(initialUserName) }
    var commentText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val ratingLabels = mapOf(
        1 to "Juda yomon 😞",
        2 to "Qoniqarsiz 😐",
        3 to "O‘rtacha 🙂",
        4 to "Yaxshi 😊",
        5 to "A’lo darajada! ⭐️⭐️⭐️⭐️⭐️"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        titleContentColor = DarkText,
        textContentColor = DarkText,
        title = {
            Column {
                Text(
                    text = "Sharh va Baho qoldirish",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = DarkText
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = productName,
                    fontSize = 13.sp,
                    color = SecondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Interactive 5-Star selector
                Text(
                    text = "Mahsulotni 5 yulduzcha orqali baholang:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkText
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        StarRatingBar(
                            rating = selectedRating,
                            maxRating = 5,
                            starSize = 36.dp,
                            starSpacing = 8.dp,
                            isInteractive = true,
                            onRatingChanged = { selectedRating = it },
                            modifier = Modifier.testTag("dialog_star_rating_bar")
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = ratingLabels[selectedRating] ?: "",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = StarGold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Name input if not provided
                OutlinedTextField(
                    value = reviewerName,
                    onValueChange = { reviewerName = it },
                    label = { Text("Ismingiz", color = SecondaryText) },
                    placeholder = { Text("Masalan: Jasur Rahimov", color = SecondaryText) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = DarkText,
                        unfocusedTextColor = DarkText,
                        focusedLabelColor = PrimaryBurgundy,
                        unfocusedLabelColor = SecondaryText,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = PrimaryBurgundy,
                        unfocusedBorderColor = BorderColor
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Comment input
                OutlinedTextField(
                    value = commentText,
                    onValueChange = {
                        commentText = it
                        errorMessage = null
                    },
                    label = { Text("Fikringiz va sharhingiz *", color = SecondaryText) },
                    placeholder = { Text("Mahsulot sifati, yetkazib berish va taassurotlaringizni yozing...", color = SecondaryText) },
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = DarkText,
                        unfocusedTextColor = DarkText,
                        focusedLabelColor = PrimaryBurgundy,
                        unfocusedLabelColor = SecondaryText,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = PrimaryBurgundy,
                        unfocusedBorderColor = BorderColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_review_comment_input")
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = errorMessage!!,
                        color = DangerRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (commentText.trim().isEmpty()) {
                        errorMessage = "Iltimos, sharh matnini yozing"
                    } else {
                        onSubmit(
                            selectedRating,
                            commentText.trim(),
                            reviewerName.trim().ifBlank { "Xaridor" }
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBurgundy,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("dialog_submit_review_btn")
            ) {
                Text(
                    text = "Yuborish",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = DarkText
                )
            ) {
                Text(
                    text = "Bekor qilish",
                    color = DarkText,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        shape = RoundedCornerShape(18.dp)
    )
}
