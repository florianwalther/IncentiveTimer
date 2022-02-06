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
        onTick: suspend (millisUntilFinished: Long) -> Unit,
        onFinish: () -> Unit,
    ) {
        millisUntilFinished = durationMillis
        val startTime = timeSource.elapsedRealTime
        val targetTime = startTime + durationMillis
        timerJob = scope.launch {
            while (true) {
                if (timeSource.elapsedRealTime < targetTime) {
                    delay(minOf(countDownInterval, durationMillis))
                    millisUntilFinished = targetTime - timeSource.elapsedRealTime
                    println("elapsedRealtime = ${timeSource.elapsedRealTime}")
                    println("targetTime = ${targetTime}")
                    onTick(millisUntilFinished)
                } else {
                    println("onFinish targetTime = $targetTime")
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