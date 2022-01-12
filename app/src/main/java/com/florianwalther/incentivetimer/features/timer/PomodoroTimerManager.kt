package com.florianwalther.incentivetimer.features.timer

import androidx.annotation.StringRes
import com.florianwalther.incentivetimer.R
import com.florianwalther.incentivetimer.core.notification.NotificationHelper
import com.florianwalther.incentivetimer.core.util.minutesToMilliseconds
import com.florianwalther.incentivetimer.data.preferences.PreferencesManager
import com.florianwalther.incentivetimer.di.ApplicationScope
import com.florianwalther.incentivetimer.features.rewards.RewardUnlockManager
import com.zhuinden.flowcombinetuplekt.combineTuple
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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
)

enum class PomodoroPhase(@StringRes val readableName: Int) {
    POMODORO(R.string.pomodoro), SHORT_BREAK(R.string.short_break), LONG_BREAK(R.string.long_break);

    val isBreak get() = this == SHORT_BREAK || this == LONG_BREAK
}

@Singleton
class PomodoroTimerManager @Inject constructor(
    private val timer: CountDownTimer,
    private val preferencesManager: PreferencesManager,
    private val timerServiceManager: TimerServiceManager,
    private val notificationHelper: NotificationHelper,
    private val rewardUnlockManager: RewardUnlockManager,
    @ApplicationScope private val applicationScope: CoroutineScope,
) {
    private val timerPreferences = preferencesManager.timerPreferences

    private val timerRunning = MutableStateFlow(false)
    private val currentPhase = MutableStateFlow(PomodoroPhase.POMODORO)
    private val timeLeftInMillis = MutableStateFlow(0L)
    private val timeTargetInMillis = MutableStateFlow(0L)
    private val pomodorosPerSetTarget = timerPreferences.map { it.pomodorosPerSet }
    private val pomodorosCompletedInSet = MutableStateFlow(0)
    private val pomodorosCompletedTotal = MutableStateFlow(0)

    val pomodoroTimerState = combineTuple(
        timerRunning,
        currentPhase,
        timeLeftInMillis,
        timeTargetInMillis,
        pomodorosCompletedInSet,
        pomodorosPerSetTarget,
        pomodorosCompletedTotal,
    ).map { (
                timerRunning,
                currentPhase,
                timeLeftInMillis,
                timeTargetInMillis,
                pomodorosCompletedInSet,
                pomodorosPerSetTarget,
                pomodorosCompletedTotal
            ) ->
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

    init {
        applicationScope.launch {
            // TODO: 10/01/2022 Quick fix to make coroutines tests work
            //  -> replace for collect later
            val timerPreferences = this@PomodoroTimerManager.timerPreferences.first()
//            timerPreferences.collectLatest { timerPreferences ->
            val currentPhase = currentPhase.value
            val currentTimeTargetInMillis = timeTargetInMillis.value
            val currentTimeLeftInMillis = timeLeftInMillis.value
            val newTimeTargetInMillis =
                timerPreferences.lengthInMinutesForPhase(currentPhase).minutesToMilliseconds()
            if (timerPreferences.pomodorosPerSet <= pomodorosCompletedInSet.value
                && currentPhase == PomodoroPhase.POMODORO
            ) {
                pomodorosCompletedInSet.value = 0
            }
            if (currentTimeTargetInMillis < newTimeTargetInMillis) {
                timeLeftInMillis.value += newTimeTargetInMillis - currentTimeTargetInMillis
            }
            if (currentTimeLeftInMillis > newTimeTargetInMillis) {
                val timerWasRunning = timerRunning.value
                stopAndResetTimer()
                if (timerWasRunning) {
                    startTimer()
                }
            }
            timeTargetInMillis.value = newTimeTargetInMillis
//            }
        }
    }

    fun startStopTimer() {
        notificationHelper.removeTimerCompletedNotification()
        val timerRunning = timerRunning.value
        if (timerRunning) {
            stopTimer()
        } else {
            notificationHelper.removeResumeTimerNotification()
            startTimer()
        }
    }

    private fun startTimer() {
        timer.startTimer(
            durationMillis = timeLeftInMillis.value,
            countDownInterval = 1000L,
            onTick = { millisUntilFinished ->
                timeLeftInMillis.value = millisUntilFinished
            },
            onFinish = {
                val currentPhase = currentPhase.value
                println("onFinish phase: $currentPhase")
                notificationHelper.showTimerCompletedNotification(currentPhase)
                if (currentPhase == PomodoroPhase.POMODORO) {
                    rewardUnlockManager.rollAllRewards()
                    pomodorosCompletedTotal.value++
                    pomodorosCompletedInSet.value++
                }
                applicationScope.launch {
                    startNextPhase()
                    startTimer()
                }
            },
        )
        timerServiceManager.startTimerService()
        timerRunning.value = true
    }

    private fun stopTimer() {
        timer.cancelTimer()
        timerServiceManager.stopTimerService()
        timerRunning.value = false
    }

    fun skipBreak() {
        if (currentPhase.value.isBreak) {
            timer.cancelTimer()
            applicationScope.launch {
                startNextPhase()
                if (timerRunning.value) {
                    startTimer()
                }
            }
        }
    }

    private suspend fun resetPomodoroCounterIfTargetReached() {
        val pomodorosCompleted = pomodorosCompletedInSet.value
        val pomodorosTarget = pomodorosPerSetTarget.first()
        if (pomodorosCompleted >= pomodorosTarget) {
            pomodorosCompletedInSet.value = 0
        }
    }

    private suspend fun startNextPhase() {
        val lastPhase = currentPhase.value
        val pomodorosCompletedInSet = pomodorosCompletedInSet.value
        val pomodorosPerSetTarget = pomodorosPerSetTarget.first()

        val nextPhase = getNextPhase(lastPhase, pomodorosCompletedInSet, pomodorosPerSetTarget)

        setPomodoroPhaseAndResetTimer(nextPhase)

        if (nextPhase == PomodoroPhase.POMODORO) {
            resetPomodoroCounterIfTargetReached()
        }
    }

    private suspend fun setPomodoroPhaseAndResetTimer(newPhase: PomodoroPhase) {
        currentPhase.value = newPhase
        val newTimeTarget = getTimeTargetInMillisecondsForPhase(newPhase)
        timeTargetInMillis.value = newTimeTarget
        timeLeftInMillis.value = newTimeTarget
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
        setPomodoroPhaseAndResetTimer(currentPhase.value)
    }

    suspend fun resetPomodoroSet() {
        stopTimer()
        pomodorosCompletedInSet.value = 0
        setPomodoroPhaseAndResetTimer(PomodoroPhase.POMODORO)
    }

    fun resetPomodoroCount() {
        pomodorosCompletedTotal.value = 0
    }

    // TODO: 26/12/2021 Save instance state in Activity.onSaveInstanceState
}