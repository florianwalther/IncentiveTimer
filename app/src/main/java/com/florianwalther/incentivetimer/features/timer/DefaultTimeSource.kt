package com.florianwalther.incentivetimer.features.timer

import android.os.SystemClock
import javax.inject.Inject

class DefaultTimeSource @Inject constructor() : TimeSource {
    override val elapsedRealTime: Long
        get() = SystemClock.elapsedRealtime()
}