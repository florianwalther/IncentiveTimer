package com.florianwalther.incentivetimer.core

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.florianwalther.incentivetimer.features.timer.DailyResetManager
import dagger.hilt.android.AndroidEntryPoint
import logcat.logcat
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var dailyResetManager: DailyResetManager

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "android.intent.action.BOOT_COMPLETED") {
            dailyResetManager.scheduleDailyReset()
        }
    }
}