package com.florianwalther.incentivetimer.features.addeditreward

import com.florianwalther.incentivetimer.core.ui.IconKey

interface AddEditRewardScreenActions {
    fun onRewardNameInputChanged(input: String)
    fun onChanceInPercentInputChanged(input: Int)
    fun onRewardIconButtonClicked()
    fun onRewardIconSelected(iconKey: IconKey)
    fun onRewardIconDialogDismissed()
    fun onSaveClicked()
    fun onDeleteRewardClicked()
    fun onDeleteRewardConfirmed()
    fun onDeleteRewardDialogDismissed()
}