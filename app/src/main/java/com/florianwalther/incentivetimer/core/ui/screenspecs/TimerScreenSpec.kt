package com.florianwalther.incentivetimer.core.ui.screenspecs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.florianwalther.incentivetimer.features.timer.TimerScreenContent
import com.florianwalther.incentivetimer.features.timer.TimerScreenAppBar
import com.florianwalther.incentivetimer.features.timer.TimerViewModel

object TimerScreenSpec : ScreenSpec {
    override val navHostRoute: String = "timer"

    @Composable
    override fun TopBar(navController: NavController, navBackStackEntry: NavBackStackEntry) {
        val viewModel: TimerViewModel = hiltViewModel(navBackStackEntry)
        TimerScreenAppBar(actions = viewModel)
    }

    @Composable
    override fun Content(navController: NavController, navBackStackEntry: NavBackStackEntry) {
        val viewModel: TimerViewModel = hiltViewModel(navBackStackEntry)
        val pomodoroTimerState by viewModel.pomodoroTimerState.observeAsState()

        TimerScreenContent(
            pomodoroTimerState = pomodoroTimerState,
            actions = viewModel,
        )
    }
}