package com.florianwalther.incentivetimer.features.timer

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TimerViewModel @Inject constructor(
    private val pomodoroTimerManager: PomodoroTimerManager,
    savedStateHandle: SavedStateHandle,
) : ViewModel(), TimerScreenActions {

    val pomodoroTimerState = pomodoroTimerManager.pomodoroTimerState.asLiveData()

    private val showResetTimerConfirmationDialogLiveData =
        savedStateHandle.getLiveData<Boolean>("showResetTimerConfirmationDialogLiveData", false)
    val showResetTimerConfirmationDialog: LiveData<Boolean> =
        showResetTimerConfirmationDialogLiveData

    private val showSkipBreakConfirmationDialogLivedata =
        savedStateHandle.getLiveData<Boolean>("showSkipBreakConfirmationDialogLivedata", false)
    val showSkipBreakConfirmationDialog: LiveData<Boolean> = showSkipBreakConfirmationDialogLivedata

    private val showResetPomodoroSetConfirmationDialogLiveData =
        savedStateHandle.getLiveData<Boolean>(
            "showResetPomodoroSetConfirmationDialogLiveData",
            false
        )
    val showResetPomodoroSetConfirmationDialog: LiveData<Boolean> =
        showResetPomodoroSetConfirmationDialogLiveData

    private val showResetPomodoroCountConfirmationDialogLiveData =
        savedStateHandle.getLiveData<Boolean>(
            "showResetPomodoroCountConfirmationDialogLiveData",
            false
        )
    val showResetPomodoroCountConfirmationDialog: LiveData<Boolean> =
        showResetPomodoroCountConfirmationDialogLiveData

    override fun onStartStopTimerClicked() {
        pomodoroTimerManager.startStopTimer()
    }

    override fun onResetTimerClicked() {
        showResetTimerConfirmationDialogLiveData.value = true
    }

    override fun onResetTimerConfirmed() {
        showResetTimerConfirmationDialogLiveData.value = false
        pomodoroTimerManager.resetTimer()
    }

    override fun onResetTimerDialogDismissed() {
        showResetTimerConfirmationDialogLiveData.value = false
    }

    override fun onSkipBreakClicked() {
        showSkipBreakConfirmationDialogLivedata.value = true
    }

    override fun onSkipBreakConfirmed() {
        showSkipBreakConfirmationDialogLivedata.value = false
        pomodoroTimerManager.skipBreak()
    }

    override fun onSkipBreakDialogDismissed() {
        showSkipBreakConfirmationDialogLivedata.value = false
    }

    override fun onResetPomodoroSetClicked() {
        showResetPomodoroSetConfirmationDialogLiveData.value = true
    }

    override fun onResetPomodoroSetConfirmed() {
        showResetPomodoroSetConfirmationDialogLiveData.value = false
        pomodoroTimerManager.resetPomodoroSet()
    }

    override fun onResetPomodoroSetDialogDismissed() {
        showResetPomodoroSetConfirmationDialogLiveData.value = false
    }

    override fun onResetPomodoroCountClicked() {
        showResetPomodoroCountConfirmationDialogLiveData.value = true
    }

    override fun onResetPomodoroCountConfirmed() {
        showResetPomodoroCountConfirmationDialogLiveData.value = false
        pomodoroTimerManager.resetPomodoroCount()
    }

    override fun onResetPomodoroCountDialogDismissed() {
        showResetPomodoroCountConfirmationDialogLiveData.value = false
    }
}