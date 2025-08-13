package com.example.sprayconnectapp.ui.screens.BoulderView

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.sprayconnectapp.data.dto.HoldType
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.PaintingStyle.Companion.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource


@Composable
fun HoldTypePicker(
    types: List<HoldType>,
    selected: HoldType,
    onSelect: (HoldType) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier
            .fillMaxWidth()
            .padding(12.dp)
            .background(Color(0xAA101010), RoundedCornerShape(20.dp))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
            .padding(vertical = 10.dp)
    ) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(types) { t ->
                TypeChip(
                    type = t,
                    selected = t == selected,
                    onClick = { onSelect(t) }
                )
            }
        }
    }
}


@Composable
fun TypeChip(
    type: HoldType,
    selected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(if (selected) 1f else 0.94f, label = "chipScale")
    val ringAlpha by animateFloatAsState(if (selected) 1f else 0f, label = "ringAlpha")
    val ringWidth by animateDpAsState(if (selected) 4.dp else 2.dp, label = "ringWidth")

    val haptics = LocalHapticFeedback.current
    fun pulse() = haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .widthIn(min = 64.dp)
            .clickable { onClick(); pulse() }
    ) {
        Box(
            modifier = Modifier
                .size(if (selected) 70.dp else 65.dp)
                .graphicsLayer { scaleX = scale; scaleY = scale }
        ) {
            // Auswahlring
            Canvas(Modifier.matchParentSize()) {
                drawArc(
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = ringWidth.toPx(), cap = StrokeCap.Round),
                    color = type.color.copy(alpha = 0.9f * ringAlpha)
                )
            }
            // Kreisfläche
            Box(
                Modifier
                    .padding(2.dp)
                    .fillMaxSize()
                    .background(if (selected) Color(0xFF2B2B2B) else Color(0xFF323232), CircleShape)
                    .border(1.dp, Color.White.copy(alpha = if (selected) 0.20f else 0.12f), CircleShape)
                    .shadow(if (selected) 10.dp else 3.dp, CircleShape, clip = false),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = type.iconRes()),
                    contentDescription = type.displayName,
                    modifier = Modifier.fillMaxSize(0.94f),
                    colorFilter = ColorFilter.tint(Color.White.copy(alpha = if (selected) 1f else 0.9f))
                )
            }
        }

        Spacer(Modifier.height(6.dp))
        Text(
            text = type.displayName,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = if (selected) 1f else 0.75f),
            maxLines = 1
        )
    }
}



