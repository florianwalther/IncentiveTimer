package com.florianwalther.incentivetimer.data.datastore

import kotlinx.coroutines.flow.Flow

interface PreferencesManager {
    val appPreferences: Flow<AppPreferences>

    val timerPreferences: Flow<TimerPreferences>

    suspend fun updatePomodoroLength(lengthInMinutes: Int)

    suspend fun updateShortBreakLength(lengthInMinutes: Int)

    suspend fun updateLongBreakLength(lengthInMinutes: Int)

    suspend fun updatePomodorosPerSet(amount: Int)

    suspend fun updateAutoStartNextTimer(autoStartNextTimer: Boolean)

    suspend fun updateSelectedTheme(theme: ThemeSelection)

    suspend fun updateAppInstructionsDialogShown(shown: Boolean)
}