package com.florianwalther.incentivetimer.features.rewards.rewardlist

import com.florianwalther.incentivetimer.data.db.Reward

interface RewardListActions {
    fun onDeleteAllUnlockedRewardsClicked()
    fun onDeleteAllUnlockedRewardsConfirmed()
    fun onDeleteAllUnlockedRewardsDialogDismissed()
    fun onDeleteAllSelectedRewardsConfirmed()
    fun onDeleteAllSelectedRewardsDialogDismissed()
    fun onRewardSwiped(reward: Reward)
    fun onUndoDeleteRewardConfirmed(reward: Reward)
    fun onRewardClicked(reward: Reward)
    fun onRewardLongClicked(reward: Reward)
    fun onCancelMultiSelectionModeClicked()
    fun onDeleteAllSelectedItemsClicked()
}