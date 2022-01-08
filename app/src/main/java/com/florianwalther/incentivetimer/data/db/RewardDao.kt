package com.florianwalther.incentivetimer.data.db

import androidx.room.*
import com.florianwalther.incentivetimer.data.db.Reward
import kotlinx.coroutines.flow.Flow

@Dao
interface RewardDao {

    @Query("SELECT * FROM rewards ORDER BY isUnlocked DESC")
    fun getAllRewardsSortedByIsUnlockedDesc(): Flow<List<Reward>>

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

    @Delete
    suspend fun deleteRewards(rewards: List<Reward>)

    @Query("DELETE FROM rewards WHERE isUnlocked = 1")
    suspend fun deleteAllUnlockedRewards()
}