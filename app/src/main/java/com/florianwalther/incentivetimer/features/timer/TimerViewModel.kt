package com.florianwalther.incentivetimer.features.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TimerViewModel @Inject constructor(
    private val pomodoroTimerManager: PomodoroTimerManager,
) : ViewModel(), TimerScreenActions {

    val pomodoroTimerState = pomodoroTimerManager.pomodoroTimerState.asLiveData()

    override fun onStartStopTimerClicked() {
        pomodoroTimerManager.startStopTimer()
    }

    override fun onResetTimerClicked() {
        pomodoroTimerManager.resetTimer()
    }

    override fun onResetPomodoroSetClicked() {
        pomodoroTimerManager.resetPomodoroSet()
    }

    override fun onResetPomodoroCountClicked() {
        pomodoroTimerManager.resetPomodoroCount()
    }
}