package com.florianwalther.incentivetimer.rewardlist

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.florianwalther.incentivetimer.data.Reward
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RewardListViewModel @Inject constructor() : ViewModel() {

    private val dummyRewardsLiveData = MutableLiveData<List<Reward>>()
    val dummyRewards: LiveData<List<Reward>> = dummyRewardsLiveData

    init {
        val dummyRewards = mutableListOf<Reward>()
        repeat(100) { index ->
            dummyRewards += Reward(icon = Icons.Default.Star, title = "Item $index", index)
        }
        dummyRewardsLiveData.value = dummyRewards
    }
}