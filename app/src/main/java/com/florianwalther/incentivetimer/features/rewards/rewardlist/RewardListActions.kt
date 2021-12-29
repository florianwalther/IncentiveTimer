package com.florianwalther.incentivetimer.features.rewards.rewardlist

import com.florianwalther.incentivetimer.data.Reward

interface RewardListActions {
    fun onDeleteAllUnlockedRewardsClicked()
    fun onDeleteAllUnlockedRewardsConfirmed()
    fun onDeleteAllUnlockedRewardsDialogDismissed()
    fun onRewardSwiped(reward: Reward)
    fun onUndoDeleteRewardConfirmed(reward: Reward)
}