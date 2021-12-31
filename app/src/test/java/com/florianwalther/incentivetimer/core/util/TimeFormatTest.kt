package com.florianwalther.incentivetimer.core.util

import org.junit.Assert.*
import org.junit.Test
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage


class TimeFormatTest {

    @Test
    fun formatMillisecondsToTimeString_oneHour_returnsCorrectString() {
        val milliseconds = 60 * 60 * 1_000L

        val formattedTimeString = formatMillisecondsToTimeString(milliseconds)

        assertThat(formattedTimeString).isEqualTo("1:00:00")
    }

    @Test
    fun formatMillisecondsToTimeString_oneMinute_returnsCorrectString() {
        val milliseconds = 60 * 1_000L

        val formattedTimeString = formatMillisecondsToTimeString(milliseconds)

        assertThat(formattedTimeString).isEqualTo("1:00")
    }

    @Test
    fun formatMillisecondsToTimeString_roundsUp() {
        val milliseconds =  1L

        val formattedTimeString = formatMillisecondsToTimeString(milliseconds)

        assertThat(formattedTimeString).isEqualTo("0:01")
    }
}