package com.florianwalther.incentivetimer.core.util

import org.junit.Assert.*
import org.junit.Test
import java.util.*
import com.google.common.truth.Truth.assertThat

class DateUtilsTest {

    private val dateTimestamp = 1644583178942L // Fri Feb 11 13:39:38 GMT+01:00 2022

    @Test
    fun dateWithoutTime_returnsCorrectValue() {
        val dateWithoutTime = Date(dateTimestamp).withOutTime()

        assertThat(dateWithoutTime.time).isEqualTo(1644534000000L)
    }

    @Test
    fun dayAfter_returnsCorrectValue() {
        val dayAfter = Date(dateTimestamp).dayAfter()

        assertThat(dayAfter.time).isEqualTo(dateTimestamp + 24 * 60 * 60 * 1000L)
    }
}