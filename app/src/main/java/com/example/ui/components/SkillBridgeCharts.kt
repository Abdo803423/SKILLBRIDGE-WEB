package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.AppLanguage
import com.example.core.LocalAppLanguage
import com.example.ui.theme.SkillBridgeColors
import com.example.ui.theme.SkillBridgeRadius
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

data class ChartDataPoint(
    val label: String,
    val value: Float,
    val color: Color = SkillBridgeColors.BridgeBlue
)

@Composable
fun SaasKpiTile(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color = SkillBridgeColors.BridgeBlueDark,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(SkillBridgeRadius.md),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = accentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun SkillBridgeLineChart(
    title: String,
    subtitle: String,
    points: List<ChartDataPoint>,
    lineColor: Color = SkillBridgeColors.BridgeBlue,
    modifier: Modifier = Modifier
) {
    val lang = LocalAppLanguage.current
    var selectedIdx by remember(points) { mutableIntStateOf(points.lastIndex.coerceAtLeast(0)) }
    val anim by animateFloatAsState(targetValue = 1f, animationSpec = tween(550), label = "line_chart")

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SkillBridgeRadius.md),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (points.isNotEmpty() && selectedIdx in points.indices) {
                    val active = points[selectedIdx]
                    Surface(
                        shape = RoundedCornerShape(SkillBridgeRadius.pill),
                        color = lineColor.copy(alpha = 0.14f)
                    ) {
                        Text(
                            text = "${active.label}: ${active.value.roundToInt()}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SkillBridgeColors.BridgeBlueDark,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            if (points.isEmpty() || points.all { it.value == 0f }) {
                ChartEmptyPlaceholder(
                    message = if (lang == AppLanguage.AR) "لا توجد بيانات كافية للعرض بعد" else "No activity records in this range yet"
                )
            } else {
                val maxVal = max(1f, points.maxOf { it.value } * 1.15f)
                val gridColor = MaterialTheme.colorScheme.outlineVariant

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(155.dp)
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(points) {
                                detectTapGestures { tap ->
                                    if (points.size > 1) {
                                        val stepX = size.width.toFloat() / (points.size - 1)
                                        selectedIdx = (tap.x / stepX).roundToInt().coerceIn(0, points.lastIndex)
                                    }
                                }
                            }
                    ) {
                        val w = size.width
                        val h = size.height
                        val padV = 12.dp.toPx()
                        val plotH = (h - padV * 2).coerceAtLeast(1f)
                        val stepX = if (points.size > 1) w / (points.size - 1) else w

                        for (i in 0..3) {
                            val y = padV + plotH * (i / 3f)
                            drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 1.dp.toPx())
                        }

                        val coords = points.mapIndexed { idx, pt ->
                            val x = if (points.size > 1) idx * stepX else w / 2f
                            val y = padV + plotH * (1f - (pt.value / maxVal) * anim)
                            Offset(x, y)
                        }

                        val linePath = Path()
                        val areaPath = Path()
                        coords.forEachIndexed { idx, pt ->
                            if (idx == 0) {
                                linePath.moveTo(pt.x, pt.y)
                                areaPath.moveTo(pt.x, h - padV)
                                areaPath.lineTo(pt.x, pt.y)
                            } else {
                                linePath.lineTo(pt.x, pt.y)
                                areaPath.lineTo(pt.x, pt.y)
                            }
                        }
                        if (coords.isNotEmpty()) {
                            areaPath.lineTo(coords.last().x, h - padV)
                            areaPath.close()
                            drawPath(
                                areaPath,
                                brush = Brush.verticalGradient(
                                    listOf(lineColor.copy(alpha = 0.28f), lineColor.copy(alpha = 0.02f))
                                )
                            )
                            drawPath(
                                linePath,
                                color = lineColor,
                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                        }

                        coords.forEachIndexed { idx, pt ->
                            val isSelected = idx == selectedIdx
                            drawCircle(
                                color = if (isSelected) SkillBridgeColors.DeepNavy else lineColor,
                                radius = if (isSelected) 6.dp.toPx() else 4.dp.toPx(),
                                center = pt
                            )
                            if (isSelected) {
                                drawCircle(
                                    color = SkillBridgeColors.LimeGreen,
                                    radius = 3.dp.toPx(),
                                    center = pt
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    points.forEachIndexed { idx, pt ->
                        Text(
                            text = pt.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (idx == selectedIdx) FontWeight.Bold else FontWeight.Normal,
                            color = if (idx == selectedIdx) SkillBridgeColors.BridgeBlueDark else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.clickable { selectedIdx = idx }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SkillBridgeBarChart(
    title: String,
    subtitle: String,
    bars: List<ChartDataPoint>,
    modifier: Modifier = Modifier
) {
    val lang = LocalAppLanguage.current
    var selectedIdx by remember(bars) { mutableIntStateOf(0) }
    val anim by animateFloatAsState(targetValue = 1f, animationSpec = tween(550), label = "bar_chart")

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SkillBridgeRadius.md),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (bars.isNotEmpty() && selectedIdx in bars.indices) {
                    val active = bars[selectedIdx]
                    Surface(
                        shape = RoundedCornerShape(SkillBridgeRadius.pill),
                        color = active.color.copy(alpha = 0.14f)
                    ) {
                        Text(
                            text = "${active.label}: ${active.value.roundToInt()}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            if (bars.isEmpty() || bars.all { it.value <= 0f }) {
                ChartEmptyPlaceholder(
                    message = if (lang == AppLanguage.AR) "لا توجد سجلات رقمية بعد" else "No database records to chart yet"
                )
            } else {
                val maxVal = max(1f, bars.maxOf { it.value } * 1.15f)
                val gridColor = MaterialTheme.colorScheme.outlineVariant

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(155.dp)
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(bars) {
                                detectTapGestures { tap ->
                                    val slotW = size.width.toFloat() / bars.size.coerceAtLeast(1)
                                    selectedIdx = (tap.x / slotW).toInt().coerceIn(0, bars.lastIndex)
                                }
                            }
                    ) {
                        val w = size.width
                        val h = size.height
                        val padBottom = 8.dp.toPx()
                        val plotH = (h - padBottom * 2).coerceAtLeast(1f)

                        for (i in 0..2) {
                            val y = padBottom + plotH * (i / 2f)
                            drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 1.dp.toPx())
                        }

                        val slotW = w / bars.size
                        val barW = (slotW * 0.54f).coerceAtMost(42.dp.toPx())
                        bars.forEachIndexed { idx, bar ->
                            val ratio = (bar.value / maxVal).coerceIn(0f, 1f) * anim
                            val barH = max(4.dp.toPx(), plotH * ratio)
                            val left = idx * slotW + (slotW - barW) / 2f
                            val top = padBottom + plotH - barH
                            val alpha = if (idx == selectedIdx) 1f else 0.78f
                            drawRoundRect(
                                color = bar.color.copy(alpha = alpha),
                                topLeft = Offset(left, top),
                                size = Size(barW, barH),
                                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    bars.forEachIndexed { idx, bar ->
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedIdx = idx },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${bar.value.roundToInt()}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (idx == selectedIdx) bar.color else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = bar.label,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SkillBridgeDoughnutChart(
    title: String,
    subtitle: String,
    centerLabel: String,
    segments: List<ChartDataPoint>,
    modifier: Modifier = Modifier
) {
    val lang = LocalAppLanguage.current
    val nonZero = segments.filter { it.value > 0f }
    val total = nonZero.sumOf { it.value.toDouble() }.toFloat()
    var selectedIdx by remember(segments) { mutableIntStateOf(0) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SkillBridgeRadius.md),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (total <= 0f) {
                ChartEmptyPlaceholder(
                    message = if (lang == AppLanguage.AR) "لا توجد بيانات توزيع بعد" else "No distribution records yet"
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier.size(124.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(nonZero) {
                                    detectTapGestures { tap ->
                                        val cx = size.width / 2f
                                        val cy = size.height / 2f
                                        val dx = tap.x - cx
                                        val dy = tap.y - cy
                                        if (hypot(dx, dy) > min(size.width, size.height) * 0.18f) {
                                            var deg = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat() + 90f
                                            if (deg < 0f) deg += 360f
                                            var acc = 0f
                                            nonZero.forEachIndexed { idx, seg ->
                                                val sweep = (seg.value / total) * 360f
                                                if (deg in acc..(acc + sweep)) {
                                                    selectedIdx = idx
                                                }
                                                acc += sweep
                                            }
                                        }
                                    }
                                }
                        ) {
                            val strokeW = 18.dp.toPx()
                            var startAngle = -90f
                            nonZero.forEachIndexed { idx, seg ->
                                val sweep = (seg.value / total) * 360f
                                drawArc(
                                    color = seg.color,
                                    startAngle = startAngle,
                                    sweepAngle = (sweep - 2.5f).coerceAtLeast(1f),
                                    useCenter = false,
                                    style = Stroke(
                                        width = if (idx == selectedIdx) strokeW + 3.dp.toPx() else strokeW,
                                        cap = StrokeCap.Butt
                                    )
                                )
                                startAngle += sweep
                            }
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val active = nonZero.getOrNull(selectedIdx)
                            Text(
                                text = "${active?.value?.roundToInt() ?: total.roundToInt()}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = active?.label ?: centerLabel,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    FlowRow(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        nonZero.forEachIndexed { idx, seg ->
                            val pct = ((seg.value / total) * 100f).roundToInt()
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedIdx = idx }
                                    .padding(horizontal = 4.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(seg.color)
                                )
                                Text(
                                    text = "${seg.label} (${seg.value.roundToInt()} • $pct%)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (idx == selectedIdx) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChartEmptyPlaceholder(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(92.dp)
            .clip(RoundedCornerShape(SkillBridgeRadius.sm))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.BarChart,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
