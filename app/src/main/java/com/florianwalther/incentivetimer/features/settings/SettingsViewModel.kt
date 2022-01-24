package com.florianwalther.incentivetimer.features.settings

import androidx.lifecycle.*
import com.florianwalther.incentivetimer.data.datastore.DefaultPreferencesManager
import com.florianwalther.incentivetimer.features.settings.model.SettingsScreenState
import com.zhuinden.flowcombinetuplekt.combineTuple
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: DefaultPreferencesManager,
    savedStateHandle: SavedStateHandle,
) : ViewModel(), SettingsScreenActions {

    private val timerPreferences = preferencesManager.timerPreferences

    private val showPomodoroLengthDialog =
        savedStateHandle.getLiveData<Boolean>("showPomodoroLengthDialog", false)

    private val showShortBreakLengthDialog =
        savedStateHandle.getLiveData<Boolean>("showShortBreakLengthDialog", false)

    private val showLongBreakLengthDialog =
        savedStateHandle.getLiveData<Boolean>("showLongBreakLengthDialog", false)

    private val showPomodorosPerSetDialog =
        savedStateHandle.getLiveData<Boolean>("showPomodorosPerSetDialog", false)

    val screenState = combineTuple(
        timerPreferences,
        showPomodoroLengthDialog.asFlow(),
        showShortBreakLengthDialog.asFlow(),
        showLongBreakLengthDialog.asFlow(),
        showPomodorosPerSetDialog.asFlow(),
    ).map { (
                timerPreferences,
                showPomodoroLengthDialog,
                showShortBreakLengthDialog,
                showLongBreakLengthDialog,
                showPomodorosPerSetDialog,
            ) ->
        SettingsScreenState(
            timerPreferences = timerPreferences,
            showPomodoroLengthDialog = showPomodoroLengthDialog,
            showShortBreakLengthDialog = showShortBreakLengthDialog,
            showLongBreakLengthDialog = showLongBreakLengthDialog,
            showPomodorosPerSetDialog = showPomodorosPerSetDialog,
        )
    }.asLiveData()

    override fun onPomodoroLengthPreferenceClicked() {
        showPomodoroLengthDialog.value = true
    }

    override fun onPomodoroLengthSet(lengthInMinutes: Int) {
        viewModelScope.launch {
            preferencesManager.updatePomodoroLength(lengthInMinutes)
            showPomodoroLengthDialog.value = false
        }
    }

    override fun onPomodoroLengthDialogDismissed() {
        showPomodoroLengthDialog.value = false
    }

    override fun onShortBreakLengthPreferenceClicked() {
        showShortBreakLengthDialog.value = true
    }

    override fun onShortBreakLengthSet(lengthInMinutes: Int) {
        viewModelScope.launch {
            preferencesManager.updateShortBreakLength(lengthInMinutes)
            showShortBreakLengthDialog.value = false
        }
    }

    override fun onShortBreakLengthDialogDismissed() {
        showShortBreakLengthDialog.value = false
    }

    override fun onLongBreakLengthPreferenceClicked() {
        showLongBreakLengthDialog.value = true
    }

    override fun onLongBreakLengthSet(lengthInMinutes: Int) {
        viewModelScope.launch {
            preferencesManager.updateLongBreakLength(lengthInMinutes)
            showLongBreakLengthDialog.value = false
        }
    }

    override fun onLongBreakLengthDialogDismissed() {
        showLongBreakLengthDialog.value = false
    }

    override fun onPomodorosPerSetPreferenceClicked() {
        showPomodorosPerSetDialog.value = true
    }

    override fun onPomodorosPerSetSet(amount: Int) {
        viewModelScope.launch {
            preferencesManager.updatePomodorosPerSet(amount)
            showPomodorosPerSetDialog.value = false
        }
    }

    override fun onPomodorosPerSetDialogDismissed() {
        showPomodorosPerSetDialog.value = false
    }
}