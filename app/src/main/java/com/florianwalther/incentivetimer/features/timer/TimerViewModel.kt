package com.florianwalther.incentivetimer.features.timer

import androidx.lifecycle.*
import com.florianwalther.incentivetimer.features.timer.model.TimerScreenState
import com.zhuinden.flowcombinetuplekt.combineTuple
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import logcat.logcat
import javax.inject.Inject

@HiltViewModel
class TimerViewModel @Inject constructor(
    private val pomodoroTimerManager: PomodoroTimerManager,
    savedStateHandle: SavedStateHandle,
) : ViewModel(), TimerScreenActions {

    val pomodoroTimerState = pomodoroTimerManager.pomodoroTimerState
        .onEach {
            logcat { "timerRunning = ${it.timerRunning}" }
        }
        .asLiveData()

    private val showResetTimerConfirmationDialog =
        savedStateHandle.getLiveData<Boolean>("showResetTimerConfirmationDialog", false)

    private val showSkipBreakConfirmationDialog =
        savedStateHandle.getLiveData<Boolean>("showSkipBreakConfirmationDialog", false)

    private val showResetPomodoroSetConfirmationDialog =
        savedStateHandle.getLiveData<Boolean>(
            "showResetPomodoroSetConfirmationDialog",
            false
        )

    private val showResetPomodoroCountConfirmationDialog =
        savedStateHandle.getLiveData<Boolean>(
            "showResetPomodoroCountConfirmationDialog",
            false
        )

    val screenState = combineTuple(
        showResetTimerConfirmationDialog.asFlow(),
        showSkipBreakConfirmationDialog.asFlow(),
        showResetPomodoroSetConfirmationDialog.asFlow(),
        showResetPomodoroCountConfirmationDialog.asFlow(),
    ).map { (
                showResetTimerConfirmationDialogLiveData,
                showSkipBreakConfirmationDialogLivedata,
                showResetPomodoroSetConfirmationDialogLiveData,
                showResetPomodoroCountConfirmationDialogLiveData
            ) ->
        TimerScreenState(
            showResetTimerConfirmationDialog = showResetTimerConfirmationDialogLiveData,
            showSkipBreakConfirmationDialog = showSkipBreakConfirmationDialogLivedata,
            showResetPomodoroSetConfirmationDialog = showResetPomodoroSetConfirmationDialogLiveData,
            showResetPomodoroCountConfirmationDialog = showResetPomodoroCountConfirmationDialogLiveData,
        )
    }.asLiveData()

    override fun onStartStopTimerClicked() {
        pomodoroTimerManager.startStopTimer()
    }

    override fun onResetTimerClicked() {
        showResetTimerConfirmationDialog.value = true
    }

    override fun onResetTimerConfirmed() {
        showResetTimerConfirmationDialog.value = false
        viewModelScope.launch {
            pomodoroTimerManager.stopAndResetTimer()
        }
    }

    override fun onResetTimerDialogDismissed() {
        showResetTimerConfirmationDialog.value = false
    }

    override fun onSkipBreakClicked() {
        showSkipBreakConfirmationDialog.value = true
    }

    override fun onSkipBreakConfirmed() {
        showSkipBreakConfirmationDialog.value = false
        pomodoroTimerManager.skipBreak()
    }

    override fun onSkipBreakDialogDismissed() {
        showSkipBreakConfirmationDialog.value = false
    }

    override fun onResetPomodoroSetClicked() {
        showResetPomodoroSetConfirmationDialog.value = true
    }

    override fun onResetPomodoroSetConfirmed() {
        showResetPomodoroSetConfirmationDialog.value = false
        viewModelScope.launch {
            pomodoroTimerManager.resetPomodoroSet()
        }
    }

    override fun onResetPomodoroSetDialogDismissed() {
        showResetPomodoroSetConfirmationDialog.value = false
    }

    override fun onResetPomodoroCountClicked() {
        showResetPomodoroCountConfirmationDialog.value = true
    }

    override fun onResetPomodoroCountConfirmed() {
        showResetPomodoroCountConfirmationDialog.value = false
        pomodoroTimerManager.resetPomodoroCount()
    }

    override fun onResetPomodoroCountDialogDismissed() {
        showResetPomodoroCountConfirmationDialog.value = false
    }
}