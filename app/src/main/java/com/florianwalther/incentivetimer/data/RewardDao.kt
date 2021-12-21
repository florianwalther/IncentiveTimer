package com.florianwalther.incentivetimer.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RewardDao {

    @Query("SELECT * FROM rewards")
    fun getAllRewards(): Flow<List<Reward>>

    @Query("SELECT * FROM rewards WHERE id = :rewardId")
    suspend fun getRewardById(rewardId: Long): Reward?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReward(reward: Reward)

    @Update
    suspend fun updateReward(reward: Reward)
}