package com.florianwalther.incentivetimer.core.notification

import androidx.core.app.NotificationCompat
import com.florianwalther.incentivetimer.data.datastore.PomodoroPhase
import com.florianwalther.incentivetimer.data.db.Reward

interface NotificationHelper {
    fun getBaseTimerServiceNotification(): NotificationCompat.Builder
    fun updateTimerServiceNotification(
        currentPhase: PomodoroPhase,
        timeLeftInMillis: Long,
        timerRunning: Boolean
    )

    fun showResumeTimerNotification(
        currentPhase: PomodoroPhase,
        timeLeftInMillis: Long,
    )

    fun showTimerCompletedNotification(finishedPhase: PomodoroPhase)
    fun showRewardUnlockedNotification(reward: Reward)
    fun removeTimerServiceNotification()
    fun removeTimerCompletedNotification()
    fun removeResumeTimerNotification()
}