package com.florianwalther.incentivetimer.core.notification

import androidx.core.app.NotificationCompat
import com.florianwalther.incentivetimer.data.datastore.PomodoroPhase
import com.florianwalther.incentivetimer.data.db.Reward

sealed class TimerCompletedNotificationState {
    data class Shown(val pomodoroPhase: PomodoroPhase) : TimerCompletedNotificationState()
    object NotShown : TimerCompletedNotificationState()
}

sealed class ResumeTimerNotificationState{
    data class Shown(
        val currentPhase: PomodoroPhase,
        val timeLeftInMillis: Long,
    ) : ResumeTimerNotificationState()

    object NotShown : ResumeTimerNotificationState()
}

class FakeNotificationHelper : NotificationHelper {

    var timerCompletedNotification: TimerCompletedNotificationState =
        TimerCompletedNotificationState.NotShown

    var resumeTimerNotification: ResumeTimerNotificationState =
        ResumeTimerNotificationState.NotShown

    override fun getBaseTimerServiceNotification(): NotificationCompat.Builder {
        TODO("Not yet implemented")
    }

    override fun updateTimerServiceNotification(
        currentPhase: PomodoroPhase,
        timeLeftInMillis: Long,
        timerRunning: Boolean
    ) {
        TODO("Not yet implemented")
    }

    override fun showResumeTimerNotification(
        currentPhase: PomodoroPhase,
        timeLeftInMillis: Long,
    ) {
        resumeTimerNotification = ResumeTimerNotificationState.Shown(
            currentPhase,
            timeLeftInMillis,
        )
    }

    override fun removeResumeTimerNotification() {
        resumeTimerNotification = ResumeTimerNotificationState.NotShown
    }

    override fun showTimerCompletedNotification(finishedPhase: PomodoroPhase) {
        timerCompletedNotification =
            TimerCompletedNotificationState.Shown(finishedPhase)
    }

    override fun removeTimerCompletedNotification() {
        timerCompletedNotification = TimerCompletedNotificationState.NotShown
    }

    override fun showRewardUnlockedNotification(reward: Reward) {
        TODO("Not yet implemented")
    }

    override fun removeTimerServiceNotification() {
        TODO("Not yet implemented")
    }
}