package com.florianwalther.incentivetimer.features.timer

class FakeTimerServiceManager : TimerServiceManager {

    var serviceRunning = false

    override fun startTimerService() {
        serviceRunning = true
    }

    override fun stopTimerService() {
        serviceRunning = false
    }
}