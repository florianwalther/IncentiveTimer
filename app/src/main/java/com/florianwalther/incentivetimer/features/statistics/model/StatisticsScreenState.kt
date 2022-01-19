package com.florianwalther.incentivetimer.features.statistics.model

data class StatisticsScreenState(
    val dailyPomodoroStatisticsInTimeframe: List<DailyPomodoroStatistic>,
    val pomodoroMinutesCompletedInTimeframe: Int,
    val selectedStatisticsGranularityIndex: Int,
    val showResetPomodoroStatisticsDialog: Boolean,
) {
    companion object {
        val initialState = StatisticsScreenState(
            dailyPomodoroStatisticsInTimeframe = emptyList(),
            pomodoroMinutesCompletedInTimeframe = 0,
            selectedStatisticsGranularityIndex = 0,
            showResetPomodoroStatisticsDialog = false,
        )
    }
}