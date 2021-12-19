package com.florianwalther.incentivetimer.rewardlist

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.florianwalther.incentivetimer.data.Reward
import com.florianwalther.incentivetimer.data.RewardDao
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RewardListViewModel @Inject constructor(
    private val rewardDao: RewardDao,
) : ViewModel() {

    val rewards = rewardDao.getAllRewards().asLiveData()
}