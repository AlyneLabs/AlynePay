package com.alynelabs.systm

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
fun AccountListItem(
    name: String,
    lastUsed: String,
    modifier: Modifier = Modifier,
    sizeMultiplier: Float = 1f,
    showVoiceIcon: Boolean = true,
    showMessageIcon: Boolean = true
) {
    val MontserratLight = FontFamily(
        Font(R.font.montserrat_light, FontWeight.Light)
    )

    Box(
        modifier = modifier
            .height(66.5.dp * sizeMultiplier)
            .glassmorphism(
                borderRadius = 15.dp * sizeMultiplier,
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
            .padding(horizontal = 20.dp * sizeMultiplier),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = name,
                    color = Color.White,
                    fontSize = (18 * sizeMultiplier).sp,
                    fontWeight = FontWeight.Light,
                    fontFamily = MontserratLight
                )
                Text(
                    text = "Last Used : $lastUsed",
                    color = Color(0xFF909090),
                    fontSize = (12 * sizeMultiplier).sp,
                    fontWeight = FontWeight.Light,
                    fontFamily = MontserratLight
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp * sizeMultiplier)) {
                if (showVoiceIcon) {
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
                }
                if (showMessageIcon) {
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
            }
        }
    }
}

@Composable
fun BackupActionsRow(
    modifier: Modifier = Modifier,
    sizeMultiplier: Float = 1f
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp * sizeMultiplier)
    ) {
        BackupActionPill(text = "Add Account", modifier = Modifier.weight(1f), sizeMultiplier = sizeMultiplier)
        BackupActionPill(text = "Load Backup", modifier = Modifier.weight(1f), sizeMultiplier = sizeMultiplier)
        BackupActionPill(text = "Create Backup", modifier = Modifier.weight(1f), sizeMultiplier = sizeMultiplier)
    }
}

@Composable
fun BackupActionPill(
    text: String,
    modifier: Modifier = Modifier,
    sizeMultiplier: Float = 1f
) {
    val MontserratLight = FontFamily(
        Font(R.font.montserrat_light, FontWeight.Light)
    )

    Box(
        modifier = modifier
            .height(35.dp * sizeMultiplier)
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
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = (12 * sizeMultiplier).sp,
            fontWeight = FontWeight.Light,
            fontFamily = MontserratLight
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AccountListItemPreview() {
    SystmTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                BackupActionsRow(sizeMultiplier = 1f)
                AccountListItem(name = "Solid Ammo", lastUsed = "12 July 2026")
                AccountListItem(name = "Gas Ammo", lastUsed = "15 July 2026")
            }
        }
    }
}
