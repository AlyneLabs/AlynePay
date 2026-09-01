package com.alynelabs.systm

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alynelabs.systm.ui.theme.SystmTheme

@Composable
fun NodesPanel(
    modifier: Modifier = Modifier,
    sizeMultiplier: Float = 1f,
    topology: Map<Long, List<Long>> = emptyMap()
) {
    val MontserratLight = FontFamily(
        Font(R.font.montserrat_light, FontWeight.Light)
    )

    Box(
        modifier = modifier
            .width(331.dp * sizeMultiplier)
            .height(215.dp * sizeMultiplier)
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
    ) {
        // 1. Information Bar
        Row(
            modifier = Modifier
                .padding(top = 7.dp * sizeMultiplier, start = 7.5.dp * sizeMultiplier, end = 7.5.dp * sizeMultiplier)
                .fillMaxSize(),
            horizontalArrangement = Arrangement.Start
        ) {
            // "Connected Nodes" Pill
            Box(
                modifier = Modifier
                    .width(200.dp * sizeMultiplier)
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
                                color = Color(0xFFB80000).copy(alpha = 0.17f),
                                blur = 75.dp * sizeMultiplier,
                                offsetX = 10.dp * sizeMultiplier,
                                offsetY = 20.dp * sizeMultiplier,
                                spread = 2.5.dp * sizeMultiplier,
                                isInner = false
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "CONNECTED NODES",
                    color = Color.White,
                    fontSize = (15 * sizeMultiplier).sp,
                    fontWeight = FontWeight.Light,
                    fontFamily = MontserratLight,
                    letterSpacing = (0.75 * sizeMultiplier).sp
                )
            }

            Spacer(modifier = Modifier.width(7.dp * sizeMultiplier))

            // Info Pill (Online/Offline)
            Box(
                modifier = Modifier
                    .width(110.dp * sizeMultiplier)
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
                                color = Color.White.copy(alpha = 0.17f),
                                blur = 75.dp * sizeMultiplier,
                                offsetX = 10.dp * sizeMultiplier,
                                offsetY = 20.dp * sizeMultiplier,
                                spread = 2.5.dp * sizeMultiplier,
                                isInner = false
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val onlineCount = topology.keys.size
                    val totalCount = (topology.keys + topology.values.flatten()).distinct().size
                    
                    Canvas(modifier = Modifier.size(6.dp * sizeMultiplier)) {
                        drawCircle(color = Color(0xFF5DFF00))
                    }
                    Text(
                        text = " " + onlineCount.toString().padStart(2, '0'),
                        color = Color.White,
                        fontSize = (15 * sizeMultiplier).sp,
                        fontWeight = FontWeight.Light,
                        fontFamily = MontserratLight
                    )
                    Spacer(modifier = Modifier.width(10.dp * sizeMultiplier))
                    Canvas(modifier = Modifier.size(6.dp * sizeMultiplier)) {
                        drawCircle(color = Color(0xFFFF4822))
                    }
                    val offlineCount = (totalCount - onlineCount).coerceAtLeast(0)
                    Text(
                        text = " " + offlineCount.toString().padStart(2, '0'),
                        color = Color.White,
                        fontSize = (15 * sizeMultiplier).sp,
                        fontWeight = FontWeight.Light,
                        fontFamily = MontserratLight
                    )
                }
            }
        }

        // 2. Network Graph
        if (topology.isEmpty()) {
            NetworkGraph(
                modifier = Modifier
                    .offset(x = 61.dp * sizeMultiplier, y = 58.5.dp * sizeMultiplier)
                    .size(203.dp * sizeMultiplier, 136.dp * sizeMultiplier),
                sizeMultiplier = sizeMultiplier
            )
        } else {
            DynamicNetworkGraph(
                modifier = Modifier
                    .offset(x = 20.dp * sizeMultiplier, y = 58.5.dp * sizeMultiplier)
                    .size(291.dp * sizeMultiplier, 136.dp * sizeMultiplier),
                sizeMultiplier = sizeMultiplier,
                topology = topology
            )
        }
    }
}

@Composable
fun DynamicNetworkGraph(
    modifier: Modifier = Modifier,
    sizeMultiplier: Float = 1f,
    topology: Map<Long, List<Long>>
) {
    Canvas(modifier = modifier) {
        val colorGreen = Color(0xFF5DFF00)
        val colorBlue = Color(0xFF00CCFF)
        val lineColor = Color.White.copy(alpha = 0.3f)
        val stroke = (0.8.dp * sizeMultiplier).toPx()
        val nodeRadius = (4.dp * sizeMultiplier).toPx()

        val allNodes = (topology.keys + topology.values.flatten()).distinct()
        val nodePositions = mutableMapOf<Long, Offset>()

        // 1. Position calculation (Self in center, others around)
        val center = Offset(size.width / 2, size.height / 2)
        
        // Find self (the one with the most outgoing connections or simply the first key)
        // In a real app, we'd pass the selfNodeId, but for now we'll pick the most connected
        val selfNodeId = topology.keys.maxByOrNull { topology[it]?.size ?: 0 } ?: allNodes.firstOrNull() ?: 0L
        nodePositions[selfNodeId] = center

        val otherNodes = allNodes.filter { it != selfNodeId }
        otherNodes.forEachIndexed { index, nodeId ->
            val angle = (2 * Math.PI * index / otherNodes.size).toFloat()
            val radiusX = size.width / 2.3f
            val radiusY = size.height / 2.3f
            val pos = Offset(
                center.x + Math.cos(angle.toDouble()).toFloat() * radiusX,
                center.y + Math.sin(angle.toDouble()).toFloat() * radiusY
            )
            nodePositions[nodeId] = pos
        }

        // 2. Draw connections
        topology.forEach { (source, neighbors) ->
            val start = nodePositions[source] ?: return@forEach
            neighbors.forEach { target ->
                val end = nodePositions[target] ?: return@forEach
                drawLine(lineColor, start, end, strokeWidth = stroke)
            }
        }

        // 3. Draw nodes with glow
        nodePositions.forEach { (nodeId, pos) ->
            val nodeColor = if (nodeId == selfNodeId) colorBlue else colorGreen
            val glowRadius = nodeRadius * 6
            
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(nodeColor.copy(alpha = 0.4f), nodeColor.copy(alpha = 0.1f), Color.Transparent),
                    center = pos,
                    radius = glowRadius
                ),
                radius = glowRadius,
                center = pos
            )
            drawCircle(color = nodeColor, radius = nodeRadius, center = pos)
        }
    }
}

@Composable
fun NetworkGraph(
    modifier: Modifier = Modifier,
    sizeMultiplier: Float = 1f
) {
    Canvas(modifier = modifier) {
        val colorGreen = Color(0xFF5DFF00)
        val colorRed = Color(0xFFF24822)
        val colorBlue = Color(0xFF00CCFF)
        val lineColor = Color.White.copy(alpha = 0.4f)
        val stroke = (0.8.dp * sizeMultiplier).toPx()
        val nodeRadius = (3.5.dp * sizeMultiplier).toPx()

        // Scaled coordinates
        val p1 = Offset(66.9f.dp.toPx() * sizeMultiplier, 0f.dp.toPx() * sizeMultiplier) // Green
        val p2 = Offset(106.45f.dp.toPx() * sizeMultiplier, 129.15f.dp.toPx() * sizeMultiplier) // Red
        val p3 = Offset(121.55f.dp.toPx() * sizeMultiplier, 62.25f.dp.toPx() * sizeMultiplier) // Green
        val p4 = Offset(36.65f.dp.toPx() * sizeMultiplier, 58.75f.dp.toPx() * sizeMultiplier) // Red
        val p5 = Offset(0f.dp.toPx() * sizeMultiplier, 111.1f.dp.toPx() * sizeMultiplier) // Red
        val p6 = Offset(187.3f.dp.toPx() * sizeMultiplier, 18.6f.dp.toPx() * sizeMultiplier) // Red
        val p7 = Offset(196.05f.dp.toPx() * sizeMultiplier, 100.05f.dp.toPx() * sizeMultiplier) // Red
        val p8 = Offset(177.05f.dp.toPx() * sizeMultiplier, 121.1f.dp.toPx() * sizeMultiplier) // Blue

        // Helper to draw glow with RadialGradient
        fun drawGlowNode(center: Offset, color: Color) {
            val glowRadius = nodeRadius * 6
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color.copy(alpha = 0.5f), color.copy(alpha = 0.1f), Color.Transparent),
                    center = center,
                    radius = glowRadius
                ),
                radius = glowRadius,
                center = center
            )
            drawCircle(
                color = color,
                radius = nodeRadius,
                center = center
            )
        }

        // Connections
        drawLine(lineColor, p1, p3, strokeWidth = stroke)
        drawLine(lineColor, p1, p4, strokeWidth = stroke)
        drawLine(lineColor, p3, p2, strokeWidth = stroke)
        drawLine(lineColor, p3, p6, strokeWidth = stroke)
        drawLine(lineColor, p3, p8, strokeWidth = stroke)
        drawLine(lineColor, p2, p7, strokeWidth = stroke)
        drawLine(lineColor, p4, p5, strokeWidth = stroke)
        drawLine(lineColor, p6, p7, strokeWidth = stroke)

        // Nodes
        drawGlowNode(p1, colorGreen)
        drawGlowNode(p3, colorGreen)
        drawGlowNode(p2, colorRed)
        drawGlowNode(p4, colorRed)
        drawGlowNode(p5, colorRed)
        drawGlowNode(p6, colorRed)
        drawGlowNode(p7, colorRed)
        drawGlowNode(p8, colorBlue)
    }
}

@Preview(showBackground = true)
@Composable
fun NodesPanelPreview() {
    SystmTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            NodesPanel()
        }
    }
}
