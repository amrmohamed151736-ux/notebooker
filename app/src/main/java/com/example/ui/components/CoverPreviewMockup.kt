package com.example.ui.components

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CoverConfig
import com.example.model.CoverStyle

@Composable
fun CoverPreviewMockup(
    coverConfig: CoverConfig,
    modifier: Modifier = Modifier
) {
    val primaryColor = remember(coverConfig.primaryColorHex) {
        try { Color(android.graphics.Color.parseColor(coverConfig.primaryColorHex)) }
        catch (_: Exception) { Color(0xFF1E293B) }
    }
    val accentColor = remember(coverConfig.accentColorHex) {
        try { Color(android.graphics.Color.parseColor(coverConfig.accentColorHex)) }
        catch (_: Exception) { Color(0xFFD97706) }
    }
    val foilColor = remember(coverConfig.foilEffect, accentColor) {
        when (coverConfig.foilEffect) {
            "SILVER_FOIL" -> Color(0xFFE2E8F0)
            "ROSE_FOIL" -> Color(0xFFFB7185)
            else -> accentColor // Gold foil
        }
    }
    val tabColor = remember(coverConfig.cornerTabs) {
        when (coverConfig.cornerTabs) {
            "SILVER" -> Color(0xFFCBD5E1)
            "ROSE_GOLD" -> Color(0xFFF472B6)
            else -> Color(0xFFF59E0B) // Brass gold
        }
    }
    val ribbonColor = remember(coverConfig.ribbonColorHex) {
        try { Color(android.graphics.Color.parseColor(coverConfig.ribbonColorHex)) }
        catch (_: Exception) { accentColor }
    }

    val aiBitmap = remember(coverConfig.aiIllustrationBase64) {
        if (!coverConfig.aiIllustrationBase64.isNullOrEmpty()) {
            try {
                val bytes = Base64.decode(coverConfig.aiIllustrationBase64, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
            } catch (_: Exception) { null }
        } else null
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(260.dp)
                .aspectRatio(0.71f)
                .shadow(elevation = 16.dp, shape = RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp, topStart = 4.dp, bottomStart = 4.dp), clip = false)
                .clip(RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp, topStart = 4.dp, bottomStart = 4.dp))
                .background(primaryColor)
                .testTag("cover_preview_mockup")
        ) {
            // Book pages edge simulation at right
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(6.dp)
                    .align(Alignment.CenterEnd)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, Color(0xFFE2E8F0), Color(0xFFCBD5E1))
                        )
                    )
            )

            // Inner content layout
            Row(modifier = Modifier.fillMaxSize()) {
                // Book Spine on Left
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(28.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Black.copy(alpha = 0.5f),
                                    Color.Black.copy(alpha = 0.2f),
                                    Color.White.copy(alpha = 0.1f)
                                )
                            )
                        )
                        .drawBehind {
                            drawLine(
                                color = foilColor.copy(alpha = 0.7f),
                                start = Offset(size.width - 2f, 0f),
                                end = Offset(size.width - 2f, size.height),
                                strokeWidth = 1.5f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = coverConfig.spineText,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif
                    )
                }

                // Front Cover Face
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp)
                        .drawBehind {
                            // Metallic foil double border
                            drawRoundRect(
                                color = foilColor.copy(alpha = 0.85f),
                                topLeft = Offset(0f, 0f),
                                size = size,
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f),
                                style = Stroke(1.8f)
                            )
                            drawRoundRect(
                                color = foilColor.copy(alpha = 0.5f),
                                topLeft = Offset(4f, 4f),
                                size = androidx.compose.ui.geometry.Size(size.width - 8f, size.height - 8f),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f),
                                style = Stroke(1.0f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f)))
                            )

                            // Render 100% offline procedural vector art pattern
                            ProceduralVectorArt.drawPattern(
                                drawScope = this,
                                pattern = coverConfig.proceduralPattern,
                                primaryColor = primaryColor,
                                foilColor = foilColor,
                                width = size.width,
                                height = size.height
                            )

                            // Draw metal corner tabs
                            if (coverConfig.cornerTabs != "NONE") {
                                val tabS = 22f
                                // Top Right tab
                                val pathTR = Path().apply {
                                    moveTo(size.width, 0f)
                                    lineTo(size.width - tabS, 0f)
                                    lineTo(size.width, tabS)
                                    close()
                                }
                                drawPath(pathTR, tabColor)

                                // Bottom Right tab
                                val pathBR = Path().apply {
                                    moveTo(size.width, size.height)
                                    lineTo(size.width - tabS, size.height)
                                    lineTo(size.width, size.height - tabS)
                                    close()
                                }
                                drawPath(pathBR, tabColor)
                            }
                        }
                        .padding(12.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Badge / Sticker icon
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val badgeIcon = when (coverConfig.coverBadge) {
                                "ATOM" -> Icons.Default.Science
                                "ORBIT" -> Icons.Default.Public
                                "CREST" -> Icons.Default.MilitaryTech
                                "STAR" -> Icons.Default.Star
                                else -> Icons.AutoMirrored.Filled.MenuBook
                            }
                            Surface(
                                shape = CircleShape,
                                color = foilColor.copy(alpha = 0.18f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, foilColor.copy(alpha = 0.6f)),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = badgeIcon,
                                        contentDescription = "Badge",
                                        tint = foilColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        // Central Art (AI Illustration or Decorative Emblem)
                        if (aiBitmap != null) {
                            Image(
                                bitmap = aiBitmap,
                                contentDescription = "AI Generated Cover Art",
                                modifier = Modifier
                                    .size(110.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.5.dp, foilColor, RoundedCornerShape(8.dp))
                            )
                        } else {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = foilColor.copy(alpha = 0.12f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, foilColor.copy(alpha = 0.4f)),
                                modifier = Modifier.size(60.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Bookmark,
                                        contentDescription = null,
                                        tint = foilColor,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }
                        }

                        // Central Title Plaque with gold foil outline
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color.White.copy(alpha = 0.95f),
                            shadowElevation = 5.dp,
                            border = androidx.compose.foundation.BorderStroke(1.2.dp, foilColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = coverConfig.title,
                                    color = Color(0xFF1E293B),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif,
                                    textAlign = TextAlign.Center,
                                    maxLines = 2
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = coverConfig.subtitle,
                                    color = Color(0xFF64748B),
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.SansSerif,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "✦  ${coverConfig.editionLabel}  ✦",
                                    color = foilColor,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // Author Label
                        Text(
                            text = coverConfig.author,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.Serif,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Bookmark Ribbon hanging from bottom
            Box(
                modifier = Modifier
                    .width(18.dp)
                    .height(26.dp)
                    .align(Alignment.BottomCenter)
                    .offset(x = 24.dp, y = 14.dp)
                    .background(ribbonColor)
                    .border(0.5.dp, Color.Black.copy(alpha = 0.2f))
            )
        }
    }
}
