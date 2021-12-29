package com.florianwalther.incentivetimer.features.rewards.rewardlist

import androidx.lifecycle.*
import com.florianwalther.incentivetimer.data.Reward
import com.florianwalther.incentivetimer.data.RewardDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RewardListViewModel @Inject constructor(
    private val rewardDao: RewardDao,
    savedStateHandle: SavedStateHandle,
) : ViewModel(), RewardListActions {

    val rewards = rewardDao.getAllRewardsSortedByIsUnlockedDesc().asLiveData()

    private val showDeleteAllUnlockedRewardsDialogLiveData =
        savedStateHandle.getLiveData<Boolean>("showDeleteAllUnlockedRewardsDialogLiveData", false)
    val showDeleteAllUnlockedRewardsDialog: LiveData<Boolean> = showDeleteAllUnlockedRewardsDialogLiveData

    private val eventChannel = Channel<Event>()
    val events = eventChannel.receiveAsFlow()

    sealed class Event {
        data class ShowUndoRewardSnackbar(val reward: Reward) : Event()
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

    override fun onRewardSwiped(reward: Reward) {
        viewModelScope.launch {
            rewardDao.deleteReward(reward)
            eventChannel.send(Event.ShowUndoRewardSnackbar(reward))
        }
    }

    override fun onUndoDeleteRewardConfirmed(reward: Reward) {
        viewModelScope.launch {
            rewardDao.insertReward(reward)
        }
    }
}