package com.florianwalther.incentivetimer.features.timer

import com.florianwalther.incentivetimer.core.notification.NotificationHelper
import com.florianwalther.incentivetimer.core.util.millisecondsToMinutes
import com.florianwalther.incentivetimer.core.util.minutesToMilliseconds
import com.florianwalther.incentivetimer.data.datastore.PomodoroPhase
import com.florianwalther.incentivetimer.data.datastore.PomodoroTimerStateManager
import com.florianwalther.incentivetimer.data.datastore.PreferencesManager
import com.florianwalther.incentivetimer.data.db.PomodoroStatistic
import com.florianwalther.incentivetimer.data.db.PomodoroStatisticDao
import com.florianwalther.incentivetimer.di.ApplicationScope
import com.florianwalther.incentivetimer.features.rewards.RewardUnlockManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import logcat.logcat
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class PomodoroTimerManager @Inject constructor(
    private val timer: CountDownTimer,
    private val preferencesManager: PreferencesManager,
    private val pomodoroTimerStateManager: PomodoroTimerStateManager,
    private val timerServiceManager: TimerServiceManager,
    private val notificationHelper: NotificationHelper,
    private val rewardUnlockManager: RewardUnlockManager,
    private val pomodoroStatisticDao: PomodoroStatisticDao,
    private val dailyResetManager: DailyResetManager,
    @ApplicationScope private val applicationScope: CoroutineScope,
) {
    private val timerPreferences = preferencesManager.timerPreferences
    val pomodoroTimerState = pomodoroTimerStateManager.timerState

    init {
        applicationScope.launch {
            pomodoroTimerStateManager.updateTimerRunning(false)
            // TODO: 10/01/2022 Quick fix to make coroutines tests work
            //  -> replace for collect later
//            val timerPreferences = this@PomodoroTimerManager.timerPreferences.first()
            timerPreferences.collectLatest { timerPreferences ->
                pomodoroTimerStateManager.updatePomodorosPerSetTarget(timerPreferences.pomodorosPerSet)
                val pomodoroTimerState = pomodoroTimerState.first()
                val currentPhase = pomodoroTimerState.currentPhase
                val currentTimeTargetInMillis = pomodoroTimerState.timeTargetInMillis
                val currentTimeLeftInMillis = pomodoroTimerState.timeLeftInMillis
                val newTimeTargetInMillis =
                    timerPreferences.lengthInMinutesForPhase(currentPhase).minutesToMilliseconds()
                if (timerPreferences.pomodorosPerSet <= pomodoroTimerState.pomodorosCompletedInSet
                    && currentPhase == PomodoroPhase.POMODORO
                ) {
                    pomodoroTimerStateManager.updatePomodorosCompletedInSet(0)
                }
                val timerWasRunning = pomodoroTimerState.timerRunning
                // TODO: 21/01/2022 Write tests for below
                if (newTimeTargetInMillis > currentTimeTargetInMillis) {
                    timer.cancelTimer()
                    pomodoroTimerStateManager.updateTimeLeftInMillis(
                        currentTimeLeftInMillis + newTimeTargetInMillis - currentTimeTargetInMillis
                    )
                    if (timerWasRunning) {
                        startTimer()
                    }
                }
                if (newTimeTargetInMillis < currentTimeTargetInMillis) {
                    timer.cancelTimer()
                    pomodoroTimerStateManager.updateTimeLeftInMillis(
                        currentTimeLeftInMillis - (currentTimeTargetInMillis - newTimeTargetInMillis)
                    )
                    if (timerWasRunning) {
                        startTimer()
                    } else {
                        onTimerFinished()
                    }
                }
                pomodoroTimerStateManager.updateTimeTargetInMillis(newTimeTargetInMillis)
            }
        }
    }

    fun startStopTimer() {
        notificationHelper.removeTimerCompletedNotification()
        applicationScope.launch {
            val timerRunning = pomodoroTimerState.first().timerRunning
            if (timerRunning) {
                stopTimer()
            } else {
                notificationHelper.removeResumeTimerNotification()
                startTimer()
            }
        }
    }

    private suspend fun startTimer() {
        timer.startTimer(
            durationMillis = pomodoroTimerState.first().timeLeftInMillis,
            countDownInterval = 1000L,
            onTick = { millisUntilFinished ->
                pomodoroTimerStateManager.updateTimeLeftInMillis(millisUntilFinished)
            },
            onFinish = {
                onTimerFinished()
            },
        )
        timerServiceManager.startTimerService()
        pomodoroTimerStateManager.updateTimerRunning(true)
        dailyResetManager.stopDailyReset()
    }

    private suspend fun stopTimer() {
        timer.cancelTimer()
        timerServiceManager.stopTimerService()
        pomodoroTimerStateManager.updateTimerRunning(false)
        dailyResetManager.scheduleDailyReset()
    }

    private fun onTimerFinished() {
        applicationScope.launch {
            val pomodoroTimerState = pomodoroTimerState.first()
            val currentPhase = pomodoroTimerState.currentPhase
            println("onFinish phase: $currentPhase")
            notificationHelper.showTimerCompletedNotification(currentPhase)

            if (currentPhase == PomodoroPhase.POMODORO) {
                pomodoroStatisticDao.insertPomodoroStatistic(
                    PomodoroStatistic(
                        pomodoroDurationInMinutes = pomodoroTimerState.timeTargetInMillis.millisecondsToMinutes(),
                        timestampInMilliseconds = System.currentTimeMillis(),
                    )
                )
                rewardUnlockManager.rollAllRewards()
                pomodoroTimerStateManager.updatePomodorosCompletedInSet(pomodoroTimerState.pomodorosCompletedInSet + 1)
                pomodoroTimerStateManager.updatePomodorosCompletedTotal(pomodoroTimerState.pomodorosCompletedTotal + 1)
            }
            startNextPhase()
            val autoStartNextTimer = timerPreferences.first().autoStartNextTimer
            if (autoStartNextTimer) {
                startTimer()
            } else {
                stopTimer()
            }
        }
    }

    fun skipBreak() {
        applicationScope.launch {
            if (pomodoroTimerState.first().currentPhase.isBreak) {
                timer.cancelTimer()
                startNextPhase()
                if (pomodoroTimerState.first().timerRunning) {
                    startTimer()
                }
            }
        }
    }

    private suspend fun resetPomodoroCounterIfTargetReached() {
        val pomodorosCompleted = pomodoroTimerState.first().pomodorosCompletedInSet
        val pomodorosTarget = pomodoroTimerState.first().pomodorosPerSetTarget
        if (pomodorosCompleted >= pomodorosTarget) {
            pomodoroTimerStateManager.updatePomodorosCompletedInSet(0)
        }
    }

    private suspend fun startNextPhase() {
        val pomodoroTimerState = pomodoroTimerState.first()
        val lastPhase = pomodoroTimerState.currentPhase
        val pomodorosCompletedInSet = pomodoroTimerState.pomodorosCompletedInSet
        val pomodorosPerSetTarget = pomodoroTimerState.pomodorosPerSetTarget

        val nextPhase = getNextPhase(lastPhase, pomodorosCompletedInSet, pomodorosPerSetTarget)

        setPomodoroPhaseAndResetTimer(nextPhase)

        if (nextPhase == PomodoroPhase.POMODORO) {
            resetPomodoroCounterIfTargetReached()
        }
    }

    private suspend fun setPomodoroPhaseAndResetTimer(newPhase: PomodoroPhase) {
        val newTimeTarget = getTimeTargetInMillisecondsForPhase(newPhase)
        pomodoroTimerStateManager.apply {
            updateCurrentPhase(newPhase)
            updateTimeTargetInMillis(newTimeTarget)
            updateTimeLeftInMillis(newTimeTarget)
        }
    }

    private fun getNextPhase(
        lastPhase: PomodoroPhase,
        pomodorosCompleted: Int,
        pomodorosTarget: Int
    ): PomodoroPhase = when (lastPhase) {
        PomodoroPhase.POMODORO -> if (pomodorosCompleted >= pomodorosTarget) PomodoroPhase.LONG_BREAK else PomodoroPhase.SHORT_BREAK
        PomodoroPhase.SHORT_BREAK, PomodoroPhase.LONG_BREAK -> PomodoroPhase.POMODORO
    }

    private suspend fun getTimeTargetInMillisecondsForPhase(phase: PomodoroPhase): Long {
        val timerPreferences = preferencesManager.timerPreferences.first()
        return when (phase) {
            PomodoroPhase.POMODORO -> timerPreferences.pomodoroLengthInMinutes
            PomodoroPhase.SHORT_BREAK -> timerPreferences.shortBreakLengthInMinutes
            PomodoroPhase.LONG_BREAK -> timerPreferences.longBreakLengthInMinutes
        }.minutesToMilliseconds()
    }

    suspend fun stopAndResetTimer() {
        stopTimer()
        setPomodoroPhaseAndResetTimer(pomodoroTimerState.first().currentPhase)
    }

    suspend fun resetPomodoroSet() {
        stopTimer()
        pomodoroTimerStateManager.updatePomodorosCompletedInSet(0)
        setPomodoroPhaseAndResetTimer(PomodoroPhase.POMODORO)
    }

    fun resetPomodoroCount() {
        applicationScope.launch {
            pomodoroTimerStateManager.updatePomodorosCompletedTotal(0)
        }
    }
}