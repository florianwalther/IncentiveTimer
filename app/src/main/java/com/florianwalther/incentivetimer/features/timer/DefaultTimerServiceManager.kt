package com.florianwalther.incentivetimer.features.timer

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class DefaultTimerServiceManager @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
) : TimerServiceManager {
    override fun startTimerService() {
        val serviceIntent = Intent(applicationContext, TimerService::class.java)
        ContextCompat.startForegroundService(applicationContext, serviceIntent)
    }

    override fun stopTimerService() {
        val serviceIntent = Intent(applicationContext, TimerService::class.java)
        applicationContext.stopService(serviceIntent)
    }
}