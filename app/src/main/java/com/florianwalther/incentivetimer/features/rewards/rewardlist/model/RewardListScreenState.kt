package com.florianwalther.incentivetimer.features.rewards.rewardlist.model

import com.florianwalther.incentivetimer.data.db.Reward

data class RewardListScreenState(
    val rewards: List<Reward>,
    val selectedRewardIds: List<Long>,
    val selectedItemCount: Int,
    val multiSelectionModeActive: Boolean,
    val showDeleteAllSelectedRewardsDialog: Boolean,
    val showDeleteAllUnlockedRewardsDialog: Boolean,
) {
    companion object {
        val initialState = RewardListScreenState(
            rewards = emptyList(),
            selectedRewardIds = emptyList(),
            selectedItemCount = 0,
            multiSelectionModeActive = false,
            showDeleteAllSelectedRewardsDialog = false,
            showDeleteAllUnlockedRewardsDialog = false,
        )
    }
}