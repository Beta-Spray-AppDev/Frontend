package com.example.sprayconnectapp.ui.screens.BoulderView

import com.example.sprayconnectapp.data.dto.BoulderDTO
import com.example.sprayconnectapp.data.dto.Hold
import com.example.sprayconnectapp.data.dto.HoldType

/** UI-Zustand für den Create/Edit-Screen eines Boulders. */
data class CreateBoulderUiState(
    val selectedType: HoldType = HoldType.START,
    val holds: List<Hold> = emptyList(),
    val selectedHoldId: String? = null,
    val boulder: BoulderDTO? = null

)