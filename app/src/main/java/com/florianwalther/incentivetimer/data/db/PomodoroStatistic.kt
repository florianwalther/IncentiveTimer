package com.florianwalther.incentivetimer.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pomodoro_statistics")
data class PomodoroStatistic(
    val pomodoroDurationInMinutes: Int,
    val timestampInMilliseconds: Long,
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
)