package com.florianwalther.incentivetimer.features.timer

import com.florianwalther.incentivetimer.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CountDownTimer @Inject constructor(
    @ApplicationScope private val scope: CoroutineScope,
    private val timeSource: TimeSource,
) {
    private var millisUntilFinished = 0L

    private var timerJob: Job? = null

    fun startTimer(
        durationMillis: Long,
        countDownInterval: Long,
        onTick: (millisUntilFinished: Long) -> Unit,
        onFinish: () -> Unit,
    ) {
        millisUntilFinished = durationMillis
        val startTime = timeSource.elapsedRealTime
        val targetTime = startTime + durationMillis
        timerJob = scope.launch {
            while (true) {
                if (timeSource.elapsedRealTime < targetTime) {
                    println("elapsedRealTime = ${timeSource.elapsedRealTime}")
                    delay(countDownInterval)
                    millisUntilFinished = targetTime - timeSource.elapsedRealTime
                    onTick(millisUntilFinished)
                } else {
                    println("onFinish")
                    onFinish()
                    break
                }
            }
        }
    }

    fun cancelTimer() {
        timerJob?.cancel()
        timerJob = null
    }
}