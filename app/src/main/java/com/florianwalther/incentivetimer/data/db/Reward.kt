package com.florianwalther.incentivetimer.data.db

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.florianwalther.incentivetimer.core.ui.IconKey
import com.florianwalther.incentivetimer.core.ui.defaultRewardIconKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "rewards")
@Parcelize
data class Reward(
    val name: String,
    val chanceInPercent: Int,
    val iconKey: IconKey,
    val isUnlocked: Boolean = false,
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
) : Parcelable {

    companion object {
        val DEFAULT = Reward(
            name = "",
            chanceInPercent = 10,
            iconKey = defaultRewardIconKey
        )
    }
}