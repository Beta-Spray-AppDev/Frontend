package com.example.sprayconnectapp.data.dto

import androidx.compose.ui.graphics.Color

enum class HoldType(val displayName: String, val color: Color) {

    START("Start", Color(0xFF00CED1)),

    GRIFF("Griff", Color(0xFFFF00FF)),
    TRITT("Tritt", Color(0xFFFFFF00)),


    TOP("Top", Color(0xFF39FF14))
}
