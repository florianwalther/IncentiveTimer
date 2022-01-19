package com.florianwalther.incentivetimer.features.rewards.addeditreward

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.florianwalther.incentivetimer.core.ui.IconKey
import com.florianwalther.incentivetimer.core.ui.defaultRewardIconKey
import com.florianwalther.incentivetimer.data.db.FakeRewardDao
import com.florianwalther.incentivetimer.data.db.Reward
import com.florianwalther.incentivetimer.getOrAwaitValue
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AddEditRewardViewModelTestWithSavedState {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val reward1 = Reward(
        name = "Reward 1",
        chanceInPercent = 10,
        iconKey = defaultRewardIconKey,
        id = 1,
    )

    private val rewardInput = Reward(
        name = "Name input",
        chanceInPercent = 69,
        iconKey = IconKey.BATH_TUB,
        id = 1,
    )

    private val data = linkedMapOf<Long, Reward>(
        1L to reward1,
    )

    private val rewardIdArg = 1L

    private lateinit var addEditRewardViewModel: AddEditRewardViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        addEditRewardViewModel = AddEditRewardViewModel(
            rewardDao = FakeRewardDao(data),
            savedStateHandle = SavedStateHandle(
                mapOf(
                    "rewardId" to rewardIdArg,
                    "KEY_REWARD_LIVE_DATA" to rewardInput
                )
            ),
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun rewardInput_restoredFromSavedStateHandle() {
        assertThat(addEditRewardViewModel.screenState.getOrAwaitValue().rewardInput).isEqualTo(
            rewardInput
        )
    }
}