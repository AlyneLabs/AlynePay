package com.alynelabs.systm

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alynelabs.systm.ui.theme.SystmTheme

@Composable
fun StatusBox(
    modifier: Modifier = Modifier,
    sizeMultiplier: Float = 1f
) {
    Box(
        modifier = modifier
            .size(75.dp * sizeMultiplier)
            .glassmorphism(
                borderRadius = 37.5.dp * sizeMultiplier,
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
                        color = Color.Black.copy(alpha = 0.2f),
                        blur = 20.dp * sizeMultiplier,
                        offsetX = (-5).dp * sizeMultiplier,
                        offsetY = (-5).dp * sizeMultiplier,
                        spread = (-5).dp * sizeMultiplier,
                        isInner = false
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(40.dp * sizeMultiplier)) {
            val strokeWidth = (6.75.dp * sizeMultiplier).toPx()

            // Outer Blue Circle
            drawCircle(
                color = Color(0xFF0088FF),
                radius = (30.dp * sizeMultiplier).toPx() - strokeWidth / 2,
                style = Stroke(width = strokeWidth)
            )

            // Middle Green Circle
            drawCircle(
                color = Color(0xFF5DFF00),
                radius = (18.75.dp * sizeMultiplier).toPx() - strokeWidth / 2,
                style = Stroke(width = strokeWidth)
            )

            // Inner Yellow Dot
            drawCircle(
                color = Color(0xFFFAC800),
                radius = (7.5.dp * sizeMultiplier).toPx()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StatusBoxPreview() {
    SystmTheme {
        Box(
            modifier = Modifier
                .size(300.dp)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            StatusBox(sizeMultiplier = 0.5f)
        }
    }
}
