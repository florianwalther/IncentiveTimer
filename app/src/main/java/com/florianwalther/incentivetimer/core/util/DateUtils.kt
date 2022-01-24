package com.florianwalther.incentivetimer.core.util

import java.util.*

fun Date.withOutTime(): Date {
    val calendar = Calendar.getInstance().apply {
        time = this@withOutTime
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return Date(calendar.timeInMillis)
}

fun Date.dayAfter(): Date {
    val calendar = Calendar.getInstance().apply {
        time = this@dayAfter
        add(Calendar.DATE, 1)
    }
    return Date(calendar.timeInMillis)
}