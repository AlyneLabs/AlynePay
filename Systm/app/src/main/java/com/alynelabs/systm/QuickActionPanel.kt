package com.alynelabs.systm

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
fun QuickActionPanel(
    modifier: Modifier = Modifier,
    sizeMultiplier: Float = 1f,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .height(137.5.dp * sizeMultiplier)
            .glassmorphism(
                borderRadius = 32.5.dp * sizeMultiplier,
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
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp * sizeMultiplier),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.Start
        ) {
            content()
        }
    }
}

@Composable
fun ActionButton(
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
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = interactionSource
            )
            .padding(vertical = 4.dp * sizeMultiplier),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(49.dp * sizeMultiplier)
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
                            ShadowParams(
                                color = glowColor.copy(alpha = 0.4f),
                                blur = 50.dp * sizeMultiplier,
                                spread = 2.dp * sizeMultiplier,
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
                fontSize = (13 * sizeMultiplier).sp,
                fontWeight = FontWeight.Light,
                fontFamily = MontserratLight,
                modifier = Modifier.padding(start = 10.dp * sizeMultiplier),
                lineHeight = (16 * sizeMultiplier).sp
            )
        }
    }
}

@Composable
fun InternetIcon(sizeMultiplier: Float = 1f) {
    Icon(
        painter = painterResource(id = R.drawable.ic_internet),
        contentDescription = "Internet",
        modifier = Modifier.size(24.dp * sizeMultiplier),
        tint = Color.Unspecified
    )
}

@Composable
fun WifiIcon(sizeMultiplier: Float = 1f) {
    Icon(
        painter = painterResource(id = R.drawable.ic_wifi),
        contentDescription = "Wifi",
        modifier = Modifier.size(24.dp * sizeMultiplier),
        tint = Color.Unspecified
    )
}

@Composable
fun EnergyIcon(sizeMultiplier: Float = 1f) {
    Icon(
        painter = painterResource(id = R.drawable.ic_energy),
        contentDescription = "Energy",
        modifier = Modifier.size(24.dp * sizeMultiplier),
        tint = Color.Unspecified
    )
}

@Composable
fun LimitIcon(sizeMultiplier: Float = 1f) {
    Icon(
        painter = painterResource(id = R.drawable.ic_limit),
        contentDescription = "Limit",
        modifier = Modifier.size(24.dp * sizeMultiplier),
        tint = Color.Unspecified
    )
}

@Preview(showBackground = true)
@Composable
fun QuickActionPanelPreview() {
    SystmTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Row {
                QuickActionPanel {
                    ActionButton(text = "Internet\nRouting", glowColor = Color(0xFF9747FF)) { InternetIcon() }
                    ActionButton(text = "Wi-Fi\nTunnels", glowColor = Color(0xFFFFA629)) { WifiIcon() }
                }
                Box(Modifier.width(20.dp))
                QuickActionPanel {
                    ActionButton(text = "Energy\nSaver", glowColor = Color(0xFF00B309)) { EnergyIcon() }
                    ActionButton(text = "Limit\nDevices", glowColor = Color(0xFFF24822)) { LimitIcon() }
                }
            }
        }
    }
}
