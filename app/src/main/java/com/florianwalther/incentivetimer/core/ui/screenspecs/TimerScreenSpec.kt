package com.florianwalther.incentivetimer.core.ui.screenspecs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDeepLink
import androidx.navigation.navDeepLink
import com.florianwalther.incentivetimer.R
import com.florianwalther.incentivetimer.data.datastore.PomodoroTimerState
import com.florianwalther.incentivetimer.features.timer.TimerScreenContent
import com.florianwalther.incentivetimer.features.timer.TimerScreenAppBar
import com.florianwalther.incentivetimer.features.timer.TimerViewModel
import com.florianwalther.incentivetimer.features.timer.model.TimerScreenState
import logcat.logcat

object TimerScreenSpec : BottomNavScreenSpec {
    override val navHostRoute: String = "timer"

    override val deepLinks: List<NavDeepLink> = listOf(
        navDeepLink {
            uriPattern = "https://www.incentivetimer.com/timer"
        }
    )

    override val icon: ImageVector = Icons.Outlined.Timer

    override val label: Int = R.string.timer

    @Composable
    override fun TopBar(navController: NavController, navBackStackEntry: NavBackStackEntry) {
        val viewModel: TimerViewModel = hiltViewModel(navBackStackEntry)
        val pomodoroTimerState by viewModel.pomodoroTimerState.observeAsState()
        TimerScreenAppBar(
            pomodoroTimerState = pomodoroTimerState,
            actions = viewModel,
        )
    }

    @Composable
    override fun Content(navController: NavController, navBackStackEntry: NavBackStackEntry) {
        val viewModel: TimerViewModel = hiltViewModel(navBackStackEntry)
        val pomodoroTimerState by viewModel.pomodoroTimerState.observeAsState(PomodoroTimerState.initialState)
        val screenState by viewModel.screenState.observeAsState(TimerScreenState.initialState)
logcat { "timerRunning = ${pomodoroTimerState.timerRunning}" }
        TimerScreenContent(
            pomodoroTimerState = pomodoroTimerState,
            screenState = screenState,
            actions = viewModel,
        )
    }
}