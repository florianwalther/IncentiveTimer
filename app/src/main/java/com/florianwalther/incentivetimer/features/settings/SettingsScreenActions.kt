package com.florianwalther.incentivetimer.features.settings

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
}