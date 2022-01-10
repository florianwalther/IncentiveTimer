package com.florianwalther.incentivetimer.features.timer

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.florianwalther.incentivetimer.core.notification.NotificationHelper
import com.florianwalther.incentivetimer.core.util.minutesToMilliseconds
import com.florianwalther.incentivetimer.data.FakeRewardDao
import com.florianwalther.incentivetimer.data.preferences.FakePreferencesManager
import com.florianwalther.incentivetimer.features.rewards.RewardUnlockManager
import com.florianwalther.incentivetimer.getOrAwaitValue

import org.junit.After
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.*
import logcat.logcat
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class TimerViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testScope = TestScope()

    @MockK
    private lateinit var notificationHelper: NotificationHelper

    private lateinit var fakeTimerServiceManager: FakeTimerServiceManager

    private lateinit var fakeTimeSource: FakeTimeSource

    private lateinit var viewModel: TimerViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        Dispatchers.setMain(UnconfinedTestDispatcher(testScope.testScheduler))
        fakeTimerServiceManager = FakeTimerServiceManager()
        fakeTimeSource = FakeTimeSource()
        viewModel = TimerViewModel(
            pomodoroTimerManager = PomodoroTimerManager(
                timer = CountDownTimer(testScope, fakeTimeSource),
                timerServiceManager = fakeTimerServiceManager,
                notificationHelper = notificationHelper,
                rewardUnlockManager = RewardUnlockManager(
                    rewardDao = FakeRewardDao(),
                    applicationScope = testScope,
                    notificationHelper = notificationHelper
                ),
                applicationScope = testScope,
                preferencesManager = FakePreferencesManager(
                    initialPomodoroLengthInMinutes = 25,
                    initialShortBreakLengthInMinutes = 5,
                    initialLongBreakLengthInMinutes = 15,
                    initialPomodorosPerSet = 4,
                )
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
    fun startTimer_startsTimerService() {
        viewModel.onStartStopTimerClicked()

        assertThat(fakeTimerServiceManager.serviceRunning).isTrue()
    }

    @Test
    fun stopTimer_stopsTimerService() {
        viewModel.onStartStopTimerClicked()
        viewModel.onStartStopTimerClicked()

        assertThat(fakeTimerServiceManager.serviceRunning).isFalse()
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
        // TODO: 10/01/2022 This test gets stuck because we collect the timerPreferences Flow in
        //  the init block of PomodoroTimerManager. Other tests with (viewModelScope).launch do
        //  not get stuck tho. -> Find out why and what to do
        testScope.runTest {
            /*viewModel.onStartStopTimerClicked()

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
            )*/
        }

    companion object {
        private val defaultTimerState = PomodoroTimerState(
            timerRunning = false,
            currentPhase = PomodoroPhase.POMODORO,
            timeLeftInMillis = 25.minutesToMilliseconds(),
            timeTargetInMillis = 25.minutesToMilliseconds(),
            pomodorosCompletedInSet = 0,
            pomodorosPerSetTarget = 4,
            pomodorosCompletedTotal = 0,
        )
    }
}