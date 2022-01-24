package com.florianwalther.incentivetimer.data.datastore

import androidx.compose.runtime.MutableState
import com.zhuinden.flowcombinetuplekt.combineTuple
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakePomodoroTimerStateManager : PomodoroTimerStateManager {

    private val timerRunning = MutableStateFlow(false)
    private val currentPhase = MutableStateFlow(PomodoroPhase.POMODORO)
    private val timeLeftInMillis = MutableStateFlow(0L)
    private val timeTargetInMillis = MutableStateFlow(0L)
    private val pomodorosPerSetTarget = MutableStateFlow(0)
    private val pomodorosCompletedInSet = MutableStateFlow(0)
    private val pomodorosCompletedTotal = MutableStateFlow(0)

    override val timerState: Flow<PomodoroTimerState> = combineTuple(
        timerRunning,
        currentPhase,
        timeLeftInMillis,
        timeTargetInMillis,
        pomodorosPerSetTarget,
        pomodorosCompletedInSet,
        pomodorosCompletedTotal,
    ).map { (
                timerRunning,
                currentPhase,
                timeLeftInMillis,
                timeTargetInMillis,
                pomodorosPerSetTarget,
                pomodorosCompletedInSet,
                pomodorosCompletedTotal,
            ) ->
        PomodoroTimerState(
            timerRunning = timerRunning,
            currentPhase = currentPhase,
            timeLeftInMillis = timeLeftInMillis,
            timeTargetInMillis = timeTargetInMillis,
            pomodorosPerSetTarget = pomodorosPerSetTarget,
            pomodorosCompletedInSet = pomodorosCompletedInSet,
            pomodorosCompletedTotal = pomodorosCompletedTotal,
        )
    }

    override suspend fun updateTimerRunning(timerRunning: Boolean) {
        this.timerRunning.value = timerRunning
    }

    override suspend fun updateCurrentPhase(phase: PomodoroPhase) {
        this.currentPhase.value = phase
    }

    override suspend fun updateTimeLeftInMillis(timeLeftInMillis: Long) {
        this.timeLeftInMillis.value = timeLeftInMillis
    }

    override suspend fun updateTimeTargetInMillis(timeTargetInMillis: Long) {
        this.timeTargetInMillis.value = timeTargetInMillis
    }

    override suspend fun updatePomodorosPerSetTarget(pomodorosPerSetTarget: Int) {
        this.pomodorosPerSetTarget.value = pomodorosPerSetTarget
    }

    override suspend fun updatePomodorosCompletedInSet(pomodorosCompletedInSet: Int) {
        this.pomodorosCompletedInSet.value = pomodorosCompletedInSet
    }

    override suspend fun updatePomodorosCompletedTotal(pomodorosCompletedTotal: Int) {
        this.pomodorosCompletedTotal.value = pomodorosCompletedTotal
    }
}