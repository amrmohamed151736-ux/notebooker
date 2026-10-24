package com.example.ui.screens.focus

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

fun formatTime(seconds: Int): Pair<String, String> {
    val m = seconds / 60
    val s = seconds % 60
    return Pair(String.format("%02d", m), String.format("%02d", s))
}

@Composable
fun RenderTimerSkin(
    skinId: String,
    remainingSeconds: Int,
    totalDurationSeconds: Int,
    isRunning: Boolean,
    isPaused: Boolean,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    when (skinId) {
        "SKIN_DIGITAL" -> DigitalTimerView(remainingSeconds, totalDurationSeconds, isRunning, isPaused, primaryColor, modifier)
        "SKIN_FLIP" -> FlipClockTimerView(remainingSeconds, totalDurationSeconds, isRunning, isPaused, primaryColor, modifier)
        "SKIN_CLASSIC" -> ClassicClockTimerView(remainingSeconds, totalDurationSeconds, isRunning, isPaused, primaryColor, modifier)
        "SKIN_BAR" -> BarGaugeTimerView(remainingSeconds, totalDurationSeconds, isRunning, isPaused, primaryColor, modifier)
        "SKIN_SCI_FI" -> SciFiHudTimerView(remainingSeconds, totalDurationSeconds, isRunning, isPaused, primaryColor, modifier)
        "SKIN_MINIMAL" -> MinimalistTimerView(remainingSeconds, totalDurationSeconds, isRunning, isPaused, primaryColor, modifier)
        else -> ModernCircularTimerView(remainingSeconds, totalDurationSeconds, isRunning, isPaused, primaryColor, modifier)
    }
}

// 1. Modern Circular Timer
@Composable
fun ModernCircularTimerView(
    remainingSeconds: Int,
    totalDurationSeconds: Int,
    isRunning: Boolean,
    isPaused: Boolean,
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    val progress = if (totalDurationSeconds > 0) {
        (remainingSeconds.toFloat() / totalDurationSeconds.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 350),
        label = "Progress"
    )

    val (minStr, secStr) = formatTime(remainingSeconds)

    Box(
        modifier = modifier.size(240.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            val strokeWidth = 14.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val arcSize = Size(diameter, diameter)
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

            // Track background
            drawArc(
                color = primaryColor.copy(alpha = 0.15f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Active progress arc
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(primaryColor, primaryColor.copy(alpha = 0.7f), primaryColor)
                ),
                startAngle = -90f,
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$minStr:$secStr",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = 2.sp
            )
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = when {
                    isPaused -> Color(0xFFD97706).copy(alpha = 0.2f)
                    isRunning -> Color(0xFF059669).copy(alpha = 0.2f)
                    else -> MaterialTheme.colorScheme.surfaceVariant
                },
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = when {
                        isPaused -> "متوقف مؤقتاً"
                        isRunning -> "جلسة نشطة • تركيز"
                        else -> "جاهز للبدء"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        isPaused -> Color(0xFFD97706)
                        isRunning -> Color(0xFF059669)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

// 2. Clean Digital Timer
@Composable
fun DigitalTimerView(
    remainingSeconds: Int,
    totalDurationSeconds: Int,
    isRunning: Boolean,
    isPaused: Boolean,
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    val (minStr, secStr) = formatTime(remainingSeconds)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(8.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF0B0F19),
        shadowElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(2.dp, primaryColor.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Minutes box
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E293B),
                    modifier = Modifier.padding(horizontal = 6.dp)
                ) {
                    Text(
                        text = minStr,
                        fontSize = 58.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = primaryColor,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }

                Text(
                    text = ":",
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                // Seconds box
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E293B),
                    modifier = Modifier.padding(horizontal = 6.dp)
                ) {
                    Text(
                        text = secStr,
                        fontSize = 58.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF38BDF8),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "DIGITAL PRECISION TIMER • LED DISPLAY",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                letterSpacing = 1.5.sp
            )
        }
    }
}

// 3. Retro Flip Clock Timer
@Composable
fun FlipClockTimerView(
    remainingSeconds: Int,
    totalDurationSeconds: Int,
    isRunning: Boolean,
    isPaused: Boolean,
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    val (minStr, secStr) = formatTime(remainingSeconds)

    Column(
        modifier = modifier.fillMaxWidth().padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Minutes Card
            FlipCardUnit(numberStr = minStr, label = "دقائق", primaryColor = primaryColor)
            Spacer(modifier = Modifier.width(10.dp))
            Text(":", fontSize = 42.sp, fontWeight = FontWeight.Bold, color = primaryColor)
            Spacer(modifier = Modifier.width(10.dp))
            // Seconds Card
            FlipCardUnit(numberStr = secStr, label = "ثواني", primaryColor = Color(0xFF059669))
        }
    }
}

@Composable
private fun FlipCardUnit(numberStr: String, label: String, primaryColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF18181B),
            shadowElevation = 6.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF27272A)),
            modifier = Modifier.size(width = 100.dp, height = 110.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                // Divider line simulating flip seam
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.Black)
                )
                Text(
                    text = numberStr,
                    fontSize = 54.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor,
                    textAlign = TextAlign.Center
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
    }
}

// 4. Classic Clock Dial
@Composable
fun ClassicClockTimerView(
    remainingSeconds: Int,
    totalDurationSeconds: Int,
    isRunning: Boolean,
    isPaused: Boolean,
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    val (minStr, secStr) = formatTime(remainingSeconds)
    val secondsAngle = (remainingSeconds % 60) * 6f
    val minutesAngle = (remainingSeconds / 60) * 6f

    Box(
        modifier = modifier.size(240.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(10.dp)) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.minDimension / 2 - 8.dp.toPx()

            // Dial Circle
            drawCircle(color = Color(0xFFF8FAFC), radius = radius, center = center)
            drawCircle(color = Color(0xFF0F172A), radius = radius, center = center, style = Stroke(width = 4.dp.toPx()))

            // Hour/Minute Ticks
            for (i in 0 until 12) {
                val angle = Math.toRadians((i * 30).toDouble())
                val start = Offset(
                    (center.x + (radius - 12.dp.toPx()) * sin(angle)).toFloat(),
                    (center.y - (radius - 12.dp.toPx()) * cos(angle)).toFloat()
                )
                val end = Offset(
                    (center.x + radius * sin(angle)).toFloat(),
                    (center.y - radius * cos(angle)).toFloat()
                )
                drawLine(color = Color(0xFF334155), start = start, end = end, strokeWidth = 3.dp.toPx())
            }

            // Minute Hand
            val minRad = Math.toRadians(minutesAngle.toDouble())
            val minEnd = Offset(
                (center.x + (radius * 0.65f) * sin(minRad)).toFloat(),
                (center.y - (radius * 0.65f) * cos(minRad)).toFloat()
            )
            drawLine(color = Color(0xFF0F172A), start = center, end = minEnd, strokeWidth = 5.dp.toPx(), cap = StrokeCap.Round)

            // Second Hand
            val secRad = Math.toRadians(secondsAngle.toDouble())
            val secEnd = Offset(
                (center.x + (radius * 0.8f) * sin(secRad)).toFloat(),
                (center.y - (radius * 0.8f) * cos(secRad)).toFloat()
            )
            drawLine(color = Color(0xFFDC2626), start = center, end = secEnd, strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)

            // Center Pin
            drawCircle(color = Color(0xFF0F172A), radius = 6.dp.toPx(), center = center)
        }

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.Black.copy(alpha = 0.75f),
            modifier = Modifier.padding(top = 110.dp)
        ) {
            Text(
                text = "$minStr:$secStr",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}

// 5. Horizontal Bar Gauge
@Composable
fun BarGaugeTimerView(
    remainingSeconds: Int,
    totalDurationSeconds: Int,
    isRunning: Boolean,
    isPaused: Boolean,
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    val progress = if (totalDurationSeconds > 0) {
        (remainingSeconds.toFloat() / totalDurationSeconds.toFloat()).coerceIn(0f, 1f)
    } else 0f
    val (minStr, secStr) = formatTime(remainingSeconds)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$minStr:$secStr",
            fontSize = 48.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(14.dp))

        // Progress Gauge Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = progress)
                    .height(28.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(primaryColor, Color(0xFF059669), Color(0xFF38BDF8))
                        )
                    )
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("مؤشر الطاقة والتركيز", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${(progress * 100).toInt()}% متبقي", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = primaryColor)
        }
    }
}

// 6. Futuristic Sci-Fi HUD
@Composable
fun SciFiHudTimerView(
    remainingSeconds: Int,
    totalDurationSeconds: Int,
    isRunning: Boolean,
    isPaused: Boolean,
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    val (minStr, secStr) = formatTime(remainingSeconds)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .padding(8.dp),
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFF030712),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF06B6D4))
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                // Tech corners
                drawLine(Color(0xFF06B6D4), Offset(0f, 0f), Offset(30f, 0f), 4f)
                drawLine(Color(0xFF06B6D4), Offset(0f, 0f), Offset(0f, 30f), 4f)
                drawLine(Color(0xFF06B6D4), Offset(w, 0f), Offset(w - 30f, 0f), 4f)
                drawLine(Color(0xFF06B6D4), Offset(w, 0f), Offset(w, 30f), 4f)
                drawLine(Color(0xFF06B6D4), Offset(0f, h), Offset(30f, h), 4f)
                drawLine(Color(0xFF06B6D4), Offset(0f, h), Offset(0f, h - 30f), 4f)
                drawLine(Color(0xFF06B6D4), Offset(w, h), Offset(w - 30f, h), 4f)
                drawLine(Color(0xFF06B6D4), Offset(w, h), Offset(w, h - 30f), 4f)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "STATUS: ${if (isRunning) "ACTIVE_SESSION" else "STANDBY"}",
                    fontSize = 11.sp,
                    color = Color(0xFF06B6D4),
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$minStr:$secStr",
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF22D3EE),
                    letterSpacing = 4.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "CHRONO HUD v4.0 // QUANTUM LOCK",
                    fontSize = 10.sp,
                    color = Color(0xFF64748B),
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

// 7. Minimalist
@Composable
fun MinimalistTimerView(
    remainingSeconds: Int,
    totalDurationSeconds: Int,
    isRunning: Boolean,
    isPaused: Boolean,
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    val (minStr, secStr) = formatTime(remainingSeconds)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$minStr:$secStr",
            fontSize = 58.sp,
            fontWeight = FontWeight.Light,
            color = MaterialTheme.colorScheme.onSurface,
            letterSpacing = 1.sp
        )
        Text(
            text = if (isRunning) "تركيز عميق" else "جاهز",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
