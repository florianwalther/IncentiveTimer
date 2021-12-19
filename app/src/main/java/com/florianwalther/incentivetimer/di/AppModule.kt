package com.florianwalther.incentivetimer.di

import android.app.Application
import androidx.room.Room
import com.florianwalther.incentivetimer.data.ITDatabase
import com.florianwalther.incentivetimer.data.RewardDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideReward(db : ITDatabase) : RewardDao = db.rewardDao()

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
    fun provideApplicationScope() = CoroutineScope(SupervisorJob())
}

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope