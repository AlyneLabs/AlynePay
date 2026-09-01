package com.alynelabs.systm

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alynelabs.systm.ui.theme.SystmTheme

@Composable
fun LogoCard(
    modifier: Modifier = Modifier,
    sizeMultiplier: Float = 1f
) {
    val MontserratLight = FontFamily(
        Font(R.font.montserrat_light, FontWeight.Light)
    )

    Box(
        modifier = modifier
            .width(200.dp * sizeMultiplier)
            .height(65.dp * sizeMultiplier),
        contentAlignment = Alignment.Center
    ) {
        // 1. Glass Container
        Box(
            modifier = Modifier
                .fillMaxSize()
                .glassmorphism(
                    borderRadius = 40.dp * sizeMultiplier,
                    backgroundColorCenter = Color.White.copy(alpha = 0.06f),
                    backgroundColorEdge = Color.White.copy(alpha = 0.03f),
                    outlineBrush = Brush.sweepGradient( // Sharp, ultra-thin reflections on corners
                        0.02f to Color.White.copy(alpha = 0.18f),
                        0.05f to Color.White.copy(alpha = 0.35f), // Bottom-Right CCW Sharp Catch
                        0.10f to Color.White.copy(alpha = 0.18f),
                        0.52f to Color.White.copy(alpha = 0.18f),
                        0.55f to Color.White.copy(alpha = 0.35f), // Top-Left CCW Sharp Catch
                        0.60f to Color.White.copy(alpha = 0.18f),
                        1.00f to Color.White.copy(alpha = 0.18f)
                    ),
                    strokeWidth = 1.2.dp * sizeMultiplier,
                    shadows = listOf(
                        // Atmospheric depth shadows
                        ShadowParams(
                            color = Color.Black.copy(alpha = 0.2f),
                            blur = 20.dp * sizeMultiplier,
                            offsetX = (-5).dp * sizeMultiplier,
                            offsetY = (-5).dp * sizeMultiplier,
                            spread = (-5).dp * sizeMultiplier,
                            isInner = false
                        ),
                        // Sharp Rim Highlights
                        ShadowParams(
                            color = Color.White.copy(alpha = 0.18f),
                            blur = 0.5.dp * sizeMultiplier,
                            offsetX = (-0.5).dp * sizeMultiplier,
                            offsetY = (-0.5).dp * sizeMultiplier,
                            isInner = true
                        ),
                        ShadowParams(
                            color = Color.White.copy(alpha = 0.18f),
                            blur = 0.5.dp * sizeMultiplier,
                            offsetX = 0.5.dp * sizeMultiplier,
                            offsetY = 0.5.dp * sizeMultiplier,
                            isInner = true
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                // 3. Logo (Nested Squares - Reverted)
                Box(modifier = Modifier.size(24.dp * sizeMultiplier), contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val stroke = (2.5.dp * sizeMultiplier).toPx()
                        // Outer square
                        drawRect(
                            color = Color(0xFF5DFF00),
                            style = Stroke(width = stroke)
                        )
                        // Middle square
                        val middleSize = (15.628.dp * sizeMultiplier).toPx()
                        drawRect(
                            color = Color(0xFF5DFF00),
                            topLeft = androidx.compose.ui.geometry.Offset((size.width - middleSize) / 2, (size.height - middleSize) / 2),
                            size = androidx.compose.ui.geometry.Size(middleSize, middleSize),
                            style = Stroke(width = stroke)
                        )
                        // Inner square
                        val innerSize = (7.256.dp * sizeMultiplier).toPx()
                        drawRect(
                            color = Color(0xFF5DFF00),
                            topLeft = androidx.compose.ui.geometry.Offset((size.width - innerSize) / 2, (size.height - innerSize) / 2),
                            size = androidx.compose.ui.geometry.Size(innerSize, innerSize),
                            style = Stroke(width = stroke)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp * sizeMultiplier))

                // 4. SYSTM Text
                Text(
                    text = "SYSTM",
                    color = Color.White,
                    fontSize = (27 * sizeMultiplier).sp,
                    fontWeight = FontWeight.Light,
                    fontFamily = MontserratLight,
                    letterSpacing = (2.7 * sizeMultiplier).sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LogoCardPreview() {
    SystmTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            LogoCard(sizeMultiplier = 0.5f)
        }
    }
}
