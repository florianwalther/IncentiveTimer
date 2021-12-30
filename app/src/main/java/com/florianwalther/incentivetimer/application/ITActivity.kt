package com.florianwalther.incentivetimer.application

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.florianwalther.incentivetimer.R
import com.florianwalther.incentivetimer.core.ui.screenspecs.RewardListScreenSpec
import com.florianwalther.incentivetimer.core.ui.screenspecs.ScreenSpec
import com.florianwalther.incentivetimer.core.ui.screenspecs.TimerScreenSpec
import com.florianwalther.incentivetimer.core.ui.theme.IncentiveTimerTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow

@AndroidEntryPoint
class ITActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IncentiveTimerTheme {
                ScreenContent()
            }
        }
    }
}

@Composable
private fun ScreenContent() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val screenSpec = ScreenSpec.allScreens[currentDestination?.route]

    Scaffold(
        topBar = {
            val navBackStackEntry = navBackStackEntry
            if (navBackStackEntry != null) {
                screenSpec?.TopBar(navController, navBackStackEntry)
            }
        },
        bottomBar = {
            val hideBottomBar = navBackStackEntry?.arguments?.getBoolean(ARG_HIDE_BOTTOM_BAR)

            if (hideBottomBar == null || !hideBottomBar) {
                BottomNavigation {
                    bottomNavDestinations.forEach { bottomNavDestination ->
                        BottomNavigationItem(
                            icon = {
                                Icon(bottomNavDestination.icon, contentDescription = null)
                            },
                            label = {
                                Text(stringResource(bottomNavDestination.label))
                            },
                            alwaysShowLabel = false,
                            selected = currentDestination?.hierarchy?.any { it.route == bottomNavDestination.screenSpec.navHostRoute } == true,
                            onClick = {
                                // TODO: 29/12/2021 Navigate to start destination not working after deep link -> library bug.
                                //  Switch back to onNewIntent if bug doesn't get fixed
                                //  Workaround: https://issuetracker.google.com/issues/194301895
                                navController.navigate(bottomNavDestination.screenSpec.navHostRoute) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = bottomNavDestinations[0].screenSpec.navHostRoute,
            modifier = Modifier.padding(innerPadding),
        ) {
            ScreenSpec.allScreens.values.forEach { screen ->
                composable(
                    route = screen.navHostRoute,
                    arguments = screen.arguments,
                    deepLinks = screen.deepLinks,
                ) { navBackStackEntry ->
                    screen.Content(
                        navController = navController,
                        navBackStackEntry = navBackStackEntry
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ScreenContentPreview() {
    IncentiveTimerTheme {
        ScreenContent()
    }
}

val bottomNavDestinations = listOf<BottomNavDestinations>(
    BottomNavDestinations.TimerScreen,
    BottomNavDestinations.RewardListScreen
)

sealed class BottomNavDestinations(
    val screenSpec: ScreenSpec,
    val icon: ImageVector,
    @StringRes val label: Int
) {
    object TimerScreen :
        BottomNavDestinations(screenSpec = TimerScreenSpec, Icons.Outlined.Timer, R.string.timer)

    object RewardListScreen :
        BottomNavDestinations(
            screenSpec = RewardListScreenSpec,
            Icons.Outlined.Star,
            R.string.rewards
        )
}

const val ARG_HIDE_BOTTOM_BAR = "ARG_HIDE_BOTTOM_BAR"