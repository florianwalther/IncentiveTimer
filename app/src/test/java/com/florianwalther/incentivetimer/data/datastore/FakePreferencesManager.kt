package com.florianwalther.incentivetimer.data.datastore

import com.zhuinden.flowcombinetuplekt.combineTuple
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakePreferencesManager(
    initialPomodoroLengthInMinutes: Int,
    initialShortBreakLengthInMinutes: Int,
    initialLongBreakLengthInMinutes: Int,
    initialPomodorosPerSet: Int,
    initialAutoStartNextTimer: Boolean,
) : PreferencesManager {

    private val pomodoroLengthInMinutes = MutableStateFlow(initialPomodoroLengthInMinutes)
    private val shortBreakLengthInMinutes = MutableStateFlow(initialShortBreakLengthInMinutes)
    private val longBreakLengthInMinutes = MutableStateFlow(initialLongBreakLengthInMinutes)
    private val pomodorosPerSet = MutableStateFlow(initialPomodorosPerSet)
    private val autoStartNextTimer = MutableStateFlow(initialAutoStartNextTimer)

    override val appPreferences: Flow<AppPreferences>
        get() = TODO("Not yet implemented")

    override val timerPreferences: Flow<TimerPreferences> = combineTuple(
        pomodoroLengthInMinutes,
        shortBreakLengthInMinutes,
        longBreakLengthInMinutes,
        pomodorosPerSet,
        autoStartNextTimer,
    ).map { (
                pomodoroLengthInMinutes,
                shortBreakLengthInMinutes,
                longBreakLengthInMinutes,
                pomodorosPerSet,
                autoStartNextTimer,
            ) ->
        TimerPreferences(
            pomodoroLengthInMinutes = pomodoroLengthInMinutes,
            shortBreakLengthInMinutes = shortBreakLengthInMinutes,
            longBreakLengthInMinutes = longBreakLengthInMinutes,
            pomodorosPerSet = pomodorosPerSet,
            autoStartNextTimer = autoStartNextTimer,
        )
    }

    override suspend fun updatePomodoroLength(lengthInMinutes: Int) {
        pomodoroLengthInMinutes.value = lengthInMinutes
    }

    override suspend fun updateShortBreakLength(lengthInMinutes: Int) {
        shortBreakLengthInMinutes.value = lengthInMinutes
    }

    override suspend fun updateLongBreakLength(lengthInMinutes: Int) {
        longBreakLengthInMinutes.value = lengthInMinutes
    }

    override suspend fun updatePomodorosPerSet(amount: Int) {
        pomodorosPerSet.value = amount
    }

    override suspend fun updateAutoStartNextTimer(autostart: Boolean) {
        autoStartNextTimer.value = autostart
    }

    override suspend fun updateSelectedTheme(theme: ThemeSelection) {
        TODO("Not yet implemented")
    }

    override suspend fun updateAppInstructionsDialogShown(shown: Boolean) {
        TODO("Not yet implemented")
    }
}