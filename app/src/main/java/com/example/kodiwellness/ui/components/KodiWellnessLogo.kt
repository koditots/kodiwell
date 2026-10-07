package com.example.kodiwellness.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

val BrandKodiBlue = Color(0xFF0284C7)
val BrandWellnessGreen = Color(0xFF16A34A)
val BrandTaglineNavy = Color(0xFF1E3A8A)

@Composable
fun KodiWellnessLogo(
    modifier: Modifier = Modifier,
    iconSize: Int = 110,
    showTagline: Boolean = true,
    textColor: Color = Color.Unspecified
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo Emblem
        Image(
            painter = painterResource(id = R.drawable.ic_kodi_wellness_logo),
            contentDescription = "Kodi Wellness Logo",
            modifier = Modifier.size(iconSize.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Kodi Wellness Brand Typography
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Kodi ",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = BrandKodiBlue,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "Wellness",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = BrandWellnessGreen,
                letterSpacing = (-0.5).sp
            )
        }

        if (showTagline) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Health Today. A Brighter Tomorrow.",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (textColor != Color.Unspecified) textColor else BrandTaglineNavy,
                textAlign = TextAlign.Center,
                letterSpacing = 0.2.sp
            )
        }
    }
}
