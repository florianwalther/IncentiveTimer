package com.florianwalther.incentivetimer.features.rewards.addeditreward

import androidx.lifecycle.*
import com.florianwalther.incentivetimer.data.db.Reward
import com.florianwalther.incentivetimer.data.db.RewardDao
import com.florianwalther.incentivetimer.core.ui.IconKey
import com.florianwalther.incentivetimer.core.ui.screenspecs.AddEditRewardScreenSpec
import com.florianwalther.incentivetimer.features.rewards.addeditreward.model.AddEditRewardScreenState
import com.zhuinden.flowcombinetuplekt.combineTuple
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditRewardViewModel @Inject constructor(
    private val rewardDao: RewardDao,
    savedStateHandle: SavedStateHandle,
) : ViewModel(), AddEditRewardScreenActions {

    private val rewardId = AddEditRewardScreenSpec.getRewardIdFromSavedStateHandle(savedStateHandle)
    private val rewardInput = savedStateHandle.getLiveData<Reward>(
        KEY_REWARD_LIVE_DATA,
        Reward.DEFAULT
    )

    val isEditMode = AddEditRewardScreenSpec.isEditMode(rewardId)

    private val unlockedStateCheckboxVisible =
        savedStateHandle.getLiveData<Boolean>("unlockedStateCheckboxVisible", false)

    private val showRewardIconSelectionDialog =
        savedStateHandle.getLiveData<Boolean>("showRewardIconSelectionDialog", false)

    private val showDeleteRewardConfirmationDialog =
        savedStateHandle.getLiveData<Boolean>("showDeleteRewardConfirmationDialog", false)

    private val rewardNameInputIsError =
        savedStateHandle.getLiveData<Boolean>("rewardNameInputIsError", false)

    val screenState = combineTuple(
        rewardInput.asFlow(),
        unlockedStateCheckboxVisible.asFlow(),
        showRewardIconSelectionDialog.asFlow(),
        showDeleteRewardConfirmationDialog.asFlow(),
        rewardNameInputIsError.asFlow(),
    ).map { (
                rewardInput,
                unlockedStateCheckboxVisible,
                showRewardIconSelectionDialog,
                showDeleteRewardConfirmationDialog,
                rewardNameInputIsError,
            ) ->
        AddEditRewardScreenState(
            rewardInput = rewardInput,
            unlockedStateCheckboxVisible = unlockedStateCheckboxVisible,
            showRewardIconSelectionDialog = showRewardIconSelectionDialog,
            showDeleteRewardConfirmationDialog = showDeleteRewardConfirmationDialog,
            rewardNameInputIsError = rewardNameInputIsError,
        )
    }.asLiveData()

    private val eventChannel = Channel<AddEditRewardEvent>()
    val events: Flow<AddEditRewardEvent> = eventChannel.receiveAsFlow()

    sealed class AddEditRewardEvent {
        object RewardCreated : AddEditRewardEvent()
        object RewardUpdated : AddEditRewardEvent()
        object RewardDeleted : AddEditRewardEvent()
    }

    init {
        if (!savedStateHandle.contains(KEY_REWARD_LIVE_DATA)) {
            if (rewardId != null && isEditMode) {
                viewModelScope.launch {
                    rewardInput.value = rewardDao.getRewardById(rewardId).firstOrNull()
                }
            } else {
                rewardInput.value = Reward.DEFAULT
            }
        }
        if (rewardId != null) {
            viewModelScope.launch {
                rewardDao.getRewardById(rewardId)
                    .distinctUntilChangedBy { it?.isUnlocked }
                    .filter { it?.isUnlocked == true }
                    .collectLatest { reward ->
                        if (reward != null) {
                            unlockedStateCheckboxVisible.value = reward.isUnlocked
                            rewardInput.value =
                                rewardInput.value?.copy(isUnlocked = reward.isUnlocked)
                        }
                    }
            }
        }
    }

    override fun onRewardNameInputChanged(input: String) {
        rewardInput.value = rewardInput.value?.copy(name = input)
    }

    override fun onChanceInPercentInputChanged(input: Int) {
        rewardInput.value = rewardInput.value?.copy(chanceInPercent = input)
    }

    override fun onRewardIconSelected(iconKey: IconKey) {
        rewardInput.value = rewardInput.value?.copy(iconKey = iconKey)
        showRewardIconSelectionDialog.value = false
    }

    override fun onRewardIconButtonClicked() {
        showRewardIconSelectionDialog.value = true
    }

    override fun onRewardIconDialogDismissed() {
        showRewardIconSelectionDialog.value = false
    }

    override fun onSaveClicked() {
        val reward = rewardInput.value ?: return
        rewardNameInputIsError.value = false

        viewModelScope.launch {
            if (reward.name.isNotBlank()) {
                if (isEditMode) {
                    updateReward(reward)
                } else {
                    createReward(reward)
                }
            } else {
                rewardNameInputIsError.value = true
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

    override fun onRewardUnlockedCheckedChanged(unlocked: Boolean) {
        rewardInput.value = rewardInput.value?.copy(isUnlocked = unlocked)
    }

    override fun onDeleteRewardClicked() {
        showDeleteRewardConfirmationDialog.value = true
    }

    override fun onDeleteRewardConfirmed() {
        showDeleteRewardConfirmationDialog.value = false
        viewModelScope.launch {
            val reward = rewardInput.value
            if (reward != null) {
                rewardDao.deleteReward(reward)
                eventChannel.send(AddEditRewardEvent.RewardDeleted)
            }
        }
    }

    override fun onDeleteRewardDialogDismissed() {
        showDeleteRewardConfirmationDialog.value = false
    }
}

private const val KEY_REWARD_LIVE_DATA = "KEY_REWARD_LIVE_DATA"

const val ADD_EDIT_REWARD_RESULT = "ADD_EDIT_REWARD_RESULT"
const val RESULT_REWARD_ADDED = "RESULT_REWARD_ADDED"
const val RESULT_REWARD_UPDATED = "RESULT_REWARD_UPDATED"
const val RESULT_REWARD_DELETE = "RESULT_REWARD_DELETED"