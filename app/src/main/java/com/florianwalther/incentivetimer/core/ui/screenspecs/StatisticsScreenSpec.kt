package com.florianwalther.incentivetimer.core.ui.screenspecs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.florianwalther.incentivetimer.R
import com.florianwalther.incentivetimer.core.util.exhaustive
import com.florianwalther.incentivetimer.features.statistics.StatisticsScreenAppBar
import com.florianwalther.incentivetimer.features.statistics.StatisticsScreenContent
import com.florianwalther.incentivetimer.features.statistics.StatisticsViewModel
import com.florianwalther.incentivetimer.features.statistics.model.StatisticsScreenState
import kotlinx.coroutines.flow.collectLatest

object StatisticsScreenSpec : BottomNavScreenSpec {
    override val navHostRoute: String = "statistics"

    override val icon: ImageVector = Icons.Outlined.BarChart

    override val label: Int = R.string.statistics

    @Composable
    override fun TopBar(navController: NavController, navBackStackEntry: NavBackStackEntry) {
        val viewModel: StatisticsViewModel = hiltViewModel(navBackStackEntry)
        StatisticsScreenAppBar(actions = viewModel)
    }

    @Composable
    override fun Content(navController: NavController, navBackStackEntry: NavBackStackEntry) {
        val viewModel: StatisticsViewModel = hiltViewModel(navBackStackEntry)
        val screenState by viewModel.screenState.observeAsState(StatisticsScreenState.initialState)

        var resetBarChartZoom by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            viewModel.events.collectLatest { event ->
                when (event) {
                    StatisticsViewModel.StatisticsEvent.ResetChartZoom ->
                        resetBarChartZoom = true
                }.exhaustive
            }
        }

        StatisticsScreenContent(
            screenState = screenState,
            actions = viewModel,
            resetBarChartZoom = resetBarChartZoom,
            onBarChartZoomReset = { resetBarChartZoom = false }
        )
    }
}