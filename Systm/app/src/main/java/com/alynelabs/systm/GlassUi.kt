package com.alynelabs.systm

import android.graphics.BlurMaskFilter
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.alynelabs.systm.ui.theme.SystmTheme

data class ShadowParams(
    val color: Color,
    val blur: Dp,
    val offsetX: Dp = 0.dp,
    val offsetY: Dp = 0.dp,
    val spread: Dp = 0.dp,
    val isInner: Boolean = false
)

fun Modifier.glassmorphism(
    borderRadius: Dp = 16.dp,
    backgroundColorCenter: Color = Color.White.copy(alpha = 0.08f),
    backgroundColorEdge: Color = Color.White.copy(alpha = 0.05f),
    outlineBrush: Brush? = null,
    outlineColor: Color = Color.White.copy(alpha = 0.1f),
    strokeWidth: Dp = 0.5.dp,
    shadows: List<ShadowParams> = emptyList()
) = this.drawBehind {
    val borderRadiusPx = borderRadius.toPx()

    // 1. Draw Drop Shadows
    shadows.filter { !it.isInner }.forEach { shadow ->
        if (shadow.blur > 0.dp || shadow.color.alpha > 0f) {
            drawIntoCanvas { canvas ->
                val shadowPaint = Paint().asFrameworkPaint().apply {
                    isAntiAlias = true
                    color = shadow.color.toArgb()
                    if (shadow.blur > 0.dp) {
                        maskFilter = BlurMaskFilter(
                            shadow.blur.toPx(),
                            BlurMaskFilter.Blur.NORMAL
                        )
                    }
                }
                
                val spreadPx = shadow.spread.toPx()
                canvas.nativeCanvas.drawRoundRect(
                    shadow.offsetX.toPx() - spreadPx,
                    shadow.offsetY.toPx() - spreadPx,
                    size.width + shadow.offsetX.toPx() + spreadPx,
                    size.height + shadow.offsetY.toPx() + spreadPx,
                    borderRadiusPx + spreadPx,
                    borderRadiusPx + spreadPx,
                    shadowPaint
                )
            }
        }
    }

    // 2. Draw Translucent Base Box
    val glassBrush = Brush.radialGradient(
        colors = listOf(backgroundColorCenter, backgroundColorEdge),
        center = center,
        radius = size.maxDimension * 0.8f
    )
    
    drawRoundRect(
        brush = glassBrush,
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(borderRadiusPx)
    )

    // 3. Draw Outline (Angular/Solid)
    val strokeStyle = androidx.compose.ui.graphics.drawscope.Stroke(
        width = strokeWidth.toPx(),
        join = androidx.compose.ui.graphics.StrokeJoin.Miter,
        miter = 4.0f
    )

    if (outlineBrush != null) {
        drawRoundRect(
            brush = outlineBrush,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(borderRadiusPx),
            style = strokeStyle
        )
    } else {
        drawRoundRect(
            color = outlineColor,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(borderRadiusPx),
            style = strokeStyle
        )
    }

    // 4. Draw Inner Shadows
    shadows.filter { it.isInner }.forEach { shadow ->
        if (shadow.blur > 0.dp || shadow.color.alpha > 0f) {
            drawIntoCanvas { canvas ->
                val innerPaint = Paint().asFrameworkPaint().apply {
                    isAntiAlias = true
                    color = shadow.color.toArgb()
                    if (shadow.blur > 0.dp) {
                        maskFilter = BlurMaskFilter(
                            shadow.blur.toPx(),
                            BlurMaskFilter.Blur.NORMAL
                        )
                    }
                }

                val outerPath = Path().apply {
                    addRoundRect(
                        androidx.compose.ui.geometry.RoundRect(
                            left = 0f,
                            top = 0f,
                            right = size.width,
                            bottom = size.height,
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(borderRadiusPx)
                        )
                    )
                }

                canvas.save()
                canvas.clipPath(outerPath)

                val innerPath = Path().apply {
                    addRoundRect(
                        androidx.compose.ui.geometry.RoundRect(
                            left = shadow.offsetX.toPx(),
                            top = shadow.offsetY.toPx(),
                            right = size.width + shadow.offsetX.toPx(),
                            bottom = size.height + shadow.offsetY.toPx(),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(borderRadiusPx)
                        )
                    )
                    fillType = androidx.compose.ui.graphics.PathFillType.EvenOdd
                }

                val shadowRegion = Path.combine(
                    operation = androidx.compose.ui.graphics.PathOperation.Difference,
                    path1 = outerPath,
                    path2 = innerPath
                )

                canvas.drawPath(shadowRegion, Paint().apply {
                    asFrameworkPaint().set(innerPaint)
                })

                canvas.restore()
            }
        }
    }
}

@Composable
fun SystmGlassCard(
    modifier: Modifier = Modifier,
    width: Dp = 380.dp,
    height: Dp = 120.dp,
    borderRadius: Dp = 60.dp,
    strokeWidth: Dp = 0.5.dp,
    text: String = "SYSTM"
) {
    Box(
        modifier = modifier
            .size(width, height)
            .glassmorphism(
                borderRadius = borderRadius,
                backgroundColorCenter = Color.White.copy(alpha = 0.08f),
                backgroundColorEdge = Color.White.copy(alpha = 0.04f),
                outlineBrush = Brush.sweepGradient(
                    0.00f to Color.White.copy(alpha = 0.25f),
                    0.10f to Color.White.copy(alpha = 0.60f),
                    0.35f to Color.White.copy(alpha = 0.15f),
                    0.60f to Color.White.copy(alpha = 0.60f),
                    0.85f to Color.White.copy(alpha = 0.25f),
                    1.00f to Color.White.copy(alpha = 0.25f)
                ),
                strokeWidth = strokeWidth,
                shadows = listOf(
                    // Inner Shadow 1: X 1.22, Y 1.13, Blur 4.62, Black 12.6%
                    ShadowParams(
                        color = Color.Black.copy(alpha = 0.126f),
                        blur = 4.62.dp,
                        offsetX = 1.22.dp,
                        offsetY = 1.13.dp,
                        isInner = true
                    ),
                    // Inner Shadow 2: X 2.15, Y 2, Blur 9.24, Black 12.6%
                    ShadowParams(
                        color = Color.Black.copy(alpha = 0.126f),
                        blur = 9.24.dp,
                        offsetX = 2.15.dp,
                        offsetY = 2.dp,
                        isInner = true
                    ),
                    // Drop Shadow 1: X -1.86, Y -1.73, Blur 12, Spread -8, Black 15%
                    ShadowParams(
                        color = Color.Black.copy(alpha = 0.15f),
                        blur = 12.dp,
                        offsetX = (-1.86).dp,
                        offsetY = (-1.73).dp,
                        spread = (-8).dp,
                        isInner = false
                    ),
                    // Drop Shadow 2: X -11.15, Y -10.39, Blur 48, Spread -12, Black 15%
                    ShadowParams(
                        color = Color.Black.copy(alpha = 0.15f),
                        blur = 48.dp,
                        offsetX = (-11.15).dp,
                        offsetY = (-10.39).dp,
                        spread = (-12).dp,
                        isInner = false
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.White.copy(alpha = 0.8f))
    }
}

@Preview(showBackground = true)
@Composable
fun GlassCardPreview() {
    SystmTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            SystmGlassCard()
        }
    }
}
