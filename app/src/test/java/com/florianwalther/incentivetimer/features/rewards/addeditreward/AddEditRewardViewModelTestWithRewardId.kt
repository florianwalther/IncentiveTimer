package com.florianwalther.incentivetimer.features.rewards.addeditreward

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.florianwalther.incentivetimer.core.ui.IconKey
import com.florianwalther.incentivetimer.core.ui.defaultRewardIconKey
import com.florianwalther.incentivetimer.data.db.FakeRewardDao
import com.florianwalther.incentivetimer.data.db.Reward
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
    private lateinit var addEditRewardViewModel: AddEditRewardViewModel

    @Before
    fun setUp() {
        fakeRewardDao = FakeRewardDao(data)
        Dispatchers.setMain(UnconfinedTestDispatcher())
        addEditRewardViewModel = AddEditRewardViewModel(
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
        assertThat(addEditRewardViewModel.screenState.getOrAwaitValue().rewardInput).isEqualTo(reward1)
    }

    @Test
    fun isEditMode_True() {
        assertThat(addEditRewardViewModel.isEditMode).isTrue()
    }

    @Test
    fun unlockedStateCheckboxVisible_rewardUnlocked_True() = runTest {
        fakeRewardDao.updateReward(reward1.copy(isUnlocked = true))

        assertThat(addEditRewardViewModel.screenState.getOrAwaitValue().unlockedStateCheckboxVisible).isTrue()
    }

    @Test
    fun onRewardUnlockedCheckedChanged_updatesRewardInput() = runTest {
        fakeRewardDao.updateReward(reward1.copy(isUnlocked = true))

        addEditRewardViewModel.onRewardUnlockedCheckedChanged(false)

        assertThat(addEditRewardViewModel.screenState.getOrAwaitValue().rewardInput.isUnlocked).isFalse()
    }

    @Test
    fun onSaveClicked_emptyNameInput_doesNotUpdateReward() = runTest {
        addEditRewardViewModel.onRewardNameInputChanged("")
        addEditRewardViewModel.onSaveClicked()

        val reward = fakeRewardDao.getRewardById(rewardIdArg).first()

        assertThat(reward).isEqualTo(reward1)
    }

    @Test
    fun onSaveClicked_validInput_updatesReward() = runTest {
        val nameInput = "new name"
        val chanceInPercentInput = 20
        val iconKeyInput = IconKey.BATH_TUB
        addEditRewardViewModel.onRewardNameInputChanged(nameInput)
        addEditRewardViewModel.onChanceInPercentInputChanged(chanceInPercentInput)
        addEditRewardViewModel.onRewardIconSelected(iconKeyInput)
        addEditRewardViewModel.onSaveClicked()

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
        addEditRewardViewModel.onSaveClicked()

        addEditRewardViewModel.events.test {
            assertThat(awaitItem()).isEqualTo(AddEditRewardViewModel.AddEditRewardEvent.RewardUpdated)
        }
    }

    @Test
    fun onDeleteRewardClicked_showsDeleteRewardDialog() {
        addEditRewardViewModel.onDeleteRewardClicked()

        assertThat(addEditRewardViewModel.screenState.getOrAwaitValue().showDeleteRewardConfirmationDialog).isTrue()
    }

    @Test
    fun onDeleteRewardConfirmed_hidesDeleteRewardDialog() {
        addEditRewardViewModel.onDeleteRewardClicked()
        addEditRewardViewModel.onDeleteRewardConfirmed()

        assertThat(addEditRewardViewModel.screenState.getOrAwaitValue().showDeleteRewardConfirmationDialog).isFalse()
    }

    @Test
    fun onDeleteRewardConfirmed_deletesReward() = runTest {
        addEditRewardViewModel.onDeleteRewardClicked()
        addEditRewardViewModel.onDeleteRewardConfirmed()

        val rewards = fakeRewardDao.getAllRewardsSortedByIsUnlockedDesc().first()
        assertThat(rewards).isEmpty()
    }


    @Test
    fun onDeleteRewardConfirmed_sendsRewardDeletedEvent() = runTest {
        addEditRewardViewModel.onDeleteRewardClicked()
        addEditRewardViewModel.onDeleteRewardConfirmed()

        addEditRewardViewModel.events.test {
            assertThat(awaitItem()).isEqualTo(AddEditRewardViewModel.AddEditRewardEvent.RewardDeleted)
        }
    }

    @Test
    fun onDeleteRewardDialogDismissed_hidesDeleteRewardDialog() {
        addEditRewardViewModel.onDeleteRewardClicked()
        addEditRewardViewModel.onDeleteRewardDialogDismissed()

        assertThat(addEditRewardViewModel.screenState.getOrAwaitValue().showDeleteRewardConfirmationDialog).isFalse()
    }
}