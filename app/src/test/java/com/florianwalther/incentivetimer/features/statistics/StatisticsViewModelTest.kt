package com.florianwalther.incentivetimer.features.statistics

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.florianwalther.incentivetimer.core.util.dayAfter
import com.florianwalther.incentivetimer.core.util.withOutTime
import com.florianwalther.incentivetimer.data.db.FakePomodoroStatisticDao
import com.florianwalther.incentivetimer.data.db.PomodoroStatistic
import com.florianwalther.incentivetimer.features.statistics.model.DailyPomodoroStatistic
import com.florianwalther.incentivetimer.getOrAwaitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.*

class StatisticsViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val timestamp1 = 1644583178942L // Fri Feb 11 13:39:38 GMT+01:00 2022
    private val timestamp1WithoutTime = 1644534000000

    private val timestamp2 = 1644583242036L // Fri Feb 11 13:40:42 GMT+01:00 2022
    private val timestamp2WithoutTime = 1644534000000

    private val timestamp3 = 1644669711155L // Sat Feb 12 13:41:51 GMT+01:00 2022
    private val timestamp3WithoutTime = 1644620400000

    private val timestamp4 = 1644756177113L // Sun Feb 13 13:42:57 GMT+01:00 2022
    private val timestamp4WithoutTime = 1644706800000

    private val timestamp5 = 1645016667728L // Wed Feb 16 14:04:27 GMT+01:00 2022
    private val timestamp5WithoutTime = 1644966000000L

    private val emptyDay1TimestampWithoutTime = 1644793200000L // Mon Feb 14 00:00:00 GMT+01:00 2022
    private val emptyDay2TimestampWithoutTime = 1644879600000L // Tue Feb 15 00:00:00 GMT+01:00 202

    private val pomodoroStatistic1 = PomodoroStatistic(
        pomodoroDurationInMinutes = 1,
        timestampInMilliseconds = timestamp1,
        id = 1L,
    )

    private val pomodoroStatistic2 = PomodoroStatistic(
        pomodoroDurationInMinutes = 2,
        timestampInMilliseconds = timestamp2,
        id = 2L,
    )

    private val pomodoroStatistic3 = PomodoroStatistic(
        pomodoroDurationInMinutes = 1,
        timestampInMilliseconds = timestamp3,
        id = 3L,
    )

    private val pomodoroStatistic4 = PomodoroStatistic(
        pomodoroDurationInMinutes = 2,
        timestampInMilliseconds = timestamp4,
        id = 4L,
    )

    private val pomodoroStatistic5 = PomodoroStatistic(
        pomodoroDurationInMinutes = 5,
        timestampInMilliseconds = timestamp5,
        id = 5L
    )

    private val data = linkedMapOf<Long, PomodoroStatistic>(
        pomodoroStatistic1.id to pomodoroStatistic1,
        pomodoroStatistic2.id to pomodoroStatistic2,
        pomodoroStatistic3.id to pomodoroStatistic3,
        pomodoroStatistic4.id to pomodoroStatistic4,
        pomodoroStatistic5.id to pomodoroStatistic5,
    )

    private lateinit var statisticsViewModel: StatisticsViewModel
    private val fakePomodoroStatisticDao = FakePomodoroStatisticDao(data)

    private suspend fun insertDummyStatistics(days: Int): List<DailyPomodoroStatistic> {
        var date = Date(1644583178942)
        repeat(days) { index ->
            fakePomodoroStatisticDao.insertPomodoroStatistic(
                PomodoroStatistic(
                    pomodoroDurationInMinutes = index + 1,
                    timestampInMilliseconds = date.time,
                    id = index.toLong() + 1L
                )
            )
            date = date.dayAfter()
        }
        val allStatistics = fakePomodoroStatisticDao.getAllPomodoroStatistics().first()
        return allStatistics.map { pomodoroStatistic ->
            DailyPomodoroStatistic(
                dateWithoutTime = Date(pomodoroStatistic.timestampInMilliseconds).withOutTime(),
                totalPomodoroDurationInMinutes = pomodoroStatistic.pomodoroDurationInMinutes,
            )
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        statisticsViewModel = StatisticsViewModel(
            pomodoroStatisticDao = fakePomodoroStatisticDao,
            savedStateHandle = SavedStateHandle(),
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun selectedStatisticsGranularityIndex_correctValue() {
        StatisticsGranularity.values().forEach { granularity ->
            statisticsViewModel.onPomodoroMinutesCompletedGranularitySelected(granularity)
            val index =
                statisticsViewModel.screenState.getOrAwaitValue().selectedStatisticsGranularityIndex
            assertThat(index).isEqualTo(StatisticsGranularity.values().indexOf(granularity))
        }
    }

    @Test
    fun dailyPomodoroStatistics_generatedCorrectly() {
        val dailyPomodoroStatistics =
            statisticsViewModel.screenState.getOrAwaitValue().dailyPomodoroStatisticsInTimeframe

        assertThat(dailyPomodoroStatistics).containsExactly(
            DailyPomodoroStatistic(
                Date(timestamp1WithoutTime), 3
            ),
            DailyPomodoroStatistic(
                Date(timestamp3WithoutTime), 1
            ),
            DailyPomodoroStatistic(
                Date(timestamp4WithoutTime), 2
            ),
            DailyPomodoroStatistic(
                Date(emptyDay1TimestampWithoutTime), 0
            ),
            DailyPomodoroStatistic(
                Date(emptyDay2TimestampWithoutTime), 0
            ),
            DailyPomodoroStatistic(
                Date(timestamp5WithoutTime), 5
            )
        ).inOrder()
    }

    @Test
    fun dailyPomodoroStatistics_defaultGranularity7Days() = runTest {
        fakePomodoroStatisticDao.deleteAllPomodoroStatistics()
        val allDailyStatistics = insertDummyStatistics(10)

        val pomodoroStatistics =
            statisticsViewModel.screenState.getOrAwaitValue().dailyPomodoroStatisticsInTimeframe

        assertThat(pomodoroStatistics).isEqualTo(allDailyStatistics.takeLast(7))
    }

    @Test
    fun onPomodoroMinutesCompletedGranularitySelected_dailyPomodoroStatisticsInTimeframeFilteredCorrectly() =
        runTest {
            fakePomodoroStatisticDao.deleteAllPomodoroStatistics()
            val allDailyStatistics = insertDummyStatistics(35)

            StatisticsGranularity.values().forEach { granularity ->
                statisticsViewModel.onPomodoroMinutesCompletedGranularitySelected(granularity)

                val pomodoroStatistics =
                    statisticsViewModel.screenState.getOrAwaitValue().dailyPomodoroStatisticsInTimeframe

                val days = granularity.days
                if (days != null) {
                    assertThat(pomodoroStatistics).isEqualTo(allDailyStatistics.takeLast(days))
                } else {
                    assertThat(pomodoroStatistics).isEqualTo(allDailyStatistics)
                }
            }
        }

    @Test
    fun onPomodoroMinutesCompletedGranularitySelected_sendsResetChartZoomEvent() = runTest {
        statisticsViewModel.onPomodoroMinutesCompletedGranularitySelected(StatisticsGranularity.ALL_TIME)

        statisticsViewModel.events.test {
            assertThat(awaitItem()).isEqualTo(
                StatisticsViewModel.StatisticsEvent.ResetChartZoom
            )
        }
    }

    @Test
    fun pomodoroMinutesCompletedInTimeframe_correctValue() {
        val minutes =
            statisticsViewModel.screenState.getOrAwaitValue().pomodoroMinutesCompletedInTimeframe

        assertThat(minutes).isEqualTo(11)
    }

    @Test
    fun showResetPomodoroStatisticsDialog_defaultValueFalse() {
        assertThat(statisticsViewModel.screenState.getOrAwaitValue().showResetPomodoroStatisticsDialog).isFalse()
    }

    @Test
    fun onResetPomodoroStatisticsClicked_showsResetPomodoroStatisticsDialog() {
        statisticsViewModel.onResetPomodoroStatisticsClicked()

        assertThat(statisticsViewModel.screenState.getOrAwaitValue().showResetPomodoroStatisticsDialog).isTrue()
    }

    @Test
    fun onResetPomodoroStatisticsConfirmed_hidesResetPomodoroStatisticsDialog() {
        statisticsViewModel.onResetPomodoroStatisticsClicked()
        statisticsViewModel.onResetPomodoroStatisticsConfirmed()

        assertThat(statisticsViewModel.screenState.getOrAwaitValue().showResetPomodoroStatisticsDialog).isFalse()
    }

    @Test
    fun onResetPomodoroStatisticsConfirmed_deletesAllPomodoroStatistics() {
        statisticsViewModel.onPomodoroMinutesCompletedGranularitySelected(StatisticsGranularity.ALL_TIME)
        statisticsViewModel.onResetPomodoroStatisticsClicked()
        statisticsViewModel.onResetPomodoroStatisticsConfirmed()

        assertThat(statisticsViewModel.screenState.getOrAwaitValue().dailyPomodoroStatisticsInTimeframe).isEmpty()
    }

    @Test
    fun onResetPomodoroStatisticsDialogDismissed_hidesResetPomodoroStatisticsDialog() {
        statisticsViewModel.onResetPomodoroStatisticsClicked()
        statisticsViewModel.onResetPomodoroStatisticsDialogDismissed()

        assertThat(statisticsViewModel.screenState.getOrAwaitValue().showResetPomodoroStatisticsDialog).isFalse()
    }
}