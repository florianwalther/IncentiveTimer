package com.florianwalther.incentivetimer.core.util

fun formatMillisecondsToTimeString(
    milliseconds: Long,
    showHoursWithoutSeconds: Boolean = false
): String {
    val secondsAdjusted = (milliseconds + 999) / 1000
    val s = secondsAdjusted % 60
    val m = (secondsAdjusted / 60) % 60
    val h = secondsAdjusted / (60 * 60)
    return if (h < 1) {
        if (!showHoursWithoutSeconds) {
            String.format("%d:%02d", m, s)
        } else {
            String.format("%d:%02d", 0, m)
        }
    } else {
        if (!showHoursWithoutSeconds) {
            String.format("%d:%02d:%02d", h, m, s)
        } else {
            String.format("%d:%02d", h, m)
        }
    }
}

fun formatMinutesToTimeString(minutes: Int, showHoursWithoutSeconds: Boolean = false): String =
    formatMillisecondsToTimeString(minutes.minutesToMilliseconds(), showHoursWithoutSeconds)


fun Int.minutesToMilliseconds(): Long = this * 60_000L

fun Long.millisecondsToMinutes(): Int = (this / 60_000L).toInt()