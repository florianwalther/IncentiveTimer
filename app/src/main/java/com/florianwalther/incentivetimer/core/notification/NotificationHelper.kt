package com.florianwalther.incentivetimer.core.notification

import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import com.florianwalther.incentivetimer.R
import com.florianwalther.incentivetimer.application.ITActivity
import com.florianwalther.incentivetimer.features.timer.PomodoroPhase
import com.florianwalther.incentivetimer.features.timer.PomodoroTimerState
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
) {
    private val notificationManager = NotificationManagerCompat.from(applicationContext)

    private val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    } else {
        PendingIntent.FLAG_UPDATE_CURRENT
    }

    private val openTimerIntent = Intent(
        Intent.ACTION_VIEW,
        "https://www.incentivetimer.com/timer".toUri(),
        applicationContext,
        ITActivity::class.java
    )
    private val openTimerPendingIntent = PendingIntent.getActivity(
        applicationContext, 0, openTimerIntent, pendingIntentFlags
    )

    init {
        createNotificationChannels()
    }

    fun getBaseTimerServiceNotification() =
        NotificationCompat.Builder(applicationContext, TIMER_SERVICE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_timer)
            .setContentIntent(openTimerPendingIntent)
            .setSilent(true)
            .setOnlyAlertOnce(true)

    fun updateTimerNotification(timerState: PomodoroTimerState) {
        val notificationUpdate = getBaseTimerServiceNotification()
            .setContentTitle(applicationContext.getString(timerState.currentPhase.readableName))
            .setContentText(timerState.timeLeftInMillis.toString())
            .build()
        notificationManager.notify(TIMER_SERVICE_NOTIFICATION_ID, notificationUpdate)
    }

    fun showTimerCompletedNotification(finishedPhase: PomodoroPhase) {
        val title: Int
        val text: Int

        when (finishedPhase) {
            PomodoroPhase.POMODORO -> {
                title = R.string.pomodoro_completed_title
                text = R.string.pomodoro_completed_message
            }
            PomodoroPhase.SHORT_BREAK, PomodoroPhase.LONG_BREAK -> {
                title = R.string.break_over_title
                text = R.string.break_over_message
            }
        }

        val timerCompletedNotification =
            NotificationCompat.Builder(applicationContext, TIMER_COMPLETED_CHANNEL_ID)
                .setContentTitle(applicationContext.getString(title))
                .setContentText(applicationContext.getString(text))
                .setSmallIcon(R.drawable.ic_timer)
                .setContentIntent(openTimerPendingIntent)
                .build()
        notificationManager.notify(TIMER_COMPLETED_NOTIFICATION_ID, timerCompletedNotification)
    }

    fun removeTimerServiceNotification() {
        notificationManager.cancel(TIMER_SERVICE_NOTIFICATION_ID)
    }

    fun removeTimerCompletedNotification() {
        notificationManager.cancel(TIMER_COMPLETED_NOTIFICATION_ID)
    }

    private fun createNotificationChannels() {
        val timerServiceChannel = NotificationChannelCompat.Builder(
            TIMER_SERVICE_CHANNEL_ID,
            NotificationManagerCompat.IMPORTANCE_DEFAULT
        )
            .setName(applicationContext.getString(R.string.timer_service_channel_name))
            .setDescription(applicationContext.getString(R.string.timer_service_channel_description))
            .setSound(null, null)
            .build()

        val timerCompletedChannel = NotificationChannelCompat.Builder(
            TIMER_COMPLETED_CHANNEL_ID,
            NotificationManagerCompat.IMPORTANCE_HIGH
        )
            .setName(applicationContext.getString(R.string.timer_completed_channel_name))
            .setDescription(applicationContext.getString(R.string.timer_completed_channel_description))
            .build()

        notificationManager.createNotificationChannelsCompat(
            listOf(
                timerServiceChannel,
                timerCompletedChannel
            )
        )
    }
}

private const val TIMER_SERVICE_CHANNEL_ID = "timer_service_channel"
private const val TIMER_COMPLETED_CHANNEL_ID = "timer_completed_notification_channel"
const val TIMER_SERVICE_NOTIFICATION_ID = 123
private const val TIMER_COMPLETED_NOTIFICATION_ID = 124