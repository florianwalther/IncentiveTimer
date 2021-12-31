package com.florianwalther.incentivetimer.features.rewards.rewardlist

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.florianwalther.incentivetimer.core.ui.defaultRewardIconKey
import com.florianwalther.incentivetimer.data.FakeRewardDao
import com.florianwalther.incentivetimer.data.Reward
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

class RewardListViewModelTest {

    private val dispatcher = StandardTestDispatcher()

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

    private val data = linkedMapOf(
        1L to reward1,
        2L to reward2,
    )

    private val fakeRewardDao = FakeRewardDao(data)

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        viewModel = RewardListViewModel(
            rewardDao = fakeRewardDao,
            savedStateHandle = SavedStateHandle()
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun onRewardSwiped_deletesReward() = runTest {
        fakeRewardDao.getAllRewardsSortedByIsUnlockedDesc().test {
            assertThat(awaitItem()).containsExactly(reward1, reward2)

            viewModel.onRewardSwiped(reward1)

            assertThat(awaitItem()).containsExactly(reward2)
        }
    }
}