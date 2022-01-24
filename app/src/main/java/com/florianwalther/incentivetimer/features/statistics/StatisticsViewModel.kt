package com.florianwalther.incentivetimer.features.statistics

import androidx.annotation.StringRes
import androidx.lifecycle.*
import com.florianwalther.incentivetimer.R
import com.florianwalther.incentivetimer.core.util.dayAfter
import com.florianwalther.incentivetimer.core.util.withOutTime
import com.florianwalther.incentivetimer.data.db.PomodoroStatisticDao
import com.florianwalther.incentivetimer.features.statistics.model.DailyPomodoroStatistic
import com.florianwalther.incentivetimer.features.statistics.model.StatisticsScreenState
import com.zhuinden.flowcombinetuplekt.combineTuple
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

enum class StatisticsGranularity(@StringRes val readableName: Int, val days: Int?) {
    LAST_7_DAYS(R.string.last_7_days, 7),
    LAST_14_DAYS(R.string.last_14_days, 14),
    LAST_30_DAYS(R.string.last_30_days, 30),
    ALL_TIME(R.string.all_time, null),
}

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val pomodoroStatisticDao: PomodoroStatisticDao,
    savedStateHandle: SavedStateHandle,
) : ViewModel(), StatisticsActions {

    private val statisticsGranularity =
        savedStateHandle.getLiveData<StatisticsGranularity>(
            "statisticsGranularity",
            StatisticsGranularity.LAST_7_DAYS
        )

    private val selectedStatisticsGranularityIndex = statisticsGranularity.map { granularity ->
        StatisticsGranularity.values().indexOf(granularity)
    }

    private val allDailyPomodoroStatistics: Flow<List<DailyPomodoroStatistic>> =
        pomodoroStatisticDao.getAllPomodoroStatistics().map { pomodoroStatistics ->
            pomodoroStatistics
                .groupBy { pomodoroStatistic ->
                    Date(pomodoroStatistic.timestampInMilliseconds).withOutTime()
                }
                .toMutableMap()
                .apply {
                    // generate statistics for zero minute days
                    if (isNotEmpty()) {
                        val firstDate = keys.first()
                        val lastDate = keys.last()
                        var currentDate = firstDate

                        while (currentDate.time < lastDate.time && currentDate.dayAfter().time < lastDate.time) {
                            if (!keys.contains(currentDate.dayAfter())) {
                                this[currentDate.dayAfter()] = emptyList()
                            }
                            currentDate = currentDate.dayAfter()
                        }
                    }
                }
                .toSortedMap()
                .map { (date, pomodoroStatistics) ->
                    DailyPomodoroStatistic(
                        date,
                        pomodoroStatistics.sumOf { it.pomodoroDurationInMinutes }
                    )
                }
        }

    private val dailyPomodoroStatisticsInTimeframe = combineTuple(
        statisticsGranularity.asFlow(),
        allDailyPomodoroStatistics
    ).map { (statisticsGranularity, allDailyStatistics) ->
        statisticsGranularity.days?.let { days ->
            allDailyStatistics.takeLast(days)
        } ?: allDailyStatistics
    }

    private val pomodoroMinutesCompletedInTimeframe: Flow<Int> =
        dailyPomodoroStatisticsInTimeframe.map { dailyPomodoroStatistics ->
            dailyPomodoroStatistics.sumOf { it.totalPomodoroDurationInMinutes }
        }

    private val showResetPomodoroStatisticsDialog =
        savedStateHandle.getLiveData<Boolean>(
            "showResetPomodoroStatisticsDialog",
            false
        )

    val screenState = combineTuple(
        dailyPomodoroStatisticsInTimeframe,
        pomodoroMinutesCompletedInTimeframe,
        selectedStatisticsGranularityIndex.asFlow(),
        showResetPomodoroStatisticsDialog.asFlow(),
    ).map { (
                dailyPomodoroStatistics,
                pomodoroMinutesCompletedInTimeframe,
                selectedStatisticsGranularityIndex,
                showResetPomodoroStatisticsDialog,
            ) ->
        StatisticsScreenState(
            dailyPomodoroStatisticsInTimeframe = dailyPomodoroStatistics,
            pomodoroMinutesCompletedInTimeframe = pomodoroMinutesCompletedInTimeframe,
            selectedStatisticsGranularityIndex = selectedStatisticsGranularityIndex,
            showResetPomodoroStatisticsDialog = showResetPomodoroStatisticsDialog,
        )
    }.asLiveData()

    private val eventChannel = Channel<StatisticsEvent>()
    val events: Flow<StatisticsEvent> = eventChannel.receiveAsFlow()

    sealed class StatisticsEvent {
        object ResetChartZoom : StatisticsEvent()
    }

    override fun onPomodoroMinutesCompletedGranularitySelected(granularity: StatisticsGranularity) {
        statisticsGranularity.value = granularity
        viewModelScope.launch {
            eventChannel.send(StatisticsEvent.ResetChartZoom)
        }
    }

    override fun onResetPomodoroStatisticsClicked() {
        showResetPomodoroStatisticsDialog.value = true
    }

    override fun onResetPomodoroStatisticsConfirmed() {
        showResetPomodoroStatisticsDialog.value = false
        viewModelScope.launch {
            pomodoroStatisticDao.deleteAllPomodoroStatistics()
        }
    }

    override fun onResetPomodoroStatisticsDialogDismissed() {
        showResetPomodoroStatisticsDialog.value = false
    }
}