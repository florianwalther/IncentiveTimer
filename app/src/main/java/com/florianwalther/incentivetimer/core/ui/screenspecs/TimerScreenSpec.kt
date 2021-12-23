package com.florianwalther.incentivetimer.core.ui.screenspecs

import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.florianwalther.incentivetimer.R
import com.florianwalther.incentivetimer.core.ui.screenspecs.ScreenSpec
import com.florianwalther.incentivetimer.features.timer.TimerScreen
import com.florianwalther.incentivetimer.features.timer.TimerScreenAppBar

object TimerScreenSpec : ScreenSpec {
    override val navHostRoute: String = "timer"

    @Composable
    override fun TopBar(navController: NavController, navBackStackEntry: NavBackStackEntry) {
       TimerScreenAppBar()
    }

    @Composable
    override fun Content(navController: NavController, navBackStackEntry: NavBackStackEntry) {
        TimerScreen(navController = navController)
    }
}