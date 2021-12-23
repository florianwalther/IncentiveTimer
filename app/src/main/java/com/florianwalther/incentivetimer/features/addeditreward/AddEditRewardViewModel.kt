package com.florianwalther.incentivetimer.features.addeditreward

import androidx.lifecycle.*
import com.florianwalther.incentivetimer.data.Reward
import com.florianwalther.incentivetimer.data.RewardDao
import com.florianwalther.incentivetimer.core.ui.IconKey
import com.florianwalther.incentivetimer.core.ui.defaultRewardIconKey
import com.florianwalther.incentivetimer.core.ui.screenspecs.AddEditRewardScreenSpec
import com.florianwalther.incentivetimer.core.ui.screenspecs.NO_REWARD_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditRewardViewModel @Inject constructor(
    private val rewardDao: RewardDao,
    savedStateHandle: SavedStateHandle,
) : ViewModel(), AddEditRewardScreenActions {

    private val rewardId = AddEditRewardScreenSpec.getRewardIdFromSavedStateHandle(savedStateHandle)
    private val rewardLiveData = savedStateHandle.getLiveData<Reward>(KEY_REWARD_LIVE_DATA)

    val isEditMode = AddEditRewardScreenSpec.isEditMode(rewardId)

    val rewardNameInput = rewardLiveData.map {
        it.name
    }

    val chanceInPercentInput = rewardLiveData.map {
        it.chanceInPercent
    }

    val rewardIconKeySelection = rewardLiveData.map {
        it.iconKey
    }

    private val rewardNameInputIsErrorLiveData =
        savedStateHandle.getLiveData<Boolean>("rewardNameInputIsError", false)
    val rewardNameInputIsError: LiveData<Boolean> = rewardNameInputIsErrorLiveData

    private val showRewardIconSelectionDialogLiveData =
        savedStateHandle.getLiveData<Boolean>("showRewardIconSelectionDialogLiveData", false)
    val showRewardIconSelectionDialog: LiveData<Boolean> = showRewardIconSelectionDialogLiveData

    private val showDeleteRewardConfirmationDialogLiveData =
        savedStateHandle.getLiveData<Boolean>("showDeleteRewardConfirmationDialogLiveData")
    val showDeleteRewardConfirmationDialog: LiveData<Boolean> =
        showDeleteRewardConfirmationDialogLiveData

    private val eventChannel = Channel<AddEditRewardEvent>()
    val events = eventChannel.receiveAsFlow()

    init {
        if (!savedStateHandle.contains("rewardLiveData")) {
            if (rewardId != null && isEditMode) {
                viewModelScope.launch {
                    rewardLiveData.value = rewardDao.getRewardById(rewardId)
                }
            } else {
                rewardLiveData.value = Reward("", 10, defaultRewardIconKey)
            }
        }
    }

    sealed class AddEditRewardEvent {
        object RewardCreated : AddEditRewardEvent()
        object RewardUpdated : AddEditRewardEvent()
        object RewardDeleted : AddEditRewardEvent()
    }

    override fun onRewardNameInputChanged(input: String) {
        rewardLiveData.value = rewardLiveData.value?.copy(name = input)
    }

    override fun onChanceInPercentInputChanged(input: Int) {
        rewardLiveData.value = rewardLiveData.value?.copy(chanceInPercent = input)
    }

    override fun onRewardIconButtonClicked() {
        showRewardIconSelectionDialogLiveData.value = true
    }

    override fun onRewardIconSelected(iconKey: IconKey) {
        rewardLiveData.value = rewardLiveData.value?.copy(iconKey = iconKey)
    }

    override fun onRewardIconDialogDismissed() {
        showRewardIconSelectionDialogLiveData.value = false
    }

    override fun onSaveClicked() {
        val reward = rewardLiveData.value ?: return
        rewardNameInputIsErrorLiveData.value = false

        viewModelScope.launch {
            if (reward.name.isNotBlank()) {
                if (isEditMode) {
                    updateReward(reward)
                } else {
                    createReward(reward)
                }
            } else {
                rewardNameInputIsErrorLiveData.value = true
            }
        }
    }

    private suspend fun updateReward(reward: Reward) {
        rewardDao.updateReward(reward)
        eventChannel.send(AddEditRewardEvent.RewardUpdated)
    }

    private suspend fun createReward(reward: Reward) {
        rewardDao.insertReward(reward)
        eventChannel.send(AddEditRewardEvent.RewardCreated)
    }

    override fun onDeleteRewardClicked() {
        showDeleteRewardConfirmationDialogLiveData.value = true
    }

    override fun onDeleteRewardConfirmed() {
        showDeleteRewardConfirmationDialogLiveData.value = false
        viewModelScope.launch {
            val reward = rewardLiveData.value
            if (reward != null) {
                rewardDao.deleteReward(reward)
                eventChannel.send(AddEditRewardEvent.RewardDeleted)
            }
        }
    }

    override fun onDeleteRewardDialogDismissed() {
        showDeleteRewardConfirmationDialogLiveData.value = false
    }
}

private const val KEY_REWARD_LIVE_DATA = "KEY_REWARD_LIVE_DATA"

const val ADD_EDIT_REWARD_RESULT = "ADD_EDIT_REWARD_RESULT"
const val RESULT_REWARD_ADDED = "RESULT_REWARD_ADDED"
const val RESULT_REWARD_UPDATED = "RESULT_REWARD_UPDATED"
const val RESULT_REWARD_DELETE = "RESULT_REWARD_DELETED"