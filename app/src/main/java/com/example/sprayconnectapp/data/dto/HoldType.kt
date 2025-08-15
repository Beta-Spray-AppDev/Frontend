package com.example.sprayconnectapp.data.dto

import androidx.compose.ui.graphics.Color

enum class HoldType(val displayName: String, val color: Color) {
    TRITT("Tritt", Color(0xFFFFFF00)),
    GRIFF("Griff", Color(0xFFFF00FF)),
    START("Start", Color(0xFF00CED1)),
    TOP("Top", Color(0xFF39FF14))
}
