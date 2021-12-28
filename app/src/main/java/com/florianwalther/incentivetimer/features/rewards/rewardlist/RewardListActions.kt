package com.florianwalther.incentivetimer.features.rewards.rewardlist

interface RewardListActions {
    fun onDeleteAllUnlockedRewardsClicked()
    fun onDeleteAllUnlockedRewardsConfirmed()
    fun onDeleteAllUnlockedRewardsDialogDismissed()
}