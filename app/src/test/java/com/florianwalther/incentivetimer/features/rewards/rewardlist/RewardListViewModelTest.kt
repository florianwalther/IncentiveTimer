package com.florianwalther.incentivetimer.features.rewards.rewardlist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.florianwalther.incentivetimer.core.ui.defaultRewardIconKey
import com.florianwalther.incentivetimer.data.db.FakeRewardDao
import com.florianwalther.incentivetimer.data.db.Reward
import com.florianwalther.incentivetimer.getOrAwaitValue
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class RewardListViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val reward1 = Reward(
        name = "Reward 1",
        chanceInPercent = 10,
        iconKey = defaultRewardIconKey,
        id = 1,
    )

    private val reward2 = Reward(
        name = "Reward 2",
        chanceInPercent = 10,
        iconKey = defaultRewardIconKey,
        id = 2,
    )

    private val reward3 = Reward(
        name = "Reward 3",
        chanceInPercent = 10,
        iconKey = defaultRewardIconKey,
        isUnlocked = true,
        id = 3,
    )

    private val data = linkedMapOf<Long, Reward>(
        reward1.id to reward1,
        reward2.id to reward2,
        reward3.id to reward3
    )

    private lateinit var rewardListViewModel: RewardListViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        rewardListViewModel = RewardListViewModel(
            rewardDao = FakeRewardDao(data),
            savedStateHandle = SavedStateHandle(),
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun rewards_sortedByIsUnlockedDesc() = runTest {
        val data = rewardListViewModel.screenState.getOrAwaitValue().rewards

        assertThat(data).containsExactly(reward3, reward1, reward2).inOrder()
    }

    @Test
    fun multiSelectionModeActive_defaultValueFalse() {
        assertThat(rewardListViewModel.screenState.getOrAwaitValue().multiSelectionModeActive).isFalse()
    }

    @Test
    fun showDeleteAllSelectedRewardsDialog_defaultValueFalse() {
        assertThat(rewardListViewModel.screenState.getOrAwaitValue().showDeleteAllSelectedRewardsDialog).isFalse()
    }

    @Test
    fun showDeleteAllUnlockedRewardsDialog_defaultValueFalse() {
        assertThat(rewardListViewModel.screenState.getOrAwaitValue().showDeleteAllUnlockedRewardsDialog).isFalse()
    }

    @Test
    fun onRewardClicked_multiSelectionModeInactive_sendNavigateToEditRewardScreenEvent() = runTest {
        rewardListViewModel.onRewardClicked(reward1)

        rewardListViewModel.events.test {
            assertThat(awaitItem()).isEqualTo(
                RewardListViewModel.RewardListEvent.NavigateToEditRewardScreen(reward1)
            )
        }
    }

    @Test
    fun onRewardClicked_multiSelectionModeActiveAndRewardNotSelected_selectsReward() = runTest {
        rewardListViewModel.onRewardLongClicked(reward1)
        rewardListViewModel.onRewardClicked(reward2)

        assertThat(rewardListViewModel.screenState.getOrAwaitValue().selectedRewardIds).containsExactly(reward1.id, reward2.id)
    }

    @Test
    fun onRewardClicked_multiSelectionModeActiveAndRewardSelected_unselectsReward() = runTest {
        rewardListViewModel.onRewardLongClicked(reward1)
        rewardListViewModel.onRewardClicked(reward2)
        rewardListViewModel.onRewardClicked(reward2)

        assertThat(rewardListViewModel.screenState.getOrAwaitValue().selectedRewardIds).containsExactly(reward1.id)
    }

    @Test
    fun onRewardClicked_lastSelectedReward_cancelsMultiSelectionMode() = runTest {
        rewardListViewModel.onRewardLongClicked(reward1)
        rewardListViewModel.onRewardClicked(reward1)

        assertThat(rewardListViewModel.screenState.getOrAwaitValue().multiSelectionModeActive).isFalse()
    }

    @Test
    fun onCancelMultiSelectionModeClicked_cancelsMultiSelectionMode() {
        rewardListViewModel.onRewardLongClicked(reward1)

        rewardListViewModel.onCancelMultiSelectionModeClicked()

        assertThat(rewardListViewModel.screenState.getOrAwaitValue().multiSelectionModeActive).isFalse()
    }

    @Test
    fun onCancelMultiSelectionModeClicked_clearsSelectedRewardsList() {
        rewardListViewModel.onRewardLongClicked(reward1)

        rewardListViewModel.onCancelMultiSelectionModeClicked()

        assertThat(rewardListViewModel.screenState.getOrAwaitValue().selectedRewardIds).isEmpty()
    }

    @Test
    fun onRewardLongClicked_multiSelectionModeInActive_activatesMultiSelectionMode() {
        rewardListViewModel.onRewardLongClicked(reward1)

        assertThat(rewardListViewModel.screenState.getOrAwaitValue().multiSelectionModeActive).isTrue()
    }

    @Test
    fun onRewardLongClicked_multiSelectionModeInActiveAndRewardNotSelect_selectsClickedReward() {
        rewardListViewModel.onRewardLongClicked(reward1)

        assertThat(rewardListViewModel.screenState.getOrAwaitValue().selectedRewardIds).containsExactly(reward1.id)
    }

    @Test
    fun onRewardLongClicked_multiSelectionModeInActiveAndRewardSelect_selectsClickedReward() {
        rewardListViewModel.onRewardLongClicked(reward1)
        rewardListViewModel.onRewardLongClicked(reward2)

        assertThat(rewardListViewModel.screenState.getOrAwaitValue().selectedRewardIds).containsExactly(reward1.id, reward2.id)
    }

    @Test
    fun selectedItemCount_returnsCorrectValue() {
        rewardListViewModel.onRewardLongClicked(reward1)

        assertThat(rewardListViewModel.screenState.getOrAwaitValue().selectedItemCount).isEqualTo(1)
    }

    @Test
    fun onDeleteAllSelectedRewardsClicked_showsDeleteAllSelectedRewardsDialog() {
        rewardListViewModel.onDeleteAllSelectedItemsClicked()

        assertThat(rewardListViewModel.screenState.getOrAwaitValue().showDeleteAllSelectedRewardsDialog).isTrue()
    }

    @Test
    fun onDeleteAllSelectedRewardsConfirmed_hidesDeleteAllSelectedRewardsDialog() {
        rewardListViewModel.onDeleteAllSelectedItemsClicked()
        rewardListViewModel.onDeleteAllSelectedRewardsConfirmed()

        assertThat(rewardListViewModel.screenState.getOrAwaitValue().showDeleteAllSelectedRewardsDialog).isFalse()
    }

    @Test
    fun onDeleteAllSelectedRewardsConfirmed_deletesAllSelectedRewards() {
        rewardListViewModel.onRewardLongClicked(reward1)

        rewardListViewModel.onDeleteAllSelectedItemsClicked()
        rewardListViewModel.onDeleteAllSelectedRewardsConfirmed()

        assertThat(rewardListViewModel.screenState.getOrAwaitValue().rewards).containsExactly(reward2, reward3)
    }


    @Test
    fun onDeleteAllSelectedRewardsDialogDismissed_hidesDeleteAllSelectedRewardsDialog() {
        rewardListViewModel.onDeleteAllSelectedItemsClicked()
        rewardListViewModel.onDeleteAllSelectedRewardsDialogDismissed()

        assertThat(rewardListViewModel.screenState.getOrAwaitValue().showDeleteAllSelectedRewardsDialog).isFalse()
    }

    @Test
    fun onDeleteAllUnlockedRewardsClicked_showsDeleteAllUnlockedRewardsDialog() {
        rewardListViewModel.onDeleteAllUnlockedRewardsClicked()

        assertThat(rewardListViewModel.screenState.getOrAwaitValue().showDeleteAllUnlockedRewardsDialog).isTrue()
    }

    @Test
    fun onDeleteAllUnlockedRewardsConfirmed_hidesDeleteAllUnlockedRewardsDialog() {
        rewardListViewModel.onDeleteAllUnlockedRewardsClicked()
        rewardListViewModel.onDeleteAllUnlockedRewardsConfirmed()

        assertThat(rewardListViewModel.screenState.getOrAwaitValue().showDeleteAllUnlockedRewardsDialog).isFalse()
    }

    @Test
    fun onDeleteAllUnlockedRewardsConfirmed_deletesAllUnlockedRewards() {
        rewardListViewModel.onDeleteAllUnlockedRewardsClicked()
        rewardListViewModel.onDeleteAllUnlockedRewardsConfirmed()

        assertThat(rewardListViewModel.screenState.getOrAwaitValue().rewards).containsExactly(reward1, reward2)
    }

    @Test
    fun onDeleteAllUnlockedRewardsDialogDismissed_hidesDeleteAllUnlockedRewardsDialog() {
        rewardListViewModel.onDeleteAllUnlockedRewardsClicked()
        rewardListViewModel.onDeleteAllUnlockedRewardsDialogDismissed()

        assertThat(rewardListViewModel.screenState.getOrAwaitValue().showDeleteAllUnlockedRewardsDialog).isFalse()
    }

    @Test
    fun onRewardSwiped_deletesReward() = runTest {
        rewardListViewModel.onRewardSwiped(reward1)

        assertThat(rewardListViewModel.screenState.getOrAwaitValue().rewards).containsExactly(reward3, reward2)
    }

    @Test
    fun onRewardSwiped_sendsShowUndoRewardSnackbarEvent() = runTest {
        rewardListViewModel.onRewardSwiped(reward1)

        rewardListViewModel.events.test {
            assertThat(awaitItem()).isEqualTo(
                RewardListViewModel.RewardListEvent.ShowUndoRewardSnackbar(
                    reward1
                )
            )
        }
    }

    @Test
    fun onUndoDeleteRewardConfirmed_insertsReward() = runTest {
        rewardListViewModel.onRewardSwiped(reward1)
        rewardListViewModel.onUndoDeleteRewardConfirmed(reward1)

        val data = rewardListViewModel.screenState.getOrAwaitValue().rewards
        assertThat(data).containsExactly(reward3, reward1, reward2)
    }
}