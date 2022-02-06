package com.florianwalther.incentivetimer.data.datastore

import android.content.Context
import androidx.annotation.StringRes
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.florianwalther.incentivetimer.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import logcat.asLog
import logcat.logcat
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

data class PomodoroTimerState(
    val timerRunning: Boolean,
    val currentPhase: PomodoroPhase,
    val timeLeftInMillis: Long,
    val timeTargetInMillis: Long,
    val pomodorosCompletedInSet: Int,
    val pomodorosPerSetTarget: Int,
    val pomodorosCompletedTotal: Int,
) {
    companion object {
        val initialState = PomodoroTimerState(
            timerRunning = false,
            currentPhase = PomodoroPhase.POMODORO,
            timeLeftInMillis = 0L,
            timeTargetInMillis = 0L,
            pomodorosCompletedInSet = 0,
            pomodorosPerSetTarget = 0,
            pomodorosCompletedTotal = 0,
        )
    }
}

@Singleton
class DefaultPomodoroTimerStateManager @Inject constructor(
    @ApplicationContext private val context: Context,
) : PomodoroTimerStateManager {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "timer_state")

    override val timerState: Flow<PomodoroTimerState> =
        context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    logcat { exception.asLog() }
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                val timerRunning = preferences[PreferencesKeys.TIMER_RUNNING] ?: false
                logcat { "map with timerRunning = $timerRunning" }
                val currentPhase = PomodoroPhase.valueOf(
                    preferences[PreferencesKeys.CURRENT_PHASE] ?: PomodoroPhase.POMODORO.name
                )
                val timeLeftInMillis = preferences[PreferencesKeys.TIME_LEFT_IN_MILLIS]
                    ?: PomodoroTimerState.initialState.timeLeftInMillis
                val timeTargetInMillis = preferences[PreferencesKeys.TIME_TARGET_IN_MILLIS]
                    ?: PomodoroTimerState.initialState.timeTargetInMillis
                val pomodorosPerSetTarget =
                    preferences[PreferencesKeys.POMODOROS_PER_SET_TARGET]
                        ?: PomodoroTimerState.initialState.pomodorosPerSetTarget
                val pomodorosCompletedInSet =
                    preferences[PreferencesKeys.POMODOROS_COMPLETED_IN_SET]
                        ?: PomodoroTimerState.initialState.pomodorosCompletedInSet
                val pomodorosCompletedTotal =
                    preferences[PreferencesKeys.POMODOROS_COMPLETED_TOTAL]
                        ?: PomodoroTimerState.initialState.pomodorosCompletedTotal
                PomodoroTimerState(
                    timerRunning = timerRunning,
                    currentPhase = currentPhase,
                    timeLeftInMillis = timeLeftInMillis,
                    timeTargetInMillis = timeTargetInMillis,
                    pomodorosPerSetTarget = pomodorosPerSetTarget,
                    pomodorosCompletedInSet = pomodorosCompletedInSet,
                    pomodorosCompletedTotal = pomodorosCompletedTotal,
                )
            }

    override suspend fun updateTimerRunning(timerRunning: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TIMER_RUNNING] = timerRunning
        }
    }

    override suspend fun updateCurrentPhase(phase: PomodoroPhase) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CURRENT_PHASE] = phase.name
        }
    }

    override suspend fun updateTimeLeftInMillis(timeLeftInMillis: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TIME_LEFT_IN_MILLIS] = timeLeftInMillis
        }
    }

    override suspend fun updateTimeTargetInMillis(timeTargetInMillis: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TIME_TARGET_IN_MILLIS] = timeTargetInMillis
        }
    }

    override suspend fun updatePomodorosPerSetTarget(pomodorosPerSetTarget: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.POMODOROS_PER_SET_TARGET] = pomodorosPerSetTarget
        }
    }

    override suspend fun updatePomodorosCompletedInSet(pomodorosCompletedInSet: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.POMODOROS_COMPLETED_IN_SET] = pomodorosCompletedInSet
        }
    }

    override suspend fun updatePomodorosCompletedTotal(pomodorosCompletedTotal: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.POMODOROS_COMPLETED_TOTAL] = pomodorosCompletedTotal
        }
    }

    private object PreferencesKeys {
        val TIMER_RUNNING = booleanPreferencesKey("TIMER_RUNNING")
        val CURRENT_PHASE = stringPreferencesKey("CURRENT_PHASE")
        val TIME_LEFT_IN_MILLIS = longPreferencesKey("TIME_LEFT_IN_MILLIS")
        val TIME_TARGET_IN_MILLIS = longPreferencesKey("TIME_TARGET_IN_MILLIS")
        val POMODOROS_PER_SET_TARGET = intPreferencesKey("POMODOROS_PER_SET_TARGET")
        val POMODOROS_COMPLETED_IN_SET = intPreferencesKey("POMODOROS_COMPLETED_IN_SET")
        val POMODOROS_COMPLETED_TOTAL = intPreferencesKey("POMODOROS_COMPLETED_TOTAL")
    }
}