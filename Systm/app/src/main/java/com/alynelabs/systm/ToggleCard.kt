package com.alynelabs.systm

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alynelabs.systm.ui.theme.SystmTheme

@Composable
fun ToggleCard(
    text: String,
    glowColor: Color,
    modifier: Modifier = Modifier,
    sizeMultiplier: Float = 1f,
    isActive: Boolean = false,
    onClick: () -> Unit = {},
    icon: @Composable () -> Unit
) {
    val MontserratLight = FontFamily(
        Font(R.font.montserrat_light, FontWeight.Light)
    )
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale = if (isPressed) 0.96f else 1f

    val iconContainerColorCenter = if (isPressed || isActive) Color.DarkGray.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.06f)
    val iconContainerColorEdge = if (isPressed || isActive) Color.DarkGray.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.03f)

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .height(70.dp * sizeMultiplier)
            .glassmorphism(
                borderRadius = 50.dp * sizeMultiplier,
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
                        offsetX = (-0.93).dp * sizeMultiplier,
                        offsetY = (-0.87).dp * sizeMultiplier,
                        spread = (-4).dp * sizeMultiplier,
                        isInner = false
                    )
                )
            )
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = interactionSource
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier
                .padding(start = 10.dp * sizeMultiplier),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Button with Glow
            Box(
                modifier = Modifier
                    .size(50.dp * sizeMultiplier)
                    .glassmorphism(
                        borderRadius = 25.dp * sizeMultiplier,
                        backgroundColorCenter = iconContainerColorCenter,
                        backgroundColorEdge = iconContainerColorEdge,
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
                            // Glow Shadow
                            ShadowParams(
                                color = glowColor.copy(alpha = 0.6f),
                                blur = 125.dp * sizeMultiplier,
                                offsetY = 2.dp * sizeMultiplier,
                                spread = 3.5.dp * sizeMultiplier,
                                isInner = false
                            ),
                            ShadowParams(
                                color = Color.Black.copy(alpha = 0.15f),
                                blur = 24.dp * sizeMultiplier,
                                offsetX = (-5.5).dp * sizeMultiplier,
                                offsetY = (-5.2).dp * sizeMultiplier,
                                spread = (-6).dp * sizeMultiplier,
                                isInner = false
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }

            Text(
                text = text,
                color = Color.White,
                fontSize = (16 * sizeMultiplier).sp,
                fontWeight = FontWeight.Light,
                fontFamily = MontserratLight,
                modifier = Modifier.padding(start = 12.dp * sizeMultiplier)
            )
        }
    }
}

@Composable
fun SystmIcon(
    modifier: Modifier = Modifier,
    sizeMultiplier: Float = 1f
) {
    Icon(
        painter = painterResource(id = R.drawable.ic_systm),
        contentDescription = "Systm",
        modifier = modifier.size(24.dp * sizeMultiplier),
        tint = Color.Unspecified
    )
}

@Composable
fun MeshIcon(
    modifier: Modifier = Modifier,
    sizeMultiplier: Float = 1f
) {
    Icon(
        painter = painterResource(id = R.drawable.ic_mesh),
        contentDescription = "Mesh",
        modifier = modifier.size(30.dp * sizeMultiplier),
        tint = Color.Unspecified
    )
}

@Preview(showBackground = true)
@Composable
fun ToggleCardsPreview() {
    SystmTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Row {
                ToggleCard(text = "SYSTM", glowColor = Color(0xFF00FF95)) { SystmIcon() }
                Box(Modifier.width(20.dp))
                ToggleCard(text = "MESH", glowColor = Color(0xFF00CCFF)) { MeshIcon() }
            }
        }
    }
}
