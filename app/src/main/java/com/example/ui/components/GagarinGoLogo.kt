package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.DarkBurgundy
import com.example.ui.theme.PrimaryBurgundy
import com.example.ui.theme.SecondaryNavy

/**
 * Gagarin Go Official Brand Logo
 * Recreates the dynamic Burgundy 'G' Arrow emblem with clean vector precision.
 */
@Composable
fun GagarinGoLogo(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    tint: Color = PrimaryBurgundy,
    backgroundColor: Color? = null
) {
    Box(
        modifier = modifier
            .size(size)
            .then(
                if (backgroundColor != null) {
                    Modifier
                        .clip(CircleShape)
                        .background(backgroundColor)
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(size * 0.05f)) {
            val w = this.size.width
            val h = this.size.height
            val cx = w * 0.5f
            val cy = h * 0.48f
            val rOuter = w * 0.45f
            val rInner = w * 0.26f

            // 1. Upper & Left Main 'G' Arc
            val upperArcPath = Path().apply {
                // Outer arc starting from ~2 o'clock (-45 deg) counter-clockwise to ~7 o'clock (135 deg)
                arcTo(
                    rect = Rect(cx - rOuter, cy - rOuter, cx + rOuter, cy + rOuter),
                    startAngleDegrees = -40f,
                    sweepAngleDegrees = -180f,
                    forceMoveTo = true
                )
                // Wing/beak taper at lower-left
                lineTo(cx - rOuter * 1.08f, cy + rOuter * 0.68f)
                lineTo(cx - rInner * 0.92f, cy + rInner * 0.38f)
                // Inner arc back to top-right
                arcTo(
                    rect = Rect(cx - rInner, cy - rInner, cx + rInner, cy + rInner),
                    startAngleDegrees = 135f,
                    sweepAngleDegrees = 175f,
                    forceMoveTo = false
                )
                close()
            }
            drawPath(path = upperArcPath, color = tint, style = Fill)

            // 2. Bottom Arc & Dynamic Forward Arrow (Go Action)
            val arrowPath = Path().apply {
                // Bottom arc from lower-left curving up to right
                arcTo(
                    rect = Rect(cx - rOuter, cy - rOuter, cx + rOuter, cy + rOuter),
                    startAngleDegrees = 145f,
                    sweepAngleDegrees = -95f,
                    forceMoveTo = true
                )
                // Arrow tip pointing forward / right into the opening
                lineTo(w * 0.98f, h * 0.44f)
                // Top inner edge of arrow leading to inner bar
                lineTo(cx + rInner * 0.12f, cy + rInner * 0.05f)
                // Inner curve returning to bottom
                arcTo(
                    rect = Rect(cx - rInner, cy - rInner, cx + rInner, cy + rInner),
                    startAngleDegrees = 20f,
                    sweepAngleDegrees = 40f,
                    forceMoveTo = false
                )
                // Arrow notch / barb
                lineTo(cx + rOuter * 0.48f, cy + rOuter * 0.80f)
                close()
            }
            drawPath(path = arrowPath, color = tint, style = Fill)
        }
    }
}

/**
 * Horizontal Brand Bar combining the Burgundy Logo with "GAGARIN GO" typography
 */
@Composable
fun GagarinGoLogoCompact(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    tint: Color = PrimaryBurgundy
) {
    GagarinGoLogo(
        modifier = modifier,
        size = size,
        tint = tint
    )
}

@Composable
fun GagarinGoBrandHeader(
    modifier: Modifier = Modifier,
    logoSize: Dp = 32.dp,
    textColor: Color = Color(0xFF6B7280),
    accentColor: Color = PrimaryBurgundy
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        GagarinGoLogo(
            size = logoSize,
            tint = accentColor
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "GAGARIN",
                fontSize = (logoSize.value * 0.52f).sp,
                fontWeight = FontWeight.Black,
                color = textColor,
                letterSpacing = 1.sp
            )
            Text(
                text = "GO",
                fontSize = (logoSize.value * 0.56f).sp,
                fontWeight = FontWeight.Black,
                color = accentColor,
                letterSpacing = 1.sp
            )
        }
    }
}

/**
 * Vertical Full Brand Lockup matching the user's official logo layout:
 * - Circular G Arrow Emblem
 * - GAGARIN (Gray)
 * - GO (Burgundy)
 */
@Composable
fun GagarinGoFullLogo(
    modifier: Modifier = Modifier,
    logoSize: Dp = 88.dp,
    textColor: Color = Color(0xFF6B7280),
    accentColor: Color = PrimaryBurgundy
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        GagarinGoLogo(
            size = logoSize,
            tint = accentColor
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "GAGARIN",
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = textColor,
            letterSpacing = 3.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "GO",
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = accentColor,
            letterSpacing = 4.sp
        )
    }
}

