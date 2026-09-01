package com.alynelabs.systm

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alynelabs.systm.ui.theme.SystmTheme


import androidx.compose.foundation.clickable
@Composable
fun UserBox(
    modifier: Modifier = Modifier,
    sizeMultiplier: Float = 1f,
    onTestClick: () -> Unit = {}
) {
    val MontserratLight = FontFamily(
        Font(R.font.montserrat_light, FontWeight.Light)
    )
    Box(modifier = modifier.clickable { onTestClick() }) {
        Text(
            text = "Liquid Ammo",
            color = Color.White,
            fontSize = (32 * sizeMultiplier).sp,
            fontWeight = FontWeight.Light,
            fontFamily = MontserratLight
        )
    }
}

@Preview(showBackground = true)
@Composable
fun UserBoxPreview() {
    SystmTheme {
        Box(
            modifier = Modifier
                .background(Color.Black)
                .padding(16.dp)
        ) {
            UserBox()
        }
    }
}
