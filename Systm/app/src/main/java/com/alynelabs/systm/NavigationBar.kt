package com.alynelabs.systm

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alynelabs.systm.ui.theme.SystmTheme

enum class SystmScreen {
    Home, Account, Settings, Apps
}

@Composable
fun NavigationBar(
    modifier: Modifier = Modifier,
    sizeMultiplier: Float = 1f,
    selectedScreen: SystmScreen = SystmScreen.Home,
    onScreenSelected: (SystmScreen) -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp * sizeMultiplier)
            .glassmorphism(
                borderRadius = 35.dp * sizeMultiplier,
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp * sizeMultiplier),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavIcon(
                painterId = R.drawable.ic_home,
                contentDescription = "Home",
                isSelected = selectedScreen == SystmScreen.Home,
                onClick = { onScreenSelected(SystmScreen.Home) },
                sizeMultiplier = sizeMultiplier
            )
            NavIcon(
                painterId = R.drawable.ic_account,
                contentDescription = "Account",
                isSelected = selectedScreen == SystmScreen.Account,
                onClick = { onScreenSelected(SystmScreen.Account) },
                sizeMultiplier = sizeMultiplier
            )
            NavIcon(
                painterId = R.drawable.ic_settings,
                contentDescription = "Settings",
                isSelected = selectedScreen == SystmScreen.Settings,
                onClick = { onScreenSelected(SystmScreen.Settings) },
                sizeMultiplier = sizeMultiplier
            )
            NavIcon(
                painterId = R.drawable.ic_apps,
                contentDescription = "Apps",
                isSelected = selectedScreen == SystmScreen.Apps,
                onClick = { onScreenSelected(SystmScreen.Apps) },
                sizeMultiplier = sizeMultiplier
            )
        }
    }
}

@Composable
fun NavIcon(
    painterId: Int,
    contentDescription: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    sizeMultiplier: Float = 1f
) {
    Icon(
        painter = painterResource(id = painterId),
        contentDescription = contentDescription,
        modifier = Modifier
            .size(20.dp * sizeMultiplier)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.4f)
    )
}

@Composable
fun HomeIcon(sizeMultiplier: Float = 1f) {
    NavIcon(R.drawable.ic_home, "Home", true, {}, sizeMultiplier)
}

@Composable
fun AccountIcon(sizeMultiplier: Float = 1f) {
    NavIcon(R.drawable.ic_account, "Account", false, {}, sizeMultiplier)
}

@Composable
fun SettingsIcon(sizeMultiplier: Float = 1f) {
    NavIcon(R.drawable.ic_settings, "Settings", false, {}, sizeMultiplier)
}

@Composable
fun AppsIcon(sizeMultiplier: Float = 1f) {
    NavIcon(R.drawable.ic_apps, "Apps", false, {}, sizeMultiplier)
}

@Preview(showBackground = true)
@Composable
fun NavigationBarPreview() {
    SystmTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            NavigationBar()
        }
    }
}
