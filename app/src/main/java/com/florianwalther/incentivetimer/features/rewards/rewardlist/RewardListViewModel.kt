package com.florianwalther.incentivetimer.features.rewards.rewardlist

import androidx.lifecycle.*
import com.florianwalther.incentivetimer.data.RewardDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RewardListViewModel @Inject constructor(
    private val rewardDao: RewardDao,
    savedStateHandle: SavedStateHandle,
) : ViewModel(), RewardListActions {

    val rewards = rewardDao.getAllRewards().asLiveData()

    private val showDeleteAllUnlockedRewardsDialogLiveData =
        savedStateHandle.getLiveData<Boolean>("showDeleteAllUnlockedRewardsDialogLiveData", false)
    val showDeleteAllUnlockedRewardsDialog: LiveData<Boolean> = showDeleteAllUnlockedRewardsDialogLiveData

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
}