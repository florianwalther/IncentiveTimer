package com.florianwalther.incentivetimer.features.rewards.rewardlist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.florianwalther.incentivetimer.core.ui.defaultRewardIconKey
import com.florianwalther.incentivetimer.data.FakeRewardDao
import com.florianwalther.incentivetimer.data.Reward
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

    private lateinit var viewModel: RewardListViewModel

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

    private val data = linkedMapOf(
        1L to reward1,
        2L to reward2,
        3L to reward3
    )

    private val fakeRewardDao = FakeRewardDao(data)

    @Before
    fun setUp() {
        val dispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(dispatcher)
        viewModel = RewardListViewModel(
            rewardDao = fakeRewardDao,
            savedStateHandle = SavedStateHandle(),
            dispatcher = dispatcher
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun rewards_sortedByIsUnlockedDesc() = runTest {
        val data = viewModel.rewards.getOrAwaitValue()

        assertThat(data).containsExactly(reward3, reward1, reward2).inOrder()
    }

    @Test
    fun onRewardClicked_multiSelectionModeInactive_sendNavigateToEditRewardScreenEvent() = runTest {
        viewModel.onRewardClicked(reward1)

        viewModel.events.test {
            assertThat(awaitItem()).isEqualTo(
                RewardListViewModel.Event.NavigateToEditRewardScreen(reward1)
            )
        }
    }

    @Test
    fun onRewardClicked_multiSelectionModeActiveAndRewardNotSelected_selectsReward() = runTest {
        viewModel.onRewardLongClicked(reward1)
        viewModel.onRewardClicked(reward2)

        assertThat(viewModel.selectedRewards.getOrAwaitValue()).containsExactly(reward1, reward2)
    }

    @Test
    fun onRewardClicked_multiSelectionModeActiveAndRewardSelected_unselectsReward() = runTest {
        viewModel.onRewardLongClicked(reward1)
        viewModel.onRewardClicked(reward2)
        viewModel.onRewardClicked(reward2)

        assertThat(viewModel.selectedRewards.getOrAwaitValue()).containsExactly(reward1)
    }

    @Test
    fun onRewardClicked_lastSelectedReward_cancelsMultiSelectionMode() = runTest {
        viewModel.onRewardLongClicked(reward1)
        viewModel.onRewardClicked(reward1)

        assertThat(viewModel.multiSelectionModeActive.getOrAwaitValue()).isFalse()
    }

    @Test
    fun onCancelMultiSelectionModeClicked_cancelsMultiSelectionMode() {
        viewModel.onRewardLongClicked(reward1)

        viewModel.onCancelMultiSelectionModeClicked()

        assertThat(viewModel.multiSelectionModeActive.getOrAwaitValue()).isFalse()
    }

    @Test
    fun onCancelMultiSelectionModeClicked_clearsSelectedRewardsList() {
        viewModel.onRewardLongClicked(reward1)

        viewModel.onCancelMultiSelectionModeClicked()

        assertThat(viewModel.selectedRewards.getOrAwaitValue()).isEmpty()
    }

    @Test
    fun onRewardLongClicked_multiSelectionModeInActive_activatesMultiSelectionMode() {
        viewModel.onRewardLongClicked(reward1)

        assertThat(viewModel.multiSelectionModeActive.getOrAwaitValue()).isTrue()
    }

    @Test
    fun onRewardLongClicked_multiSelectionModeInActiveAndRewardNotSelect_selectsClickedReward() {
        viewModel.onRewardLongClicked(reward1)

        assertThat(viewModel.selectedRewards.getOrAwaitValue()).containsExactly(reward1)
    }

    @Test
    fun onRewardLongClicked_multiSelectionModeInActiveAndRewardSelect_selectsClickedReward() {
        viewModel.onRewardLongClicked(reward1)
        viewModel.onRewardLongClicked(reward2)

        assertThat(viewModel.selectedRewards.getOrAwaitValue()).containsExactly(reward1, reward2)
    }

    @Test
    fun selectedItemCount_returnsCorrectValue() {
        viewModel.onRewardLongClicked(reward1)

        assertThat(viewModel.selectedItemCount.getOrAwaitValue()).isEqualTo(1)
    }

    @Test
    fun onDeleteAllSelectedRewardsClicked_showsDeleteAllSelectedRewardsDialog() {
        viewModel.onDeleteAllSelectedItemsClicked()

        assertThat(viewModel.showDeleteAllSelectedRewardsDialog.getOrAwaitValue()).isTrue()
    }

    @Test
    fun onDeleteAllSelectedRewardsConfirmed_hidesDeleteAllSelectedRewardsDialog() {
        viewModel.onDeleteAllSelectedItemsClicked()
        viewModel.onDeleteAllSelectedRewardsConfirmed()

        assertThat(viewModel.showDeleteAllSelectedRewardsDialog.getOrAwaitValue()).isFalse()
    }


    @Test
    fun onDeleteAllSelectedRewardsConfirmed_deletesAllSelectedRewards() {
        viewModel.onRewardLongClicked(reward1)

        viewModel.onDeleteAllSelectedItemsClicked()
        viewModel.onDeleteAllSelectedRewardsConfirmed()

        assertThat(viewModel.rewards.getOrAwaitValue()).containsExactly(reward2, reward3)
    }

    @Test
    fun onDeleteAllUnlockedRewardsClicked_showsDeleteAllUnlockedRewardsDialog() {
        viewModel.onDeleteAllUnlockedRewardsClicked()

        assertThat(viewModel.showDeleteAllUnlockedRewardsDialog.getOrAwaitValue()).isTrue()
    }

    @Test
    fun onDeleteAllUnlockedRewardsDialogDismissed_hidesDeleteAllUnlockedRewardsDialog() {
        viewModel.onDeleteAllUnlockedRewardsClicked()
        viewModel.onDeleteAllUnlockedRewardsDialogDismissed()

        assertThat(viewModel.showDeleteAllUnlockedRewardsDialog.getOrAwaitValue()).isFalse()
    }

    @Test
    fun onDeleteAllUnlockedRewardsConfirmed_hidesDeleteAllUnlockedRewardsDialog() {
        viewModel.onDeleteAllUnlockedRewardsClicked()
        viewModel.onDeleteAllUnlockedRewardsConfirmed()

        assertThat(viewModel.showDeleteAllUnlockedRewardsDialog.getOrAwaitValue()).isFalse()
    }

    @Test
    fun onDeleteAllUnlockedRewardsConfirmed_deletesAllUnlockedRewards() {
        viewModel.onDeleteAllUnlockedRewardsClicked()
        viewModel.onDeleteAllUnlockedRewardsConfirmed()

        assertThat(viewModel.rewards.getOrAwaitValue()).containsExactly(reward1, reward2)
    }

    @Test
    fun onRewardSwiped_deletesReward() = runTest {
        viewModel.onRewardSwiped(reward1)

        assertThat(viewModel.rewards.getOrAwaitValue()).containsExactly(reward3, reward2)
    }

    @Test
    fun onRewardSwiped_sendsShowUndoRewardSnackbarEvent() = runTest {
        viewModel.onRewardSwiped(reward1)

        viewModel.events.test {
            assertThat(awaitItem()).isEqualTo(
                RewardListViewModel.Event.ShowUndoRewardSnackbar(
                    reward1
                )
            )
        }
    }

    @Test
    fun onUndoDeleteRewardConfirmed_insertsReward() = runTest {
        viewModel.onRewardSwiped(reward1)
        viewModel.onUndoDeleteRewardConfirmed(reward1)

        val data = viewModel.rewards.getOrAwaitValue()
        assertThat(data).containsExactly(reward3, reward1, reward2)
    }
}