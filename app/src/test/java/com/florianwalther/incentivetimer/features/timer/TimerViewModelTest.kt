package com.florianwalther.incentivetimer.features.timer

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.florianwalther.incentivetimer.core.notification.NotificationHelper
import com.florianwalther.incentivetimer.core.util.minutesToMilliseconds
import com.florianwalther.incentivetimer.data.FakeRewardDao
import com.florianwalther.incentivetimer.features.rewards.RewardUnlockManager
import com.florianwalther.incentivetimer.getOrAwaitValue

import org.junit.After
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class TimerViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testScope = TestScope()

    @MockK
    private lateinit var notificationHelper: NotificationHelper

    private lateinit var timerServiceManager: TimerServiceManager

    private lateinit var fakeTimeSource: FakeTimeSource

    private lateinit var viewModel: TimerViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        Dispatchers.setMain(UnconfinedTestDispatcher(testScope.testScheduler))
        fakeTimeSource = FakeTimeSource()
        viewModel = TimerViewModel(
            pomodoroTimerManager = PomodoroTimerManager(
                timer = CountDownTimer(testScope, fakeTimeSource),
                timerServiceManager = timerServiceManager,
                notificationHelper = notificationHelper,
                rewardUnlockManager = RewardUnlockManager(
                    rewardDao = FakeRewardDao(),
                    applicationScope = testScope,
                    notificationHelper = notificationHelper
                ),
                applicationScope = testScope,
            ),
            savedStateHandle = SavedStateHandle()
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun pomodoroTimerState_hasCorrectDefaultValues() {
        assertThat(viewModel.pomodoroTimerState.getOrAwaitValue()).isEqualTo(
            defaultTimerState
        )
    }

    @Test
    fun showResetTimerConfirmationDialog_defaultValueFalse() {
        assertThat(viewModel.screenState.getOrAwaitValue().showResetTimerConfirmationDialog).isFalse()
    }

    @Test
    fun showSkipBreakConfirmationDialog_defaultValueFalse() {
        assertThat(viewModel.screenState.getOrAwaitValue().showSkipBreakConfirmationDialog).isFalse()
    }

    @Test
    fun showResetPomodoroSetConfirmationDialog_defaultValueFalse() {
        assertThat(viewModel.screenState.getOrAwaitValue().showResetPomodoroSetConfirmationDialog).isFalse()
    }

    @Test
    fun showResetPomodoroCountConfirmationDialog_defaultValueFalse() {
        assertThat(viewModel.screenState.getOrAwaitValue().showResetPomodoroCountConfirmationDialog).isFalse()
    }

    @Test
    fun onStartStopTimerClicked_callsRemoveTimerCompletedNotification() {
        viewModel.onStartStopTimerClicked()

        // TODO: Should we use a fake with state instead of a mock?
        verify { notificationHelper.removeTimerCompletedNotification() }
    }

    @Test
    fun onStartStopTimerClicked_timerNotRunning_callsRemoveResumeTimerNotification() {
        viewModel.onStartStopTimerClicked()

        verify { notificationHelper.removeResumeTimerNotification() }
    }

    @Test
    fun onStartStopTimerClicked_timerNotRunning_setsTimerRunningTrue() {
        viewModel.onStartStopTimerClicked()

        assertThat(viewModel.pomodoroTimerState.getOrAwaitValue().timerRunning).isTrue()
    }

    @Test
    fun onStartStopTimerClicked_timerRunning_setsTimerRunningFalse() {
        viewModel.onStartStopTimerClicked()
        viewModel.onStartStopTimerClicked()

        assertThat(viewModel.pomodoroTimerState.getOrAwaitValue().timerRunning).isFalse()
    }

    @Test
    fun onStartStopTimerClicked_timerNotRunningAndPomodoroTargetReached_resetsPomodoroCounter() =
        testScope.runTest {
            viewModel.onStartStopTimerClicked()

            repeat(3) {
               advanceTimeBy(25.minutesToMilliseconds())
               fakeTimeSource.advanceTimeBy(25.minutesToMilliseconds())
                runCurrent()
                advanceTimeBy(5.minutesToMilliseconds())
                fakeTimeSource.advanceTimeBy(5.minutesToMilliseconds())
                runCurrent()
            }
            testScope.advanceTimeBy(25.minutesToMilliseconds())
            fakeTimeSource.advanceTimeBy(25.minutesToMilliseconds())
            runCurrent()
            testScope.advanceTimeBy(15.minutesToMilliseconds())
            fakeTimeSource.advanceTimeBy(15.minutesToMilliseconds())
            runCurrent()

            viewModel.onStartStopTimerClicked()

            assertThat(viewModel.pomodoroTimerState.getOrAwaitValue().pomodorosCompletedInSet).isEqualTo(
                0
            )
        }

    companion object {
        private val defaultTimerState = PomodoroTimerState(
            timerRunning = false,
            currentPhase = PomodoroPhase.POMODORO,
            timeLeftInMillis = 25 * 60 * 1_000L,
            timeTargetInMillis = 25 * 60 * 1_000L,
            pomodorosCompletedInSet = 0,
            pomodorosPerSetTarget = 4,
            pomodorosCompletedTotal = 0,
        )
    }
}