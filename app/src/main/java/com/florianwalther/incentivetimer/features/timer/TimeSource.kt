package com.florianwalther.incentivetimer.features.timer

interface TimeSource {
    val elapsedRealTime: Long
}