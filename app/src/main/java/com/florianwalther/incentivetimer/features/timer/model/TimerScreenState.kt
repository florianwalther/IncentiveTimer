package com.florianwalther.incentivetimer.features.timer.model

data class TimerScreenState(
    val showResetTimerConfirmationDialog: Boolean,
    val showSkipBreakConfirmationDialog: Boolean,
    val showResetPomodoroSetConfirmationDialog: Boolean,
    val showResetPomodoroCountConfirmationDialog: Boolean,
) {
    companion object {
        val initialState = TimerScreenState(
            showResetTimerConfirmationDialog = false,
            showSkipBreakConfirmationDialog = false,
            showResetPomodoroSetConfirmationDialog = false,
            showResetPomodoroCountConfirmationDialog = false,
        )
    }
}