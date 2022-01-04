package com.florianwalther.incentivetimer.features.timer

class FakeTimeSource : TimeSource {
    override var elapsedRealTime = 0L

    fun advanceTimeBy(milliseconds: Long) {
        elapsedRealTime += milliseconds
    }
}