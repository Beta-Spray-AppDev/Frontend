package com.example.sprayconnectapp.ui.screens.BoulderView

import com.example.sprayconnectapp.data.dto.Hold
import com.example.sprayconnectapp.data.dto.HoldType

data class CreateBoulderUiState(
    val selectedType: HoldType = HoldType.RED,
    val holds: List<Hold> = emptyList(),
    val spraywallUrl: String = "",
    val selectedHoldId: String? = null

)