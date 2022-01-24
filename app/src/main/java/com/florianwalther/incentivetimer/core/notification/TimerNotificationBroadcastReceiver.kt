package com.florianwalther.incentivetimer.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.florianwalther.incentivetimer.data.datastore.PomodoroPhase
import com.florianwalther.incentivetimer.features.timer.PomodoroTimerManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TimerNotificationBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var pomodoroTimerManager: PomodoroTimerManager

    @Inject
    lateinit var notificationHelper: DefaultNotificationHelper

    override fun onReceive(p0: Context?, intent: Intent?) {
        val timerRunning = intent?.getBooleanExtra(EXTRA_TIMER_RUNNING, false)
        pomodoroTimerManager.startStopTimer()
        if (timerRunning == true) {
            val currentPhase = intent.getSerializableExtra(EXTRA_POMODORO_PHASE) as? PomodoroPhase
            val timeLeftInMillis = intent.getLongExtra(EXTRA_TIME_LEFT_IN_MILLIS, -1)
            if (currentPhase != null && timeLeftInMillis != -1L) {
                notificationHelper.showResumeTimerNotification(
                    currentPhase = currentPhase,
                    timeLeftInMillis = timeLeftInMillis
                )
            }
        }
    }
}

const val EXTRA_TIMER_RUNNING = "EXTRA_TIMER_RUNNING"
const val EXTRA_POMODORO_PHASE = "EXTRA_POMODORO_PHASE"
const val EXTRA_TIME_LEFT_IN_MILLIS = "EXTRA_TIME_LEFT_IN_MILLIS"