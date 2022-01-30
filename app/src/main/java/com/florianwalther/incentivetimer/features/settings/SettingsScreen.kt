package com.florianwalther.incentivetimer.features.settings

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Watch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.florianwalther.incentivetimer.BuildConfig
import com.florianwalther.incentivetimer.R
import com.florianwalther.incentivetimer.core.ui.composables.AppInstructionsDialog
import com.florianwalther.incentivetimer.core.ui.composables.LabeledRadioButton
import com.florianwalther.incentivetimer.core.ui.composables.NumberPicker
import com.florianwalther.incentivetimer.core.ui.theme.IncentiveTimerTheme
import com.florianwalther.incentivetimer.data.datastore.ThemeSelection
import com.florianwalther.incentivetimer.features.settings.model.SettingsScreenState

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
    val autoStartNextTimer = screenState.timerPreferences.autoStartNextTimer

    val selectedTheme = screenState.appPreferences.selectedTheme

    val scrollState = rememberScrollState()

    Column(Modifier.verticalScroll(scrollState)) {
        SectionTitle(R.string.general)
        BasicPreference(
            title = stringResource(R.string.theme),
            summary = stringResource(selectedTheme.readableName),
            onClick = actions::onThemePreferenceClicked
        )
        SectionTitle(R.string.timer)
        BasicPreference(
            title = stringResource(R.string.pomodoro_length),
            summary = stringResource(R.string.minutes_placeholder, pomodoroLength),
            onClick = actions::onPomodoroLengthPreferenceClicked,
        )
        BasicPreference(
            title = stringResource(R.string.short_break_length),
            summary = stringResource(R.string.minutes_placeholder, shortBreakLength),
            onClick = actions::onShortBreakLengthPreferenceClicked,

            )
        BasicPreference(
            title = stringResource(R.string.long_break_length),
            summary = stringResource(R.string.minutes_placeholder, longBreakLength),
            onClick = actions::onLongBreakLengthPreferenceClicked,
        )
        BasicPreference(
            title = stringResource(R.string.number_of_pomodoros_before_long_break),
            summary = pomodorosPerSet.toString(),
            onClick = actions::onPomodorosPerSetPreferenceClicked,
        )
        SwitchPreference(
            title = stringResource(R.string.auto_start_timer),
            summary = stringResource(R.string.timer_behaviour_description),
            checked = autoStartNextTimer,
            onCheckedChanged = actions::onAutoStartNextTimerCheckedChanged
        )
        SectionTitle(R.string.links)
        val context = LocalContext.current
        BasicPreference(
            title = stringResource(R.string.show_app_instructions),
            onClick = actions::onShowAppInstructionsClicked
        )
        BasicPreference(
            title = stringResource(R.string.show_privacy_policy),
            onClick = {
                val website =
                    Uri.parse("https://codinginflow.com/privacy-policy-incentivetimer-app")
                val intent = Intent(Intent.ACTION_VIEW, website)
                context.startActivity(intent)
            }
        )
        BasicPreference(
            title = stringResource(R.string.contact_support),
            summary = stringResource(R.string.bug_report_feature_request_text),
            onClick = {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:info@codinginflow.com")
                    putExtra(Intent.EXTRA_SUBJECT, "IncentiveTimer feature request/bug report")
                }
                context.startActivity(intent)
            }
        )
        SectionTitle(R.string.info)
        BasicPreference(
            title = stringResource(R.string.app_version),
            summary = BuildConfig.VERSION_NAME,
            icon = Icons.Default.Info,
        )
    }

    if (screenState.showThemeDialog) {
        SingleChoiceDialog(
            title = stringResource(R.string.theme),
            optionNames = ThemeSelection.values().toList().map { stringResource(it.readableName) },
            initialSelectedOptionIndex = ThemeSelection.values()
                .indexOf(screenState.appPreferences.selectedTheme),
            onConfirmed = { index ->
                actions.onThemeSelected(ThemeSelection.values()[index])
            },
            onDismissRequest = actions::onThemeDialogDismissed
        )
    }

    if (screenState.showPomodoroLengthDialog) {
        NumberPickerDialog(
            title = stringResource(R.string.pomodoro_length),
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
            title = stringResource(R.string.short_break_length),
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
            title = stringResource(R.string.long_break_length),
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
            title = stringResource(R.string.number_of_pomodoros_before_long_break),
            minValue = 1,
            maxValue = 16,
            initialValue = pomodorosPerSet,
            onConfirmed = actions::onPomodorosPerSetSet,
            onDismissRequest = actions::onPomodorosPerSetDialogDismissed
        )
    }

    if (screenState.showAppInstructionsDialog) {
        AppInstructionsDialog(
            onDismissRequest = actions::onAppInstructionsDialogDismissed
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
    modifier: Modifier = Modifier,
    summary: String? = null,
    onClick: (() -> Unit)? = null,
    icon: ImageVector? = null,
) {
    Row(
        modifier
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(16.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null)
            Spacer(modifier = Modifier.width(16.dp))
        }
        Column {
            Text(text = title)
            if (summary != null) {
                Text(
                    text = summary,
                    color = Color.Gray,
                    style = MaterialTheme.typography.body2
                )
            }
        }
    }
}

@Composable
private fun SwitchPreference(
    title: String,
    modifier: Modifier = Modifier,
    summary: String? = null,
    checked: Boolean,
    onCheckedChanged: (checked: Boolean) -> Unit,
    icon: ImageVector? = null,
) {
    Row(
        modifier
            .clickable(onClick = { onCheckedChanged(!checked) })
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null)
            Spacer(modifier = Modifier.width(16.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(text = title)
            if (summary != null) {
                Text(
                    text = summary,
                    color = Color.Gray,
                    style = MaterialTheme.typography.body2
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChanged,
        )
    }
}

@Composable
private fun NumberPickerDialog(
    title: String,
    minValue: Int,
    maxValue: Int,
    initialValue: Int,
    modifier: Modifier = Modifier,
    onConfirmed: (selectedValue: Int) -> Unit,
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
                Text(title)
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

@Composable
private fun SingleChoiceDialog(
    title: String,
    optionNames: List<String>,
    initialSelectedOptionIndex: Int,
    modifier: Modifier = Modifier,
    onConfirmed: (index: Int) -> Unit,
    onDismissRequest: () -> Unit,
) {
    var selectedOptionIndex by rememberSaveable { mutableStateOf(initialSelectedOptionIndex) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(title)
        },
        text = {
            Column {
                optionNames.forEach { optionName ->
                    LabeledRadioButton(
                        text = optionName,
                        selected = selectedOptionIndex == optionNames.indexOf(optionName),
                        onClick = { selectedOptionIndex = optionNames.indexOf(optionName) }
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.cancel))
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirmed(selectedOptionIndex) }) {
                Text(stringResource(R.string.ok))
            }
        },
        modifier = modifier,
    )
}

@Preview(
    name = "Light mode",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
    name = "Dark mode",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun SwitchPreferencePreview() {
    IncentiveTimerTheme {
        Surface {
            SwitchPreference(
                title = "Switch preference",
                summary = "This is the summary",
                checked = true,
                onCheckedChanged = {},
                icon = Icons.Default.Watch,
            )
        }
    }
}
/*
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
                    override fun onAutoStartNextTimerCheckedChanged(checked: Boolean) {}
                    override fun onShowAppInstructionsClicked() {}
                    override fun onAppInstructionsDialogDismissed() {}
                    override fun onThemePreferenceClicked() {}
                    override fun onThemeSelected(theme: ThemeSelection) {}
                    override fun onThemeDialogDismissed() {}
                }
            )
        }
    }
}
*/
