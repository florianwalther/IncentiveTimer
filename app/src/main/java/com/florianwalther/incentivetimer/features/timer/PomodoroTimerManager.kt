package com.florianwalther.incentivetimer.features.timer

import androidx.annotation.StringRes
import com.florianwalther.incentivetimer.R
import com.florianwalther.incentivetimer.core.notification.NotificationHelper
import com.florianwalther.incentivetimer.di.ApplicationScope
import com.florianwalther.incentivetimer.features.rewards.RewardUnlockManager
import com.zhuinden.flowcombinetuplekt.combineTuple
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

data class PomodoroTimerState(
    val timerRunning: Boolean,
    val currentPhase: PomodoroPhase,
    val timeLeftInMillis: Long,
    val timeTargetInMillis: Long,
    val pomodorosCompletedInSet: Int,
    val pomodorosPerSetTarget: Int,
    val pomodorosCompletedTotal: Int,
) {
    companion object {
        val DEFAULT = PomodoroTimerState(
            timerRunning = false,
            currentPhase = PomodoroPhase.POMODORO,
            timeLeftInMillis = POMODORO_DURATION_IN_MILLIS,
            timeTargetInMillis = POMODORO_DURATION_IN_MILLIS,
            pomodorosCompletedInSet = 0,
            pomodorosPerSetTarget = POMODOROS_PER_SET,
            pomodorosCompletedTotal = 0,
        )
    }
}

enum class PomodoroPhase(@StringRes val readableName: Int) {
    POMODORO(R.string.pomodoro), SHORT_BREAK(R.string.short_break), LONG_BREAK(R.string.long_break);

    val isBreak get() = this == SHORT_BREAK || this == LONG_BREAK
}

const val POMODORO_DURATION_IN_MILLIS = 25 * 60 * 1_000L
const val SHORT_BREAK_DURATION_IN_MILLIS = 5 * 60 * 1_000L
const val LONG_BREAK_DURATION_IN_MILLIS = 15 * 60 * 1_000L
const val POMODOROS_PER_SET = 4

@Singleton
class PomodoroTimerManager @Inject constructor(
    private val timer: CountDownTimer,
    private val timerServiceManager: TimerServiceManager,
    private val notificationHelper: NotificationHelper,
    private val rewardUnlockManager: RewardUnlockManager,
    @ApplicationScope private val applicationScope: CoroutineScope,
) {
    private val timerRunningFlow = MutableStateFlow(PomodoroTimerState.DEFAULT.timerRunning)
    private val currentPhaseFlow = MutableStateFlow(PomodoroTimerState.DEFAULT.currentPhase)
    private val timeLeftInMillisFlow = MutableStateFlow(PomodoroTimerState.DEFAULT.timeLeftInMillis)
    private val timeTargetInMillisFlow =
        MutableStateFlow(PomodoroTimerState.DEFAULT.timeTargetInMillis)
    private val pomodorosCompletedInSetFlow =
        MutableStateFlow(PomodoroTimerState.DEFAULT.pomodorosCompletedInSet)
    private val pomodorosPerSetTargetFlow =
        MutableStateFlow(PomodoroTimerState.DEFAULT.pomodorosPerSetTarget)
    private val pomodorosCompletedTotalFlow =
        MutableStateFlow(PomodoroTimerState.DEFAULT.pomodorosCompletedTotal)

    val pomodoroTimerState = combineTuple(
        timerRunningFlow,
        currentPhaseFlow,
        timeLeftInMillisFlow,
        timeTargetInMillisFlow,
        pomodorosCompletedInSetFlow,
        pomodorosPerSetTargetFlow,
        pomodorosCompletedTotalFlow,
    ).map { (timerRunning, currentPhase, timeLeftInMillis, timeTargetInMillis, pomodorosCompletedInSet, pomodorosPerSetTarget, pomodorosCompletedTotal) ->
        PomodoroTimerState(
            timerRunning = timerRunning,
            currentPhase = currentPhase,
            timeLeftInMillis = timeLeftInMillis,
            timeTargetInMillis = timeTargetInMillis,
            pomodorosCompletedInSet = pomodorosCompletedInSet,
            pomodorosPerSetTarget = pomodorosPerSetTarget,
            pomodorosCompletedTotal = pomodorosCompletedTotal,
        )
    }

    fun startStopTimer() {
        notificationHelper.removeTimerCompletedNotification()
        val timerRunning = timerRunningFlow.value
        if (timerRunning) {
            stopTimer()
        } else {
            notificationHelper.removeResumeTimerNotification()
            startTimer()
        }
    }

    private fun startTimer() {
        resetPomodoroCounterIfTargetReached()

        val timeLeftInMillis = timeLeftInMillisFlow.value

        timer.startTimer(
            durationMillis = timeLeftInMillis,
            countDownInterval = 0L,
            onTick = { millisUntilFinished ->
                timeLeftInMillisFlow.value = millisUntilFinished
            },
            onFinish = {
                val currentPhase = currentPhaseFlow.value
                notificationHelper.showTimerCompletedNotification(currentPhase)
                if (currentPhase == PomodoroPhase.POMODORO) {
                    rewardUnlockManager.rollAllRewards()
                    pomodorosCompletedTotalFlow.value++
                    pomodorosCompletedInSetFlow.value++
                }
                startNextPhase()
                startTimer()
            },
        )

        timerServiceManager.startTimerService()
        timerRunningFlow.value = true
    }

    private fun stopTimer() {
        timer.cancelTimer()
        timerServiceManager.stopTimerService()
        timerRunningFlow.value = false
    }

    fun skipBreak() {
        if (currentPhaseFlow.value.isBreak) {
            timer.cancelTimer()
            startNextPhase()
            if (timerRunningFlow.value) {
                startTimer()
            }
        }
    }

    private fun resetPomodoroCounterIfTargetReached() {
        val pomodorosCompleted = pomodorosCompletedInSetFlow.value
        val pomodorosTarget = pomodorosPerSetTargetFlow.value
        val currentPhase = currentPhaseFlow.value
        if (pomodorosCompleted >= pomodorosTarget && currentPhase == PomodoroPhase.POMODORO) {
            pomodorosCompletedInSetFlow.value = 0
        }
    }

    private fun startNextPhase() {
        val lastPhase = currentPhaseFlow.value
        val pomodorosCompletedInSet = pomodorosCompletedInSetFlow.value
        val pomodorosPerTarget = pomodorosPerSetTargetFlow.value
        pomodorosCompletedInSetFlow.value = pomodorosCompletedInSet

        setPomodoroPhase(getNextPhase(lastPhase, pomodorosCompletedInSet, pomodorosPerTarget))
    }

    private fun setPomodoroPhase(newPhase: PomodoroPhase) {
        currentPhaseFlow.value = newPhase
        val newTimeTarget = getTimeTargetForPhase(newPhase)
        timeTargetInMillisFlow.value = newTimeTarget
        timeLeftInMillisFlow.value = newTimeTarget
    }

    private fun getNextPhase(
        lastPhase: PomodoroPhase,
        pomodorosCompleted: Int,
        pomodorosTarget: Int
    ): PomodoroPhase = when (lastPhase) {
        PomodoroPhase.POMODORO -> if (pomodorosCompleted >= pomodorosTarget) PomodoroPhase.LONG_BREAK else PomodoroPhase.SHORT_BREAK
        PomodoroPhase.SHORT_BREAK, PomodoroPhase.LONG_BREAK -> PomodoroPhase.POMODORO
    }

    private fun getTimeTargetForPhase(phase: PomodoroPhase): Long = when (phase) {
        PomodoroPhase.POMODORO -> POMODORO_DURATION_IN_MILLIS
        PomodoroPhase.SHORT_BREAK -> SHORT_BREAK_DURATION_IN_MILLIS
        PomodoroPhase.LONG_BREAK -> LONG_BREAK_DURATION_IN_MILLIS
    }

    fun resetTimer() {
        stopTimer()
        timeLeftInMillisFlow.value = timeTargetInMillisFlow.value
    }

    fun resetPomodoroSet() {
        resetTimer()
        pomodorosCompletedInSetFlow.value = 0
        setPomodoroPhase(PomodoroPhase.POMODORO)
    }

    fun resetPomodoroCount() {
        pomodorosCompletedTotalFlow.value = 0
    }

    // TODO: 26/12/2021 Save instance state in Activity.onSaveInstanceState
}