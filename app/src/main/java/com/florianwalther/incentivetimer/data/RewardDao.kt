package com.florianwalther.incentivetimer.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RewardDao {

    @Query("SELECT * FROM rewards")
    fun getAllRewards(): Flow<List<Reward>>

    @Query("SELECT * FROM rewards WHERE isUnlocked = 0")
    fun getAllNotUnlockedRewards(): Flow<List<Reward>>

    @Query("SELECT * FROM rewards WHERE id = :rewardId")
    fun getRewardById(rewardId: Long): Flow<Reward?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReward(reward: Reward)

    @Update
    suspend fun updateReward(reward: Reward)

    @Delete
    suspend fun deleteReward(reward: Reward)
}