package com.example.sprayconnectapp.data.dto

import androidx.compose.ui.graphics.Color

enum class HoldType(val displayName: String, val color: Color) {
    TRITT("Tritt", Color(0xFF26A69A)),
    GRIFF("Griff", Color(0xFFFFD600)),
    START("Start", Color(0xFFEF5350)),
    TOP("Top", Color(0xFF42A5F5))
}
