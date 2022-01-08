package com.florianwalther.incentivetimer.features.rewards.addeditreward.model

import com.florianwalther.incentivetimer.data.db.Reward

data class AddEditRewardScreenState(
    val rewardInput: Reward,
    val unlockedStateCheckboxVisible: Boolean,
    val showRewardIconSelectionDialog: Boolean,
    val showDeleteRewardConfirmationDialog: Boolean,
    val rewardNameInputIsError: Boolean,
    ) {
    companion object {
        val initialState = AddEditRewardScreenState(
            rewardInput = Reward.DEFAULT,
            unlockedStateCheckboxVisible = false,
            showRewardIconSelectionDialog = false,
            showDeleteRewardConfirmationDialog = false,
            rewardNameInputIsError = false,
        )
    }
}