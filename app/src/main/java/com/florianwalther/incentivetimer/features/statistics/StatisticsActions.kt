package com.florianwalther.incentivetimer.features.statistics

interface StatisticsActions {

    fun onPomodoroMinutesCompletedGranularitySelected(granularity: StatisticsGranularity)
    fun onResetPomodoroStatisticsClicked()
    fun onResetPomodoroStatisticsConfirmed()
    fun onResetPomodoroStatisticsDialogDismissed()
}