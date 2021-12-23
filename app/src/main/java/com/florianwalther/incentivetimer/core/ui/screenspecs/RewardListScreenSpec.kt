package com.florianwalther.incentivetimer.core.ui.screenspecs

import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.florianwalther.incentivetimer.R
import com.florianwalther.incentivetimer.core.ui.screenspecs.ScreenSpec
import com.florianwalther.incentivetimer.features.rewardlist.RewardListScreen
import com.florianwalther.incentivetimer.features.rewardlist.RewardListScreenAppBar
import com.florianwalther.incentivetimer.features.timer.TimerScreen

object RewardListScreenSpec : ScreenSpec {
    override val navHostRoute: String = "rewardList"

    @Composable
    override fun TopBar(navController: NavController, navBackStackEntry: NavBackStackEntry) {
        RewardListScreenAppBar()
    }

    @Composable
    override fun Content(navController: NavController, navBackStackEntry: NavBackStackEntry) {
        RewardListScreen(navController = navController)
    }
}