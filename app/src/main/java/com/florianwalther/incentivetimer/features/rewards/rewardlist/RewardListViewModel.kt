package com.florianwalther.incentivetimer.features.rewards.rewardlist

import androidx.lifecycle.*
import com.florianwalther.incentivetimer.data.db.Reward
import com.florianwalther.incentivetimer.data.db.RewardDao
import com.florianwalther.incentivetimer.features.rewards.rewardlist.model.RewardListScreenState
import com.zhuinden.flowcombinetuplekt.combineTuple
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RewardListViewModel @Inject constructor(
    private val rewardDao: RewardDao,
    savedStateHandle: SavedStateHandle,
) : ViewModel(), RewardListActions {

    private val rewards = rewardDao.getAllRewardsSortedByIsUnlockedDesc()

    private val selectedRewardIds =
        savedStateHandle.getLiveData<List<Long>>("selectedRewardIds", emptyList())

    private val selectedItemCount = selectedRewardIds.map { it.size }

    private val multiSelectionModeActive =
        savedStateHandle.getLiveData<Boolean>("multiSelectionModeActiveLiveData", false)

    private val showDeleteAllSelectedRewardsDialog =
        savedStateHandle.getLiveData<Boolean>("showDeleteAllSelectedRewardsDialog", false)

    private val showDeleteAllUnlockedRewardsDialog =
        savedStateHandle.getLiveData<Boolean>("showDeleteAllUnlockedRewardsDialog", false)

    val screenState = combineTuple(
        rewards,
        selectedRewardIds.asFlow(),
        selectedItemCount.asFlow(),
        multiSelectionModeActive.asFlow(),
        showDeleteAllSelectedRewardsDialog.asFlow(),
        showDeleteAllUnlockedRewardsDialog.asFlow()
    ).map { (
                rewards,
                selectedRewardIds,
                selectedItemCount,
                multiSelectionModeActive,
                showDeleteAllSelectedRewardsDialogLiveData,
                showDeleteAllUnlockedRewardsDialogLiveData
            ) ->
        RewardListScreenState(
            rewards = rewards,
            selectedRewardIds = selectedRewardIds,
            selectedItemCount = selectedItemCount,
            multiSelectionModeActive = multiSelectionModeActive,
            showDeleteAllSelectedRewardsDialog = showDeleteAllSelectedRewardsDialogLiveData,
            showDeleteAllUnlockedRewardsDialog = showDeleteAllUnlockedRewardsDialogLiveData,
        )
    }.asLiveData()

    private val eventChannel = Channel<RewardListEvent>()
    val events: Flow<RewardListEvent> = eventChannel.receiveAsFlow()

    sealed class RewardListEvent {
        data class ShowUndoRewardSnackbar(val reward: Reward) : RewardListEvent()
        data class NavigateToEditRewardScreen(val reward: Reward) : RewardListEvent()
    }

    init {
        viewModelScope.launch {
            rewards.collectLatest { rewards ->
                val rewardIds = rewards.map { it.id }
                selectedRewardIds.value = selectedRewardIds.value?.filter { rewardIds.contains(it) }
                if (selectedRewardIds.value.isNullOrEmpty()) {
                    cancelMultiSelectionMode()
                }
            }
        }
    }

    override fun onDeleteAllSelectedItemsClicked() {
        showDeleteAllSelectedRewardsDialog.value = true
    }

    override fun onDeleteAllSelectedRewardsConfirmed() {
        showDeleteAllSelectedRewardsDialog.value = false
        viewModelScope.launch {
            val rewards = rewards.first()
            val selectedRewardIds = selectedRewardIds.value ?: emptyList()
            val selectedRewards =
                rewards.filter { selectedRewardIds.contains(it.id) }
            rewardDao.deleteRewards(selectedRewards)
            cancelMultiSelectionMode()
        }
    }

    override fun onDeleteAllSelectedRewardsDialogDismissed() {
        showDeleteAllSelectedRewardsDialog.value = false
    }

    override fun onDeleteAllUnlockedRewardsClicked() {
        showDeleteAllUnlockedRewardsDialog.value = true
    }

    override fun onDeleteAllUnlockedRewardsConfirmed() {
        showDeleteAllUnlockedRewardsDialog.value = false
        viewModelScope.launch {
            rewardDao.deleteAllUnlockedRewards()
        }
    }

    override fun onDeleteAllUnlockedRewardsDialogDismissed() {
        showDeleteAllUnlockedRewardsDialog.value = false
    }

    override fun onRewardClicked(reward: Reward) {
        val multiSelectionModeActive = multiSelectionModeActive.value
        if (multiSelectionModeActive == false) {
            viewModelScope.launch {
                eventChannel.send(RewardListEvent.NavigateToEditRewardScreen(reward))
            }
        } else {
            val selectedRewardIds = selectedRewardIds.value
            if (selectedRewardIds != null) {
                addOrRemoveSelectedReward(reward)
            }
        }
    }

    override fun onRewardLongClicked(reward: Reward) {
        if (multiSelectionModeActive.value == false) {
            multiSelectionModeActive.value = true
        }
        addOrRemoveSelectedReward(reward)
    }

    override fun onCancelMultiSelectionModeClicked() {
        cancelMultiSelectionMode()
    }

    private fun cancelMultiSelectionMode() {
        if (multiSelectionModeActive.value == false) return
        selectedRewardIds.value = emptyList()
        multiSelectionModeActive.value = false
    }

    private fun addOrRemoveSelectedReward(reward: Reward) {
        val selectedRewardIds = selectedRewardIds.value
        if (selectedRewardIds != null) {
            if (selectedRewardIds.contains(reward.id)) {
                val selectedRewardsUpdate = selectedRewardIds.toMutableList().apply {
                    remove(reward.id)
                    if (this.isEmpty()) {
                        multiSelectionModeActive.value = false
                    }
                }
                this.selectedRewardIds.value = selectedRewardsUpdate
            } else {
                val selectedRewardsUpdate = selectedRewardIds.toMutableList().apply {
                    add(reward.id)
                }
                this.selectedRewardIds.value = selectedRewardsUpdate
            }
        }
    }

    override fun onRewardSwiped(reward: Reward) {
        viewModelScope.launch {
            rewardDao.deleteReward(reward)
            eventChannel.send(RewardListEvent.ShowUndoRewardSnackbar(reward))
        }
    }

    override fun onUndoDeleteRewardConfirmed(reward: Reward) {
        viewModelScope.launch {
            rewardDao.insertReward(reward)
        }
    }
}