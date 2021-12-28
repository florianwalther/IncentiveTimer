package com.florianwalther.incentivetimer.features.rewards.addeditreward

import com.florianwalther.incentivetimer.core.ui.IconKey

interface AddEditRewardScreenActions {
    fun onRewardNameInputChanged(input: String)
    fun onChanceInPercentInputChanged(input: Int)
    fun onRewardIconButtonClicked()
    fun onRewardIconSelected(iconKey: IconKey)
    fun onRewardIconDialogDismissed()
    fun onSaveClicked()
    fun onRewardUnlockedCheckedChanged(unlocked: Boolean)
    fun onDeleteRewardClicked()
    fun onDeleteRewardConfirmed()
    fun onDeleteRewardDialogDismissed()
}