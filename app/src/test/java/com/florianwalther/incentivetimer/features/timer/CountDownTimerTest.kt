package com.florianwalther.incentivetimer.features.timer

import app.cash.turbine.test
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
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

    @Test
    fun startTimer_countDownInterval1000_onTickCalledAt1000() = testScope.runTest {
        countDownTimer.startTimer(
            millisInFuture = 20_000,
            countDownInterval = 1000,
        )

        advanceTimeBy(1000)
        fakeTimeSource.advanceTimeBy(1000)

        countDownTimer.tickEvent.test {
            assertThat(awaitItem().value).isEqualTo(19_000)
        }
    }
}