package com.florianwalther.incentivetimer.data.preferences

import com.zhuinden.flowcombinetuplekt.combineTuple
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakePreferencesManager(
    initialPomodoroLengthInMinutes: Int,
    initialShortBreakLengthInMinutes: Int,
    initialLongBreakLengthInMinutes: Int,
    initialPomodorosPerSet: Int,
) : PreferencesManager {

    private val pomodoroLengthInMinutes = MutableStateFlow(initialPomodoroLengthInMinutes)
    private val shortBreakLengthInMinutes = MutableStateFlow(initialShortBreakLengthInMinutes)
    private val longBreakLengthInMinutes = MutableStateFlow(initialLongBreakLengthInMinutes)
    private val pomodorosPerSet = MutableStateFlow(initialPomodorosPerSet)

    override val timerPreferences: Flow<TimerPreferences> = combineTuple(
        pomodoroLengthInMinutes,
        shortBreakLengthInMinutes,
        longBreakLengthInMinutes,
        pomodorosPerSet,
    ).map { (
                pomodoroLengthInMinutes,
                shortBreakLengthInMinutes,
                longBreakLengthInMinutes,
                pomodorosPerSet
            ) ->
        TimerPreferences(
            pomodoroLengthInMinutes = pomodoroLengthInMinutes,
            shortBreakLengthInMinutes = shortBreakLengthInMinutes,
            longBreakLengthInMinutes = longBreakLengthInMinutes,
            pomodorosPerSet = pomodorosPerSet,
        )
    }

    override suspend fun updatePomodoroLength(lengthInMinutes: Int) {
        pomodoroLengthInMinutes.value = lengthInMinutes
    }

    override suspend fun updateShortBreakLength(lengthInMinutes: Int) {
        shortBreakLengthInMinutes.value = lengthInMinutes
    }

    override suspend fun updateLongBreakLength(lengthInMinutes: Int) {
        longBreakLengthInMinutes.value = lengthInMinutes
    }

    override suspend fun updatePomodorosPerSet(amount: Int) {
        pomodorosPerSet.value = amount
    }
}