package com.example.sprayconnectapp.data.dto

import androidx.compose.ui.graphics.Color

enum class HoldType(val displayName: String, val color: Color) {
    TRITT("Tritt", Color(0xFFFFFFFF)),
    GRIFF("Griff", Color(0xFFFFD600)),
    START("Start", Color(0xFF50C2EF)),
    TOP("Top", Color(0xFFF54242))
}
