package com.florianwalther.incentivetimer.data.datastore

import kotlinx.coroutines.flow.Flow

interface PomodoroTimerStateManager {
    val timerState: Flow<PomodoroTimerState>

    suspend fun updateTimerRunning(timerRunning: Boolean)

    suspend fun updateCurrentPhase(phase: PomodoroPhase)

    suspend fun updateTimeLeftInMillis(timeLeftInMillis: Long)

    suspend fun updateTimeTargetInMillis(timeTargetInMillis: Long)

    suspend fun updatePomodorosPerSetTarget(pomodorosPerSetTarget: Int)

    suspend fun updatePomodorosCompletedInSet(pomodorosCompletedInSet: Int)

    suspend fun updatePomodorosCompletedTotal(pomodorosCompletedTotal: Int)
}