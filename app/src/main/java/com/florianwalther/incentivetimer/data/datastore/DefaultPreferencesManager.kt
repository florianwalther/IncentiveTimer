package com.florianwalther.incentivetimer.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import logcat.asLog
import logcat.logcat
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

data class TimerPreferences(
    val pomodoroLengthInMinutes: Int,
    val shortBreakLengthInMinutes: Int,
    val longBreakLengthInMinutes: Int,
    val pomodorosPerSet: Int,
) {
    fun lengthInMinutesForPhase(phase: PomodoroPhase) = when(phase){
        PomodoroPhase.POMODORO -> pomodoroLengthInMinutes
        PomodoroPhase.SHORT_BREAK -> shortBreakLengthInMinutes
        PomodoroPhase.LONG_BREAK -> longBreakLengthInMinutes
    }
}

@Singleton
class DefaultPreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context,
) : PreferencesManager {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "preferences")

    override val timerPreferences: Flow<TimerPreferences> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                logcat { exception.asLog() }
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val pomodoroLengthInMinutes = preferences[PreferencesKeys.POMODORO_LENGTH_IN_MINUTES]
                ?: POMODORO_LENGTH_IN_MINUTES_DEFAULT
            val shortBreakLengthInMinutes =
                preferences[PreferencesKeys.SHORT_BREAK_LENGTH_IN_MINUTES]
                    ?: SHORT_BREAK_LENGTH_IN_MINUTES_DEFAULT
            val longBreakLengthInMinutes =
                preferences[PreferencesKeys.LONG_BREAK_LENGTH_IN_MINUTES]
                    ?: LONG_BREAK_LENGTH_IN_MINUTES_DEFAULT
            val pomodorosPerSet =
                preferences[PreferencesKeys.POMODOROS_PER_SET] ?: POMODOROS_PER_SET_DEFAULT
            TimerPreferences(
                pomodoroLengthInMinutes = pomodoroLengthInMinutes,
                shortBreakLengthInMinutes = shortBreakLengthInMinutes,
                longBreakLengthInMinutes = longBreakLengthInMinutes,
                pomodorosPerSet = pomodorosPerSet,
            )
        }

    override suspend fun updatePomodoroLength(lengthInMinutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.POMODORO_LENGTH_IN_MINUTES] = lengthInMinutes
        }
    }

    override suspend fun updateShortBreakLength(lengthInMinutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHORT_BREAK_LENGTH_IN_MINUTES] = lengthInMinutes
        }
    }

    override suspend fun updateLongBreakLength(lengthInMinutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LONG_BREAK_LENGTH_IN_MINUTES] = lengthInMinutes
        }
    }

    override suspend fun updatePomodorosPerSet(amount: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.POMODOROS_PER_SET] = amount
        }
    }

    private object PreferencesKeys {
        val POMODORO_LENGTH_IN_MINUTES = intPreferencesKey("POMODORO_LENGTH_IN_MINUTES")
        val SHORT_BREAK_LENGTH_IN_MINUTES = intPreferencesKey("SHORT_BREAK_LENGTH_IN_MINUTES")
        val LONG_BREAK_LENGTH_IN_MINUTES = intPreferencesKey("LONG_BREAK_LENGTH_IN_MINUTES")
        val POMODOROS_PER_SET = intPreferencesKey("POMODOROS_PER_SET")
    }
}

const val POMODORO_LENGTH_IN_MINUTES_DEFAULT = 25
const val SHORT_BREAK_LENGTH_IN_MINUTES_DEFAULT = 5
const val LONG_BREAK_LENGTH_IN_MINUTES_DEFAULT = 15
const val POMODOROS_PER_SET_DEFAULT = 4