package com.florianwalther.incentivetimer.application

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.florianwalther.incentivetimer.data.datastore.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ITActivityViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    val appPreferences = preferencesManager.appPreferences.asLiveData()

    fun onAppInstructionsDialogDismissed() {
        viewModelScope.launch {
            preferencesManager.updateAppInstructionsDialogShown(true)
        }
    }
}