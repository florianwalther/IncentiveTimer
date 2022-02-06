package com.florianwalther.incentivetimer.core

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.florianwalther.incentivetimer.core.notification.DefaultNotificationHelper
import com.florianwalther.incentivetimer.data.datastore.PomodoroPhase
import com.florianwalther.incentivetimer.data.datastore.PomodoroTimerStateManager
import com.florianwalther.incentivetimer.di.ApplicationScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import logcat.logcat
import javax.inject.Inject

@AndroidEntryPoint
class DailyResetBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationHelper: DefaultNotificationHelper

    @Inject
    lateinit var timerStateManager: PomodoroTimerStateManager

    @ApplicationScope
    @Inject
    lateinit var applicationScope: CoroutineScope

    override fun onReceive(context: Context?, intent: Intent?) {
        notificationHelper.removeResumeTimerNotification()
        applicationScope.launch {
            timerStateManager.apply {
                val timeTargetInMillis = timerStateManager.timerState.first().timeTargetInMillis
                updateTimeLeftInMillis(timeTargetInMillis)
                updateCurrentPhase(PomodoroPhase.POMODORO)
                updatePomodorosCompletedInSet(0)
                updatePomodorosCompletedTotal(0)
            }
        }
    }
}