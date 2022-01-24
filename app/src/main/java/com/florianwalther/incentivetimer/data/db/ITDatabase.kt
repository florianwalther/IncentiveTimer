package com.florianwalther.incentivetimer.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.florianwalther.incentivetimer.di.ApplicationScope
import com.florianwalther.incentivetimer.core.ui.IconKey
import com.florianwalther.incentivetimer.core.util.dayAfter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import javax.inject.Provider

@Database(entities = [Reward::class, PomodoroStatistic::class], version = 1)
abstract class ITDatabase : RoomDatabase() {

    abstract fun rewardDao(): RewardDao

    abstract fun pomodoroStatisticDao(): PomodoroStatisticDao

    class Callback @Inject constructor(
        private val database: Provider<ITDatabase>,
        @ApplicationScope private val applicationScope: CoroutineScope,
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            val rewardDao = database.get().rewardDao()

            applicationScope.launch {
                rewardDao.insertReward(
                    Reward(
                        iconKey = IconKey.CAKE,
                        name = "1 piece of cake",
                        chanceInPercent = 5
                    )
                )
                rewardDao.insertReward(
                    Reward(
                        iconKey = IconKey.BATH_TUB,
                        name = "Take a bath",
                        chanceInPercent = 7
                    )
                )
                rewardDao.insertReward(
                    Reward(
                        iconKey = IconKey.TV,
                        name = "Watch 1 episode of my favorite show",
                        chanceInPercent = 10
                    )
                )
            }
        }
    }
}