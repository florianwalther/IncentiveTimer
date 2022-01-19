package com.florianwalther.incentivetimer.features.statistics.model

import java.util.*

data class DailyPomodoroStatistic(
    val dateWithoutTime: Date,
    val totalPomodoroDurationInMinutes: Int,
)