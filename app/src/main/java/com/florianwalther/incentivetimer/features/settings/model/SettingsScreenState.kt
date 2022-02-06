package com.florianwalther.incentivetimer.features.settings.model

import com.florianwalther.incentivetimer.data.datastore.AppPreferences
import com.florianwalther.incentivetimer.data.datastore.ThemeSelection
import com.florianwalther.incentivetimer.data.datastore.TimerPreferences

data class SettingsScreenState(
    val appPreferences: AppPreferences,
    val timerPreferences: TimerPreferences,
    val showThemeDialog: Boolean,
    val showPomodoroLengthDialog: Boolean,
    val showShortBreakLengthDialog: Boolean,
    val showLongBreakLengthDialog: Boolean,
    val showPomodorosPerSetDialog: Boolean,
    val showAppInstructionsDialog: Boolean,
) {
    companion object {
        val initialState = SettingsScreenState(
            appPreferences = AppPreferences(
                selectedTheme = ThemeSelection.SYSTEM,
                appInstructionsDialogShown = false,
            ),
            timerPreferences = TimerPreferences(
                pomodoroLengthInMinutes = 0,
                shortBreakLengthInMinutes = 0,
                longBreakLengthInMinutes = 0,
                pomodorosPerSet = 0,
                autoStartNextTimer = false,
            ),
            showThemeDialog = false,
            showPomodoroLengthDialog = false,
            showShortBreakLengthDialog = false,
            showLongBreakLengthDialog = false,
            showPomodorosPerSetDialog = false,
            showAppInstructionsDialog = false,
        )
    }
}