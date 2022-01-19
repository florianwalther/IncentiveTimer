package com.florianwalther.incentivetimer.features.rewards.addeditreward

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.florianwalther.incentivetimer.core.ui.IconKey
import com.florianwalther.incentivetimer.core.ui.defaultRewardIconKey
import com.florianwalther.incentivetimer.data.db.FakeRewardDao
import com.florianwalther.incentivetimer.data.db.Reward
import com.florianwalther.incentivetimer.getOrAwaitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AddEditRewardViewModelTestWithoutRewardId {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var fakeRewardDao: FakeRewardDao
    private lateinit var viewModel: AddEditRewardViewModel

    @Before
    fun setUp() {
        fakeRewardDao = FakeRewardDao()
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = AddEditRewardViewModel(
            rewardDao = fakeRewardDao,
            savedStateHandle = SavedStateHandle(),
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun rewardInput_hasCorrectDefaultValues() {
        assertThat(viewModel.screenState.getOrAwaitValue().rewardInput).isEqualTo(
            defaultReward
        )
    }

    @Test
    fun isEditMode_False() {
        assertThat(viewModel.isEditMode).isFalse()
    }

    @Test
    fun unlockedStateCheckboxVisible_defaultValueFalse() {
        assertThat(viewModel.screenState.getOrAwaitValue().unlockedStateCheckboxVisible).isFalse()
    }

    @Test
    fun showRewardIconSelectionDialog_defaultValueFalse() {
        assertThat(viewModel.screenState.getOrAwaitValue().showRewardIconSelectionDialog).isFalse()
    }

    @Test
    fun showDeleteRewardConfirmationDialog_defaultValueFalse() {
        assertThat(viewModel.screenState.getOrAwaitValue().showDeleteRewardConfirmationDialog).isFalse()
    }

    @Test
    fun rewardNameInputIsError_defaultValueFalse() {
        assertThat(viewModel.screenState.getOrAwaitValue().rewardNameInputIsError).isFalse()
    }

    @Test
    fun onRewardNameInputChanged_updatesRewardInput() {
        val input = "new input"
        viewModel.onRewardNameInputChanged(input)

        assertThat(viewModel.screenState.getOrAwaitValue().rewardInput.name).isEqualTo(input)
    }

    @Test
    fun onChanceInPercentInputChanged_updatesRewardInput() {
        val input = 22
        viewModel.onChanceInPercentInputChanged(input)

        assertThat(viewModel.screenState.getOrAwaitValue().rewardInput.chanceInPercent).isEqualTo(input)
    }

    @Test
    fun onRewardIconSelected_updatesRewardInput() {
        val input = IconKey.BEVERAGE
        viewModel.onRewardIconSelected(input)

        assertThat(viewModel.screenState.getOrAwaitValue().rewardInput.iconKey).isEqualTo(input)
    }

    @Test
    fun onRewardIconSelected_hidesRewardIconSelectionDialog() {
        viewModel.onRewardIconButtonClicked()
        viewModel.onRewardIconSelected(IconKey.BEVERAGE)

        assertThat(viewModel.screenState.getOrAwaitValue().showRewardIconSelectionDialog).isFalse()
    }

    @Test
    fun onRewardIconButtonClicked_showsRewardIconSelectionDialog() {
        viewModel.onRewardIconButtonClicked()

        assertThat(viewModel.screenState.getOrAwaitValue().showRewardIconSelectionDialog).isTrue()
    }

    @Test
    fun onRewardIconDialogDismissed_hidesRewardIconSelectionDialog() {
        viewModel.onRewardIconButtonClicked()
        viewModel.onRewardIconDialogDismissed()

        assertThat(viewModel.screenState.getOrAwaitValue().showRewardIconSelectionDialog).isFalse()
    }

    @Test
    fun onSaveClicked_emptyNameInput_doesNotCreateReward() = runTest {
        viewModel.onSaveClicked()

        val rewards = fakeRewardDao.getAllRewardsSortedByIsUnlockedDesc().first()

        assertThat(rewards).isEmpty()
    }

    @Test
    fun onSaveClicked_emptyNameInput_setsRewardNameInputIsErrorTrue() {
        viewModel.onSaveClicked()

        assertThat(viewModel.screenState.getOrAwaitValue().rewardNameInputIsError).isTrue()
    }

    @Test
    fun onSaveClicked_emptyNameInput_sendsNoEvent() = runTest {
        viewModel.onSaveClicked()

        viewModel.events.test {
            expectNoEvents()
        }
    }

    @Test
    fun onSaveClicked_validInput_createsNewReward() = runTest {
        val nameInput = "new reward"
        val chanceInPercentInput = 20
        val iconKeyInput = IconKey.BATH_TUB
        viewModel.onRewardNameInputChanged(nameInput)
        viewModel.onChanceInPercentInputChanged(chanceInPercentInput)
        viewModel.onRewardIconSelected(iconKeyInput)
        viewModel.onSaveClicked()

        val rewards = fakeRewardDao.getAllRewardsSortedByIsUnlockedDesc().first()

        val expectedReward =
           defaultReward.copy(
                name = nameInput,
                chanceInPercent = chanceInPercentInput,
                iconKey = iconKeyInput,
                id = 1
            )
        assertThat(rewards).containsExactly(expectedReward)
    }

    @Test
    fun onSaveClicked_validInput_setsRewardNameInputIsErrorFalse() {
        viewModel.onSaveClicked()

        viewModel.onRewardNameInputChanged("new reward")
        viewModel.onSaveClicked()

        assertThat(viewModel.screenState.getOrAwaitValue().rewardNameInputIsError).isFalse()
    }

    @Test
    fun onSaveClicked_validInput_sendsRewardCreatedEvent() = runTest {
        viewModel.onRewardNameInputChanged("new reward")
        viewModel.onSaveClicked()

        viewModel.events.test {
            assertThat(awaitItem()).isEqualTo(AddEditRewardViewModel.AddEditRewardEvent.RewardCreated)
        }
    }

    companion object {
        private val defaultReward = Reward(
            name = "",
            chanceInPercent = 10,
            iconKey = defaultRewardIconKey,
            isUnlocked = false,
            id = 0,
        )
    }
}