package com.florianwalther.incentivetimer.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PomodoroStatisticDao {

    @Query("SELECT * FROM pomodoro_statistics ORDER BY timestampInMilliseconds")
    fun getAllPomodoroStatistics(): Flow<List<PomodoroStatistic>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPomodoroStatistic(pomodoroStatistic: PomodoroStatistic)

    @Query("DELETE FROM pomodoro_statistics")
    suspend fun deleteAllPomodoroStatistics()
}