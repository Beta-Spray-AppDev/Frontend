package com.example.sprayconnectapp.ui.screens.BoulderView

import com.example.sprayconnectapp.data.dto.BoulderDTO
import com.example.sprayconnectapp.data.dto.Hold
import com.example.sprayconnectapp.data.dto.HoldType

data class CreateBoulderUiState(
    val selectedType: HoldType = HoldType.GRIFF,
    val holds: List<Hold> = emptyList(),
    val selectedHoldId: String? = null,
    val boulder: BoulderDTO? = null

)