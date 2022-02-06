package com.florianwalther.incentivetimer.features.timer

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.florianwalther.incentivetimer.R
import com.florianwalther.incentivetimer.core.ui.composables.RoundedCornerCircularProgressIndicatorWithBackground
import com.florianwalther.incentivetimer.core.ui.composables.SimpleConfirmationDialog
import com.florianwalther.incentivetimer.core.ui.theme.IncentiveTimerTheme
import com.florianwalther.incentivetimer.core.ui.theme.PrimaryLightAlpha
import com.florianwalther.incentivetimer.core.util.formatMillisecondsToTimeString
import com.florianwalther.incentivetimer.core.util.minutesToMilliseconds
import com.florianwalther.incentivetimer.data.datastore.PomodoroPhase
import com.florianwalther.incentivetimer.data.datastore.PomodoroTimerState
import com.florianwalther.incentivetimer.features.timer.model.TimerScreenState
import com.google.accompanist.flowlayout.FlowCrossAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment

@Composable
fun TimerScreenAppBar(
    pomodoroTimerState: PomodoroTimerState?,
    actions: TimerScreenActions,
) {
    TopAppBar(
        title = {
            Text(stringResource(R.string.timer))
        },
        actions = {
            Box {
                var expanded by remember { mutableStateOf(false) }
                IconButton(onClick = { expanded = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.open_menu)
                    )
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(onClick = {
                        expanded = false
                        actions.onResetTimerClicked()
                    }) {
                        Text(stringResource(R.string.reset_timer))
                    }
                    if (pomodoroTimerState?.currentPhase?.isBreak == true) {
                        DropdownMenuItem(onClick = {
                            expanded = false
                            actions.onSkipBreakClicked()
                        }) {
                            Text(stringResource(R.string.skip_break))
                        }
                    }
                    DropdownMenuItem(onClick = {
                        expanded = false
                        actions.onResetPomodoroSetClicked()
                    }) {
                        Text(stringResource(R.string.reset_pomodoro_set))
                    }
                    DropdownMenuItem(onClick = {
                        expanded = false
                        actions.onResetPomodoroCountClicked()
                    }) {
                        Text(stringResource(R.string.reset_pomodoro_count))
                    }
                }
            }
        }
    )
}

@Composable
fun TimerScreenContent(
    pomodoroTimerState: PomodoroTimerState,
    screenState: TimerScreenState,
    actions: TimerScreenActions,
) {
    val timerRunning = pomodoroTimerState.timerRunning
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Timer(pomodoroTimerState)
        Spacer(Modifier.height(48.dp))
        TimerStartStopButton(
            timerRunning = timerRunning,
            actions = actions
        )
    }

    if (screenState.showResetTimerConfirmationDialog) {
        SimpleConfirmationDialog(
            title = R.string.reset_timer,
            text = R.string.reset_timer_confirmation_message,
            confirmButtonText = R.string.reset_timer,
            dismissAction = actions::onResetTimerDialogDismissed,
            confirmAction = actions::onResetTimerConfirmed,
        )
    }

    if (screenState.showSkipBreakConfirmationDialog) {
        SimpleConfirmationDialog(
            title = R.string.skip_break,
            text = R.string.skip_break_confirmation_message,
            confirmButtonText = R.string.skip_break,
            dismissAction = actions::onSkipBreakDialogDismissed,
            confirmAction = actions::onSkipBreakConfirmed,
        )
    }

    if (screenState.showResetPomodoroSetConfirmationDialog) {
        SimpleConfirmationDialog(
            title = R.string.reset_pomodoro_set,
            text = R.string.reset_pomodoro_set_confirmation_message,
            confirmButtonText = R.string.reset_pomodoro_set,
            dismissAction = actions::onResetPomodoroSetDialogDismissed,
            confirmAction = actions::onResetPomodoroSetConfirmed,
        )
    }

    if (screenState.showResetPomodoroCountConfirmationDialog) {
        SimpleConfirmationDialog(
            title = R.string.reset_pomodoro_count,
            text = R.string.reset_pomodoro_count_confirmation_message,
            confirmButtonText = R.string.reset_pomodoro_count,
            dismissAction = actions::onResetPomodoroCountDialogDismissed,
            confirmAction = actions::onResetPomodoroCountConfirmed,
        )
    }
}

@Composable
private fun Timer(
    pomodoroTimerState: PomodoroTimerState?,
    modifier: Modifier = Modifier,
) {
    val timeLeftInMillis = pomodoroTimerState?.timeLeftInMillis ?: 0L
    val timeTargetInMillis = pomodoroTimerState?.timeTargetInMillis ?: 0L
    val currentPhase = pomodoroTimerState?.currentPhase
    val pomodorosCompletedInSet = pomodoroTimerState?.pomodorosCompletedInSet ?: 0
    val pomodorosPerSetTarget = pomodoroTimerState?.pomodorosPerSetTarget ?: 0
    val pomodorosCompletedTotal = pomodoroTimerState?.pomodorosCompletedTotal ?: 0

    val progress = timeLeftInMillis.toFloat() / timeTargetInMillis.toFloat()

    Box(modifier.size(250.dp), contentAlignment = Alignment.Center) {
        RoundedCornerCircularProgressIndicatorWithBackground(
            progress = progress,
            modifier = modifier
                .fillMaxSize()
                .scale(scaleX = -1f, scaleY = 1f),
            strokeWidth = 16.dp,
        )
        Text(
            text = formatMillisecondsToTimeString(timeLeftInMillis),
            style = MaterialTheme.typography.h4,
            modifier = Modifier.align(Alignment.Center)
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            val phaseText =
                if (currentPhase != null) stringResource(currentPhase.readableName) else ""
            Text(phaseText, Modifier.padding(top = 48.dp), style = MaterialTheme.typography.body2)
            Spacer(modifier = Modifier.height(4.dp))
            val timerRunning = pomodoroTimerState?.timerRunning ?: false
            PomodorosCompletedIndicatorRow(
                pomodorosCompletedInSet = pomodorosCompletedInSet,
                pomodorosPerSetTarget = pomodorosPerSetTarget,
                timerRunning = timerRunning,
                currentPhase = currentPhase,
            )
        }
        Text(
            text = stringResource(R.string.total) + ": $pomodorosCompletedTotal",
            style = MaterialTheme.typography.body2,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
        )
    }
}

@Composable
private fun TimerStartStopButton(
    timerRunning: Boolean,
    actions: TimerScreenActions,
    modifier: Modifier = Modifier,
) {
    val startStopIcon = if (!timerRunning) Icons.Default.PlayArrow else Icons.Default.Pause
    val contentDescription =
        stringResource(if (!timerRunning) R.string.start_timer else R.string.stop_timer)

    FloatingActionButton(
        onClick = actions::onStartStopTimerClicked,
        modifier = modifier.size(64.dp)
    ) {
        Icon(
            imageVector = startStopIcon,
            contentDescription = contentDescription,
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
        )
    }
}

@Composable
private fun PomodorosCompletedIndicatorRow(
    pomodorosCompletedInSet: Int,
    pomodorosPerSetTarget: Int,
    timerRunning: Boolean,
    currentPhase: PomodoroPhase?,
    modifier: Modifier = Modifier,
) {
    val pomodoroInProgress = timerRunning && currentPhase == PomodoroPhase.POMODORO

    FlowRow(
        modifier.width(100.dp),
        crossAxisSpacing = 4.dp,
        crossAxisAlignment = FlowCrossAxisAlignment.Center,
        mainAxisAlignment = MainAxisAlignment.Center,
        mainAxisSpacing = 4.dp
    ) {
        repeat(pomodorosPerSetTarget) { index ->
            key(index) {
                SinglePomodoroCompletedIndicator(
                    completed = pomodorosCompletedInSet > index,
                    inProgress = pomodoroInProgress && pomodorosCompletedInSet == index
                )
            }
        }
    }
}

@Composable
private fun SinglePomodoroCompletedIndicator(
    completed: Boolean,
    inProgress: Boolean,
    modifier: Modifier = Modifier,
) {
    val uncompletedColor = MaterialTheme.colors.primary.copy(alpha = PrimaryLightAlpha)
    val completedColor = MaterialTheme.colors.primary

    val infiniteTransition = rememberInfiniteTransition()
    val inProgressColor by infiniteTransition.animateColor(
        initialValue = uncompletedColor,
        targetValue = completedColor,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        )
    )

    val color =
        when {
            completed -> completedColor
            inProgress -> inProgressColor
            else -> uncompletedColor
        }
    Box(
        modifier = modifier
            .clip(CircleShape)
            .size(8.dp)
            .background(color)
    )
}

@Preview(
    name = "Light mode",
    uiMode = UI_MODE_NIGHT_NO
)
@Preview(
    name = "Dark mode",
    uiMode = UI_MODE_NIGHT_YES,
)
@Composable
private fun ScreenContentPreview() {
    IncentiveTimerTheme {
        Surface {
            TimerScreenContent(
                pomodoroTimerState = PomodoroTimerState(
                    timerRunning = true,
                    timeLeftInMillis = 15.minutesToMilliseconds(),
                    timeTargetInMillis = 25,
                    currentPhase = PomodoroPhase.POMODORO,
                    pomodorosCompletedInSet = 3,
                    pomodorosCompletedTotal = 5,
                    pomodorosPerSetTarget = 4,
                ),
                actions = object : TimerScreenActions {
                    override fun onResetTimerClicked() {}
                    override fun onResetPomodoroSetClicked() {}
                    override fun onStartStopTimerClicked() {}
                    override fun onResetPomodoroCountClicked() {}
                    override fun onResetTimerConfirmed() {}
                    override fun onResetTimerDialogDismissed() {}
                    override fun onSkipBreakClicked() {}
                    override fun onSkipBreakConfirmed() {}
                    override fun onSkipBreakDialogDismissed() {}
                    override fun onResetPomodoroSetConfirmed() {}
                    override fun onResetPomodoroSetDialogDismissed() {}
                    override fun onResetPomodoroCountConfirmed() {}
                    override fun onResetPomodoroCountDialogDismissed() {}
                },
                screenState = TimerScreenState.initialState,
            )
        }
    }
}