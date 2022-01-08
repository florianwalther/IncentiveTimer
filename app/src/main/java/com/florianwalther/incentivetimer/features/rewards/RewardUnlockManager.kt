package com.florianwalther.incentivetimer.features.rewards

import com.florianwalther.incentivetimer.core.notification.NotificationHelper
import com.florianwalther.incentivetimer.data.db.RewardDao
import com.florianwalther.incentivetimer.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import logcat.logcat
import javax.inject.Inject
import kotlin.random.Random

class RewardUnlockManager @Inject constructor(
    private val rewardDao: RewardDao,
    @ApplicationScope private val applicationScope: CoroutineScope,
    private val notificationHelper: NotificationHelper,
) {
    fun rollAllRewards() {
        applicationScope.launch {
            val allNotUnlockedRewards = rewardDao.getAllNotUnlockedRewards().first()
            allNotUnlockedRewards.forEach { reward ->
                val chanceInPercent = reward.chanceInPercent
                val randomNumber = Random.nextInt(from = 1, until = 100)
                val unlocked = chanceInPercent >= randomNumber
                if (unlocked) {
                    val rewardUpdate = reward.copy(isUnlocked = unlocked)
                    logcat { "Name: ${reward.name}, chance: $chanceInPercent, rn: $randomNumber" }
                    rewardDao.updateReward(rewardUpdate)
                    notificationHelper.showRewardUnlockedNotification(reward)
                }
            }
        }
    }
}