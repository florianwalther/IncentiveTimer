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
import logcat.asLog
import logcat.logcat
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

enum class ThemeSelection(@StringRes val readableName: Int) {
    SYSTEM(R.string.auto_system_default), LIGHT(R.string.light), DARK(R.string.dark)
}

data class AppPreferences(
    val selectedTheme: ThemeSelection,
    val appInstructionsDialogShown: Boolean,
)

enum class PomodoroPhase(@StringRes val readableName: Int) {
    POMODORO(R.string.pomodoro), SHORT_BREAK(R.string.short_break), LONG_BREAK(R.string.long_break);

    val isBreak get() = this == SHORT_BREAK || this == LONG_BREAK
}

data class TimerPreferences(
    val pomodoroLengthInMinutes: Int,
    val shortBreakLengthInMinutes: Int,
    val longBreakLengthInMinutes: Int,
    val pomodorosPerSet: Int,
    val autoStartNextTimer: Boolean,
) {
    fun lengthInMinutesForPhase(phase: PomodoroPhase) = when (phase) {
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

    override val appPreferences: Flow<AppPreferences> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                logcat { exception.asLog() }
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val appInstructionsDialogShown =
                preferences[PreferencesKeys.APP_INSTRUCTIONS_DIALOG_SHOWN] ?: false
            val selectedTheme = ThemeSelection.valueOf(
                preferences[PreferencesKeys.SELECTED_THEME] ?: ThemeSelection.SYSTEM.name
            )
            AppPreferences(
                appInstructionsDialogShown = appInstructionsDialogShown,
                selectedTheme = selectedTheme,
            )
        }

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
            val timerBehaviour = preferences[PreferencesKeys.AUTO_START_NEXT_TIMER] ?: true
            TimerPreferences(
                pomodoroLengthInMinutes = pomodoroLengthInMinutes,
                shortBreakLengthInMinutes = shortBreakLengthInMinutes,
                longBreakLengthInMinutes = longBreakLengthInMinutes,
                pomodorosPerSet = pomodorosPerSet,
                autoStartNextTimer = timerBehaviour,
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

    override suspend fun updateAutoStartNextTimer(autoStartNextTimer: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_START_NEXT_TIMER] = autoStartNextTimer
        }
    }

    override suspend fun updateSelectedTheme(theme: ThemeSelection) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_THEME] = theme.name
        }
    }

    override suspend fun updateAppInstructionsDialogShown(shown: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.APP_INSTRUCTIONS_DIALOG_SHOWN] = shown
        }
    }

    private object PreferencesKeys {
        val POMODORO_LENGTH_IN_MINUTES = intPreferencesKey("POMODORO_LENGTH_IN_MINUTES")
        val SHORT_BREAK_LENGTH_IN_MINUTES = intPreferencesKey("SHORT_BREAK_LENGTH_IN_MINUTES")
        val LONG_BREAK_LENGTH_IN_MINUTES = intPreferencesKey("LONG_BREAK_LENGTH_IN_MINUTES")
        val POMODOROS_PER_SET = intPreferencesKey("POMODOROS_PER_SET")
        val AUTO_START_NEXT_TIMER = booleanPreferencesKey("AUTO_START_NEXT_TIMER")
        val SELECTED_THEME = stringPreferencesKey("SELECTED_THEME")
        val APP_INSTRUCTIONS_DIALOG_SHOWN = booleanPreferencesKey("APP_INSTRUCTIONS_DIALOG_SHOWN")
    }
}

const val POMODORO_LENGTH_IN_MINUTES_DEFAULT = 25
const val SHORT_BREAK_LENGTH_IN_MINUTES_DEFAULT = 5
const val LONG_BREAK_LENGTH_IN_MINUTES_DEFAULT = 15
const val POMODOROS_PER_SET_DEFAULT = 4