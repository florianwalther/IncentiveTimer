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

    private val tickEventChannel = Channel<MillisUntilFinished>()
    val tickEvent = tickEventChannel.receiveAsFlow()

    private val finishEventChannel = Channel<Unit>()
    val finishEvent = finishEventChannel.receiveAsFlow()

    fun startTimer(
        millisInFuture: Long,
        countDownInterval: Long,
    ) {
        millisUntilFinished = millisInFuture
        timerJob = scope.launch {
            val startTime = timeSource.elapsedRealTime
            val targetTime = startTime + millisInFuture
            while (true) {
                println("elapsedRealTime = ${timeSource.elapsedRealTime}")
                if (timeSource.elapsedRealTime < targetTime) {
                    println("if")
                    delay(countDownInterval)
                    println("targetTime = $targetTime")
                    println("elapsedRealTime = ${timeSource.elapsedRealTime}")
                    millisUntilFinished = targetTime - timeSource.elapsedRealTime
                    println("millisUntilFinished = $millisUntilFinished")
                    tickEventChannel.send(MillisUntilFinished(millisUntilFinished))
                } else {
                    println("else")
                    finishEventChannel.send(Unit)
                }
            }
        }
    }

    fun cancelTimer() {
        timerJob?.cancel()
        timerJob = null
    }
}