package com.florianwalther.incentivetimer.features.timer

interface TimerScreenActions {
    fun onStartStopTimerClicked()
    fun onResetTimerClicked()
    fun onResetTimerConfirmed()
    fun onResetTimerDialogDismissed()
    fun onSkipBreakClicked()
    fun onSkipBreakConfirmed()
    fun onSkipBreakDialogDismissed()
    fun onResetPomodoroSetClicked()
    fun onResetPomodoroSetConfirmed()
    fun onResetPomodoroSetDialogDismissed()
    fun onResetPomodoroCountClicked()
    fun onResetPomodoroCountConfirmed()
    fun onResetPomodoroCountDialogDismissed()
}