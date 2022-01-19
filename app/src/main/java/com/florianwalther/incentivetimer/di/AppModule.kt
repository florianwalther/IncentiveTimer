package com.florianwalther.incentivetimer.di

import android.app.Application
import androidx.room.Room
import com.florianwalther.incentivetimer.core.notification.DefaultNotificationHelper
import com.florianwalther.incentivetimer.core.notification.NotificationHelper
import com.florianwalther.incentivetimer.data.db.ITDatabase
import com.florianwalther.incentivetimer.data.db.PomodoroStatisticDao
import com.florianwalther.incentivetimer.data.db.RewardDao
import com.florianwalther.incentivetimer.data.preferences.DefaultPreferencesManager
import com.florianwalther.incentivetimer.data.preferences.PreferencesManager
import com.florianwalther.incentivetimer.features.timer.DefaultTimeSource
import com.florianwalther.incentivetimer.features.timer.DefaultTimerServiceManager
import com.florianwalther.incentivetimer.features.timer.TimeSource
import com.florianwalther.incentivetimer.features.timer.TimerServiceManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    companion object {
        @Provides
        fun provideRewardDao(db: ITDatabase): RewardDao = db.rewardDao()

        @Provides
        fun providePomodoroStatisticDao(db: ITDatabase): PomodoroStatisticDao = db.pomodoroStatisticDao()

        @Singleton
        @Provides
        fun provideDatabase(
            app: Application,
            callback: ITDatabase.Callback,
        ): ITDatabase = Room.databaseBuilder(app, ITDatabase::class.java, "it_database")
            .addCallback(callback)
            .build()

        @ApplicationScope
        @Singleton
        @Provides
        fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob())

        @MainDispatcher
        @Singleton
        @Provides
        fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main
    }

    @Binds
    abstract fun bindTimeSource(timeSource: DefaultTimeSource): TimeSource

    @Binds
    abstract fun bindPreferencesManager(preferencesManager: DefaultPreferencesManager): PreferencesManager

    @Binds
    abstract fun bindTimerServiceManager(timerServiceManager: DefaultTimerServiceManager): TimerServiceManager

   @Binds
    abstract fun bindNotificationHelper(notificationHelper: DefaultNotificationHelper): NotificationHelper
}


@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class MainDispatcher