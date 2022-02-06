package com.florianwalther.incentivetimer.features.timer

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.florianwalther.incentivetimer.core.DailyResetBroadcastReceiver
import com.florianwalther.incentivetimer.core.util.dayAfter
import com.florianwalther.incentivetimer.core.util.withOutTime
import dagger.hilt.android.qualifiers.ApplicationContext
import logcat.logcat
import java.util.*
import javax.inject.Inject

class DailyResetManager @Inject constructor(@ApplicationContext context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    } else {
        PendingIntent.FLAG_UPDATE_CURRENT
    }

    private val intent = Intent(context, DailyResetBroadcastReceiver::class.java)
    private val broadcastIntent = PendingIntent.getBroadcast(context, 0, intent, pendingIntentFlags)

    fun scheduleDailyReset() {
        val tomorrowMidnight = Date().withOutTime().dayAfter().time
        alarmManager.set(AlarmManager.RTC, tomorrowMidnight, broadcastIntent)
    }

    fun stopDailyReset() {
        alarmManager.cancel(broadcastIntent)
    }
}