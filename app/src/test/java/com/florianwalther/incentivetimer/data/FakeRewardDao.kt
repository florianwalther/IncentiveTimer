package com.florianwalther.incentivetimer.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeRewardDao(
    initialData: LinkedHashMap<Long, Reward> = LinkedHashMap()
) : RewardDao {

    private val dataFlow = MutableStateFlow<Map<Long, Reward>>(initialData)

    override fun getAllRewardsSortedByIsUnlockedDesc(): Flow<List<Reward>> =
        dataFlow.map { it.values.sortedByDescending { it.isUnlocked }.toList() }

    override fun getAllNotUnlockedRewards(): Flow<List<Reward>> {
        TODO("Not yet implemented")
    }

    override fun getRewardById(rewardId: Long): Flow<Reward?> {
        TODO("Not yet implemented")
    }

    override suspend fun insertReward(reward: Reward) {
        updateRewardMap {
            this[reward.id] = reward
        }
    }

    override suspend fun updateReward(reward: Reward) {
        TODO("Not yet implemented")
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
        dataFlow.value = dataFlow.value.toMutableMap().apply(block).toMap()
    }
}