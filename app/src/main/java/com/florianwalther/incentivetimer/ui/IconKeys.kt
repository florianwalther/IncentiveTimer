package com.florianwalther.incentivetimer.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bathtub
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.ui.graphics.vector.ImageVector

object IconKeys {
    const val CAKE = "CAKE"
    const val BATH_TUB = "BATH_TUB"
    const val TV = "TV"
}

val rewardIcons = mapOf<String, ImageVector>(
    Pair(IconKeys.CAKE, Icons.Default.Cake),
    Pair(IconKeys.BATH_TUB, Icons.Default.Bathtub),
    Pair(IconKeys.TV, Icons.Default.Tv)
)

val defaultIcon = Icons.Default.Star