package com.florianwalther.incentivetimer.features.settings

interface SettingsScreenActions {
    fun onPomodoroLengthPreferenceClicked()
    fun onPomodoroLengthSet(value: Int)
    fun onPomodoroLengthDialogDismissed()
    fun onShortBreakLengthPreferenceClicked()
    fun onShortBreakLengthSet(value: Int)
    fun onShortBreakLengthDialogDismissed()
    fun onLongBreakLengthPreferenceClicked()
    fun onLongBreakLengthSet(value: Int)
    fun onLongBreakLengthDialogDismissed()
}