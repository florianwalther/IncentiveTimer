package com.florianwalther.incentivetimer.core.util

import java.text.DateFormat
import java.util.*

fun formatMillisecondsToTimeString(milliseconds: Long): String {
    val secondsAdjusted = (milliseconds + 999) / 1000
    val s = secondsAdjusted % 60
    val m = (secondsAdjusted / 60) % 60
    val h = secondsAdjusted / (60 * 60)
    return if (h < 1) {
        String.format("%d:%02d", m, s)
    } else {
        String.format("%d:%02d:%02d", h, m, s)
    }
}

fun Int.minutesToMilliseconds(): Long = this * 60_000L

fun Long.millisecondsToMinutes(): Int = (this / 60_000L).toInt()