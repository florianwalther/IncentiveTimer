package com.florianwalther.incentivetimer.features.timer

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.*
import kotlinx.coroutines.withContext
import org.junit.Assert.*

import org.junit.After
import org.junit.Before
import org.junit.Test

class CountDownTimerTest {

    private val testScope = TestScope()

    private lateinit var fakeTimeSource: FakeTimeSource

    private lateinit var countDownTimer: CountDownTimer

    @Before
    fun setUp() {
        fakeTimeSource = FakeTimeSource()
        countDownTimer = CountDownTimer(
            scope = testScope,
            timeSource = fakeTimeSource,
        )
    }

    // TODO: 06/01/2022 Our tests get stuck in infinite loops if delay in the startTimer
    //  method evaluates to 0 -> Can we fix this?

    @Test
    fun startTimer_countDownInterval1_emitsOnTickEvery1() = testScope.runTest {
        var tickCount = 0

        countDownTimer.startTimer(
            durationMillis = 10,
            countDownInterval = 1,
            onTick = { tickCount++ },
            onFinish = { }
        )

        repeat(10) { count ->
            advanceTimeBy(1)
            fakeTimeSource.advanceTimeBy(1)
            runCurrent()
            assertThat(tickCount).isEqualTo(count + 1)
        }
    }

    @Test
    fun startTimer_countDownInterval1000_emitsOnTickEvery1000() = testScope.runTest {
        var tickCount = 0

        countDownTimer.startTimer(
            durationMillis = 10_000,
            countDownInterval = 1_000,
            onTick = { tickCount++ },
            onFinish = { }
        )

        repeat(10) { count ->
            advanceTimeBy(1_000)
            fakeTimeSource.advanceTimeBy(1_000)
            runCurrent()
            assertThat(tickCount).isEqualTo(count + 1)
        }
    }

    @Test
    fun startTimer_countDownInterval4000_emitsOnTickEvery4000() = testScope.runTest {
        var tickCount = 0

        countDownTimer.startTimer(
            durationMillis = 12_000,
            countDownInterval = 4_000,
            onTick = { tickCount++ },
            onFinish = { }
        )

        repeat(3) { count ->
            advanceTimeBy(4_000)
            fakeTimeSource.advanceTimeBy(4_000)
            runCurrent()
            assertThat(tickCount).isEqualTo(count + 1)
        }
    }

    @Test
    fun startTimer_countDownIntervalGreaterThanDuration_callsOnFinishAfterDuration() = testScope.runTest {
        var onFinishCount = 0

        countDownTimer.startTimer(
            durationMillis = 10_000,
            countDownInterval = 20_000,
            onTick = {},
            onFinish = { onFinishCount++ }
        )

        advanceTimeBy(10_000)
        fakeTimeSource.advanceTimeBy(10_000)
        runCurrent()
        assertThat(onFinishCount).isEqualTo( 1)
    }

    // TODO: 06/01/2022 Ask Gabor about this test because I don't see anything we could change
    /*@Test
    fun startTimer_overDuration_returnsCorrectTickCount() = testScope.runTest {
        var tickCount = 0

        countDownTimer.startTimer(
            durationMillis = 10_000,
            countDownInterval = 1_000,
            onTick = { tickCount++ },
            onFinish = { }
        )

        repeat(3) { count ->
            advanceTimeBy(4_000)
            fakeTimeSource.advanceTimeBy(4_000)
            runCurrent()
        }
        assertThat(tickCount).isEqualTo(10)
    }*/

    @Test
    fun startTimer_onFinishCalledExactlyWhenDurationReached() = testScope.runTest {
        var onFinishCount = 0

        countDownTimer.startTimer(
            durationMillis = 10,
            countDownInterval = 1,
            onTick = {},
            onFinish = { onFinishCount++ }
        )

        repeat(10) { count ->
            advanceTimeBy(1)
            fakeTimeSource.advanceTimeBy(1)
            runCurrent()
            if (count < 9) {
                assertThat(onFinishCount).isEqualTo(0)
            }
        }
        assertThat(onFinishCount).isEqualTo(1)
    }

    @Test
    fun cancelTimer_cancelsTimer() = testScope.runTest {
        var tickCount = 0
        var onFinishCount = 0

        countDownTimer.startTimer(
            durationMillis = 10_000,
            countDownInterval = 1_000,
            onTick = { tickCount++ },
            onFinish = { onFinishCount++ }
        )

        countDownTimer.cancelTimer()

        repeat(10) {
            advanceTimeBy(1_000)
            fakeTimeSource.advanceTimeBy(1_000)
            runCurrent()
        }
        assertThat(tickCount).isEqualTo(0)
        assertThat(onFinishCount).isEqualTo(0)
    }
}