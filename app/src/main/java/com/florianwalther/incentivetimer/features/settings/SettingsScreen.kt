package com.florianwalther.incentivetimer.features.settings

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.florianwalther.incentivetimer.R
import com.florianwalther.incentivetimer.core.ui.composables.NumberPicker
import com.florianwalther.incentivetimer.core.ui.theme.IncentiveTimerTheme
import com.florianwalther.incentivetimer.features.settings.model.SettingsScreenState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun SettingsScreenAppBar() {
    TopAppBar(
        title = {
            Text(stringResource(R.string.settings))
        }
    )
}

@Composable
fun SettingsScreenContent(
    screenState: SettingsScreenState,
    actions: SettingsScreenActions,
) {
    val pomodoroLength = screenState.timerPreferences.pomodoroLengthInMinutes
    val shortBreakLength = screenState.timerPreferences.shortBreakLengthInMinutes
    val longBreakLength = screenState.timerPreferences.longBreakLengthInMinutes
    val pomodorosPerSet = screenState.timerPreferences.pomodorosPerSet

    Column {
        SectionTitle(R.string.timer)
        BasicPreference(
            title = stringResource(R.string.pomodoro_length),
            summary = stringResource(R.string.minutes_placeholder, pomodoroLength),
            onClick = actions::onPomodoroLengthPreferenceClicked,
        )
        Divider()
        BasicPreference(
            title = stringResource(R.string.short_break_length),
            summary = stringResource(R.string.minutes_placeholder, shortBreakLength),
            onClick = actions::onShortBreakLengthPreferenceClicked,

            )
        Divider()
        BasicPreference(
            title = stringResource(R.string.long_break_length),
            summary = stringResource(R.string.minutes_placeholder, longBreakLength),
            onClick = actions::onLongBreakLengthPreferenceClicked,
        )
        Divider()
        BasicPreference(
            title = stringResource(R.string.number_of_pomodoros_before_long_break),
            summary = pomodorosPerSet.toString(),
            onClick = actions::onPomodorosPerSetPreferenceClicked,
        )
    }

    if (screenState.showPomodoroLengthDialog) {
        NumberPickerDialog(
            title = R.string.pomodoro_length,
            minValue = 1,
            maxValue = 180,
            initialValue = pomodoroLength,
            onConfirmed = actions::onPomodoroLengthSet,
            onDismissRequest = actions::onPomodoroLengthDialogDismissed,
            label = R.string.minutes_abbr,
        )
    }

    if (screenState.showShortBreakLengthDialog) {
        NumberPickerDialog(
            title = R.string.short_break_length,
            minValue = 1,
            maxValue = 180,
            initialValue = shortBreakLength,
            onConfirmed = actions::onShortBreakLengthSet,
            onDismissRequest = actions::onShortBreakLengthDialogDismissed,
            label = R.string.minutes_abbr,
        )
    }

    if (screenState.showLongBreakLengthDialog) {
        NumberPickerDialog(
            title = R.string.long_break_length,
            minValue = 1,
            maxValue = 180,
            initialValue = longBreakLength,
            onConfirmed = actions::onLongBreakLengthSet,
            onDismissRequest = actions::onLongBreakLengthDialogDismissed,
            label = R.string.minutes_abbr,
        )
    }

    if (screenState.showPomodorosPerSetDialog) {
        NumberPickerDialog(
            title = R.string.number_of_pomodoros_before_long_break,
            minValue = 1,
            maxValue = 16,
            initialValue = pomodorosPerSet,
            onConfirmed = actions::onPomodorosPerSetSet,
            onDismissRequest = actions::onPomodorosPerSetDialogDismissed
        )
    }
}

@Composable
private fun SectionTitle(
    @StringRes title: Int,
    modifier: Modifier = Modifier,
) {
    Text(
        stringResource(title),
        style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colors.primary,
        modifier = modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp)
    )

}

@Composable
private fun BasicPreference(
    title: String,
    summary: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    Row(
        modifier
            .clickable(onClick = onClick)
            .padding(16.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null)
            Spacer(modifier = Modifier.width(16.dp))
        }
        Column {
            Text(text = title, fontWeight = FontWeight.Bold)
            Text(
                text = summary,
                color = Color.Gray,
                style = MaterialTheme.typography.body2
            )
        }
    }
}

@Composable
private fun NumberPickerDialog(
    @StringRes title: Int,
    minValue: Int,
    maxValue: Int,
    initialValue: Int,
    modifier: Modifier = Modifier,
    onConfirmed: (value: Int) -> Unit,
    onDismissRequest: () -> Unit,
    @StringRes label: Int? = null,
) {
    var value by rememberSaveable { mutableStateOf(initialValue) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(stringResource(title), modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    NumberPicker(
                        minValue = minValue,
                        maxValue = maxValue,
                        value = initialValue,
                        onValueChanged = { value = it },
                    )
                    if (label != null) {
                        Text(stringResource(label))
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        },
        text = null, // Vertical alignment broken
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.cancel))
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirmed(value) }) {
                Text(stringResource(R.string.ok))
            }
        },
        modifier = modifier,
    )
}

@Preview(
    name = "Light mode",
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Dark mode",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun ScreenContentPreview() {
    IncentiveTimerTheme {
        Surface {
            SettingsScreenContent(
                screenState = SettingsScreenState.initialState,
                actions = object : SettingsScreenActions {
                    override fun onPomodoroLengthPreferenceClicked() {}
                    override fun onShortBreakLengthPreferenceClicked() {}
                    override fun onLongBreakLengthPreferenceClicked() {}
                    override fun onPomodoroLengthSet(lengthInMinutes: Int) {}
                    override fun onPomodoroLengthDialogDismissed() {}
                    override fun onShortBreakLengthSet(lengthInMinutes: Int) {}
                    override fun onShortBreakLengthDialogDismissed() {}
                    override fun onLongBreakLengthSet(lengthInMinutes: Int) {}
                    override fun onLongBreakLengthDialogDismissed() {}
                    override fun onPomodorosPerSetPreferenceClicked() {}
                    override fun onPomodorosPerSetSet(amount: Int) {}
                    override fun onPomodorosPerSetDialogDismissed() {}
                }
            )
        }
    }
}
