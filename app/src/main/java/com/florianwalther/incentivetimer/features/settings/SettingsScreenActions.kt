package com.florianwalther.incentivetimer.features.settings

import com.florianwalther.incentivetimer.data.datastore.ThemeSelection

interface SettingsScreenActions {
    fun onPomodoroLengthPreferenceClicked()
    fun onPomodoroLengthSet(lengthInMinutes: Int)
    fun onPomodoroLengthDialogDismissed()
    fun onShortBreakLengthPreferenceClicked()
    fun onShortBreakLengthSet(lengthInMinutes: Int)
    fun onShortBreakLengthDialogDismissed()
    fun onLongBreakLengthPreferenceClicked()
    fun onLongBreakLengthSet(lengthInMinutes: Int)
    fun onLongBreakLengthDialogDismissed()
    fun onPomodorosPerSetPreferenceClicked()
    fun onPomodorosPerSetSet(amount: Int)
    fun onPomodorosPerSetDialogDismissed()
    fun onAutoStartNextTimerCheckedChanged(checked: Boolean)
    fun onShowAppInstructionsClicked()
    fun onAppInstructionsDialogDismissed()
    fun onThemePreferenceClicked()
    fun onThemeSelected(theme: ThemeSelection)
    fun onThemeDialogDismissed()
}