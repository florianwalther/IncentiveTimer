package com.florianwalther.incentivetimer.features.settings.model

import com.florianwalther.incentivetimer.data.preferences.TimerPreferences

data class SettingsScreenState(
    val timerPreferences: TimerPreferences,
    val showPomodoroLengthDialog: Boolean,
    val showShortBreakLengthDialog: Boolean,
    val showLongBreakLengthDialog: Boolean,
    val showPomodorosPerSetDialog: Boolean,
) {
    companion object {
        val initialState = SettingsScreenState(
            timerPreferences = TimerPreferences(
                pomodoroLengthInMinutes = 0,
                shortBreakLengthInMinutes = 0,
                longBreakLengthInMinutes = 0,
                pomodorosPerSet = 0,
            ),
            showPomodoroLengthDialog = false,
            showShortBreakLengthDialog = false,
            showLongBreakLengthDialog = false,
            showPomodorosPerSetDialog = false,
        )
    }
}