package com.florianwalther.incentivetimer.features.rewards.rewardlist

import androidx.lifecycle.*
import com.florianwalther.incentivetimer.data.Reward
import com.florianwalther.incentivetimer.data.RewardDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RewardListViewModel @Inject constructor(
    private val rewardDao: RewardDao,
    savedStateHandle: SavedStateHandle,
) : ViewModel(), RewardListActions {

    private val rewardsFlow = rewardDao.getAllRewardsSortedByIsUnlockedDesc()
    val rewards = rewardsFlow.asLiveData()

    private val selectedRewardsLiveData =
        savedStateHandle.getLiveData<List<Reward>>("selectedRewardsLiveData", listOf())

    val selectedRewards: LiveData<List<Reward>> = combine(
        selectedRewardsLiveData.asFlow(),
        rewardsFlow
    ) { selectedRewards, rewards ->
        selectedRewards.filter { rewards.contains(it) }
    }.onEach { selectedRewards ->
        if (selectedRewards.isEmpty()) {
            cancelMultiSelectionMode()
        }
    }.asLiveData()

    val selectedItemCount = selectedRewards.map { it.size }

    private val multiSelectionModeActiveLiveData =
        savedStateHandle.getLiveData<Boolean>("multiSelectionModeActiveLiveData", false)
    val multiSelectionModeActive: LiveData<Boolean> = multiSelectionModeActiveLiveData

    private val showDeleteAllSelectedRewardsDialogLiveData =
        savedStateHandle.getLiveData<Boolean>("showDeleteAllSelectedRewardsDialogLiveData", false)
    val showDeleteAllSelectedRewardsDialog: LiveData<Boolean> =
        showDeleteAllSelectedRewardsDialogLiveData

    private val showDeleteAllUnlockedRewardsDialogLiveData =
        savedStateHandle.getLiveData<Boolean>("showDeleteAllUnlockedRewardsDialogLiveData", false)
    val showDeleteAllUnlockedRewardsDialog: LiveData<Boolean> =
        showDeleteAllUnlockedRewardsDialogLiveData

    private val eventChannel = Channel<Event>()
    val events = eventChannel.receiveAsFlow()

    sealed class Event {
        data class ShowUndoRewardSnackbar(val reward: Reward) : Event()
        data class NavigateToEditRewardScreen(val reward: Reward) : Event()
    }

    override fun onDeleteAllUnlockedRewardsClicked() {
        showDeleteAllUnlockedRewardsDialogLiveData.value = true
    }

    override fun onDeleteAllUnlockedRewardsConfirmed() {
        showDeleteAllUnlockedRewardsDialogLiveData.value = false
        viewModelScope.launch {
            rewardDao.deleteAllUnlockedRewards()
        }
    }

    override fun onDeleteAllUnlockedRewardsDialogDismissed() {
        showDeleteAllUnlockedRewardsDialogLiveData.value = false
    }

    override fun onRewardClicked(reward: Reward) {
        val multiSelectionModeActive = multiSelectionModeActiveLiveData.value
        if (multiSelectionModeActive == false) {
            viewModelScope.launch {
                eventChannel.send(Event.NavigateToEditRewardScreen(reward))
            }
        } else {
            val selectedRewards = selectedRewardsLiveData.value
            if (selectedRewards != null) {
                addOrRemoveSelectedReward(reward)
            }
        }
    }

    override fun onRewardLongClicked(reward: Reward) {
        val multiSelectionModeActive = multiSelectionModeActiveLiveData.value
        if (multiSelectionModeActive == false) {
            multiSelectionModeActiveLiveData.value = true
        }
        addOrRemoveSelectedReward(reward)
    }

    override fun onCancelMultiSelectionModeClicked() {
        cancelMultiSelectionMode()
    }

    private fun cancelMultiSelectionMode() {
        selectedRewardsLiveData.value = emptyList()
        multiSelectionModeActiveLiveData.value = false
    }

    override fun onDeleteAllSelectedItemsClicked() {
        showDeleteAllSelectedRewardsDialogLiveData.value = true
    }

    override fun onDeleteAllSelectedRewardsConfirmed() {
        showDeleteAllSelectedRewardsDialogLiveData.value = false
        viewModelScope.launch {
            val selectedRewards = selectedRewardsLiveData.value ?: emptyList()
            rewardDao.deleteRewards(selectedRewards)
            cancelMultiSelectionMode()
        }
    }

    override fun onDeleteAllSelectedRewardsDialogDismissed() {
        showDeleteAllSelectedRewardsDialogLiveData.value = false
    }

    private fun addOrRemoveSelectedReward(reward: Reward) {
        val selectedRewards = selectedRewardsLiveData.value
        if (selectedRewards != null) {
            if (selectedRewards.contains(reward)) {
                val selectedRewardsUpdate = selectedRewards.toMutableList().apply {
                    remove(reward)
                    if (this.isEmpty()) {
                        multiSelectionModeActiveLiveData.value = false
                    }
                }
                selectedRewardsLiveData.value = selectedRewardsUpdate
            } else {
                val selectedRewardsUpdate = selectedRewards.toMutableList().apply {
                    add(reward)
                }
                selectedRewardsLiveData.value = selectedRewardsUpdate
            }
        }
    }

    override fun onRewardSwiped(reward: Reward) {
        viewModelScope.launch {
//            rewardDao.deleteReward(reward)
            eventChannel.send(Event.ShowUndoRewardSnackbar(reward))
        }
    }

    override fun onUndoDeleteRewardConfirmed(reward: Reward) {
        viewModelScope.launch {
            rewardDao.insertReward(reward)
        }
    }
}