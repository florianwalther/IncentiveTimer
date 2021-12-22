package com.florianwalther.incentivetimer.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.florianwalther.incentivetimer.core.ui.IconKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "rewards")
@Parcelize
data class Reward(
    val name: String,
    val chanceInPercent: Int,
    val iconKey: IconKey,
    val isUnlocked: Boolean = false,
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
) : Parcelable