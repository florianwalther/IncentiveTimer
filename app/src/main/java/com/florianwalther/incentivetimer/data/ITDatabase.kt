package com.florianwalther.incentivetimer.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.florianwalther.incentivetimer.di.ApplicationScope
import com.florianwalther.incentivetimer.ui.IconKeys
import com.florianwalther.incentivetimer.ui.rewardIcons
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@Database(entities = [Reward::class], version = 1)
abstract class ITDatabase : RoomDatabase() {

    abstract fun rewardDao(): RewardDao

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
                        iconKey = IconKeys.CAKE,
                        title = "1 piece of cake",
                        chanceInPercent = 5
                    )
                )
                rewardDao.insertReward(
                    Reward(
                        iconKey = IconKeys.BATH_TUB,
                        title = "Take a bath",
                        chanceInPercent = 7
                    )
                )
                rewardDao.insertReward(
                    Reward(
                        iconKey = IconKeys.TV,
                        title = "Watch 1 episode of my favorite show",
                        chanceInPercent = 10
                    )
                )
            }
        }
    }
}