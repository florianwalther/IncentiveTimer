package com.florianwalther.incentivetimer.features.timer

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.florianwalther.incentivetimer.core.notification.FakeNotificationHelper
import com.florianwalther.incentivetimer.core.notification.ResumeTimerNotificationState
import com.florianwalther.incentivetimer.core.notification.TimerCompletedNotificationState
import com.florianwalther.incentivetimer.core.util.minutesToMilliseconds
import com.florianwalther.incentivetimer.data.datastore.FakePomodoroTimerStateManager
import com.florianwalther.incentivetimer.data.datastore.FakePreferencesManager
import com.florianwalther.incentivetimer.data.datastore.PomodoroPhase
import com.florianwalther.incentivetimer.data.datastore.PomodoroTimerState
import com.florianwalther.incentivetimer.data.db.FakePomodoroStatisticDao
import com.florianwalther.incentivetimer.data.db.FakeRewardDao
import com.florianwalther.incentivetimer.features.rewards.RewardUnlockManager
import com.florianwalther.incentivetimer.getOrAwaitValue
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class TimerViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testScope = TestScope()

    private lateinit var fakeNotificationHelper: FakeNotificationHelper

    private lateinit var fakePreferencesManager: FakePreferencesManager

    private lateinit var fakePomodoroTimerStateManager: FakePomodoroTimerStateManager

    private lateinit var fakeTimerServiceManager: FakeTimerServiceManager

    private lateinit var fakeTimeSource: FakeTimeSource

    private lateinit var countDownTimer: CountDownTimer

    private lateinit var timerViewModel: TimerViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScope.testScheduler))
        fakeTimerServiceManager = FakeTimerServiceManager()
        fakePreferencesManager = FakePreferencesManager(
            initialPomodoroLengthInMinutes = 25,
            initialShortBreakLengthInMinutes = 5,
            initialLongBreakLengthInMinutes = 15,
            initialPomodorosPerSet = 4,
            initialAutoStartNextTimer = true,
        )
        fakePomodoroTimerStateManager = FakePomodoroTimerStateManager()
        fakeNotificationHelper = FakeNotificationHelper()
        fakeTimeSource = FakeTimeSource()
        countDownTimer = CountDownTimer(testScope, fakeTimeSource)
        timerViewModel = TimerViewModel(
            pomodoroTimerManager = PomodoroTimerManager(
                timer = countDownTimer,
                timerServiceManager = fakeTimerServiceManager,
                notificationHelper = fakeNotificationHelper,
                rewardUnlockManager = RewardUnlockManager(
                    rewardDao = FakeRewardDao(),
                    applicationScope = testScope,
                    notificationHelper = fakeNotificationHelper
                ),
                applicationScope = testScope,
                preferencesManager = fakePreferencesManager,
                pomodoroTimerStateManager = fakePomodoroTimerStateManager,
                pomodoroStatisticDao = FakePomodoroStatisticDao()
            ),
            savedStateHandle = SavedStateHandle()
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun pomodoroTimerState_hasCorrectDefaultValues() = testScope.runTest {
        fakePomodoroTimerStateManager.updateTimerRunning(true)
        timerViewModel = TimerViewModel(
            pomodoroTimerManager = PomodoroTimerManager(
                timer = countDownTimer,
                timerServiceManager = fakeTimerServiceManager,
                notificationHelper = fakeNotificationHelper,
                rewardUnlockManager = RewardUnlockManager(
                    rewardDao = FakeRewardDao(),
                    applicationScope = testScope,
                    notificationHelper = fakeNotificationHelper
                ),
                applicationScope = testScope,
                preferencesManager = fakePreferencesManager,
                pomodoroTimerStateManager = fakePomodoroTimerStateManager,
                pomodoroStatisticDao = FakePomodoroStatisticDao()
            ),
            savedStateHandle = SavedStateHandle()
        )
        runCurrent()
        assertThat(timerViewModel.pomodoroTimerState.getOrAwaitValue()).isEqualTo(
            defaultTimerState
        )
    }

    @Test
    fun showResetTimerConfirmationDialog_defaultValueFalse() {
        assertThat(timerViewModel.screenState.getOrAwaitValue().showResetTimerConfirmationDialog).isFalse()
    }

    @Test
    fun showSkipBreakConfirmationDialog_defaultValueFalse() {
        assertThat(timerViewModel.screenState.getOrAwaitValue().showSkipBreakConfirmationDialog).isFalse()
    }

    @Test
    fun showResetPomodoroSetConfirmationDialog_defaultValueFalse() {
        assertThat(timerViewModel.screenState.getOrAwaitValue().showResetPomodoroSetConfirmationDialog).isFalse()
    }

    @Test
    fun showResetPomodoroCountConfirmationDialog_defaultValueFalse() {
        assertThat(timerViewModel.screenState.getOrAwaitValue().showResetPomodoroCountConfirmationDialog).isFalse()
    }

    @Test
    fun finishTimer_showsTimerCompletedNotificationForCorrectPhase() = testScope.runTest {
        runCurrent()
        timerViewModel.onStartStopTimerClicked()
        advanceTimeBy(25.minutesToMilliseconds())
        fakeTimeSource.advanceTimeBy(25.minutesToMilliseconds())
        runCurrent()

        assertThat(fakeNotificationHelper.timerCompletedNotification).isEqualTo(
            TimerCompletedNotificationState.Shown(PomodoroPhase.POMODORO)
        )

        advanceTimeBy(5.minutesToMilliseconds())
        fakeTimeSource.advanceTimeBy(5.minutesToMilliseconds())
        runCurrent()

        assertThat(fakeNotificationHelper.timerCompletedNotification).isEqualTo(
            TimerCompletedNotificationState.Shown(PomodoroPhase.SHORT_BREAK)
        )

        repeat(2) {
            advanceTimeBy(25.minutesToMilliseconds())
            fakeTimeSource.advanceTimeBy(25.minutesToMilliseconds())
            runCurrent()
            advanceTimeBy(5.minutesToMilliseconds())
            fakeTimeSource.advanceTimeBy(5.minutesToMilliseconds())
            runCurrent()
        }

        advanceTimeBy(25.minutesToMilliseconds())
        fakeTimeSource.advanceTimeBy(25.minutesToMilliseconds())
        runCurrent()
        advanceTimeBy(15.minutesToMilliseconds())
        fakeTimeSource.advanceTimeBy(15.minutesToMilliseconds())
        runCurrent()

        assertThat(fakeNotificationHelper.timerCompletedNotification).isEqualTo(
            TimerCompletedNotificationState.Shown(PomodoroPhase.LONG_BREAK)
        )

        timerViewModel.onStartStopTimerClicked()
    }

    @Test
    fun finishTimer_autoStartNextTimerTrue_keepsTimerRunning() = testScope.runTest {
        runCurrent()
        timerViewModel.onStartStopTimerClicked()
        advanceTimeBy(25.minutesToMilliseconds())
        fakeTimeSource.advanceTimeBy(25.minutesToMilliseconds())
        runCurrent()

        assertThat(timerViewModel.pomodoroTimerState.getOrAwaitValue().timerRunning).isTrue()

        timerViewModel.onStartStopTimerClicked()
    }

    @Test
    fun finishTimer_autoStartNextTimerFalse_stopsTimer() = testScope.runTest {
        fakePreferencesManager.updateAutoStartNextTimer(false)
        runCurrent()
        timerViewModel.onStartStopTimerClicked()
        advanceTimeBy(25.minutesToMilliseconds())
        fakeTimeSource.advanceTimeBy(25.minutesToMilliseconds())
        runCurrent()

        assertThat(timerViewModel.pomodoroTimerState.getOrAwaitValue().timerRunning).isFalse()
    }

    @Test
    fun onStartStopTimerClicked_removesTimerCompletedNotification() = testScope.runTest {
        runCurrent()
        timerViewModel.onStartStopTimerClicked()
        advanceTimeBy(25.minutesToMilliseconds())
        fakeTimeSource.advanceTimeBy(25.minutesToMilliseconds())
        runCurrent()
        timerViewModel.onStartStopTimerClicked()

        assertThat(fakeNotificationHelper.timerCompletedNotification).isEqualTo(
            TimerCompletedNotificationState.NotShown
        )
    }

    @Test
    fun onStartStopTimerClicked_timerRunning_showsResumeTimerNotificationWithCorrectValues() {
        // TODO: 10/01/2022 Doesn't work right now because BroadcastReceiver shows the notification
        /*viewModel.onStartStopTimerClicked()
        viewModel.onStartStopTimerClicked()

        assertThat(fakeNotificationHelper.resumeTimerNotification).isEqualTo(
            ResumeTimerNotificationState.Shown(
                defaultTimerState.currentPhase,
                defaultTimerState.timeLeftInMillis,
                defaultTimerState.timerRunning
            )
        )*/
    }

    @Test
    fun onStartStopTimerClicked_timerNotRunning_removesResumeTimerNotification() =
        testScope.runTest {
            fakeNotificationHelper.showResumeTimerNotification(
                currentPhase = defaultTimerState.currentPhase,
                timeLeftInMillis = defaultTimerState.timeLeftInMillis,
            )
            timerViewModel.onStartStopTimerClicked()
            runCurrent()

            assertThat(fakeNotificationHelper.resumeTimerNotification).isEqualTo(
                ResumeTimerNotificationState.NotShown
            )

            timerViewModel.onStartStopTimerClicked()
        }

    @Test
    fun onStartStopTimerClicked_timerNotRunning_startsTimerService() = testScope.runTest {
        timerViewModel.onStartStopTimerClicked()
        runCurrent()

        assertThat(fakeTimerServiceManager.serviceRunning).isTrue()

        timerViewModel.onStartStopTimerClicked()
    }

    @Test
    fun onStartStopTimerClicked_timerNotRunning_setsTimerRunningTrue() = testScope.runTest {
        timerViewModel.onStartStopTimerClicked()
        runCurrent()

        assertThat(timerViewModel.pomodoroTimerState.getOrAwaitValue().timerRunning).isTrue()

        timerViewModel.onStartStopTimerClicked()
    }

    @Test
    fun onStartStopTimerClicked_timerRunning_cancelsTimer() = testScope.runTest {
        runCurrent()
        timerViewModel.onStartStopTimerClicked()
        runCurrent()
        timerViewModel.onStartStopTimerClicked()

        advanceTimeBy(5000)
        fakeTimeSource.advanceTimeBy(5000)
        runCurrent()

        assertThat(timerViewModel.pomodoroTimerState.getOrAwaitValue().timeLeftInMillis).isEqualTo(
            defaultTimerState.timeLeftInMillis
        )
    }

    @Test
    fun onStartStopTimerClicked_timerRunning_stopsTimerService() {
        timerViewModel.onStartStopTimerClicked()
        timerViewModel.onStartStopTimerClicked()

        assertThat(fakeTimerServiceManager.serviceRunning).isFalse()
    }

    @Test
    fun onStartStopTimerClicked_timerRunning_setsTimerRunningFalse() {
        timerViewModel.onStartStopTimerClicked()
        timerViewModel.onStartStopTimerClicked()

        assertThat(timerViewModel.pomodoroTimerState.getOrAwaitValue().timerRunning).isFalse()
    }

    @Test
    fun onStartStopTimerClicked_timerNotRunningAndPomodoroTargetReached_resetsPomodoroCounter() =
        testScope.runTest {
            runCurrent()
            timerViewModel.onStartStopTimerClicked()

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

            timerViewModel.onStartStopTimerClicked()

            assertThat(timerViewModel.pomodoroTimerState.getOrAwaitValue().pomodorosCompletedInSet).isEqualTo(
                0
            )
        }

    @Test
    fun onResetTimerClicked_showsResetTimerConfirmationDialog() {
        timerViewModel.onResetTimerClicked()

        assertThat(timerViewModel.screenState.getOrAwaitValue().showResetTimerConfirmationDialog).isTrue()
    }

    @Test
    fun onResetTimerDialogDismissed_hidesResetTimerConfirmationDialog() {
        timerViewModel.onResetTimerClicked()
        timerViewModel.onResetTimerDialogDismissed()

        assertThat(timerViewModel.screenState.getOrAwaitValue().showResetTimerConfirmationDialog).isFalse()
    }

    @Test
    fun onResetTimerConfirmed_hidesResetTimerConfirmationDialog() {
        timerViewModel.onResetTimerClicked()
        timerViewModel.onResetTimerConfirmed()

        assertThat(timerViewModel.screenState.getOrAwaitValue().showResetTimerConfirmationDialog).isFalse()
    }

    @Test
    fun onResetTimerConfirmed_cancelsTimer() = testScope.runTest {
        runCurrent()
        timerViewModel.onStartStopTimerClicked()
        runCurrent()
        timerViewModel.onResetTimerClicked()
        timerViewModel.onResetTimerConfirmed()

        advanceTimeBy(5000)
        fakeTimeSource.advanceTimeBy(5000)
        runCurrent()

        assertThat(timerViewModel.pomodoroTimerState.getOrAwaitValue().timeLeftInMillis).isEqualTo(
            defaultTimerState.timeLeftInMillis
        )
    }

    @Test
    fun onResetTimerConfirmed_stopsTimerService() {
        timerViewModel.onStartStopTimerClicked()
        timerViewModel.onResetTimerClicked()
        timerViewModel.onResetTimerConfirmed()

        assertThat(fakeTimerServiceManager.serviceRunning).isFalse()
    }

    @Test
    fun onResetTimerConfirmed_setsTimerRunningFalse() {
        timerViewModel.onStartStopTimerClicked()
        timerViewModel.onResetTimerClicked()
        timerViewModel.onResetTimerConfirmed()

        assertThat(timerViewModel.pomodoroTimerState.getOrAwaitValue().timerRunning).isFalse()
    }

    @Test
    fun onResetTimerConfirmed_keepsCurrentPhase() {
        timerViewModel.onStartStopTimerClicked()
        timerViewModel.onResetTimerClicked()
        timerViewModel.onResetTimerConfirmed()

        assertThat(timerViewModel.pomodoroTimerState.getOrAwaitValue().currentPhase).isEqualTo(
            defaultTimerState.currentPhase
        )
    }

    @Test
    fun onResetTimerConfirmed_resetsTimeTargetToCorrectValue() {
        timerViewModel.onStartStopTimerClicked()
        timerViewModel.onResetTimerClicked()
        timerViewModel.onResetTimerConfirmed()

        assertThat(timerViewModel.pomodoroTimerState.getOrAwaitValue().timeTargetInMillis).isEqualTo(
            defaultTimerState.timeTargetInMillis
        )
    }

    @Test
    fun onResetTimerConfirmed_resetsTimeLeftToCorrectValue() = testScope.runTest {
        runCurrent()
        timerViewModel.onStartStopTimerClicked()
        advanceTimeBy(5000)
        fakeTimeSource.advanceTimeBy(5000)
        runCurrent()

        timerViewModel.onResetTimerClicked()
        timerViewModel.onResetTimerConfirmed()

        assertThat(timerViewModel.pomodoroTimerState.getOrAwaitValue().timeLeftInMillis).isEqualTo(
            defaultTimerState.timeLeftInMillis
        )
    }

    @Test
    fun onSkipBreakClicked_showsSkipBreakConfirmationDialog() {
        timerViewModel.onSkipBreakClicked()

        assertThat(timerViewModel.screenState.getOrAwaitValue().showSkipBreakConfirmationDialog).isTrue()
    }

    @Test
    fun onSkipBreakDialogDismissed_hidesSkipBreakConfirmationDialog() {
        timerViewModel.onSkipBreakClicked()
        timerViewModel.onSkipBreakDialogDismissed()

        assertThat(timerViewModel.screenState.getOrAwaitValue().showSkipBreakConfirmationDialog).isFalse()
    }

    @Test
    fun onSkipBreakConfirmed_hidesSkipBreakConfirmationDialog() {
        timerViewModel.onSkipBreakClicked()
        timerViewModel.onSkipBreakConfirmed()

        assertThat(timerViewModel.screenState.getOrAwaitValue().showSkipBreakConfirmationDialog).isFalse()
    }

    @Test
    fun onSkipBreakConfirmed_setsCorrectPomodoroTimerState() = testScope.runTest {
        runCurrent()
        timerViewModel.onStartStopTimerClicked()
        advanceTimeBy(25.minutesToMilliseconds())
        fakeTimeSource.advanceTimeBy(25.minutesToMilliseconds())
        runCurrent()

        timerViewModel.onStartStopTimerClicked()
        timerViewModel.onSkipBreakClicked()
        timerViewModel.onSkipBreakConfirmed()
        runCurrent()

        assertThat(timerViewModel.pomodoroTimerState.getOrAwaitValue()).isEqualTo(
            PomodoroTimerState(
                timerRunning = false,
                currentPhase = PomodoroPhase.POMODORO,
                timeLeftInMillis = 25.minutesToMilliseconds(),
                timeTargetInMillis = 25.minutesToMilliseconds(),
                pomodorosCompletedInSet = 1,
                pomodorosPerSetTarget = defaultTimerState.pomodorosPerSetTarget,
                pomodorosCompletedTotal = 1,
            )
        )
    }

    @Test
    fun onSkipBreakConfirmed_timerNotRunning_keepsTimerNotRunning() = testScope.runTest {
        runCurrent()
        timerViewModel.onStartStopTimerClicked()
        advanceTimeBy(25.minutesToMilliseconds())
        fakeTimeSource.advanceTimeBy(25.minutesToMilliseconds())
        runCurrent()

        timerViewModel.onStartStopTimerClicked()
        timerViewModel.onSkipBreakClicked()
        timerViewModel.onSkipBreakConfirmed()
        advanceTimeBy(5000)
        fakeTimeSource.advanceTimeBy(5000)
        runCurrent()

        assertThat(timerViewModel.pomodoroTimerState.getOrAwaitValue().timeLeftInMillis).isEqualTo(
            25.minutesToMilliseconds()
        )
    }

    @Test
    fun onSkipBreakConfirmed_timerRunning_keepsTimerRunning() = testScope.runTest {
        runCurrent()
        timerViewModel.onStartStopTimerClicked()
        advanceTimeBy(25.minutesToMilliseconds())
        fakeTimeSource.advanceTimeBy(25.minutesToMilliseconds())
        runCurrent()

        timerViewModel.onSkipBreakClicked()
        timerViewModel.onSkipBreakConfirmed()
        advanceTimeBy(5000)
        fakeTimeSource.advanceTimeBy(5000)
        runCurrent()

        timerViewModel.onStartStopTimerClicked()

        assertThat(timerViewModel.pomodoroTimerState.getOrAwaitValue().timeLeftInMillis).isEqualTo(
            25.minutesToMilliseconds() - 5000L
        )
    }

    @Test
    fun onSkipBreakConfirmed_currentPhasePomodoro_keepsCorrectTimerState() = testScope.runTest {
        runCurrent()
        timerViewModel.onSkipBreakClicked()
        timerViewModel.onSkipBreakConfirmed()
        runCurrent()

        assertThat(timerViewModel.pomodoroTimerState.getOrAwaitValue()).isEqualTo(
            PomodoroTimerState(
                timerRunning = false,
                currentPhase = PomodoroPhase.POMODORO,
                timeLeftInMillis = 25.minutesToMilliseconds(),
                timeTargetInMillis = 25.minutesToMilliseconds(),
                pomodorosCompletedInSet = 0,
                pomodorosPerSetTarget = defaultTimerState.pomodorosPerSetTarget,
                pomodorosCompletedTotal = 0,
            )
        )
    }

    @Test
    fun onResetPomodoroSetClicked_showsResetPomodoroSetConfirmationDialog() {
        timerViewModel.onResetPomodoroSetClicked()

        assertThat(timerViewModel.screenState.getOrAwaitValue().showResetPomodoroSetConfirmationDialog).isTrue()
    }

    @Test
    fun onResetPomodoroSetDialogDismissed_hidesResetPomodoroSetConfirmationDialog() {
        timerViewModel.onResetPomodoroSetClicked()
        timerViewModel.onResetPomodoroSetDialogDismissed()

        assertThat(timerViewModel.screenState.getOrAwaitValue().showResetPomodoroSetConfirmationDialog).isFalse()
    }

    @Test
    fun onResetPomodoroSetConfirmed_hidesResetPomodoroSetConfirmationDialog() {
        timerViewModel.onResetPomodoroSetClicked()
        timerViewModel.onResetPomodoroSetConfirmed()

        assertThat(timerViewModel.screenState.getOrAwaitValue().showResetPomodoroSetConfirmationDialog).isFalse()
    }

    @Test
    fun onResetPomodoroSetConfirmed_timerRunning_cancelsTimer() = testScope.runTest {
        runCurrent()
        timerViewModel.onStartStopTimerClicked()
        runCurrent()
        timerViewModel.onResetPomodoroSetClicked()
        timerViewModel.onResetPomodoroSetConfirmed()

        advanceTimeBy(5000)
        fakeTimeSource.advanceTimeBy(5000)
        runCurrent()

        assertThat(timerViewModel.pomodoroTimerState.getOrAwaitValue().timeLeftInMillis).isEqualTo(
            defaultTimerState.timeLeftInMillis
        )
    }

    @Test
    fun onResetPomodoroSetConfirmed_timerRunning_stopsTimerService() {
        timerViewModel.onStartStopTimerClicked()
        timerViewModel.onResetPomodoroSetClicked()
        timerViewModel.onResetPomodoroSetConfirmed()

        assertThat(fakeTimerServiceManager.serviceRunning).isFalse()
    }

    @Test
    fun onResetPomodoroSetConfirmed_timerRunning_setsTimerRunningFalse() {
        timerViewModel.onStartStopTimerClicked()
        timerViewModel.onResetPomodoroSetClicked()
        timerViewModel.onResetPomodoroSetConfirmed()

        assertThat(timerViewModel.pomodoroTimerState.getOrAwaitValue().timerRunning).isFalse()
    }

    @Test
    fun onResetPomodoroSetConfirmed_setsCorrectPomodoroTimerState() = testScope.runTest {
        runCurrent()
        timerViewModel.onStartStopTimerClicked()
        advanceTimeBy(25.minutesToMilliseconds())
        fakeTimeSource.advanceTimeBy(25.minutesToMilliseconds())
        runCurrent()
        timerViewModel.onStartStopTimerClicked()
        runCurrent()

        timerViewModel.onResetPomodoroSetClicked()
        timerViewModel.onResetPomodoroSetConfirmed()
        runCurrent()

        assertThat(timerViewModel.pomodoroTimerState.getOrAwaitValue()).isEqualTo(
            defaultTimerState.copy(
                pomodorosCompletedTotal = 1
            )
        )
    }

    @Test
    fun onResetPomodoroCountClicked_showsResetPomodoroCountConfirmationDialog() {
        timerViewModel.onResetPomodoroCountClicked()

        assertThat(timerViewModel.screenState.getOrAwaitValue().showResetPomodoroCountConfirmationDialog).isTrue()
    }

    @Test
    fun onResetPomodoroCountDialogDismissed_hidesResetPomodoroCountConfirmationDialog() {
        timerViewModel.onResetPomodoroCountClicked()
        timerViewModel.onResetPomodoroCountDialogDismissed()

        assertThat(timerViewModel.screenState.getOrAwaitValue().showResetPomodoroCountConfirmationDialog).isFalse()
    }

    @Test
    fun onResetPomodoroCountConfirmed_hidesResetPomodoroCountConfirmationDialog() {
        timerViewModel.onResetPomodoroCountClicked()
        timerViewModel.onResetPomodoroCountConfirmed()

        assertThat(timerViewModel.screenState.getOrAwaitValue().showResetPomodoroCountConfirmationDialog).isFalse()
    }

    @Test
    fun onResetPomodoroCountConfirmed_resetsPomodorosCompletedTotal() = testScope.runTest {
        runCurrent()
        timerViewModel.onStartStopTimerClicked()
        advanceTimeBy(25.minutesToMilliseconds())
        fakeTimeSource.advanceTimeBy(25.minutesToMilliseconds())
        runCurrent()
        timerViewModel.onStartStopTimerClicked()

        timerViewModel.onResetPomodoroCountClicked()
        timerViewModel.onResetPomodoroCountConfirmed()
        runCurrent()

        assertThat(timerViewModel.pomodoroTimerState.getOrAwaitValue().pomodorosCompletedTotal).isEqualTo(
            0
        )
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