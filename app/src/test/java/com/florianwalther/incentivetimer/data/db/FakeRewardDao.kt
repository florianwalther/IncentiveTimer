package com.florianwalther.incentivetimer.data.db

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeRewardDao(
    rewards: LinkedHashMap<Long, Reward> = LinkedHashMap()
) : RewardDao {

    private val rewards = MutableStateFlow<Map<Long, Reward>>(rewards)

    override fun getAllRewardsSortedByIsUnlockedDesc(): Flow<List<Reward>> =
        rewards.map { it.values.sortedByDescending { it.isUnlocked }.toList() }

    override fun getAllNotUnlockedRewards(): Flow<List<Reward>> =
        rewards.map { it.values.filter { !it.isUnlocked } }

    override fun getRewardById(rewardId: Long): Flow<Reward?> =
        rewards.map { rewardMap ->
            rewardMap[rewardId]
        }


    override suspend fun insertReward(reward: Reward) {
        val rewardWithId = if (reward.id > 0) {
            reward
        } else {
            val newId = getCurrentHighestRewardId() + 1
            reward.copy(id = newId)
        }

        updateRewardMap {
            this[rewardWithId.id] = rewardWithId
        }
    }

    override suspend fun updateReward(reward: Reward) {
        updateRewardMap {
            this[reward.id] = reward
        }
    }

    override suspend fun deleteReward(reward: Reward) {
        updateRewardMap {
            remove(reward.id)
        }
    }

    override suspend fun deleteRewards(rewards: List<Reward>) {
        updateRewardMap {
            rewards.forEach { reward ->
                remove(reward.id)
            }
        }
    }

    override suspend fun deleteAllUnlockedRewards() {
        updateRewardMap {
            values.filter { reward ->
                reward.isUnlocked
            }.forEach { unlockedReward ->
                remove(unlockedReward.id)
            }
        }
    }

    private fun updateRewardMap(block: MutableMap<Long, Reward>.() -> Unit) {
        rewards.value = rewards.value.toMutableMap().apply(block).toMap()
    }

    private fun getCurrentHighestRewardId(): Long =
        rewards.value.keys.lastOrNull() ?: 0L
}