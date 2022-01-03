package com.florianwalther.incentivetimer.features.rewards.addeditreward

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.florianwalther.incentivetimer.core.ui.IconKey
import com.florianwalther.incentivetimer.core.ui.defaultRewardIconKey
import com.florianwalther.incentivetimer.data.FakeRewardDao
import com.florianwalther.incentivetimer.data.Reward
import com.florianwalther.incentivetimer.getOrAwaitValue
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AddEditRewardViewModelTestWithRewardId {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val reward1 = Reward(
        name = "Reward 1",
        chanceInPercent = 10,
        iconKey = defaultRewardIconKey,
        id = 1,
    )

    private val data = linkedMapOf<Long, Reward>(
        1L to reward1,
    )

    private val rewardIdArg = 1L

    private lateinit var fakeRewardDao: FakeRewardDao
    private lateinit var viewModel: AddEditRewardViewModel

    @Before
    fun setUp() {
        fakeRewardDao = FakeRewardDao(data)
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = AddEditRewardViewModel(
            rewardDao = fakeRewardDao,
            savedStateHandle = SavedStateHandle(
                mapOf(
                    "rewardId" to rewardIdArg
                )
            ),
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun rewardInput_hasRewardValues() {
        assertThat(viewModel.rewardInput.getOrAwaitValue()).isEqualTo(reward1)
    }

    @Test
    fun isEditMode_True() {
        assertThat(viewModel.isEditMode).isTrue()
    }

    @Test
    fun unlockedStateCheckboxVisible_rewardUnlocked_True() = runTest {
        fakeRewardDao.updateReward(reward1.copy(isUnlocked = true))

        assertThat(viewModel.unlockedStateCheckboxVisible.getOrAwaitValue()).isTrue()
    }

    @Test
    fun onRewardUnlockedCheckedChanged_updatesRewardInput() = runTest {
        fakeRewardDao.updateReward(reward1.copy(isUnlocked = true))

        viewModel.onRewardUnlockedCheckedChanged(false)

        assertThat(viewModel.rewardInput.getOrAwaitValue().isUnlocked).isFalse()
    }

    @Test
    fun onSaveClicked_emptyNameInput_doesNotUpdateReward() = runTest {
        viewModel.onRewardNameInputChanged("")
        viewModel.onSaveClicked()

        val reward = fakeRewardDao.getRewardById(rewardIdArg).first()

        assertThat(reward).isEqualTo(reward1)
    }

    @Test
    fun onSaveClicked_validInput_updatesReward() = runTest {
        val nameInput = "new name"
        val chanceInPercentInput = 20
        val iconKeyInput = IconKey.BATH_TUB
        viewModel.onRewardNameInputChanged(nameInput)
        viewModel.onChanceInPercentInputChanged(chanceInPercentInput)
        viewModel.onRewardIconSelected(iconKeyInput)
        viewModel.onSaveClicked()

        val reward = fakeRewardDao.getRewardById(rewardIdArg).first()

        val expectedReward = reward1.copy(
            name = nameInput,
            chanceInPercent = chanceInPercentInput,
            iconKey = iconKeyInput,
        )

        assertThat(reward).isEqualTo(expectedReward)
    }

    @Test
    fun onSaveClicked_validInput_sendsRewardUpdatedEvent() = runTest {
        viewModel.onSaveClicked()

        viewModel.events.test {
            assertThat(awaitItem()).isEqualTo(AddEditRewardViewModel.AddEditRewardEvent.RewardUpdated)
        }
    }

    @Test
    fun onDeleteRewardClicked_showsDeleteRewardDialog() {
        viewModel.onDeleteRewardClicked()

        assertThat(viewModel.showDeleteRewardConfirmationDialog.getOrAwaitValue()).isTrue()
    }

    @Test
    fun onDeleteRewardConfirmed_hidesDeleteRewardDialog() {
        viewModel.onDeleteRewardClicked()
        viewModel.onDeleteRewardConfirmed()

        assertThat(viewModel.showDeleteRewardConfirmationDialog.getOrAwaitValue()).isFalse()
    }

    @Test
    fun onDeleteRewardConfirmed_deletesReward() = runTest {
        viewModel.onDeleteRewardClicked()
        viewModel.onDeleteRewardConfirmed()

        val rewards = fakeRewardDao.getAllRewardsSortedByIsUnlockedDesc().first()
        assertThat(rewards).isEmpty()
    }


    @Test
    fun onDeleteRewardConfirmed_sendsRewardDeletedEvent() = runTest {
        viewModel.onDeleteRewardClicked()
        viewModel.onDeleteRewardConfirmed()

        viewModel.events.test {
            assertThat(awaitItem()).isEqualTo(AddEditRewardViewModel.AddEditRewardEvent.RewardDeleted)
        }
    }

    @Test
    fun onDeleteRewardDialogDismissed_hidesDeleteRewardDialog() {
        viewModel.onDeleteRewardClicked()
        viewModel.onDeleteRewardDialogDismissed()

        assertThat(viewModel.showDeleteRewardConfirmationDialog.getOrAwaitValue()).isFalse()
    }
}