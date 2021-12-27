package com.florianwalther.incentivetimer.features.timer

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class TimerServiceManager @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
) {
    fun startTimerService() {
        val serviceIntent = Intent(applicationContext, TimerService::class.java)
        ContextCompat.startForegroundService(applicationContext, serviceIntent)
    }

    fun stopTimerService() {
        val serviceIntent = Intent(applicationContext, TimerService::class.java)
        applicationContext.stopService(serviceIntent)
    }
}