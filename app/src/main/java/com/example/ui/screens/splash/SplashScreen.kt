package com.example.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PrimaryBurgundy
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Premium, smooth mobile app entrance animation for the GagarinGo app.
 *
 * Sequence:
 * 1. Clean pure white canvas.
 * 2. "GAGARIN" text smoothly fades in and slightly floats up to its upper-center position.
 * 3. The large bold "GO" logo smoothly scales up from 80% to 100% in burgundy/dark maroon.
 * 4. The camera smoothly focuses directly on the center of the "O".
 * 5. Fast, elegant zoom directly through the center of the "O" portal into the main app.
 * 6. Bottom signature: "by: mahmud.buriboyev".
 */
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 1. "GAGARIN" upper text animations
    val gagarinAlpha = remember { Animatable(0f) }
    val gagarinOffsetY = remember { Animatable(22f) }

    // 2. "GO" large logo entrance scale & alpha
    val goScale = remember { Animatable(0.80f) }
    val goAlpha = remember { Animatable(0f) }

    // 3. Bottom creator signature animation
    val footerAlpha = remember { Animatable(0f) }

    // 4. Portal zoom through the "O" animation
    val zoomScale = remember { Animatable(1.0f) }
    val portalFadeOut = remember { Animatable(1.0f) }

    // Exact screen pivot coordinates for the "O" center
    var pivotX by remember { mutableFloatStateOf(0.5f) }
    var pivotY by remember { mutableFloatStateOf(0.54f) }

    var rootWindowSize by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        // --- 0ms - 200ms: Clean white initial state ---
        delay(150)

        // --- Step 2: "GAGARIN" fades in and moves upward (200ms - 750ms) ---
        launch {
            gagarinAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing)
            )
        }
        launch {
            gagarinOffsetY.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing)
            )
        }

        delay(320)

        // --- Step 3: Large "GO" logo scales up from 80% to 100% (520ms - 1250ms) ---
        launch {
            goAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
            )
        }
        launch {
            goScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 700,
                    easing = CubicBezierEasing(0.2f, 0.0f, 0.2f, 1.0f)
                )
            )
        }
        launch {
            footerAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
            )
        }

        // --- Step 4: Settle moment & pause at full brand clarity (1250ms - 1750ms) ---
        delay(850)

        // --- Step 5 & 6: Fast, elegant forward zoom directly through the center of "O" (1750ms - 2650ms) ---
        launch {
            // Rapid fade of the outer text so only the "O" portal expansion is visible
            gagarinAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
            )
            footerAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)
            )
        }

        // Zoom scale accelerates smoothly into the center hole of the O
        zoomScale.animateTo(
            targetValue = 65.0f,
            animationSpec = tween(
                durationMillis = 850,
                easing = CubicBezierEasing(0.42f, 0.0f, 0.12f, 1.0f)
            )
        )

        // --- Step 7: Natural handoff to main app screen ---
        delay(60)
        onSplashFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .onGloballyPositioned { rootCoordinates ->
                val size = rootCoordinates.size
                if (size.width > 0 && size.height > 0) {
                    rootWindowSize = size.height.toFloat()
                }
            }
            .testTag("splash_screen_root"),
        contentAlignment = Alignment.Center
    ) {
        // Main Zoomable Viewport pivoted on the center of the 'O'
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = zoomScale.value
                    scaleY = zoomScale.value
                    transformOrigin = TransformOrigin(pivotX, pivotY)
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                // 1. Upper "GAGARIN" text
                Box(
                    modifier = Modifier
                        .alpha(gagarinAlpha.value)
                        .offset { IntOffset(0, gagarinOffsetY.value.roundToInt()) }
                ) {
                    Text(
                        text = "GAGARIN",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBurgundy,
                        letterSpacing = 6.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 2. Large "GO" Logo in center-lower area
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .alpha(goAlpha.value)
                        .graphicsLayer {
                            scaleX = goScale.value
                            scaleY = goScale.value
                        }
                ) {
                    // Letter "G"
                    Text(
                        text = "G",
                        fontSize = 104.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Default,
                        color = PrimaryBurgundy,
                        letterSpacing = (-2).sp,
                        lineHeight = 104.sp
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    // Letter "O" (The Transition Portal)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .onGloballyPositioned { oCoordinates ->
                                val posInWindow = oCoordinates.positionInWindow()
                                val oSize = oCoordinates.size
                                val rootSize = oCoordinates.parentLayoutCoordinates

                                if (rootSize != null && rootSize.size.width > 0 && rootSize.size.height > 0) {
                                    val centerX = posInWindow.x + (oSize.width / 2f)
                                    val centerY = posInWindow.y + (oSize.height / 2f)
                                    val screenW = rootSize.size.width.toFloat()
                                    val screenH = rootSize.size.height.toFloat()

                                    if (screenW > 0 && screenH > 0) {
                                        pivotX = (centerX / screenW).coerceIn(0.1f, 0.9f)
                                        pivotY = (centerY / screenH).coerceIn(0.1f, 0.9f)
                                    }
                                }
                            }
                    ) {
                        // Letter "O" text with exact proportional weight and circle geometry
                        Text(
                            text = "O",
                            fontSize = 104.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Default,
                            color = PrimaryBurgundy,
                            letterSpacing = (-2).sp,
                            lineHeight = 104.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        // 3. Creator Signature at the very bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp)
                .alpha(footerAlpha.value)
        ) {
            Text(
                text = "by: mahmud.buriboyev",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Medium,
                color = PrimaryBurgundy.copy(alpha = 0.72f),
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
