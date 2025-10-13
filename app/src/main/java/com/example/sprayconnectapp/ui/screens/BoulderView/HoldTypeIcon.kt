package com.example.sprayconnectapp.ui.screens.BoulderView

import androidx.annotation.DrawableRes
import com.example.sprayconnectapp.R
import com.example.sprayconnectapp.data.dto.HoldType

@DrawableRes
fun HoldType.iconRes(): Int = when (this) {
    HoldType.TRITT -> R.drawable.tritt
    HoldType.GRIFF -> R.drawable.griff
    HoldType.START -> R.drawable.logoalpha
    HoldType.TOP   -> R.drawable.top
}
