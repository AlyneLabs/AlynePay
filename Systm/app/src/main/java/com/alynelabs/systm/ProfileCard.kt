package com.alynelabs.systm

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alynelabs.systm.ui.theme.SystmTheme

@Composable
fun ProfileCard(
    modifier: Modifier = Modifier,
    sizeMultiplier: Float = 1f
) {
    val MontserratLight = FontFamily(
        Font(R.font.montserrat_light, FontWeight.Light)
    )

    Box(
        modifier = modifier
            .width(380.dp * sizeMultiplier)
            .height(240.dp * sizeMultiplier)
            .glassmorphism(
                borderRadius = 25.dp * sizeMultiplier,
                backgroundColorCenter = Color.White.copy(alpha = 0.06f),
                backgroundColorEdge = Color.White.copy(alpha = 0.03f),
                outlineBrush = Brush.sweepGradient(
                    0.02f to Color.White.copy(alpha = 0.18f),
                    0.05f to Color.White.copy(alpha = 0.35f),
                    0.10f to Color.White.copy(alpha = 0.18f),
                    0.52f to Color.White.copy(alpha = 0.18f),
                    0.55f to Color.White.copy(alpha = 0.35f),
                    0.60f to Color.White.copy(alpha = 0.18f),
                    1.00f to Color.White.copy(alpha = 0.18f)
                ),
                strokeWidth = 1.2.dp * sizeMultiplier,
                shadows = listOf(
                    ShadowParams(
                        color = Color.Black.copy(alpha = 0.15f),
                        blur = 6.dp * sizeMultiplier,
                        offsetX = (-1).dp * sizeMultiplier,
                        offsetY = (-1).dp * sizeMultiplier,
                        spread = (-4).dp * sizeMultiplier,
                        isInner = false
                    )
                )
            )
            .padding(12.dp * sizeMultiplier)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.weight(1f)) {
                // Left Content: Username, Icons, Address
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Top
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Liquid Ammo",
                            color = Color.White,
                            fontSize = (20 * sizeMultiplier).sp,
                            fontWeight = FontWeight.Light,
                            fontFamily = MontserratLight
                        )
                        Spacer(modifier = Modifier.width(8.dp * sizeMultiplier))
                        // Share icon (Arrow)
                        Canvas(modifier = Modifier.size(14.dp * sizeMultiplier)) {
                            val stroke = (1.5.dp * sizeMultiplier).toPx()
                            val path = androidx.compose.ui.graphics.Path().apply {
                                moveTo(size.width * 0.2f, size.height * 0.8f)
                                lineTo(size.width * 0.8f, size.height * 0.2f)
                                moveTo(size.width * 0.4f, size.height * 0.2f)
                                lineTo(size.width * 0.8f, size.height * 0.2f)
                                lineTo(size.width * 0.8f, size.height * 0.6f)
                            }
                            drawPath(path, Color.White, style = Stroke(width = stroke))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp * sizeMultiplier))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp * sizeMultiplier)) {
                        // Voice icon (Arcs)
                        Canvas(modifier = Modifier.size(16.dp * sizeMultiplier)) {
                            val stroke = (1.5.dp * sizeMultiplier).toPx()
                            drawArc(
                                color = Color.White,
                                startAngle = 135f,
                                sweepAngle = 90f,
                                useCenter = false,
                                topLeft = androidx.compose.ui.geometry.Offset(0f, 0f),
                                size = androidx.compose.ui.geometry.Size(size.width, size.height),
                                style = Stroke(width = stroke)
                            )
                            drawArc(
                                color = Color.White,
                                startAngle = 135f,
                                sweepAngle = 90f,
                                useCenter = false,
                                topLeft = androidx.compose.ui.geometry.Offset(size.width * 0.25f, size.height * 0.25f),
                                size = androidx.compose.ui.geometry.Size(size.width * 0.5f, size.height * 0.5f),
                                style = Stroke(width = stroke)
                            )
                        }
                        // Message icon (Square)
                        Canvas(modifier = Modifier.size(16.dp * sizeMultiplier)) {
                            val stroke = (1.5.dp * sizeMultiplier).toPx()
                            drawRect(
                                color = Color.White,
                                topLeft = androidx.compose.ui.geometry.Offset(0f, 0f),
                                size = androidx.compose.ui.geometry.Size(size.width, size.height * 0.8f),
                                style = Stroke(width = stroke)
                            )
                            val path = androidx.compose.ui.graphics.Path().apply {
                                moveTo(size.width * 0.2f, size.height * 0.8f)
                                lineTo(size.width * 0.2f, size.height)
                                lineTo(size.width * 0.5f, size.height * 0.8f)
                            }
                            drawPath(path, Color.White, style = Stroke(width = stroke))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp * sizeMultiplier))

                    Text(
                        text = "0x046B0D3A67A819BB5FFCB9E62AF1F86C2A9FBBE9A71F82064AF8036AC74F1640A04CA5895B06316CF63FF9123DC785F76D50299FE9E3B4BA7C2C34C6E91909F6EC",
                        color = Color.White,
                        fontSize = (7.5 * sizeMultiplier).sp,
                        fontWeight = FontWeight.Light,
                        fontFamily = MontserratLight,
                        lineHeight = (10 * sizeMultiplier).sp,
                        textAlign = TextAlign.Start,
                        maxLines = 5
                    )
                }

                Spacer(modifier = Modifier.width(12.dp * sizeMultiplier))

                // Right Content: QR Code
                Box(
                    modifier = Modifier
                        .size(125.dp * sizeMultiplier)
                        .glassmorphism(
                            borderRadius = 10.dp * sizeMultiplier,
                            backgroundColorCenter = Color.White.copy(alpha = 0.06f),
                            backgroundColorEdge = Color.White.copy(alpha = 0.03f),
                            outlineBrush = Brush.sweepGradient(
                                0.02f to Color.White.copy(alpha = 0.18f),
                                0.05f to Color.White.copy(alpha = 0.35f),
                                0.10f to Color.White.copy(alpha = 0.18f),
                                0.52f to Color.White.copy(alpha = 0.18f),
                                0.55f to Color.White.copy(alpha = 0.35f),
                                0.60f to Color.White.copy(alpha = 0.18f),
                                1.00f to Color.White.copy(alpha = 0.18f)
                            ),
                            strokeWidth = 1.2.dp * sizeMultiplier
                        )
                        .padding(12.dp * sizeMultiplier),
                    contentAlignment = Alignment.Center
                ) {
                    // QR Code Image Placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White.copy(alpha = 0.1f))
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp * sizeMultiplier))

            // Bottom part: Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp * sizeMultiplier)
            ) {
                ProfileActionButton(
                    text = "Delete Acc",
                    glowColor = Color(0xFFDD0000),
                    backgroundColor = Color(0xFFFF0000).copy(alpha = 0.72f),
                    modifier = Modifier.weight(1f),
                    sizeMultiplier = sizeMultiplier
                )
                ProfileActionButton(
                    text = "Reveal Seed",
                    glowColor = Color(0xFF4285F4),
                    modifier = Modifier.weight(1f),
                    sizeMultiplier = sizeMultiplier
                )
                ProfileActionButton(
                    text = "Share Duration",
                    glowColor = Color(0xFF9747FF),
                    modifier = Modifier.weight(1.3f),
                    sizeMultiplier = sizeMultiplier
                )
            }
        }
    }
}

@Composable
fun ProfileActionButton(
    text: String,
    glowColor: Color,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White.copy(alpha = 0.06f),
    sizeMultiplier: Float = 1f
) {
    val MontserratLight = FontFamily(
        Font(R.font.montserrat_light, FontWeight.Light)
    )

    Box(
        modifier = modifier
            .height(30.dp * sizeMultiplier)
            .glassmorphism(
                borderRadius = 10.dp * sizeMultiplier,
                backgroundColorCenter = backgroundColor,
                backgroundColorEdge = backgroundColor.copy(alpha = 0.5f),
                outlineBrush = Brush.sweepGradient(
                    0.02f to Color.White.copy(alpha = 0.18f),
                    0.05f to Color.White.copy(alpha = 0.35f),
                    0.10f to Color.White.copy(alpha = 0.18f),
                    0.52f to Color.White.copy(alpha = 0.18f),
                    0.55f to Color.White.copy(alpha = 0.35f),
                    0.60f to Color.White.copy(alpha = 0.18f),
                    1.00f to Color.White.copy(alpha = 0.18f)
                ),
                strokeWidth = 1.2.dp * sizeMultiplier,
                shadows = listOf(
                    ShadowParams(
                        color = glowColor.copy(alpha = 0.5f),
                        blur = 40.dp * sizeMultiplier,
                        spread = 2.dp * sizeMultiplier,
                        isInner = false
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = (11 * sizeMultiplier).sp,
            fontWeight = FontWeight.Light,
            fontFamily = MontserratLight,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileCardPreview() {
    SystmTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            ProfileCard(sizeMultiplier = 1.5f)
        }
    }
}
