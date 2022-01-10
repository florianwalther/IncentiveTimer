package com.florianwalther.incentivetimer.data.preferences

import kotlinx.coroutines.flow.Flow

interface PreferencesManager {
    val timerPreferences: Flow<TimerPreferences>

    suspend fun updatePomodoroLength(lengthInMinutes: Int)

    suspend fun updateShortBreakLength(lengthInMinutes: Int)

    suspend fun updateLongBreakLength(lengthInMinutes: Int)

    suspend fun updatePomodorosPerSet(amount: Int)
}